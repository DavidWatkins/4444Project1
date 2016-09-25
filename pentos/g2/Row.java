package pentos.g2;

import pentos.sim.Building;

import java.util.HashSet;
import java.util.Set;

public class Row {

	private int start, end;

	private int currentLocation;
	private int roadLocation;
    private int building_dim;

	private Set<Building> buildings;

	public Row(int start, int end, int roadLocation, int building_dim) {
		this.start = start;
		this.end = end;
		this.currentLocation = 0;
		this.buildings = new HashSet<Building>();
		this.roadLocation = roadLocation;
        this.building_dim = building_dim;
	}

	public Row(int start, int end, int roadLocation, int currentLocation, int building_dim) {
		this.start = start;
		this.end = end;
		this.currentLocation = currentLocation;
		this.buildings = new HashSet<Building>();
		this.roadLocation = roadLocation;
        this.building_dim = building_dim;
	}

	public int size() {
		return this.end - this.start;
	}

	public int getStart(){
		return start;
	}

	public int getEnd(){
		return end;
	}

	public int getCurrentLocation() {
		return currentLocation;
	}

	public int getRoadLocation(){
		return roadLocation;
	}

	public void setCurrentLocation(int currentLocation) {
		this.currentLocation = currentLocation;
	}

}
