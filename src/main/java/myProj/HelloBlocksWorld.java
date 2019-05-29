package myProj;

import burlap.domain.singleagent.blocksworld.BlocksWorld;
import burlap.domain.singleagent.blocksworld.BlocksWorldBlock;
import burlap.domain.singleagent.blocksworld.BlocksWorldState;
import burlap.domain.singleagent.blocksworld.BlocksWorldVisualizer;
import burlap.domain.singleagent.blocksworld.BWModel;
import burlap.domain.singleagent.blocksworld.BlocksWorldTerminalFunction;
import burlap.domain.singleagent.blocksworld.BlocksWorldRewardFunction;
import burlap.domain.singleagent.blocksworld.BlocksWorldTower;

import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.auxiliary.common.NullTermination;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.oo.OODomain;
import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.oo.propositional.GroundedProp;
import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.mdp.singleagent.model.SampleModel;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.common.NullRewardFunction;
import burlap.shell.visual.VisualExplorer;
import burlap.mdp.singleagent.model.FactoredModel;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.mdp.core.action.Action;
import burlap.mdp.singleagent.oo.ObjectParameterizedActionType;
import burlap.shell.EnvironmentShell;
import burlap.statehashing.simple.SimpleHashableStateFactory;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.LinkedList;
import java.util.Random;

import java.util.concurrent.TimeUnit;

public class HelloBlocksWorld {

	/**
	 * initialize blocks and colors
	 */
	public static State getNewState(int nBlocks, Color[] colors) {
		BlocksWorldState s = new BlocksWorldState();
		for (int i = 0; i < nBlocks; i++) {
			BlocksWorldBlock b = new BlocksWorldBlock("block" + i);
			b.color = colors[i];
			s.addObject(b);
		}
		return s;
	}

	/**
	 * print out current state
	 */
	public static void printCurrentState(SADomain domain, State s){

		if(!(domain instanceof OODomain) || !(s instanceof OOState)){
			return ;
		}

	    StringBuilder buf = new StringBuilder();
		
		List <PropositionalFunction> props = ((OODomain)domain).propFunctions();
		for(PropositionalFunction pf : props){
			List<GroundedProp> gps = pf.allGroundings((OOState)s);
			for(GroundedProp gp : gps){
				if(gp.isTrue((OOState)s)){
					buf.append(gp.toString()).append("\n");
				}
			}
		}
		System.out.println("Current state is:");
		System.out.println(buf.toString());
		
	}

	/**
	 * print out current available actions
	 */
	public static void printAvailableActions(BlocksWorld bw, State s) {

		ObjectParameterizedActionType actiontype = bw.new StackActionType("stack");

		StringBuilder buf = new StringBuilder();
		// all applicable stack actions
		List<Action> actions = actiontype.allApplicableActions(s);

		for (Action act : actions) {
			buf.append(((ObjectParameterizedActionType.SAObjectParameterizedAction) act).toString()).append("\n");
		}

		// all applicable unstack actions
		List<ObjectInstance> blocks = ((BlocksWorldState) s).objects();
		for (ObjectInstance b : blocks) {
			BlocksWorldBlock bwb = (BlocksWorldBlock) b;
			if (bwb.clear && !(bwb.on.equals(BlocksWorld.TABLE_VAL))) {
				buf.append("unstack " + bwb.name() + "\n");
			}
		}

		System.out.println("Current available actions are:");
		System.out.println(buf.toString());
		
	}

	/**
	 * Test 1
	 * Basic version of blocks world
	 */
	public static void test1(String [] args) {

		BlocksWorld bw = new BlocksWorld();

		// set terminal state
		BlocksWorldTerminalFunction tf = new BlocksWorldTerminalFunction("clear", "block1");
		BlocksWorldRewardFunction rf = new BlocksWorldRewardFunction(tf, 10., -1.);
		bw.setTf(tf);
		bw.setRf(rf);

		SADomain domain = bw.generateDomain();

		SampleModel model = domain.getModel();
		
		// initial state
		State s0 = getNewState(4, new Color[]{Color.red, Color.green, Color.blue, Color.yellow});
		
		int expMode = 1;
		if (args.length > 0) {
			if (args[0].equals("v")) {
				expMode = 1;
			} else if(args[0].equals("t")) {
				expMode = 0;
			}
		}
		
		
		if (expMode == 0) {

			EnvironmentShell shell = new EnvironmentShell(domain, s0);
			shell.start();
			
		} else if (expMode == 1) {
			VisualExplorer exp = new VisualExplorer(domain, BlocksWorldVisualizer.getVisualizer(24), s0);
			
			
			exp.initGUI();

			printCurrentState(domain, s0);

			// stack
			ObjectParameterizedActionType mystack = bw.new StackActionType("stack");
			// unstack
			ObjectParameterizedActionType myunstack = bw.new StackActionType("unstack");

			printAvailableActions(bw, s0);
			System.out.println(model.terminal(s0));

			// stack block0 onto block1
			try {
				TimeUnit.SECONDS.sleep(2);
			} catch (Exception ex) {

			}
			
			Action a0 = mystack.associatedAction("block0 block1");
			State s1 = ((BWModel) ((FactoredModel) domain.getModel()).getStateModel()).sample(s0, a0);
			exp.updateState(s1);

			printCurrentState(domain, s1);
			printAvailableActions(bw, s1);
			System.out.println(model.terminal(s1));
			System.out.println(rf.reward(s0, a0, s1));

			// stack block2 onto block0
			try {
				TimeUnit.SECONDS.sleep(2);
			} catch (Exception ex) {

			}

			Action a1 = mystack.associatedAction("block2 block0");
			State s2 = ((BWModel) ((FactoredModel) domain.getModel()).getStateModel()).sample(s1, a1);
			exp.updateState(s2);

			printCurrentState(domain, s2);
			printAvailableActions(bw, s2);
			System.out.println(model.terminal(s2));
			System.out.println(rf.reward(s1, a1, s2));

			// stack block3 onto block2
			try {
				TimeUnit.SECONDS.sleep(2);
			} catch (Exception ex) {

			}

			Action a2 = mystack.associatedAction("block3 block2");
			State s3 = ((BWModel) ((FactoredModel) domain.getModel()).getStateModel()).sample(s2, a2);
			exp.updateState(s3);

			printCurrentState(domain, s3);
			printAvailableActions(bw, s3);
			System.out.println(model.terminal(s3));
			System.out.println(rf.reward(s2, a2, s3));

			// unstack block3
			try {
				TimeUnit.SECONDS.sleep(2);
			} catch (Exception ex) {

			}
			
			Action a3 = myunstack.associatedAction("block3");
			State s4 = ((BWModel) ((FactoredModel) domain.getModel()).getStateModel()).sample(s3, a3);
			exp.updateState(s4);

			printCurrentState(domain, s4);
			printAvailableActions(bw, s4);
			System.out.println(model.terminal(s4));
			System.out.println(rf.reward(s3, a3, s4));

			// unstack block2
			try {
				TimeUnit.SECONDS.sleep(2);
			} catch (Exception ex) {

			}
			
			Action a4 = myunstack.associatedAction("block2");
			State s5 = ((BWModel) ((FactoredModel) domain.getModel()).getStateModel()).sample(s4, a4);
			exp.updateState(s5);

			printCurrentState(domain, s5);
			printAvailableActions(bw, s5);
			System.out.println(model.terminal(s5));
			System.out.println(rf.reward(s4, a4, s5));



			// unstack block0
			try {
				TimeUnit.SECONDS.sleep(2);
			} catch (Exception ex) {

			}

			Action a5 = myunstack.associatedAction("block0");
			State s6 = ((BWModel) ((FactoredModel) domain.getModel()).getStateModel()).sample(s5, a5);
			exp.updateState(s6);

			printCurrentState(domain, s6);
			printAvailableActions(bw, s6);
			System.out.println(model.terminal(s6));
			System.out.println(rf.reward(s5, a5, s6));
		}
	}

	/**
	 * Print out the blocks in each tower
	 */
	public static void printBlocks(State s) {

		System.out.println("Blocks in each tower:");
		for (int i = 0; i < 3; i++) {
			BlocksWorldTower tower = ((BlocksWorldState) s).getTower(i);
			System.out.println("-" + tower.name());
			List<BlocksWorldBlock> blocks = (List<BlocksWorldBlock>) tower.get(BlocksWorld.VAR_TOP);
			System.out.print("--");
			for (BlocksWorldBlock b : blocks) {
				System.out.print(b.name() + ",");
			}
			System.out.println();
		}
		System.out.println();
	}

	/**
	 * Move one block from one tower to another tower with state updated
	 */
	public static State moveFromTowerToTower(BlocksWorld bw, SADomain domain, State s, BlocksWorldTower curTower, 
		BlocksWorldTower targetTower) {

		ObjectParameterizedActionType mystack = bw.new StackActionType("stack");

		BlocksWorldBlock curBlock = curTower.getTopBlock();
		BlocksWorldBlock targetBlock = targetTower.getTopBlock();
		curTower.removeBlockTop();
		targetTower.removeBlockTop();

		Action a = mystack.associatedAction(curBlock.name() + " " + targetBlock.name());
		State sprime = ((BWModel) ((FactoredModel) domain.getModel()).getStateModel()).sample(s, a);

		curBlock = (BlocksWorldBlock) ((BlocksWorldState) sprime).object(curBlock.name());
		targetBlock = (BlocksWorldBlock) ((BlocksWorldState) sprime).object(targetBlock.name());
		targetTower.addBlockTop(targetBlock);
		targetTower.addBlockTop(curBlock);

		return sprime;
	}

	/**
	 * return one of the heighest towers
	 */
	public static BlocksWorldTower maxTower(List<BlocksWorldTower> towers) {
		int high = -1;
		List<Integer> indices = null;
		for (int i = 0; i < towers.size(); i++) {
			BlocksWorldTower tower = towers.get(i);
			if (indices == null || high < tower.getHeight()) {
				high = tower.getHeight();
				indices = new ArrayList<Integer>();
				indices.add(i);
			} else if (high == tower.getHeight()) {
				indices.add(i);
			}
		}

		// randomly select one heighest tower as the target tower
		Random rand = new Random();
		int ind = rand.nextInt(indices.size());
		return towers.get((int) indices.get(ind));
	}


	/**
	 * Test 2:
	 * Blocks world with towers and random initialization
	 */
	public static void test2(String [] args) {

		BlocksWorld bw = new BlocksWorld();

		// set terminal state
		BlocksWorldTerminalFunction tf = new BlocksWorldTerminalFunction("stack");
		BlocksWorldRewardFunction rf = new BlocksWorldRewardFunction(tf, 10., -1.);
		bw.setTf(tf);
		bw.setRf(rf);

		SADomain domain = bw.generateDomain();

		SampleModel model = domain.getModel();
		
		// initial state
		State s0 = BlocksWorld.getRandomNewState(5, 3);
		
		int expMode = 1;
		if (args.length > 0) {
			if (args[0].equals("v")) {
				expMode = 1;
			} else if(args[0].equals("t")) {
				expMode = 0;
			}
		}
		
		
		if (expMode == 0) {

			EnvironmentShell shell = new EnvironmentShell(domain, s0);
			shell.start();
			
		} else if (expMode == 1) {
			VisualExplorer exp = new VisualExplorer(domain, BlocksWorldVisualizer.getVisualizer(24), s0);
			
			
			exp.initGUI();

			// print out the blocks in each tower
			printBlocks(s0);

			// test the BlocksWorldTower class by implementing the optimal strategy
			List<BlocksWorldTower> towers = new LinkedList<BlocksWorldTower>(((BlocksWorldState) s0).getTowers());
			
			Random rand = new Random();

			// --------Testing Tower---------
			// randomly select one heighest tower as the target tower
			// BlocksWorldTower targetTower = maxTower(towers);
			// towers.remove(tower);
			// loop until no tower left:
			// randomly select one tower and move all blocks one by one in that tower to the target tower
			// State s = s0;
			// while (!towers.isEmpty()) {
			// 	int cur = rand.nextInt(towers.size());
			// 	BlocksWorldTower curTower = towers.remove(cur);
			// 	while (curTower.getHeight() > 0) {
			// 		s = moveFromTowerToTower(bw, domain, s, curTower, targetTower);

			// 		try {
			// 			TimeUnit.SECONDS.sleep(2);
			// 		} catch (Exception ex) {

			// 		}

			// 		exp.updateState(s);

			// 		// print out the blocks in each tower
			// 		printBlocks(s);
			// 		System.out.println(model.terminal(s));
			// 		System.out.println();
			// 	}
			// }

			// -------Optimal vs. Not optimal--------
			// decide optimality on step basis
			State s = s0;

			int numSteps = 0;
			int maxSteps = 10;

			// probability to choose random action
			double epsilon = 0.7;

			while (!model.terminal(s) && (numSteps < maxSteps)) {

				double p = rand.nextDouble();

				if (p < epsilon) {
					// random action
					System.out.println("--random action--");
					ObjectParameterizedActionType actiontype = bw.new StackActionType("stack");

					List<Action> actions = actiontype.allApplicableActions(s);

					// randomly select an action
					int ind = rand.nextInt(actions.size());
					Action action = actions.get(ind);

					String [] params = ((ObjectParameterizedAction) action).getObjectParameters();

					BlocksWorldBlock aBlock = (BlocksWorldBlock) ((BlocksWorldState) s).object(params[0]);
					BlocksWorldBlock bBlock = (BlocksWorldBlock) ((BlocksWorldState) s).object(params[1]);

					BlocksWorldTower aTower = aBlock.tower;
					BlocksWorldTower bTower = bBlock.tower;

					aTower.removeBlockTop();
					bTower.removeBlockTop();

					s = ((BWModel) ((FactoredModel) domain.getModel()).getStateModel()).sample(s, action);

					aBlock = (BlocksWorldBlock) ((BlocksWorldState) s).object(params[0]);
					bBlock = (BlocksWorldBlock) ((BlocksWorldState) s).object(params[1]);

					bTower.addBlockTop(bBlock);
					bTower.addBlockTop(aBlock);

				} else {
					// optimal action
					System.out.println("--optimal action--");
					BlocksWorldTower targetTower = maxTower(towers);
					BlocksWorldTower curTower = targetTower;
					while ((curTower == targetTower) || (curTower.getHeight() == 0)) {
						int ind = rand.nextInt(towers.size());
						curTower = towers.get(ind);
					}
					s = moveFromTowerToTower(bw, domain, s, curTower, targetTower);
				}

				try {
					TimeUnit.SECONDS.sleep(2);
				} catch (Exception ex) {

				}

				exp.updateState(s);

				// print out the blocks in each tower
				printBlocks(s);
				
			}

			System.out.println("--agent finishes--");

		}

	}

	/**
	 * Main method for exploring the domain. The initial state will have 3 red blocks starting on the table. By default this method will launch the visual explorer.
	 * Pass a "t" argument to use the terminal explorer.
	 * @param args process arguments
	 */
	public static void main(String [] args) {
		
		test2(args);
		
	}

}