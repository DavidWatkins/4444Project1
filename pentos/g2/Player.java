package pentos.g2;

import pentos.sim.Building;
import pentos.sim.Cell;
import pentos.sim.Land;
import pentos.sim.Move;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Player implements pentos.sim.Player {

    private GridContainer gridContainer;

    public void init() { // function is called once at the beginning before play is called
        gridContainer = new GridContainer();
    }

    public Move play(Building request, Land land) {
        BuildingEnhanced betterRequest = new BuildingEnhanced(request);
        if(request.getType() == Building.Type.FACTORY) {
            return gridContainer.bestFactoryLocation(betterRequest, land);
        } else { //Handle residence
            return gridContainer.bestResidentLocation(request, land);
        }
    }

}
