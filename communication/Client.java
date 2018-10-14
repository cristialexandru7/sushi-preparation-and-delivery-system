package communication;

import client.ClientInterface;
import common.UpdateEvent;
import common.UpdateListener;
import food.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Client implements ClientInterface, Serializable, Runnable  {

    private Comms comms = new Comms(this);
    private boolean logIn = false;

    public Client() {
        Thread thread = new Thread(this);
        thread.start();
        this.comms.start();
    }

    @Override
    public User register(String username, String password, String address, Postcode postcode) {
        User newUser = new User(username, password, address, postcode);
        comms.sendMessage(100, newUser);
        logIn = true;
        return (User) comms.receiveMessage();
    }


    @Override
    public User login(String username, String password) {
        comms.sendMessage(102, new LogIn(username,password));
        logIn = true;
        return (User) comms.receiveMessage();
    }

    @Override
    public List<Postcode> getPostcodes() {
        comms.sendMessage(104);
        return (List<Postcode>) comms.receiveMessage();
    }

    @Override
    public List<Dish> getDishes() {
        comms.sendMessage(200);
        return (List<Dish>) comms.receiveMessage();
    }

    private List<UpdateListener> updateListeners = new ArrayList<>();

    @Override
    public void addUpdateListener(UpdateListener listener) {
        updateListeners.add(listener);
    }

    @Override
    public void notifyUpdate() {
        for(UpdateListener listener : updateListeners) {
            listener.updated(new UpdateEvent());
        }
    }

    @Override
    public void cancelOrder(Order order) {
        comms.sendMessage(302, order);
    }

    @Override
    public Number getOrderCost(Order order) {
        comms.sendMessage(303, order);
        return (Number) comms.receiveMessage();
    }

    @Override
    public String getOrderStatus(Order order) {
        comms.sendMessage(304, order);
        return (String) comms.receiveMessage();
    }

    @Override
    public boolean isOrderComplete(Order order) {
        comms.sendMessage(304, order);
        if(comms.receiveMessage().equals(Order.COMPLETE_STATUS)) {
            return true;
        }
        return false;
    }

    @Override
    public List<Order> getOrders(User user) {
        comms.sendMessage(103, user);
        return (List<Order>) comms.receiveMessage();
    }

    @Override
    public void clearBasket(User user) {
        comms.sendMessage(108, user);
    }

    @Override
    public Order checkoutBasket(User user) {
        comms.sendMessage(109, user);
        return (Order) comms.receiveMessage();
    }

    @Override
    public void updateDishInBasket(User user, Dish dish, Number quantity) {
        BasketEntry basketEntry = new BasketEntry(user, dish, quantity);
        comms.sendMessage(130, basketEntry);
    }

    @Override
    public void addDishToBasket(User user, Dish dish, Number quantity) {
        BasketEntry basketEntry = new BasketEntry(user, dish, quantity);
        comms.sendMessage(130, basketEntry);

    }

    @Override
    public Number getBasketCost(User user) {
        comms.sendMessage(110, user);
        return (Number) comms.receiveMessage();
    }

    @Override
    public Map<Dish, Number> getBasket(User user) {
        comms.sendMessage(111, user);
        return (Map<Dish, Number>) comms.receiveMessage();
    }

    @Override
    public Number getDishPrice(Dish dish) {
        comms.sendMessage(400, dish);
        return (Number) comms.receiveMessage();
    }

    @Override
    public String getDishDescription(Dish dish) {
        comms.sendMessage(401, dish);
        return (String) comms.receiveMessage();
    }

    @Override
    public void run() {
        while(true) {
            if(logIn) {
                try {
                    this.notifyUpdate();
                } catch (NullPointerException e) {
                    this.run();
                }
            }
        }
    }
}
