package communication;

import common.Model;

import java.io.Serializable;

public class Postcode extends Model implements Serializable {

	private String name;
	private Number distance;

	public Postcode(String postcode, Number distance) {
		this.name = postcode;
		this.distance = distance;
	}

	@Override
	public String getName() {
		return this.name;
	}

    public Number getDistance() {
        return distance;
    }
}
