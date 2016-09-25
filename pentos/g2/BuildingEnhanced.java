package pentos.g2;

import pentos.sim.Building;
import pentos.sim.Cell;

import java.util.*;

/**
 * Created by David on 9/24/2016.
 */
public class BuildingEnhanced {

    // cells in building (this rotation)
    private Set<Cell> cells;

    // Mapping of rotations
    private Map<Rotations, Set<Cell>> rotations;
    private List<Set<Cell>> rotationsList;

    public final pentos.sim.Building.Type type;

    private int width;
    private int height;

    public enum Rotations {ZERO, NINETY, ONEEIGHTY, TWOSEVENTY};

    public BuildingEnhanced(Building request) {
        this.cells = new HashSet<>();
        for(Cell c : request) {
            this.cells.add(c);
        }

        this.generateRotations();
        this.setDimensions();

        this.type = request.type;
    }

    private void generateRotations() {
        if (!Cell.isConnected(cells))
            throw new IllegalArgumentException("Cells not connected");
        int min_i = Integer.MAX_VALUE;
        int min_j = Integer.MAX_VALUE;
        int max_i = Integer.MIN_VALUE;
        int max_j = Integer.MIN_VALUE;
        for (Cell p : cells) {
            if (min_i > p.i) min_i = p.i;
            if (max_i < p.i) max_i = p.i;
            if (min_j > p.j) min_j = p.j;
            if (max_j < p.j) max_j = p.j;
        }
        Set <Cell> cells_0 = new HashSet<Cell>();
        Set <Cell> cells_1 = new HashSet <Cell> ();
        Set <Cell> cells_2 = new HashSet <Cell> ();
        Set <Cell> cells_3 = new HashSet <Cell> ();
        for (Cell p : cells) {
            cells_0.add(new Cell(p.i - min_i, p.j - min_j));
            cells_1.add(new Cell(p.j - min_j, max_i - p.i));
            cells_2.add(new Cell(max_i - p.i, max_j - p.j));
            cells_3.add(new Cell(max_j - p.j, p.i - min_i));
        }
        this.cells = cells_0;

        this.rotations = new HashMap<>();
        this.rotationsList = new ArrayList<>();

        this.rotations.put(Rotations.ZERO, cells_0);
        this.rotations.put(Rotations.NINETY, cells_1);
        this.rotations.put(Rotations.ONEEIGHTY, cells_2);
        this.rotations.put(Rotations.TWOSEVENTY, cells_3);

        this.rotationsList.add(cells_0);
        this.rotationsList.add(cells_1);
        this.rotationsList.add(cells_2);
        this.rotationsList.add(cells_3);
    }

    public Set<Cell> getRotation(Rotations rotation) {
        return this.rotations.get(rotation);
    }

    public int getNorthernMostIndex() {
        return getNorthernMostIndex(Rotations.ZERO);
    }

    public int getNorthernMostIndex(Rotations rotation) {
        int northernMostIndex = Integer.MAX_VALUE;
        for(Cell c : rotations.get(rotation)) {
            if(c.j < northernMostIndex)
                northernMostIndex = c.j;
        }
        return northernMostIndex;
    }

    public int getSouthernMostIndex() {
        return getSouthernMostIndex(Rotations.ZERO);
    }

    public int getSouthernMostIndex(Rotations rotation) {
        int southernMostIndex = Integer.MIN_VALUE;
        for(Cell c : rotations.get(rotation)) {
            if(c.j > southernMostIndex)
                southernMostIndex = c.j;
        }
        return southernMostIndex;
    }

    private void setDimensions() {
        int rowMin = Integer.MAX_VALUE, rowMax = Integer.MIN_VALUE, colMin = Integer.MAX_VALUE,
                colMax = Integer.MIN_VALUE;

        for(Cell temp : this.getRotation(Rotations.ZERO)) {
            rowMin = (temp.i < rowMin) ? temp.i : rowMin;
            rowMax = (temp.i > rowMax) ? temp.i : rowMax;
            colMin = (temp.j < colMin) ? temp.j : colMin;
            colMax = (temp.j > colMax) ? temp.j : colMax;
        }

        this.height = rowMax - rowMin + 1;
        this.width = colMax - colMin + 1;
    }

    public int getWidth(Rotations rotation) {
        if(rotation == Rotations.ZERO || rotation == Rotations.ONEEIGHTY)
            return width;
        else
            return height;
    }

    public int getHeight(Rotations rotation) {
        if(rotation == Rotations.ZERO || rotation == Rotations.ONEEIGHTY)
            return height;
        else
            return width;
    }

    public int getWesternMostIndex() {
        return getWesternMostIndex(Rotations.ZERO);
    }

    public int getWesternMostIndex(Rotations rotation) {
        int westernMostIndex = Integer.MAX_VALUE;
        for(Cell c : rotations.get(rotation)) {
            if(c.i < westernMostIndex)
                westernMostIndex = c.i;
        }
        return westernMostIndex;
    }

    public int getEasternMostIndex() {
        return getEasternMostIndex(Rotations.ZERO);
    }

    public int getEasternMostIndex(Rotations rotation) {
        int easternMostIndex = Integer.MIN_VALUE;
        for(Cell c : rotations.get(rotation)) {
            if(c.i > easternMostIndex)
                easternMostIndex = c.i;
        }
        return easternMostIndex;
    }

    /**
     * Returns whether a cell exists at position (i,j)
     * @param i
     * @param j
     * @return
     */
    public boolean cellAt(int i, int j) {
        for(Cell c : cells) {
            if(c.i == i && c.j == j)
                return true;
        }
        return false;
    }

    public List<Set<Cell>> rotations() {
        return rotationsList;
    }

    public boolean valid() {
        if (type == pentos.sim.Building.Type.FACTORY) {
            int mini = 0;
            int maxi = Integer.MAX_VALUE;
            int minj = 0;
            int maxj = Integer.MAX_VALUE;
            for (Cell p : cells) {
                if (p.i < mini)
                    mini = p.i;
                if (p.i > maxi)
                    maxi = p.i;
                if (p.j < minj)
                    minj = p.j;
                if (p.j > maxj)
                    maxj = p.j;
            }
            if ( (maxi - mini > 5) || (maxj - minj > 5) )
                return false;
            return ( (maxi - mini) * (maxj - minj) == size());
        }
        else if (type == pentos.sim.Building.Type.RESIDENCE)
            return cells.size() == 5;
        else
            return false;
    }

    public Building toBuilding() {
        return this.toBuildingRotatedBy(Rotations.ZERO);
    }

    public Building toBuildingRotatedBy(Rotations rotation) {
        Set<Cell> cell_set = this.rotations.get(rotation);
        Cell[] cells = new Cell[cell_set.size()];
        Iterator<Cell> iter = cell_set.iterator();
        for(int i = 0; i < cell_set.size(); ++i)
            cells[i] = iter.next();

        return new Building(cells, this.type);
    }

    // size of building
    public int size() {
        return cells.size();
    }

    // check if buildings are equal
    public boolean equals(BuildingEnhanced building) {
        if (cells.size() == building.cells.size()) {
            if(this.rotations.equals(building.rotations))
                return true;
        }
        return false;
    }

    public pentos.sim.Building.Type getType() {return type;}

    // generic equal
    public boolean equals(Object obj) {
        if (obj instanceof pentos.sim.Building)
            return equals((pentos.sim.Building) obj);
        return false;
    }

    // the only way to access the cells
    public Iterator<Cell> iterator()
    {
        return cells.iterator();
    }

    // convert building to string
    public String toString(Rotations rotation) {
        String out = "";
        if(this.type == Building.Type.FACTORY) {
            out += "FACTORY ";
            int rowMin = Integer.MAX_VALUE, rowMax = Integer.MIN_VALUE, colMin = Integer.MAX_VALUE,
                    colMax = Integer.MIN_VALUE;

            for(Cell temp : cells) {
                rowMin = (temp.i < rowMin) ? temp.i : rowMin;
                rowMax = (temp.i > rowMax) ? temp.i : rowMax;
                colMin = (temp.j < colMin) ? temp.j : colMin;
                colMax = (temp.j > colMax) ? temp.j : colMax;
            }

            int height = rowMax - rowMin + 1;
            int width = colMax - colMin + 1;

            out += height + " " + width;
        } else { //Residence
            out += "RESIDENCE ";
            for(Cell c : cells) {
                out += c.i + "," + c.j + " ";
            }
        }
        return out;
    }

    // default convert to string
    public String toString() {
        return toString(Rotations.ZERO);
    }
}