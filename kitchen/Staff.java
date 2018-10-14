package kitchen;

import java.io.Serializable;
import java.util.*;

import common.Model;
import communication.Database;
import communication.StockManagement;
import food.Dish;
import food.Ingredient;


public class Staff extends Model implements Runnable, Serializable {

    public static final String DEFAULT_STATUS = "Idle";
    public static final String PREPARING_STATUS = "Preparing dish";

    private static final int LOWERBOUND_TIME = 20;
    private static final int UPPERBOUND_TIME = 60;

    private String name;
    private String status = DEFAULT_STATUS;
    private StockManagement stockManagement;
    private Database database;
    private boolean shutdown = false;

    public Staff(String name, StockManagement stockManagement, Database database) {
        this.name = name;
        this.stockManagement = stockManagement;
        this.database = database;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        notifyUpdate("status", this.status, status);
        this.status = status;
    }

    public void setDatabase(Database database) {
        this.database = database;
    }

    public void setStockManagement(StockManagement stockManagement) {
        this.stockManagement = stockManagement;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void run() {
        status = DEFAULT_STATUS;
        shutdown = false;
        while (!shutdown) {
            try {
                if (database.getRestockingDishes())
                    this.checkDishes();
                this.prepareDishesForOrders();
            } catch (NullPointerException e) {
                this.run();
            }
        }
    }

    public void shutdown() {
        shutdown = true;
    }

    private void checkDishes() {
        Iterator<Dish> it = this.stockManagement.getDishes().iterator();
        while (it.hasNext()) {
            Dish dish = it.next();
            if (dish.getLock().tryLock()) {
                try {
                    if (!dish.getStock().checkStock()) {
                        this.prepareDish(dish);
                    }
                } finally {
                    dish.getLock().unlock();
                }
            }
        }
    }

    private void prepareDishesForOrders() {
        Map<Dish, Number> dishesForOrder = database.getDishesForOrders();
        Set<Dish> dishes = dishesForOrder.keySet();

        Iterator<Dish> it = dishes.iterator();
        while (it.hasNext()) {
            Dish dish = it.next();
            Set<Ingredient> ingredients = dish.getRecipe().keySet();
            int length = ingredients.size();
            List<Ingredient> holdedIngredients = new ArrayList<>();

            if(length != 0) {
                Iterator<Ingredient> ing = ingredients.iterator();
                while (ing.hasNext()) {
                    Ingredient ingredient = ing.next();
                    if (ingredient.getLock().tryLock()) {
                        holdedIngredients.add(ingredient);
                    }
                }

                if (holdedIngredients.size() == length) {

                    int ingredientsContor = 0;

                    double needToBePrepared = dishesForOrder.get(dish).doubleValue();

                    ing = ingredients.iterator();
                    while (ing.hasNext()) {
                        Ingredient ingredient = ing.next();
                        if (ingredient.getStock().getCurrentStock().doubleValue() >= dish.getRecipe().get(ingredient).doubleValue() * needToBePrepared)
                            ingredientsContor++;
                    }

                    if (ingredientsContor == length) {
                        this.setStatus(PREPARING_STATUS);
                        Random random = new Random();
                        int waitingTime = random.nextInt(UPPERBOUND_TIME - LOWERBOUND_TIME) + LOWERBOUND_TIME;

                        ing = ingredients.iterator();
                        while (ing.hasNext()) {
                            Ingredient ingredient = ing.next();
                            ingredient.notifyUpdate();
                            ingredient.getStock().setCurrentStock(ingredient.getStock().getCurrentStock().doubleValue() - dish.getRecipe().get(ingredient).doubleValue() * needToBePrepared);
                        }

                        ing = holdedIngredients.iterator();

                        while (ing.hasNext()) {
                            Ingredient ingredient = ing.next();
                            ingredient.getLock().unlock();
                        }

                        holdedIngredients = new ArrayList<>();
                        try {
                            Thread.sleep(waitingTime * 1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        //good
                        if (dish != null)
                            dish.getStock().setCurrentStock(dish.getStock().getCurrentStock().doubleValue() + needToBePrepared);
                        this.setStatus(DEFAULT_STATUS);

                    }
                }

                if (holdedIngredients.size() != 0) {
                    ing = holdedIngredients.iterator();
                    while (ing.hasNext()) {
                        Ingredient ingredient = ing.next();
                        ingredient.getLock().unlock();
                    }
                }
            }

        }

    }


    private void prepareDish(Dish dishToPrepare) {
        Set<Ingredient> ingredients = dishToPrepare.getRecipe().keySet();
        int length = ingredients.size();
        List<Ingredient> holdedIngredients = new ArrayList<>();

        if(length != 0) {
            Iterator<Ingredient> it = ingredients.iterator();
            while (it.hasNext()) {
                Ingredient ingredient = it.next();
                if (ingredient.getLock().tryLock()) {
                    holdedIngredients.add(ingredient);
                }
            }

            if (holdedIngredients.size() == length) {

                int ingredientsContor = 0;

                double needToBePrepared = dishToPrepare.getStock().getRestockingAmount().doubleValue() + dishToPrepare.getStock().getRestockingThreshold().doubleValue() - dishToPrepare.getStock().getCurrentStock().doubleValue();

                it = ingredients.iterator();
                while (it.hasNext()) {
                    Ingredient ingredient = it.next();
                    if (ingredient.getStock().getCurrentStock().doubleValue() >= dishToPrepare.getRecipe().get(ingredient).doubleValue() * needToBePrepared)
                        ingredientsContor++;
                }

                if (ingredientsContor == length) {

                    this.setStatus(PREPARING_STATUS);
                    Random random = new Random();
                    int waitingTime = random.nextInt(UPPERBOUND_TIME - LOWERBOUND_TIME) + LOWERBOUND_TIME;

                    it = ingredients.iterator();
                    while (it.hasNext()) {
                        Ingredient ingredient = it.next();
                        ingredient.notifyUpdate();
                        ingredient.getStock().setCurrentStock(ingredient.getStock().getCurrentStock().doubleValue() - dishToPrepare.getRecipe().get(ingredient).doubleValue() * needToBePrepared);
                    }

                    it = holdedIngredients.iterator();
                    while (it.hasNext()) {
                        Ingredient ingredient = it.next();
                        ingredient.getLock().unlock();
                    }

                    holdedIngredients = new ArrayList<>();
                    try {
                        Thread.sleep(waitingTime * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (dishToPrepare != null)
                        dishToPrepare.getStock().setCurrentStock(dishToPrepare.getStock().getCurrentStock().doubleValue() + needToBePrepared);
                    this.setStatus(DEFAULT_STATUS);

                }

            }

            if (holdedIngredients.size() != 0) {
                it = holdedIngredients.iterator();
                while (it.hasNext()) {
                    Ingredient ingredient = it.next();
                    ingredient.getLock().unlock();
                }
            }
        }
    }

}
