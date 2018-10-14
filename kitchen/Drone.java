package kitchen;

import common.Model;
import communication.Database;
import communication.Order;
import communication.StockManagement;
import food.Dish;
import food.Ingredient;

import java.io.Serializable;
import java.util.*;

public class Drone extends Model implements Runnable, Serializable {

    private Number speed;
    private StockManagement stockManagement;
    private Database database;
    private boolean shutdown = false;

    public static final String DEFAULT_STATUS = "Idle";
    private static final String COLLECTING_STATUS = "Collecting ingredient";
    private static final String DELIVERY_STATUS = "Delivering";

    private String status = DEFAULT_STATUS;

    public Drone(Number speed, StockManagement stockManagement, Database database) {
        this.speed = speed;
        this.stockManagement = stockManagement;
        this.database = database;
    }

    public String getStatus() {
        return status;
    }

    public Number getSpeed() {
        return speed;
    }

    @Override
    public String getName() {
        return "Drone";
    }

    public void setDatabase(Database database) {
        this.database = database;
    }

    public void setStockManagement(StockManagement stockManagement) {
        this.stockManagement = stockManagement;
    }

    @Override
    public void run() {
        status = DEFAULT_STATUS;
        shutdown = false;
        try {
            while(!shutdown) {
                if(database.getRestockingIngredients())
                    this.checkRestockIngredients();
                this.checkDishesIngredients();
                this.checkOrdersIngredients();
                this.checkOrders();
                this.deliverOrder();
            }
        } catch (NullPointerException e) {
            this.run();
        }
    }

    public void shutdown() {
        shutdown = true;
    }

    public void checkRestockIngredients() {
        Iterator<Ingredient> it = stockManagement.getIngredients().iterator();
        while (it.hasNext()) {
            Ingredient ingredient = it.next();
            if(ingredient.getLock().tryLock()) {
                try {
                    if (!ingredient.getStock().checkStock()) {
                        Number distance = ingredient.getSupplier().getDistance();
                        try {
                            status = COLLECTING_STATUS;
                            Thread.sleep((long) ((double) distance / (double) speed * 10000 * 2));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        ingredient.getStock().setCurrentStock(ingredient.getStock().getRestockingThreshold().doubleValue() + ingredient.getStock().getRestockingAmount().doubleValue());
                        status = DEFAULT_STATUS;
                    }
                } finally {
                    ingredient.getLock().unlock();
                }
            }
        }
    }

    public void checkDishesIngredients() {
        Map<Ingredient, Number> ingredientsForDishes = stockManagement.getIngredientsForDishes();
        Set<Ingredient> ingredients = ingredientsForDishes.keySet();

        Iterator<Ingredient> it = ingredients.iterator();
        while (it.hasNext()) {
            Ingredient ingredient = it.next();
            if(ingredient.getLock().tryLock()) {
                try {
                    Number current = ingredientsForDishes.get(ingredient);
                    if(current.doubleValue() != 0.00) {
                        Number distance = ingredient.getSupplier().getDistance();
                        try {
                            status = COLLECTING_STATUS;
                            Thread.sleep((long) ((double) distance / (double) speed * 10000 * 2));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if(ingredient != null) {
                            //System.out.println(stockManagement.getIngredientsForDishes().get(ingredient));
                            ingredient.getStock().setCurrentStock(ingredient.getStock().getCurrentStock().doubleValue() + ingredientsForDishes.get(ingredient).doubleValue());
                        }
                        status = DEFAULT_STATUS;
                        if(ingredient != null)
                            ingredientsForDishes.put(ingredient, 0.00);
                    }
                } finally {
                    ingredient.getLock().unlock();
                }
            }
        }
    }

    public void checkOrdersIngredients() {

        Set<Ingredient> ingredients = database.getIngredientsForDishes().keySet();

        Iterator<Ingredient> it = ingredients.iterator();
        while (it.hasNext()) {
            Ingredient ingredient = it.next();
            if(ingredient.getLock().tryLock()) {
                try {
                    Number current = database.getIngredientsForDishes().get(ingredient);
                    if(current.doubleValue() != 0.00) {
                        Number distance = ingredient.getSupplier().getDistance();
                        try {
                            status = COLLECTING_STATUS;
                            Thread.sleep((long) ((double) distance / (double) speed * 10000 * 2));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if(ingredient != null) {
                            for(Ingredient ingredientToModify : stockManagement.getIngredients())
                                synchronized (ingredientToModify.getLock()) {
                                    if (ingredientToModify.getName().equals(ingredient.getName()))
                                        ingredientToModify.getStock().setCurrentStock(ingredientToModify.getStock().getCurrentStock().doubleValue() + database.getIngredientsForDishes().get(ingredient).doubleValue());
                                }
                                //System.out.println(ingredient.getName() + " " + database.getIngredientsForDishes().get(ingredient).doubleValue());
                        }
                        status = DEFAULT_STATUS;
                        if(ingredient != null)
                            database.getIngredientsForDishes().put(ingredient, 0.00);
                    }
                } finally {
                    ingredient.getLock().unlock();
                }
            }
        }
    }

    public void checkOrders() {

        for(Order order : database.getOrders()) {
            if(order.getLock().tryLock()) {
                try {
                    if(order.getStatus().equals(Order.PREPARING_STATUS)) {
                        List<Dish> holdedDishes = new ArrayList<>();
                        int length = order.getOrder().keySet().size();
                        for(Dish dish : order.getOrder().keySet()) {
                            if(dish.getLock().tryLock()) {
                                holdedDishes.add(dish);
                            }
                        }
                        if(holdedDishes.size() == length) {
                            int dishContor = 0;
                            for(Dish dish : order.getOrder().keySet()) {
                                if((double) dish.getStock().getCurrentStock() >= (double) order.getOrder().get(dish))
                                    dishContor++;
                            }
                            if(dishContor == length) {
                                order.setStatus(Order.COMPLETE_STATUS);
                                for (Dish dish : order.getOrder().keySet()) {
                                    Number currentStock = dish.getStock().getCurrentStock();
                                    dish.getStock().setCurrentStock((double) currentStock - (double) order.getOrder().get(dish));
                                }
                            }
                        }
                        for(Dish dish : holdedDishes) {
                            dish.getLock().unlock();
                        }
                    }
                } finally {
                    order.getLock().unlock();
                }
            }
        }
    }

    public void deliverOrder() {

        for (Order order : database.getOrders()) {
            if (order.getLock().tryLock()) {
                try {
                    if (order.getStatus().equals(Order.COMPLETE_STATUS)) {
                        order.setStatus(Order.DELIVERY_STATUS);
                        this.status = DELIVERY_STATUS;
                        try {
                            Thread.sleep((long) ((double) 10000 * 2));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        order.setStatus(Order.DELIVERED_STATUS);
                        this.status = DEFAULT_STATUS;
                    }
                } finally {
                    order.getLock().unlock();
                }
            }
        }
    }
}
