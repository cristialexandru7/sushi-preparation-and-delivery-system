package communication;

import common.Model;

import java.io.Serializable;

public class User extends Model implements Serializable {

    private String name;
    private String password;
    private String address;
    private Postcode postcode;

    public User(String username, String password, String address, Postcode postcode) {
        this.name = username;
        this.password = password;
        this.address = address;
        this.postcode = postcode;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAddress() {
        return this.address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Postcode getPostcode() {
        return postcode;
    }

    public void setPostcode(Postcode postcode) {
        this.postcode = postcode;
    }

    @Override
	public String getName() {
		return this.name;
	}

}
