package edu.cwru.SimpleRTS.agent;

import java.util.*;

import edu.cwru.SimpleRTS.action.*;
import edu.cwru.SimpleRTS.environment.State.StateView;
import edu.cwru.SimpleRTS.model.Direction;
import edu.cwru.SimpleRTS.model.unit.Unit;
import edu.cwru.SimpleRTS.model.unit.Unit.UnitView;
import edu.cwru.SimpleRTS.model.unit.UnitTemplate;
import edu.cwru.SimpleRTS.util.DistanceMetrics;

public class ABAgent extends Agent {

	private static final long serialVersionUID = 1L;
	static int playernum = 0;
	static int nInfinity = -9999999;
	static int pInfinity = 9999999;
	static String archer = "Archer";
	static String peasant = "Peasant";
	static String farm = "Farm";
	static String barracks = "Barracks";
	static String footman = "Footman";
	private int DEPTH = 0;

	//Constructor
	public ABAgent(int playernum, int depth) 
	{
		super(playernum);
		DEPTH = depth;
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
		
		if(footmanIds.size() > 0 || archerIds.size() > 0)
		{
			actions = alphabeta(footmanIds.get(0), DEPTH, nInfinity, pInfinity, MaxPlayer);
		}
		else
		{
			if(footmanIds.size() <= 0)
			{
				System.out.println("There's no footmen on the map.");
			}
			else if(archerIds.size() <= 0)
			{
				System.out.println("There's no archers on the map.");
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
	
	public Map<Integer, Action> alphabeta(Integer node, int depth, int alpha, int beta, int Player)
	{
		Map<Integer, Action> actions = new HashMap<Integer, Action>();
		
		if ( depth == 0  || node == terminal)
        	return actions; //the heuristic value of node
        if ( Player == MaxPlayer)
        {
        	for (Node childofnode : child)
            	alpha = max(alpha, alphabeta(child, depth-1, alpha, beta, not(Player)));
            	if (beta <= alpha)
            	{
                	break; //(* Beta cut-off *)
            	}
                return alpha;
        }
       else
       {
    	   for (Node childofnode : child )
    	   {
           		beta = min(beta, alphabeta(child, depth-1, alpha, beta, not(Player) )); 
           		if (beta <= alpha)
           		{
           			break; //(* Alpha cut-off *)
           		}
    	   }
    	   return beta;
       }
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
		
	public boolean checkGoal(UnitView neighbor, UnitView goal, StateView state) //checks if we have reached the goal based on if we neighbor the goalSpace
	{
		
		ArrayList<UnitView> units = getNeighbors(neighbor, state, true);
		
		Integer x = goal.getXPosition();
		Integer y = goal.getYPosition();
		
		for (UnitView unit : units) //for all neighbors
		{
			Integer unitX = unit.getXPosition();
			Integer unitY = unit.getYPosition();
			
			if (x == unitX && y == unitY) //if it's the same as the goal x, y
			{
				return true; //we found it!
			}
		}
		
		return false;
	}
	
	public UnitView checkXYList(ArrayList<UnitView> list, UnitView unit) //Used for checking based on whether or not we all ready have the space of values: x, y
	{
		Integer x = unit.getXPosition();
		Integer y = unit.getYPosition();
		
		for (UnitView item : list) //for every item in the list
		{
			if (item.getXPosition() == (x) && item.getYPosition() == (y)) //if it's there
				return item; //return it
		}
		return null; //otherwise return nothing
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
		
		for (int j = 0; j < 8; j++) //go through all possible 8 squares
		{
			switch(j) //Could use something better but it's too much thinking right now
			{
				case 0: //x + 1, y
					tempX = xPlusOne;
					tempY = y;
					break;
				case 1: //x + 1, y + 1
					tempX = xPlusOne;
					tempY = yPlusOne;
					break;
				case 2: //x + 1, y - 1
					tempX = xPlusOne;
					tempY = yMinusOne;
					break;
				case 3: //x, y + 1
					tempX = x;
					tempY = yPlusOne;
					break;
				case 4: //x, y - 1
					tempX = x;
					tempY = yMinusOne;
					break;
				case 5: //x - 1, y
					tempX = xMinusOne;
					tempY = y;
					break;
				case 6: //x - 1, y + 1
					tempX = xMinusOne;
					tempY = yPlusOne;
					break;
				case 7: //x - 1, y - 1
					tempX = xMinusOne;
					tempY = yMinusOne;
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
	
	public Integer heuristicCostCalculator(UnitView a, UnitView b)	{ //Just uses Chebyshev distances
	
		int x1 = a.getXPosition();
		int x2 = b.getXPosition();
		int y1 = a.getYPosition();
		int y2 = b.getYPosition();
		
		return (DistanceMetrics.chebyshevDistance(x1, y1, x2, y2));
	}
	
	public boolean checkValidNeighbor(Integer x, Integer y, StateView state, boolean unitDoesntMatter)	{ //returns if a space is empty and valid
		
		boolean isResource = state.isResourceAt(x, y); //check if there is a resource here
		boolean isUnit = state.isUnitAt(x, y); //check if there is a unit here
		boolean isValid = state.inBounds(x, y); //check if the square is valid
		
		boolean isNotTaken = !isResource && !isUnit; //if it is not an occupied square
		
		if ((isNotTaken || unitDoesntMatter) && isValid) //if there is no resource here and no unit and it's valid it means it's an empty square
		{
			return true;
		}
		
		return false;
	}

}
