package communication;

import java.io.Serializable;

public class LogIn implements Serializable {
    String username;
    String password;

    public LogIn(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}