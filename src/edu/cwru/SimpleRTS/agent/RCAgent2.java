package edu.cwru.SimpleRTS.agent;

import java.util.*;

import edu.cwru.SimpleRTS.action.*;
import edu.cwru.SimpleRTS.environment.State.StateView;
import edu.cwru.SimpleRTS.model.Template.TemplateView;
import edu.cwru.SimpleRTS.model.resource.ResourceNode.Type;
import edu.cwru.SimpleRTS.model.resource.ResourceType;
import edu.cwru.SimpleRTS.model.unit.Unit.UnitView;

public class RCAgent2 extends Agent {

	private static final long serialVersionUID = 1L;
	static int minGoldToCarry = 0;
	static int minWoodToCarry = 0;
	static int playernum = 0;
	static int numPeasantsToBuild = 3;
	static int numFootmanToBuild = 2;
	static String townHall = "TownHall";
	static String peasant = "Peasant";
	static String farm = "Farm";
	static String barracks = "Barracks";
	static String footman = "Footman";

	public RCAgent2(int playernum) {
		super(playernum);
	}

	@Override
	public Map<Integer, Action> initialStep(StateView state) {
		return middleStep(state);
	}

	@Override
	public Map<Integer, Action> middleStep(StateView state) {
		
		List<Integer> allUnitIds = state.getAllUnitIds();
		List<Integer> townHalls = findUnitType(allUnitIds, state, townHall);
		List<Integer> peasants = findUnitType(allUnitIds, state, peasant);
		
		Map<Integer, Action> actions = new HashMap<Integer,Action>();
		
		int costOfPeasant =  state.getUnit(peasants.get(0)).getTemplateView().getGoldCost();
		int farmGoldCost = state.getTemplate(playernum, farm).getGoldCost();
		int farmWoodCost = state.getTemplate(playernum, farm).getWoodCost();
		int barracksGoldCost = state.getTemplate(playernum, barracks).getGoldCost();
		int barracksWoodCost = state.getTemplate(playernum, barracks).getWoodCost();
		int footmanCost = state.getTemplate(playernum, footman).getGoldCost();
		int goldPile = state.getResourceAmount(playernum, ResourceType.GOLD);
		int woodPile = state.getResourceAmount(playernum, ResourceType.WOOD);
		
		if(peasants.size() > 0 && townHalls.size() > 0) 
		{
			if (peasants.size() < numPeasantsToBuild ) //build more peasants when we can
			{
				if ( goldPile >= costOfPeasant * 2 || 
						goldPile >= costOfPeasant && peasants.size() == numPeasantsToBuild - 1) //build 2 peasants
				{
					
					TemplateView peasantTemplate = state.getTemplate(playernum, peasant);
					Action buildPeasants = Action.createCompoundProduction(townHalls.get(0), peasantTemplate.getID());
					
					actions.put(townHalls.get(0), buildPeasants);
				}
				
				collectResource(peasants, actions, state, townHalls.get(0), Type.GOLD_MINE, minGoldToCarry);
			}
			else if (peasants.size() == numPeasantsToBuild)
			{
				if (findUnitType(allUnitIds, state, farm).size() <= 0)
				{
					if (goldPile <= farmGoldCost)
					{
						collectResource(peasants, actions, state, townHalls.get(0), Type.GOLD_MINE, minGoldToCarry);
					}
					else if (woodPile <= farmWoodCost)
					{
						collectResource(peasants, actions, state, townHalls.get(0), Type.TREE, minWoodToCarry);
					}
					else
					{					
						int peasantId = peasants.get(0);
						Action buildFarm = Action.createPrimitiveBuild(peasantId, state.getTemplate(playernum, farm).getID());
						actions.put(peasantId, buildFarm);
					}
				}
				else if (findUnitType(allUnitIds, state, barracks).size() <= 0)
				{
					if (goldPile <= barracksGoldCost)
					{
						collectResource(peasants, actions, state, townHalls.get(0), Type.GOLD_MINE, minGoldToCarry);
					}
					else if (woodPile <= barracksWoodCost)
					{
						collectResource(peasants, actions, state, townHalls.get(0), Type.TREE, minWoodToCarry);
					}
					else
					{					
						int peasantId = peasants.get(0);
						Action buildBarracks = Action.createPrimitiveBuild(peasantId, state.getTemplate(playernum, barracks).getID());
						actions.put(peasantId, buildBarracks);
					}
				}
				else
				{
					if (goldPile >= footmanCost && findUnitType(allUnitIds, state, footman).size() <= numFootmanToBuild)
					{
						int barracksId = findUnitType(allUnitIds, state, barracks).get(0);
						TemplateView footmanTemplate = state.getTemplate(playernum, footman);
						Action buildFootman = Action.createCompoundProduction(barracksId, footmanTemplate.getID());
						
						actions.put(barracksId, buildFootman);
					}
					collectResource(peasants, actions, state, townHalls.get(0), Type.GOLD_MINE, minGoldToCarry);
				}
			}
		}
		else
		{
			System.out.println("Error: bad config, not enough peasants or town halls to start");
		}
		
		return actions;
	}

	@Override
	public void terminalStep(StateView state) {
		// TODO Auto-generated method stub

	}
	
	public List<Integer> findUnitType(List<Integer> ids, StateView state, String name)	{
		
		List<Integer> unitIds = new ArrayList<Integer>();
		
		for (int x = 0; x < ids.size(); x++)
		{
			Integer unitId = ids.get(x);
			UnitView unit = state.getUnit(unitId);
			
			if(unit.getTemplateView().getUnitName().equals(name))
			{
				unitIds.add(unitId);
			}
		}
		
		return unitIds;
	}
	
	public boolean collectResource(List<Integer> peasants, Map<Integer, Action> actionList, StateView state, Integer townHall, Type resource, int minToGather)	{
		
		Action action = null;
		
		for (Integer peasantId: peasants)
		{
			List<Integer> resourceIds = state.getResourceNodeIds(resource);
			
			if(state.getUnit(peasantId).getCargoType() == Type.getResourceType(resource) && state.getUnit(peasantId).getCargoAmount() > minToGather)
			{
				action = new TargetedAction(peasantId, ActionType.COMPOUNDDEPOSIT, townHall);
			}
			else if(resourceIds.size() > 0)
			{
				action = new TargetedAction(peasantId, ActionType.COMPOUNDGATHER, resourceIds.get(0));
			}
			else
			{
				System.out.println("Can't collect anymore " + resource.toString());
				return false;
				//do nothing
			}
			
			if (action != null)
			{
				actionList.put(peasantId, action);
			}
		}
		return true;
	}

}
