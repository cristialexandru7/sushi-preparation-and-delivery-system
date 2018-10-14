package communication;

import common.Model;
import food.Dish;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Order extends Model implements Serializable {

	public static final String PREPARING_STATUS = "Preparing order";
	public static final String COMPLETE_STATUS = "Complete. Waiting for delivery!";
	public static final String DELIVERY_STATUS = "On your way";
	public static final String DELIVERED_STATUS = "Delivered";

	private Map<Dish, Number> order = new HashMap<>();
	private User user;
	private String status = PREPARING_STATUS;
	private Number price = 0.00;
    private Lock lock = new ReentrantLock();

	public Order(User user) {
	    this.user = user;
    }

    public Order(User user, Map<Dish, Number> order) {
        this.user = user;
        this.order = order;
    }

	public void setOrder(Map<Dish, Number> order) {
		this.order = order;
	}

	public User getUser() {
		return user;
	}

    public Lock getLock() {
        return lock;
    }

    @Override
	public String getName() {
		return "Order";
	}

	public Map<Dish, Number> getOrder(){
		return this.order;
	}

	public void addEntry(Dish dish, Number number) {

		order.put(dish, number);
		price=(double) price + (double) dish.getPrice() * (double) number;
	}

	public void removeEntry(Dish dish, Integer number) {

		order.remove(dish, number);
		price=(double) price - (double) dish.getPrice() * number;
	}

	public Number getCost() {
	    price = 0.00;
		for(Dish dish : order.keySet()){
		    price = (double) price + (double) dish.getPrice() * (double) order.get(dish);
        }
		return this.price;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}
