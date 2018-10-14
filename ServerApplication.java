import communication.Server;
import server.ServerInterface;
import server.ServerWindow;

public class ServerApplication {

    public static ServerInterface initialise() {
        Server server = new Server();
        return server;
    }

    public static void launchGUI(ServerInterface server){
        ServerWindow windows = new ServerWindow(server);
    }

    public static void main(String[] args){
        launchGUI(initialise());
    }
}
