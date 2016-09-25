package pentos.RecordedSequence;

import pentos.sim.Building;
import pentos.sim.Cell;

import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * Created by David on 9/24/2016.
 */
public class Sequencer implements pentos.sim.Sequencer{

    private String filename;
    private Scanner sc;

    public static final String FACTORY = "FACTORY", RESIDENCE = "RESIDENCE";

    public Sequencer() {
        this("sequence.out");
    }

    public Sequencer(String filename) {
        this.filename = filename;
    }

    public void init() {
        File file = new File(filename);
        try{
            sc = new Scanner(file);
        } catch(FileNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private Building constructResidence(String line, String[] params) throws UnsupportedEncodingException {
        Set<Cell> residence = new HashSet<Cell>();
        if(params.length != 6)
            throw new UnsupportedEncodingException("File formatted illegally: " + line);

        for(int i = 1; i < params.length; ++i) {
            String[] coords = params[i].split(",");
            int xCoord = Integer.parseInt(coords[0]);
            int yCoord = Integer.parseInt(coords[1]);
            residence.add(new Cell(xCoord, yCoord));
        }
        return new Building(residence.toArray(new Cell[residence.size()]), Building.Type.RESIDENCE);
    }

    private Building constructFactory(String line, String[] params) throws UnsupportedEncodingException {
        Set<Cell> factory = new HashSet<Cell>();
        if(params.length != 3)
            throw new UnsupportedEncodingException("File formatted illegally: " + line);

        int width = Integer.parseInt(params[1]);
        int height = Integer.parseInt(params[2]);
        for (int i=0; i<width+2; i++) {
            for (int j=0; j<height+2; j++) {
                factory.add(new Cell(i,j));
            }
        }

        return new Building(factory.toArray(new Cell[factory.size()]), Building.Type.FACTORY);
    }

    public Building next() throws EOFException, UnsupportedEncodingException {
        if(sc.hasNextLine()) {
            String buildingType = sc.nextLine();
            String[] buildingParams = buildingType.split(" ");
            if(buildingParams.length < 1)
                throw new UnsupportedEncodingException("File formatted illegally: " + buildingType);
            if(buildingParams[0].startsWith(FACTORY)) {
                return constructFactory(buildingType, buildingParams);
            } else if(buildingParams[0].startsWith(RESIDENCE)) {
                return constructResidence(buildingType, buildingParams);
            }
        }
        throw new EOFException("Reached end of sequence file");
    }

}
