package communication;

import client.ClientInterface;
import food.Dish;
import server.ServerInterface;

import java.io.*;
import java.net.*;
import java.util.Random;

public class Comms extends Thread {
    private static final int businessPortNumber = 1111;
    private int portNumber;             // Is businessPortNumber if this is the business application
    private ServerSocket serverSocket;
    private Socket socket;
    private int receiverPortNumber;     // Is businessPortNumber if this is a client application
    // Is last client application port number if this is the business application
    private Object app;

    public Comms(Object object) {
        app = object;
        try {
            if (object instanceof Client) {// Condition has to be tested
                serverSocket = new ServerSocket(new Random().nextInt(100) + 1111);     // Let the server find a port number for the client application
                portNumber = serverSocket.getLocalPort();    // And store it in this variable
                receiverPortNumber = businessPortNumber;
            } else {
                portNumber = businessPortNumber;
                serverSocket = new ServerSocket(portNumber);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        while (true && this.portNumber == businessPortNumber) {
            receiveMessage();
        }
    }

    public void sendMessage(int request) {
        sendMessage(request, null);
    }

    public void sendMessage(int request, Object msgObject) {
        try {
            socket = new Socket("localhost", receiverPortNumber);

            synchronized (socket) {
                // Serialize Message
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                objectOutputStream.writeObject(new Message(request, msgObject, portNumber));     // Give portNumber so receiver will know who to respond to

                // Clean up
                objectOutputStream.flush();
                objectOutputStream.close();
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Object receiveMessage() {
        try {
            //this.sleep(10000);
            socket = serverSocket.accept();

            synchronized (socket) {
                ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
                Message msg = (Message) objectInputStream.readObject();

                receiverPortNumber = msg.getSender();

                objectInputStream.close();
                socket.close();

                if (this.portNumber == businessPortNumber) {
                    switch (msg.getRequest()) {
                        case 100:
                            sendMessage(100, ((Server) app).addUser((User) msg.getContent()));
                            break;
                        case 102:
                            sendMessage(102, ((Server) app).loginUser((LogIn) msg.getContent()));
                            break;
                        case 103:
                            sendMessage(103, ((Server) app).getOrdersUser((User) msg.getContent()));
                            break;
                        case 108:
                            ((Server) app).clearBasket((User) msg.getContent());
                            break;
                        case 109:
                            sendMessage(109, ((Server) app).checkoutBasket((User) msg.getContent()));
                            break;
                        case 110:
                            sendMessage(100, ((Server) app).getBasketPrice((User) msg.getContent()));
                            break;
                        case 111:
                            sendMessage(111, ((Server) app).getBasket((User) msg.getContent()));
                            break;
                        case 104:
                            sendMessage(104, ((Server) app).getPostcodes());
                            break;
                        case 130:
                            ((Server) app).addInBasket((BasketEntry) msg.getContent());
                            break;
                        case 200:
                            sendMessage(200, ((Server) app).getDishes());
                            break;
                        case 303:
                            sendMessage(303, ((Server) app).getOrderCost((Order) msg.getContent()));
                            break;
                        case 302:
                            ((Server) app).removeOrder((Order) msg.getContent());
                            break;
                        case 304:
                            sendMessage(304, ((Server) app).getOrderStatus((Order) msg.getContent()));
                            break;
                        case 400:
                            sendMessage(400, ((Server) app).getDish((Dish) msg.getContent()).getPrice());
                            break;
                        case 401:
                            sendMessage(401, ((Server) app).getDish((Dish) msg.getContent()).getDescription());
                            break;
                    }
                }
                return msg.getContent();
            }

        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        } catch (ServerInterface.UnableToDeleteException e) {
            e.printStackTrace();
        }

        return null;
    }
}
