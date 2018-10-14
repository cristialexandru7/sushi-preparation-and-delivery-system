import client.ClientInterface;
import client.ClientWindow;
import communication.Client;
import communication.Comms;

public class ClientApplication {


    public static ClientInterface initialise(){
        Client client = new Client();
        Thread thread = new Thread(client);
        thread.start();
        return client;
    }

    public static void launchGUI(ClientInterface client){
        ClientWindow windows = new ClientWindow(client);
    }

    public static void main(String[] args){
        launchGUI(initialise());
    }
}
