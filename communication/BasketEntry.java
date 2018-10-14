package communication;

import common.Model;
import food.Dish;

import java.io.Serializable;

public class BasketEntry extends Model implements Serializable {

    private User user;
    private Dish dish;
    private Number quantity;

    public BasketEntry(User user, Dish dish, Number quantity) {
        this.user = user;
        this.dish = dish;
        this.quantity = quantity;
    }

    public User getUser() {
        return user;
    }

    public Dish getDish() {
        return dish;
    }

    public Number getQuantity() {
        return quantity;
    }

    @Override
    public String getName() {
        return null;
    }


}
