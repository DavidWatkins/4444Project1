package pentos.g2;

import javafx.util.Pair;
import pentos.sim.Building;
import pentos.sim.Cell;
import pentos.sim.Land;
import pentos.sim.Move;

import java.util.*;

import static pentos.g2.BuildingEnhanced.Rotations.ZERO;

/**
 * Created by David on 9/24/2016.
 */
public class GridContainer {

    private final HashSet<Cell> road_cells;
    //Variables for factory placement
    private Set<RoadMarker> roadMarkers;
    private int triangleIndex;
    

    private Type[][] grid;
    private int numberOfInvalidPlacements;
    public final static int DIMENSION = 50;

    enum Type {FACTORY, RESIDENCE, NONE, POND, PARK, ROAD};

    private Random gen;

    public GridContainer() {
        road_cells = new HashSet<>();
        roadMarkers = new HashSet<>();
        numberOfInvalidPlacements = 0;
        grid = new Type[DIMENSION][DIMENSION];
        for(int i = 0; i < DIMENSION; ++i) {
            for(int j = 0; j < DIMENSION; ++j) {
                grid[i][j] = Type.NONE;
            }
        }
        gen = new Random(1243);

        //Add road marker to bottom:
        roadMarkers.add(new RoadMarker(true, DIMENSION));
        triangleIndex = 10;
    }

    private boolean withinTriangle(Cell c) {
        return (DIMENSION - c.i) + (DIMENSION - c.j) <= triangleIndex;
    }

    private int getNextRoadMarker(int height) {
        int highest = Integer.MAX_VALUE;
        for(RoadMarker roadMarker : roadMarkers) {
            if(roadMarker.getIndex() < highest) {
                highest = roadMarker.getIndex();
            }
        }
        return highest - 3 * height;
    }

    private Set<Cell> getRoadInstructions(Cell initialPosition, int length, Land land) {
        //Step through linear locations. If the path is obstructed return null
        Set<Cell> roads = new HashSet<>();
        if(initialPosition.j == DIMENSION)
            return new HashSet<Cell>();

        for(int i = DIMENSION - 1; i > DIMENSION - length; --i) {
            if(grid[initialPosition.j][i] == Type.ROAD)
                continue;
            if(!land.unoccupied(initialPosition.j, i))
                return null;
            roads.add(new Cell(initialPosition.j, i, Cell.Type.ROAD));
        }
        return roads;
    }

    public Move bestFactoryLocation(BuildingEnhanced request, Land land) {

        Set<RoadMarker.BuildInstruction> validPlacements = new HashSet<>();
        Set<RoadMarker.BuildInstruction> withinTriangle = new HashSet<>();

        //Find all valid placements of current roadmarkers
        for(RoadMarker roadMarker : roadMarkers) {
            RoadMarker.BuildInstruction instruction = roadMarker.getNextPlacement(request, ZERO, land);
            if(instruction != null)
                validPlacements.add(instruction);
        }

        //In order to determine if a point is within the bottom right triangle:
        //  Sum (DIMENSION - x)+(DIMENSION - y) and check if it is less than triangleIndex

        while(triangleIndex < 2*DIMENSION) {

            //Check to see which points are within the current triangleIndex:
            for(RoadMarker.BuildInstruction b : validPlacements) {
                if(withinTriangle(b.location) && b.roadLength < triangleIndex) {
                    withinTriangle.add(b);
                }
            }

            //If no points exist in the initial validPlacements that are within the triangle, try adding a new roadMarker
            //2*height of the current request
            if(withinTriangle.isEmpty()) {
                int nextRoadMarker = getNextRoadMarker(request.getHeight(ZERO));

                //Check to see that we aren't out of bounds
                //Check to see if road marker is within triangle, if not bump up triangle size
                if(nextRoadMarker < 0 || (DIMENSION - nextRoadMarker) > triangleIndex){
                    triangleIndex += 5;
                    continue;
                }

                RoadMarker roadMarker = new RoadMarker(true, nextRoadMarker);
//                roadMarker.getValidPlacements(request, ZERO, validPlacements);
                RoadMarker.BuildInstruction instruction = roadMarker.getNextPlacement(request, ZERO, land);
                if(instruction != null)
                    validPlacements.add(instruction);
                roadMarkers.add(roadMarker);
                continue;
            }

            //Now check to see that the tiles within the triangle are buildable. If it is buildable return it
            for(RoadMarker.BuildInstruction b : withinTriangle) {
                if(land.buildable(request.toBuilding(), b.location)) {
                    //Once we know it is buildable, we need to find a path to it from the base tile
                    //We should be checking if horizontal here, but we are ignoring that for now.
                    Set<Cell> roadPositions = getRoadInstructions(b.initialPosition, b.roadLength, land);

                    if(roadPositions != null) {
                        Move newMove = new Move(true, request.toBuilding(), b.location, 0, roadPositions, new HashSet<Cell>(), new HashSet<Cell>());
                        updateGrid(newMove);
                        return newMove;
                    }
                }
            }

            //No valid placements found
            triangleIndex += 5;
        }

        numberOfInvalidPlacements++;
        return new Move(false);
    }

//    private boolean unoccupied(int i, int j) {
//        return i >= 0 && i < grid.length &&
//                j >= 0 && j < grid[i].length &&
//                grid[i][j] == Type.NONE;
//    }
//
//    private boolean buildable(BuildingEnhanced request, BuildingEnhanced.Rotations rotation, int i, int j) {
//        for (Cell p : request.getRotation(rotation)) {
//            if (!unoccupied(i + p.i, j + p.j))
//                return false;
//
//            for(int x_offset = -1; x_offset <= 1; ++x_offset) {
//                for(int y_offset = -1; y_offset <= 1; ++y_offset) {
//                    if(x_offset == 0 && x_offset == 0) continue;
//
//                    int x = i + x_offset;
//                    int y = j + y_offset;
//                    if(x >= 0 && y >= 0 && x < DIMENSION && y < DIMENSION &&
//                            grid[x][y] != null &&
//                            ((grid[x][y] == Type.FACTORY && request.type == Building.Type.RESIDENCE) ||
//                                    (grid[x][y] == Type.RESIDENCE && request.type == Building.Type.FACTORY)))
//                        return false;
//                }
//            }
//        }
//        return true;
//    }

    //Also need to update ponds and the such
    private void updateGrid(Move m) {
        Building request = m.request;
        Cell location = m.location;
        for(Cell c : request) {
            if(c.i + location.i < DIMENSION && c.j + location.j < DIMENSION) {
                if(request.type == Building.Type.FACTORY)
                    grid[c.i + location.i][c.j + location.j] = Type.FACTORY;
                if(request.type == Building.Type.RESIDENCE)
                    grid[c.i + location.i][c.j + location.j] = Type.RESIDENCE;
            }

        }
        for(Cell c : m.park) {
            grid[c.i][c.j] = Type.PARK;
        }
        for(Cell c : m.water) {
            grid[c.i][c.j] = Type.POND;
        }
        for(Cell c : m.road) {
            grid[c.i][c.j] = Type.ROAD;
        }
    }

    public ArrayList<Move> getPossibleMoves(Building request, Land land) {

        ArrayList <Move> moves = new ArrayList <Move> ();
        for (int i = 0 ; i < land.side ; i++)
            for (int j = 0 ; j < land.side ; j++) {
                Cell p = new Cell(i, j);
                Building[] rotations = request.rotations();
                for (int ri = 0 ; ri < rotations.length ; ri++) {
                    Building b = rotations[ri];
                    if (land.buildable(b, p))
                        moves.add(new Move(true, request, p, ri, new HashSet<Cell>(), new HashSet<Cell>(), new HashSet<Cell>()));
                }
            }

//        ArrayList <Move> moves = new ArrayList <Move> ();
//        for (int i = 0 ; i < land.side ; i++)
//            for (int j = 0 ; j < land.side ; j++) {
//                Cell p = new Cell(i, j);
//                for(BuildingEnhanced.Rotations rotation : BuildingEnhanced.Rotations.values()) {
//                    Building rotated = request.toBuildingRotatedBy(rotation);
//                    if(land.buildable(rotated, p)) {//buildable(request, rotation, i, j)) {
//                        moves.add(new Move(true, rotated, p, 0, new HashSet<Cell>(), new HashSet<Cell>(), new HashSet<Cell>()));
//                    }
//                }
//            }
        return moves;
    }

    private void getParkAndPondPlacement(Move current, Land land, Set<Cell> shiftedCells, Set<Cell> roadCells) {
        // Generate random parks/ponds
        Set<Cell> markedForConstruction = new HashSet<Cell>();
        markedForConstruction.addAll(roadCells);
        current.water = randomWalk(shiftedCells, markedForConstruction, land, 4);
        markedForConstruction.addAll(current.water);
        current.park = randomWalk(shiftedCells, markedForConstruction, land, 4);
    }

    public Move bestResidentLocation(Building request, Land land) {
        ArrayList <Move> moves = getPossibleMoves(request, land);
        // choose a building placement at random
        if (moves.isEmpty()) // reject if no valid placements
            return new Move(false);

        for(Move chosen : moves) {
            // Get coordinates of building placement (position plus local building cell coordinates).
            Set<Cell> shiftedCells = new HashSet<Cell>();
            for (Cell x : chosen.request.rotations()[chosen.rotation]) {
                shiftedCells.add(new Cell(x.i+chosen.location.i, x.j+chosen.location.j));
            }

            // Build a road to connect this building to perimeter.
            Set<Cell> roadCells = findShortestRoad(shiftedCells, land);
            if (roadCells != null) {

                // For the existing road algorithm
                chosen.road = roadCells;
                road_cells.addAll(roadCells);

                // Generate random parks/ponds
                Set<Cell> markedForConstruction = new HashSet<Cell>();
                markedForConstruction.addAll(roadCells);
                chosen.water = randomWalk(shiftedCells, markedForConstruction, land, 4);
                markedForConstruction.addAll(chosen.water);
                chosen.park = randomWalk(shiftedCells, markedForConstruction, land, 4);

                updateGrid(chosen);
                return chosen;
            }
        }

        //None of the valid positions have a possible road position
        numberOfInvalidPlacements++;
        return new Move(false);
    }

    public Set<Cell> findShortestRoad(Set<Cell> b, Land land) {
        Set<Cell> output = new HashSet<Cell>();
        boolean[][] checked = new boolean[land.side][land.side];
        Queue<Cell> queue = new LinkedList<Cell>();
        // add border cells that don't have a road currently
        Cell source = new Cell(Integer.MAX_VALUE,Integer.MAX_VALUE); // dummy cell to serve as road connector to perimeter cells
        for (int z=0; z<land.side; z++) {
            if (b.contains(new Cell(0,z)) || b.contains(new Cell(z,0)) || b.contains(new Cell(land.side-1,z)) || b.contains(new Cell(z,land.side-1))) //if already on border don't build any roads
                return output;
            if (land.unoccupied(0,z))
                queue.add(new Cell(0,z,source));
            if (land.unoccupied(z,0))
                queue.add(new Cell(z,0,source));
            if (land.unoccupied(z,land.side-1))
                queue.add(new Cell(z,land.side-1,source));
            if (land.unoccupied(land.side-1,z))
                queue.add(new Cell(land.side-1,z,source));
        }
        // add cells adjacent to current road cells
        for (Cell p : road_cells) {
            for (Cell q : p.neighbors()) {
                if (!road_cells.contains(q) && land.unoccupied(q) && !b.contains(q))
                    queue.add(new Cell(q.i,q.j,p)); // use tail field of cell to keep track of previous road cell during the search
            }
        }
        while (!queue.isEmpty()) {
            Cell p = queue.remove();
            checked[p.i][p.j] = true;
            for (Cell x : p.neighbors()) {
                if (b.contains(x)) { // trace back through search tree to find path
                    Cell tail = p;
                    while (!b.contains(tail) && !road_cells.contains(tail) && !tail.equals(source)) {
                        output.add(new Cell(tail.i,tail.j));
                        tail = tail.previous;
                    }
                    if (!output.isEmpty())
                        return output;
                }
                else if (!checked[x.i][x.j] && land.unoccupied(x.i,x.j)) {
                    x.previous = p;
                    queue.add(x);
                }

            }
        }
        if (output.isEmpty() && queue.isEmpty())
            return null;
        else
            return output;
    }

    public Set<Cell> randomWalk(Set<Cell> b, Set<Cell> marked, Land land, int n) {
        ArrayList<Cell> adjCells = new ArrayList<Cell>();
        Set<Cell> output = new HashSet<Cell>();
        for (Cell p : b) {
            for (Cell q : p.neighbors()) {
                if (land.isField(q) || land.isPond(q))
                    return new HashSet<Cell>();
                if (!b.contains(q) && !marked.contains(q) && land.unoccupied(q))
                    adjCells.add(q);
            }
        }
        if (adjCells.isEmpty())
            return new HashSet<Cell>();
        Cell tail = adjCells.get(gen.nextInt(adjCells.size()));
        for (int ii=0; ii<n; ii++) {
            ArrayList<Cell> walk_cells = new ArrayList<Cell>();
            for (Cell p : tail.neighbors()) {
                if (!b.contains(p) && !marked.contains(p) && land.unoccupied(p) && !output.contains(p))
                    walk_cells.add(p);
            }
            if (walk_cells.isEmpty()) {
                //return output; //if you want to build it anyway
                return new HashSet<Cell>();
            }
            output.add(tail);
            tail = walk_cells.get(gen.nextInt(walk_cells.size()));
        }
        return output;
    }

    public void addRowMarker(int index, boolean isHorizontal) {

    }

    public void recalculateBlobs() {

    }
}
