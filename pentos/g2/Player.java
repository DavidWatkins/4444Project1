package pentos.g2;

import pentos.sim.Cell;
import pentos.sim.Building;
import pentos.sim.Land;
import pentos.sim.Move;

import java.util.*;

public class Player implements pentos.sim.Player {

    private Random gen = new Random();
    private Set<Cell> road_cells = new HashSet<Cell>();
    private Set<Cell> road_cells_on_board = new HashSet<Cell>();

    private static int POND_SIZE=4;
    private static int PARK_SIZE=4;

    /* Data structures for blob detection:
    private boolean[][] occupied_cells;

    private int max_i = 0;
    private int max_j = 0;

    private int staging_max_i = 0;
    private int staging_max_j = 0;
    */


    public void init() { // function is called once at the beginning before play is called
        // For blob detection:
        //occupied_cells = new boolean[50][50];
    }

    private boolean isborderRoad(int i, int j) {
        return ( (i == -1) ||
                 (i == 51) ||
                 (j == -1) ||
                 (j == 51)
               );
    }

    public Move play(Building request, Land land) {



        // find all valid building locations and orientations
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
        // choose a building placement at random
        if (moves.isEmpty()) { // reject if no valid placements
            System.out.println("YOu do not have any valid moves");
            return new Move(false);
        } else {
            int inc;
            int placement_idx;
            if(request.type == Building.Type.FACTORY) {
                placement_idx = moves.size() - 1;
                inc = -1;
            } else {
                placement_idx = 0;
                inc = 1;
            }

            int numAdj = 0;
            Set<Cell> shiftedCells_final = new HashSet<Cell>();
            Move chosen_final = new Move(false);
            int counter = 0;
            int internal_counter = 0;
            boolean buildParkPonds = false;
            boolean alreadyConnectedToPond = false;
            boolean alreadyConnectedToPark = false;
            while (counter < moves.size()) {

                // Look at the next possible place to look for
                Move chosen = moves.get(placement_idx);

                if (request.type == Building.Type.FACTORY) {

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

                        return chosen;
                    }
                    else { // Reject placement if building cannot be connected by road
                        placement_idx += inc;
                        if(placement_idx < 0 || placement_idx >= moves.size()) {
                            printRejectedRequest(shiftedCells);
                            return new Move(false);
                        }
                    }


                }


                if ((request.type == Building.Type.RESIDENCE)) {


                    // Get coordinates of building placement (position plus local building cell coordinates).
                    Set<Cell> shiftedCells = new HashSet<Cell>();
                    for (Cell x : chosen.request.rotations()[chosen.rotation]) {
                        shiftedCells.add(new Cell(x.i+chosen.location.i, x.j+chosen.location.j));
                    }
                    int curr_sides = 0;
                    boolean alreadyConnectedToRoad = false;
                    alreadyConnectedToPond = false;
                    alreadyConnectedToPark = false;
                    for (Cell x : shiftedCells) {
                        for (int i_seg = -1; i_seg <= 1; i_seg++) {
                            for (int j_seg = -1; j_seg <= 1; j_seg++) {
                                if (Math.abs(i_seg) == Math.abs(j_seg)) continue;
                                int curr_i = x.i + i_seg;
                                int curr_j = x.j + j_seg;
                                int curr_i_actual = curr_i;
                                int curr_j_actual = curr_j;
                                curr_i = (int)Math.max((double)curr_i, 0.0);
                                curr_i = (int)Math.min((double)curr_i, land.side-1);
                                curr_j = (int)Math.max((double)curr_j, 0.0);
                                curr_j = (int)Math.min((double)curr_j, land.side-1);
                                Cell curr = new Cell(curr_i, curr_j);
                                if ( ((!land.unoccupied(curr)) && (!shiftedCells.contains(curr))) ||
                                     ((land.isField(curr)) && (!shiftedCells.contains(curr))) ||
                                     ((land.isPond(curr)) && (!shiftedCells.contains(curr))) ||
                                     (isborderRoad(curr_i_actual, curr_j_actual)) )
                                {
                                    curr_sides++;
                                }

                                alreadyConnectedToRoad = (road_cells_on_board.contains(curr) || alreadyConnectedToRoad);
                                alreadyConnectedToPond = (land.isPond(curr) || alreadyConnectedToPond);
                                alreadyConnectedToPark = (land.isField(curr) || alreadyConnectedToPark);
                            }
                        }
                    }
                    //String s = String.format("number of current sides occupied is %d", curr_sides);
                    //System.out.println(s);
                    if (curr_sides <= numAdj) {
                        placement_idx += inc;
                        if(placement_idx < 0 || placement_idx >= moves.size()) {
                            printRejectedRequest(shiftedCells);
                            return new Move(false);
                        }
                        internal_counter++;
                        if (internal_counter > (.4 * moves.size())) {
                            counter = moves.size();
                            internal_counter = 0;
                        }
                        continue;
                    } else {
                        numAdj = curr_sides;
                        internal_counter = 0;
                    }

                    // Build a road to connect this building to perimeter.
                    if (!alreadyConnectedToRoad) {
//                    if (!alreadyConnectedToRoad) {
                        Set<Cell> roadCells = findShortestRoad(shiftedCells, land);
                        if (roadCells != null) {

                            // For the existing road algorithm
                            chosen.road = roadCells;
                            buildParkPonds = true;
                            shiftedCells_final = shiftedCells;
                            chosen_final = chosen;

                        }
                        else { // Reject placement if building cannot be connected by road
                            placement_idx += inc;
                            if(placement_idx < 0 || placement_idx >= moves.size()) {
                                printRejectedRequest(shiftedCells);
                                return new Move(false);
                            }
                        }
                    } else {
                        buildParkPonds = true;
                        shiftedCells_final = shiftedCells;
                        chosen_final = chosen;
                    }
                    counter++;
                }
            }
//            if (false) {
            if (buildParkPonds) {

                // Generate pseudo-random parks/ponds
                Set<Cell> markedForConstruction = new HashSet<Cell>();
                markedForConstruction.addAll(chosen_final.road);

                int topscore = 0;
                if (alreadyConnectedToPond) topscore = 100;
                Set<Cell> PP = new HashSet<Cell>();
                for (int tryidx = 0; tryidx < 10; tryidx++) {
                    PP = randomShape(shiftedCells_final, markedForConstruction, land, POND_SIZE);
                    int score = 0;
                    for (Cell PPn : PP) {
                        for (Cell c : PPn.neighbors()) {
                            if (PP.contains(c)) continue;
                            if (shiftedCells_final.contains(c)) score++;
                            if (chosen_final.road.contains(c)) score--;
                            if (!land.unoccupied(c) &&
                                !land.isPond(c) &&
                                !land.isField(c) &&
                                !shiftedCells_final.contains(c)) score--;

                        }
                    }
                    if (score > topscore) {
                        topscore = score;
                        chosen_final.water = PP;
                    }
                }

                //chosen_final.water = randomWalk(shiftedCells_final, markedForConstruction, land, 4);
                markedForConstruction.addAll(chosen_final.water);

                topscore = 0;
                if (alreadyConnectedToPark) topscore = 100;
                for (int tryidx = 0; tryidx < 10; tryidx++) {
                    PP = randomShape(shiftedCells_final, markedForConstruction, land, PARK_SIZE);
                    int score = 0;
                    for (Cell PPn : PP) {
                        for (Cell c : PPn.neighbors()) {
                            if (PP.contains(c)) continue;
                            if (shiftedCells_final.contains(c)) score++;
                            if (chosen_final.road.contains(c)) score--;
                            if (chosen_final.park.contains(c)) score++;
                            if (!land.unoccupied(c) &&
                                !land.isPond(c) &&
                                !land.isField(c) &&
                                !shiftedCells_final.contains(c)) score--;
                        }
                    }
                    if (score > topscore) {
                        topscore = score;
                        chosen_final.park = PP;
                    }
                }

                //chosen_final.park = randomWalk(shiftedCells_final, markedForConstruction, land, 4);
            }

            if (chosen_final.road != null) {
                road_cells.addAll(chosen_final.road);
                road_cells_on_board.addAll(chosen_final.road);
            }

            if (chosen_final == (new Move(false))) {
                System.out.println("YOU ARE REJECTING RIGHT HERE");
            }

            // Return the final chosen move
            return chosen_final;

        }
    }






    // build shortest sequence of road cells to connect to a set of cells b
    private Set<Cell> findShortestRoad(Set<Cell> b, Land land) {
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









    // walk n consecutive cells starting from a building. Used to build a random field or pond.
    private Set<Cell> randomWalk(Set<Cell> b, Set<Cell> marked, Land land, int n) {

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
                if (!b.contains(p) &&
                    !marked.contains(p) &&
                    land.unoccupied(p) &&
                    !output.contains(p)) {
                        walk_cells.add(p);
                    }
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





    // walk n consecutive cells starting from a building. Used to build a random field or pond.
    private Set<Cell> randomShape(Set<Cell> b, Set<Cell> marked, Land land, int n) {

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

        // if there aren't any open cells, return empty set
        if (adjCells.isEmpty())
            return new HashSet<Cell>();

        // get a random Tail
        Cell tail = adjCells.get(gen.nextInt(adjCells.size()));

        // no create a ranodm shape, not even a random walk
        Set<Cell> shape = new HashSet<Cell>();
        shape.add(tail);
        for (int ii=0; ii<n; ii++) {
            ArrayList<Cell> walk_cells = new ArrayList<Cell>();
            for (Cell x : shape) {
                for (Cell p : x.neighbors()) {
                    if (!b.contains(p) && !marked.contains(p) && land.unoccupied(p) && !output.contains(p) && !shape.contains(p))
                        walk_cells.add(p);
                }
            }
            if (walk_cells.isEmpty()) {
            //return output; //if you want to build it anyway
                continue;
            }
//            String s = String.format("The shape size is currently %d and the walk_cells size is currently %d", shape.size(), walk_cells.size());
//            System.out.println(s);
            output.add(tail);
            tail = walk_cells.get(gen.nextInt(walk_cells.size()));
            shape.add(tail);

        }
        return output;
    }






    private void printRejectedRequest(Set<Cell> cells) {
        String s = "The coordinates of the rejected request are ";
        for (Cell c : cells) {
            s = s + c.toString() + " ";
        }
        System.out.println(s);
    }


}
