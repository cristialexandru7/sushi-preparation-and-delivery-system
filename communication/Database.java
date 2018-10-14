package communication;

import common.Model;
import food.Dish;
import food.Ingredient;
import kitchen.Drone;
import kitchen.Staff;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class Database extends Model implements Serializable {

    List<User> users = new CopyOnWriteArrayList<>();
    List<Order> orders = new CopyOnWriteArrayList<>();
    List<Postcode> postcodes = new CopyOnWriteArrayList<>();
    List<Staff> staffs = new CopyOnWriteArrayList<>();
    List<Drone> drones = new CopyOnWriteArrayList<>();
    List<Basket> baskets = new CopyOnWriteArrayList<>();

    private boolean restockingIngredients = true;
    private boolean restockingDishes = true;

    Map<Dish, Number> dishesForOrders = new ConcurrentHashMap<>();
    Map<Ingredient, Number> ingredientsForDishes = new ConcurrentHashMap<>();

    public List<Basket> getBaskets() {
        return baskets;
    }

    public boolean getRestockingIngredients() {
        return restockingIngredients;
    }

    public boolean getRestockingDishes() {
        return restockingDishes;
    }

    public void setRestockingDishes(boolean restockingDishes) {
        this.restockingDishes = restockingDishes;
    }

    public void setRestockingIngredients(boolean restockingIngredients) {
        this.restockingIngredients = restockingIngredients;
    }

    public List<Postcode> getPostcodes() {
        return postcodes;
    }

    public List<User> getUsers() {
        return this.users;
    }

    public List<Drone> getDrones() {
        return drones;
    }

    public List<Staff> getStaff() {

        return staffs;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void addUser(User user){
        users.add(user);
    }

    public void addOrder(Order order){

        orders.add(order);
    }

    public void addPostcode(Postcode postcode){
        postcodes.add(postcode);
    }

    public void removeOrder(Order order) {
        orders.remove(order);
    }

    public void addStaff(Staff staff) {
        Thread thread = new Thread(staff);
        thread.start();
        staffs.add(staff);
    }

    public void addDrone(Drone drone) {
        Thread thread = new Thread(drone);
        thread.start();
        drones.add(drone);
    }

    public void startDronesStaff() {
        for(Drone drone : drones) {
            Thread thread = new Thread(drone);
            thread.start();
        }
        for(Staff staff : staffs) {
            Thread thread = new Thread(staff);
            thread.start();
        }
    }

    public void refreshDishesForOrders() {
        Map<Dish, Number> dishesForOrders = new ConcurrentHashMap<>();
        for(Order order : orders) {
            if(order.getStatus().equals(Order.PREPARING_STATUS)) {
                for (Dish dish : order.getOrder().keySet()) {
                    //System.out.println(dish.getStock().checkStock());
                    if (dish.getStock().checkStock() && dish.getStock().getCurrentStock().doubleValue() <= order.getOrder().get(dish).doubleValue()) {
                        //System.out.println(dish.getStock().checkStock());
                        if (dishesForOrders.containsKey(dish)) {
                            Number current = dishesForOrders.get(dish);
                            dishesForOrders.put(dish, current.doubleValue() + order.getOrder().get(dish).doubleValue());
                        } else {
                            dishesForOrders.put(dish, order.getOrder().get(dish));
                        }
                    }
                }
            }
        }

        for (Dish dish : dishesForOrders.keySet()) {
                Number current = dishesForOrders.get(dish);
                dishesForOrders.put(dish, current.doubleValue() - dish.getStock().getCurrentStock().doubleValue());
                //System.out.println(dish.getName() + " " + dishesForOrders.get(dish).doubleValue());
        }
        this.dishesForOrders = dishesForOrders;
    }

    public void refreshIngredientForDishes() {
        this.refreshDishesForOrders();

        Map<Ingredient, Number> ingredientsForDishes = new ConcurrentHashMap<>();
        for(Dish dish : dishesForOrders.keySet()) {
            synchronized (dish.getLock()) {
                if (dish.getStock().checkStock()) {
                    for (Ingredient ingredient : dish.getRecipe().keySet()) {
                        if (ingredientsForDishes.containsKey(ingredient)) {
                            Number current = ingredientsForDishes.get(ingredient);
                            ingredientsForDishes.put(ingredient, current.doubleValue() + dish.getRecipe().get(ingredient).doubleValue() * dishesForOrders.get(dish).doubleValue());
                        } else {
                            ingredientsForDishes.put(ingredient, dish.getRecipe().get(ingredient).doubleValue() * dishesForOrders.get(dish).doubleValue());
                        }
                    }
                }
            }
        }

        for (Ingredient ingredient : ingredientsForDishes.keySet()) {
            Number current = ingredientsForDishes.get(ingredient);
            ingredientsForDishes.put(ingredient, current.doubleValue() - ingredient.getStock().getCurrentStock().doubleValue());
            //System.out.println(ingredient.getName() + " " + ingredientsForDishes.get(ingredient).doubleValue());
        }
        this.ingredientsForDishes = ingredientsForDishes;
    }

    public Map<Ingredient, Number> getIngredientsForDishes() {
        return ingredientsForDishes;
    }

    public Map<Dish, Number> getDishesForOrders() {
        return dishesForOrders;
    }

    @Override
    public String getName() {
        return "Database";
    }
}
