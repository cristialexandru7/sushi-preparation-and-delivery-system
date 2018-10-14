package food;

import common.Model;

import java.io.Serializable;

public class Supplier extends Model implements Serializable {

	private String name;
	private Number distance;
	
	public Supplier(String name, Number distance) {
		this.name = name;
		this.distance = distance;
	}

	@Override
	public String getName() {
		return this.name;
	}

	public Number getDistance() {
		return this.distance;
	}

	public void setDistance(double distance) {
		this.notifyUpdate("distance", this.distance, distance);
		this.distance = distance;
	}
}
