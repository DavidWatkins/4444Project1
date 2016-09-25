package pentos.g2;

import javafx.util.Pair;
import pentos.sim.Building;
import pentos.sim.Cell;
import pentos.sim.Land;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by David on 9/24/2016.
 * Road markers always start on the rightmost side
 */
public class RoadMarker {
    private boolean isHorizontal;

    private int index;

    public RoadMarker(boolean isHorizontal, int index) {
        this.isHorizontal = isHorizontal;
        this.index = index;
    }

    protected class BuildInstruction {
        protected Cell location;
        protected Cell initialPosition;
        protected int roadLength;

        protected BuildInstruction(int x, int y, Cell initialPosition, int roadLength) {
            this.location = new Cell(x, y);
            this.initialPosition = initialPosition;
            this.roadLength = roadLength;
        }
    }

    /**
     * Add to validPlacements all valid placements around this particular road markers
     */
    public BuildInstruction getNextPlacement(BuildingEnhanced request, BuildingEnhanced.Rotations rotation, Land land) {
        int height = request.getHeight(rotation);
        int width = request.getWidth(rotation);
        Cell initial = this.getBasePosition();

        Building b = request.toBuildingRotatedBy(rotation);

        if(isHorizontal) {
            for (int i = GridContainer.DIMENSION; i >= 0; --i) {

                //Add position on top of road
                if (index - height > 0 && i - width > 0) {
                    Cell pos = new Cell(index - height, i - width);
                    if (land.buildable(b, pos))
                        return new BuildInstruction(pos.i, pos.j, initial, GridContainer.DIMENSION - i + width);
                }
//                    validPlacements.add(new BuildInstruction(index - height, i - width, initial, GridContainer.DIMENSION - i + width));

                //Add position on top of road
                if (index + 1 < GridContainer.DIMENSION && i - width > 0) {
                    Cell pos = new Cell(index + 1, i - width);
                    if (land.buildable(b, pos))
                        return new BuildInstruction(pos.i, pos.j, initial, GridContainer.DIMENSION - i + width);
                }
//                //Add positions below road
//                if(index + 1 < GridContainer.DIMENSION && i - width > 0)
//                    validPlacements.add(new BuildInstruction(index + 1, i - width, initial, GridContainer.DIMENSION - i + width));
            }
        }
//        } else {// if(isVertical) {
//            for(int i = GridContainer.DIMENSION - 1; i >= 0; --i) {
//
//                //Add position to left of road
//                if(i - height > 0 && index - 1 - width > 0)
//                    validPlacements.add(new BuildInstruction(index - 1 - width, i - height, initial, GridContainer.DIMENSION - i + height));
//
//                //Add positions to the right of the road
//                if(index + 1 < GridContainer.DIMENSION && i - height > 0)
//                    validPlacements.add(new BuildInstruction(index + 1, i - height, initial, GridContainer.DIMENSION - i + height));
//            }
//        }
            return null;
    }

    public Cell getBasePosition() {
        if(isHorizontal)
            return new Cell(GridContainer.DIMENSION - 1, index);
        else
            return new Cell(index, GridContainer.DIMENSION - 1);
    }

    public int getIndex() {
        return index;
    }
}
