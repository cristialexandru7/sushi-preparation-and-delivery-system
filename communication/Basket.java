package communication;

import common.Model;
import food.Dish;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Basket extends Model implements Serializable {

    private User user;
    private Number totalPrice = 0.00;
    private Map<Dish, Number> basket = new ConcurrentHashMap<>();

    @Override
    public String getName() {
        return "Basket";
    }

    public Basket(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public Number getTotalPrice() {
        totalPrice = 0.00;
        for(Dish dish : basket.keySet()) {
            totalPrice = totalPrice.doubleValue() + dish.getPrice().doubleValue() * basket.get(dish).doubleValue();

        }
        return totalPrice;
    }

    public Map<Dish, Number> getBasket() {
        return basket;
    }

    public void addToBasket(Dish dish, Number quantity) {
        double quantityTemp = quantity.doubleValue();

        boolean found = false;

        if(quantityTemp != 0) {
            for (Dish dishCount : basket.keySet()) {
                if (dishCount.getName().equals(dish.getName())) {
                    found = true;
                    double currentQuantity = basket.get(dishCount).doubleValue();
                    basket.put(dishCount, currentQuantity + quantityTemp);
                }
            }
            if (found != true) {
                basket.put(dish, quantityTemp);
            }
        } else {
            for (Dish dishCount : basket.keySet()) {
                if (dishCount.getName().equals(dish.getName())) {
                    found = true;
                    basket.remove(dishCount);
                }
            }
        }
    }

    public void clearBasket() {
        basket = new HashMap<>();
    }
}
