package edu.cwru.SimpleRTS.agent;

import java.util.*;

import edu.cwru.SimpleRTS.action.*;
import edu.cwru.SimpleRTS.environment.State;
import edu.cwru.SimpleRTS.environment.State.StateView;
import edu.cwru.SimpleRTS.model.Direction;
import edu.cwru.SimpleRTS.model.Template.TemplateView;
import edu.cwru.SimpleRTS.model.unit.Unit;
import edu.cwru.SimpleRTS.model.unit.Unit.UnitView;
import edu.cwru.SimpleRTS.model.unit.UnitTemplate;
import edu.cwru.SimpleRTS.util.DistanceMetrics;

public class ABAgent extends Agent {

	private static final long serialVersionUID = 1L;
	static int playernum = 0;
	static int nInfinity = -9999999;
	static int pInfinity = 9999999;
	static boolean maxPlayer = true;
	static String archer = "Archer";
	static String peasant = "Peasant";
	static String farm = "Farm";
	static String barracks = "Barracks";
	static String footman = "Footman";
	private int DEPTH = 0;

	//Constructor
	public ABAgent(int playernum) 
	{
		super(playernum);
	}

	@Override
	public Map<Integer, Action> initialStep(StateView state) 
	{
		return middleStep(state);
	}

	@Override
	public Map<Integer, Action> middleStep(StateView state) 
	{

		Map<Integer, Action> actions = new HashMap<Integer, Action>();
		List<Integer> allUnitIds = state.getAllUnitIds();
		List<Integer> footmanIds = findUnitType(allUnitIds, state, footman);
		List<Integer> archerIds = findUnitType(allUnitIds, state, archer);

		if(footmanIds.size() > 0 && archerIds.size() > 0)
		{
			actions = alphaBeta(footmanIds, archerIds, state);
		}
		else
		{
			if(footmanIds.size() <= 0)
			{
				System.out.println("There are no footmen on the map.");
			}
			if(archerIds.size() <= 0)
			{
				System.out.println("There are no archers on the map.");
			}
		}

		if(actions == null)
		{
			actions = new HashMap<Integer, Action>();
		}

		return actions;
	}

	@Override
	public void terminalStep(StateView state) {
		// TODO Auto-generated method stub
	}

	
	//The following three methods should follow the same format
	//CHRIS: I apologize if I misunderstood you in terms of how to calculate the heuristic. I just used our function which called on chebyshev
	//		but it needed two UnitViews as inputs, so I assumed the ArrayList was like a path and used it to calculate heuristic, if that's wrong, i'm sorry
	public ArrayList<UnitView> maxAB(ArrayList<UnitView> a, ArrayList<UnitView> b) //AB are null if they are at infinity
	{
		Integer aCost = new Integer(0);
		Integer bCost = new Integer(0);
		
		for(int i = 1; i < a.size(); i++)
		{
			aCost = aCost + heuristicCostCalculator(a.get(i-1), a.get(i));
		}
		for(int i = 1; i < b.size(); i++)
		{
			bCost = bCost + heuristicCostCalculator(b.get(i-1), b.get(i));
		}
		
		if(aCost >= pInfinity || aCost <= nInfinity || bCost >= pInfinity || bCost <= nInfinity)
			return null;
		else
		{
			if(aCost >= bCost)
			{
				return a;
			}
			else
			{
				return b;
			}
		}
	}

	public ArrayList<UnitView> minAB(ArrayList<UnitView> a, ArrayList<UnitView> b) //AB are null if they are at infinity
	{
		Integer aCost = new Integer(0);
		Integer bCost = new Integer(0);
		
		for(int i = 1; i < a.size(); i++)
		{
			aCost = aCost + heuristicCostCalculator(a.get(i-1), a.get(i));
		}
		for(int i = 1; i < b.size(); i++)
		{
			bCost = bCost + heuristicCostCalculator(b.get(i-1), b.get(i));
		}
		
		if(aCost >= pInfinity || aCost <= nInfinity || bCost >= pInfinity || bCost <= nInfinity)
			return null;
		else
		{
			if(aCost < bCost)
			{
				return a;
			}
			else
			{
				return b;
			}
		}
	}

	public boolean ABCutOff(ArrayList<UnitView> a, ArrayList<UnitView> b) //AB are null if they are at infinity
	{
		Integer aCost = new Integer(0);
		Integer bCost = new Integer(0);
		
		for(int i = 1; i < a.size(); i++)
		{
			aCost = aCost + heuristicCostCalculator(a.get(i-1), a.get(i));
		}
		for(int i = 1; i < b.size(); i++)
		{
			bCost = bCost + heuristicCostCalculator(b.get(i-1), b.get(i));
		}
		
		if(aCost >= pInfinity || aCost <= nInfinity || bCost >= pInfinity || bCost <= nInfinity)
			return false;
		else
		{
			if(bCost <= aCost)
			{
				return true;
			}
			else
			{
				return false;
			}
		}
	}

	
	
	//generate all possible neighboring nodes
	public ArrayList<ArrayList<UnitView>> createStates(ArrayList<UnitView> units, StateView state, HashMap<UnitView, UnitView> parents)
	{
		ArrayList <ArrayList<UnitView>> validStates = new ArrayList<ArrayList<UnitView>>(); //the return array of states
		ArrayList <ArrayList<UnitView>> returnStates = new ArrayList<ArrayList<UnitView>>();

		for (UnitView unit: units) //for each unit in units
		{
			validStates.add(getNeighbors(unit, state, false)); // add this unit's neighbors

			for (UnitView child: validStates.get(validStates.size() - 1))
			{
				parents.put(child, unit); //add parent nodes to hash map				
			}
		}

		//ArrayList <UnitView> listOfUnitsToAdd = new ArrayList <UnitView> ();
		//returnStates = createStatesRecursive(validStates, listOfUnitsToAdd, 0);
		
		if(validStates.size() == 1)
		{
			for (int x = 0; x < validStates.get(0).size(); x++)
			{
				ArrayList<UnitView> temp = new ArrayList<UnitView>();
				temp.add(validStates.get(0).get(x));
				returnStates.add(temp);
			}
			return returnStates;
		}
		else //if(validStates.size() == 2) //currently defaulting all to this
		{
			for (int x = 0; x < validStates.get(0).size(); x++) //fix for recursion only goes two deep now
			{
				UnitView xView = validStates.get(0).get(x);

				for (int y = 0; y < validStates.get(1).size(); y++)
				{
					ArrayList<UnitView> temp = new ArrayList<UnitView>();
					temp.add(xView);
					temp.add(validStates.get(1).get(y));
					returnStates.add(temp);

				}
			}
			return returnStates;
		}
	}
	
	/*public ArrayList<ArrayList<UnitView>> createStatesRecursive(ArrayList <ArrayList<UnitView>> validStates, ArrayList<UnitView> units, int index )
	{
		ArrayList<ArrayList<UnitView>> returnStates = new ArrayList<ArrayList<UnitView>>();
		
		//base case
		if(index == validStates.size() - 1)
		{
			for (int y = 0; y < validStates.get(index).size(); y++)
			{
				ArrayList<UnitView> temp = new ArrayList<UnitView>();
				temp.add(xView);
				temp.add(validStates.get(1).get(y));
				returnStates.add(temp);
			}
		}
	}*/
	
	
	//checks to see if there is an archer / means to attack because this function is being used as the base class in ABrecurse
	public boolean checkAttack(ArrayList<UnitView> arr, boolean player)
	{
		if(player)
		{
			for(int i = 0; i < arr.size(); i++)
			{
				if(arr.get(i).getTemplateView().getUnitName().equals(archer))
					return true;
			}
			return false;
		}
		else
		{
			for(int i = 0; i < arr.size(); i++)
			{
				if(arr.get(i).getTemplateView().getUnitName().equals(footman))
					return true;
			}
			return false;
		}
	}
	
	//recursive AB pruning
	public ArrayList<UnitView> alphaBetaRecurse(ArrayList<UnitView> node, int depth, ArrayList<UnitView> alpha, ArrayList<UnitView> beta, boolean player, StateView state, HashMap<UnitView, UnitView> parents, ArrayList<UnitView> archers, ArrayList<UnitView> footmen)
	{		
		ArrayList<ArrayList<UnitView>> children = createStates(node, state, parents);

		if ( depth == 0 || (children.size() == 1 && checkAttack(children.get(0), player))) //should be based on no neighbors and can only attack
			return node; //don't create anymore children

		if(player == maxPlayer)
		{
			for (ArrayList<UnitView> child: children)
			{
				alpha = maxAB(alpha, alphaBetaRecurse(archers, depth-1, alpha, beta, !player, state, parents, archers, child)); // note we are passing in a new footman

				if (ABCutOff(alpha, beta))
				{
					break; //Beta cut off
				}
			}
			return alpha;
		}
		else
		{
			for (ArrayList<UnitView> child: children)
			{
				beta = minAB(alpha, alphaBetaRecurse(footmen, depth-1, alpha, beta, !player, state, parents, child, footmen)); // note we are passing in a new archer
				
				if (ABCutOff(alpha, beta))
				{
					break; //alpha cut off
				}
			}
			return beta;
		}
	}

	//sets up a-B search for recursion
	public Map<Integer, Action> alphaBeta(List<Integer> footmenIds, List<Integer> archerIds, StateView state)
	{
		Map<Integer, Action> actions = new HashMap<Integer, Action>();
		HashMap<UnitView, UnitView> parents = new HashMap<UnitView, UnitView>();
		ArrayList<UnitView> footmen = new ArrayList<UnitView>();
		ArrayList<UnitView> archers = new ArrayList<UnitView>();

		for (Integer ID: footmenIds)
		{
			footmen.add(state.getUnit(ID));
		}
		
		for (Integer ID: archerIds)
		{
			archers.add(state.getUnit(ID));
		}

		alphaBetaRecurse(archers, DEPTH, null, null, maxPlayer, state, parents, archers, footmen);
		//NEED TO IMPLEMENT RETRACE ALG... WILL RETURN PARENT MOVE
		return null;
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

	public UnitView createOpenSpace(Integer x, Integer y) //creates a dummy UnitView at the requested space
	{
		UnitTemplate template = new UnitTemplate(0); //The template, ID 0 is used because we don't care what type it is
		Unit unit = new Unit(template, y);	//The actual Unit

		unit.setxPosition(x); //set its x
		unit.setyPosition(y); //set its y

		UnitView openSpace = new UnitView(unit); //make a UnitView from it

		return openSpace; //return the UnitView
	}

	public ArrayList<UnitView> getNeighbors(UnitView currentParent, StateView state, boolean unitDoesntMatter) //returns neighbors 
	{
		//NOTE: boolean unitDoesntMatter tells it whether we care about whether or not the space is occupied
		//		It should ONLY be set to true if we are checking goals or cheating...

		ArrayList<UnitView> neighbors = new ArrayList<UnitView>(); //The return list of all neighbors

		Integer x = currentParent.getXPosition();
		Integer y = currentParent.getYPosition();
		Integer xPlusOne = x + 1;
		Integer xMinusOne = x - 1;
		Integer yPlusOne = y + 1;
		Integer yMinusOne = y - 1;		

		Integer tempX = 0, tempY = 0;

		for (int j = 0; j < 4; j++) //go through all possible 4 squares
		{
			switch(j) //Could use something better but it's too much thinking right now
			{
			case 0: //x + 1, y
				tempX = xPlusOne;
				tempY = y;
				break;
			case 1: //x, y + 1
				tempX = x;
				tempY = yPlusOne;
				break;
			case 2: //x, y - 1
				tempX = x;
				tempY = yMinusOne;
				break;
			case 3: //x - 1, y
				tempX = xMinusOne;
				tempY = y;
				break;
			default:
				break;
			}

			UnitView neighbor = createOpenSpace(tempX, tempY); //make a dummy space

			if(checkValidNeighbor(tempX, tempY, state, unitDoesntMatter)) //check if it's a valid space
			{
				neighbors.add(neighbor);
			}
		}		

		return neighbors;
	}

	//Calculating Heuristic with chebyshev
	public Integer heuristicCostCalculator(UnitView a, UnitView b)
	{
		int x1 = a.getXPosition();
		int x2 = b.getXPosition();
		int y1 = a.getYPosition();
		int y2 = b.getYPosition();
		return (DistanceMetrics.chebyshevDistance(x1, y1, x2, y2));
	}

	//returns if a space is empty and valid or if the space is occupied by an archer
	public boolean checkValidNeighbor(Integer x, Integer y, StateView state, boolean unitDoesntMatter)	
	{

		boolean isResource = state.isResourceAt(x, y); //check if there is a resource here
		boolean isUnit = state.isUnitAt(x, y); //check if there is a unit here
		boolean isValid = state.inBounds(x, y); //check if the square is valid

		boolean isNotTaken = !isResource && !isUnit; //if it is not an occupied square

		if ((isNotTaken || unitDoesntMatter) && isValid) //if there is no resource here and no unit and it's valid it means it's an empty square
		{
			return true;
		}
		else if(isUnit && isValid && !isResource) //there's a unit, see if we can attack it
		{
			if(state.getUnit(state.unitAt(x,y)).getTemplateView().getUnitName().equals(archer)) //it's an archer, we can attack
			{	
				return true;
			}
			else //not an archer, don't attack (for now...)
			{
				return false;
			}
		}

		return false;
	}

}
