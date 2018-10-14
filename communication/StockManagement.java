package communication;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import common.Model;
import food.Dish;
import food.Ingredient;
import food.Supplier;

public class StockManagement extends Model implements Serializable, Runnable {

    private List<Supplier> suppliers = new CopyOnWriteArrayList<>();
    private List<Ingredient> ingredients = new CopyOnWriteArrayList<>();
    private List<Dish> dishes = new CopyOnWriteArrayList<>();
    private Map<Ingredient, Number> ingredientsForDishes = new ConcurrentHashMap<>();
    private boolean shutdown = false;

    @Override
    public String getName() {
        return this.name;
    }

    public boolean addSupplier(Supplier supplier){
        this.notifyUpdate();
        return suppliers.add(supplier);
    }

    public List<Supplier> getSuppliers() {
        return this.suppliers;
    }

    public boolean addIngredient(Ingredient ingredient) {
        this.notifyUpdate();
        return ingredients.add(ingredient);
    }

    public List<Ingredient> getIngredients() {
        return this.ingredients;
    }

    public boolean addDish(Dish dish) {
        this.notifyUpdate();
        return this.dishes.add(dish);
    }

    public Map<Ingredient, Number> getIngredientsForDishes() {
        return ingredientsForDishes;
    }

    public void refreshIngredientForDishes() {
        Map<Ingredient, Number> ingredientsForDishes = new ConcurrentHashMap<>();
        for(Dish dish : dishes) {
            if (dish.getLock().tryLock()) {
                if (dish.getStock().checkStock() == false) {
                    for (Ingredient ingredient : dish.getRecipe().keySet()) {
                        if (ingredientsForDishes.containsKey(ingredient)) {
                            Number current = ingredientsForDishes.get(ingredient);
                            ingredientsForDishes.put(ingredient, current.doubleValue() + dish.getRecipe().get(ingredient).doubleValue() * (dish.getStock().getRestockingAmount().doubleValue() + dish.getStock().getRestockingThreshold().doubleValue() - dish.getStock().getCurrentStock().doubleValue()));
                        } else {
                            ingredientsForDishes.put(ingredient, dish.getRecipe().get(ingredient).doubleValue() * (dish.getStock().getRestockingAmount().doubleValue() + dish.getStock().getRestockingThreshold().doubleValue() - dish.getStock().getCurrentStock().doubleValue()));
                        }
                    }
                }
                dish.getLock().unlock();
            }
        }

        for (Ingredient ingredient : ingredientsForDishes.keySet()) {
            Number current = ingredientsForDishes.get(ingredient);
            ingredientsForDishes.put(ingredient, current.doubleValue() - ingredient.getStock().getCurrentStock().doubleValue());
        }
        this.ingredientsForDishes = ingredientsForDishes;

    }

    public List<Dish> getDishes() {
        return this.dishes;
    }

    @Override
    public void run() {
        shutdown = false;
        while (!shutdown) {
            this.refreshIngredientForDishes();
        }
    }

    public void shutdown() {
        shutdown = true;
    }
}
