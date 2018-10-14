package communication;

import common.UpdateEvent;
import common.UpdateListener;
import food.Dish;
import food.Ingredient;
import food.Stock;
import food.Supplier;
import kitchen.Drone;
import kitchen.Staff;
import server.ServerInterface;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server implements ServerInterface {

    private Database database = new Database();
    private StockManagement stockManagement = new StockManagement();
    private Configuration configuration;
    private Comms comms = new Comms(this);
    private DataPersistence dataPersistence = new DataPersistence(this);

    public Server() {
        comms.start();

        dataPersistence.loadState();

        for(Drone drone : database.getDrones()) {
            drone.setDatabase(database);
            drone.setStockManagement(stockManagement);
        }

        for(Staff staff : database.getStaff()) {
            staff.setDatabase(database);
            staff.setStockManagement(stockManagement);
        }

        for(Order order : database.getOrders()) {
            Map<Dish, Number> recipe = new ConcurrentHashMap<>();
            for(Dish dish : order.getOrder().keySet()) {
                for(Dish dishToCompare : stockManagement.getDishes()) {
                    if(dishToCompare.getName().equals(dish.getName())){
                        recipe.put(dishToCompare, order.getOrder().get(dish));
                    }
                }
            }
            order.setOrder(recipe);
        }

        database.startDronesStaff();

        Thread thread2 = new Thread(dataPersistence);
        thread2.start();

        Thread thread3 = new Thread(stockManagement);
        thread3.start();
        //stockManagement.refreshIngredientForDishes();
        //database.refreshIngredientForDishes();
    }

    public Database getDatabase() {
        return database;
    }

    public StockManagement getStockManagement() {
        return stockManagement;
    }

    public void setDatabase(Database database) {
        this.database = database;
    }

    public void setStockManagement(StockManagement stockManagement) {
        this.stockManagement = stockManagement;
    }

    public User addUser(User user) {
        database.addUser(user);
        return user;
    }

    public User loginUser(LogIn login) {
        for (User user : database.getUsers()) {
            if (login.getUsername().equals(user.getName()) && login.getPassword().equals(user.getPassword()))
                return user;
        }
        return null;
    }

    public Dish getDish(Dish dish) {
        return dish;
    }

    @Override
    public void loadConfiguration(String filename) throws FileNotFoundException {
        for(Staff staff : database.getStaff()) {
            staff.shutdown();
        }

        for(Drone drone : database.getDrones()) {
            drone.shutdown();
        }

        stockManagement.shutdown();

        database = new Database();
        stockManagement = new StockManagement();

        configuration = new Configuration(database, stockManagement, filename);
        configuration.loadConfiguration();

        dataPersistence = new DataPersistence(this);
        dataPersistence.saveState();

        Thread thread1 = new Thread(dataPersistence);
        thread1.start();

        Thread thread3 = new Thread(stockManagement);
        thread3.start();

        database.startDronesStaff();

        database.refreshIngredientForDishes();
    }

    @Override
    public void setRestockingIngredientsEnabled(boolean enabled) {
        database.setRestockingIngredients(enabled);
    }

    @Override
    public void setRestockingDishesEnabled(boolean enabled) {
        database.setRestockingDishes(enabled);
    }

    @Override
    public void setStock(Dish dish, Number stock) {
        double stockTemp = stock.doubleValue();
        dish.getStock().setCurrentStock(stockTemp);
    }

    @Override
    public void setStock(Ingredient ingredient, Number stock) {
        double stockTemp = stock.doubleValue();
        ingredient.getStock().setCurrentStock(stockTemp);
    }

    @Override
    public List<Dish> getDishes() {
        return stockManagement.getDishes();
    }

    @Override
    public Dish addDish(String name, String description, Number price, Number restockThreshold, Number restockAmount) {
        double restockThresholdTemp = restockThreshold.doubleValue();
        double restockAmountTemp = restockAmount.doubleValue();
        double priceTemp = price.doubleValue();
        Dish dish = new Dish(name, description, priceTemp, new HashMap<>(), new Stock(restockThresholdTemp, restockAmountTemp));
        stockManagement.addDish(dish);
        stockManagement.refreshIngredientForDishes();
        return dish;
    }

    @Override
    public void removeDish(Dish dish) throws UnableToDeleteException {
        boolean found = false;
        for (Order order : database.getOrders()) {
            if (order.getOrder().containsKey(dish)) {
                found = true;
                break;
            }
        }
        if (found == false) {
            synchronized(dish.getLock()) {
                stockManagement.getDishes().remove(dish);
            }
        } else throw new UnableToDeleteException("Dish is used by another process.");

    }

    @Override
    public void addIngredientToDish(Dish dish, Ingredient ingredient, Number quantity) {
        double quantityTemp = quantity.doubleValue();
        synchronized (dish.getLock()) {
            dish.getRecipe().put(ingredient, quantityTemp);
        }

    }

    @Override
    public void removeIngredientFromDish(Dish dish, Ingredient ingredient) {
        synchronized (dish.getLock()) {
            dish.getRecipe().remove(ingredient);
        }

    }

    @Override
    public void setRecipe(Dish dish, Map<Ingredient, Number> recipe) {
        synchronized (dish.getLock()) {
            dish.setRecipe(recipe);
        }
    }

    @Override
    public void setRestockLevels(Dish dish, Number restockThreshold, Number restockAmount) {
        double restockThresholdTemp = restockThreshold.doubleValue();
        double restockAmountTemp = restockAmount.doubleValue();
        System.out.println("nn");
        dish.getStock().setRestockingAmount(restockAmountTemp);
        dish.getStock().setRestockingThreshold(restockThresholdTemp);

        synchronized (stockManagement) {
            stockManagement.refreshIngredientForDishes();
        }
    }

    @Override
    public Number getRestockThreshold(Dish dish) {
        return dish.getStock().getRestockingThreshold();
    }

    @Override
    public Number getRestockAmount(Dish dish) {
        return dish.getStock().getRestockingAmount();
    }

    @Override
    public Map<Ingredient, Number> getRecipe(Dish dish) {
        return dish.getRecipe();
    }

    @Override
    public Map<Dish, Number> getDishStockLevels() {
        Map<Dish, Number> dishStockLevels = new HashMap<>();
        for (Dish dish : stockManagement.getDishes()) {
            dishStockLevels.put(dish, dish.getStock().getCurrentStock());
        }
        return dishStockLevels;
    }

    @Override
    public List<Ingredient> getIngredients() {
        return stockManagement.getIngredients();
    }

    @Override
    public Ingredient addIngredient(String name, String unit, Supplier supplier, Number restockThreshold, Number restockAmount) {
        double restockThresholdTemp = restockThreshold.doubleValue();
        double restockAmountTemp = restockAmount.doubleValue();
        Ingredient ingredient = new Ingredient(name, unit, supplier, new Stock(restockThresholdTemp, restockAmountTemp));
        stockManagement.addIngredient(ingredient);
        return ingredient;
    }

    @Override
    public void removeIngredient(Ingredient ingredient) throws UnableToDeleteException {
        boolean found = false;
        for (Dish dish : stockManagement.getDishes()) {
            for (Ingredient ingredientTemp : dish.getRecipe().keySet()) {
                if (ingredientTemp == ingredient) {
                    found = true;
                    break;
                }
            }

        }
        if (found == false)
            synchronized(ingredient.getLock()) {
                stockManagement.getIngredients().remove(ingredient);
            }
        else throw new UnableToDeleteException("Remove the dishes or the ingredient from the dishes.");
    }

    @Override
    public void setRestockLevels(Ingredient ingredient, Number restockThreshold, Number restockAmount) {
        double restockThresholdTemp = restockThreshold.doubleValue();
        double restockAmountTemp = restockAmount.doubleValue();
        ingredient.getStock().setRestockingAmount(restockAmountTemp);
        ingredient.getStock().setRestockingThreshold(restockThresholdTemp);
    }

    @Override
    public Number getRestockThreshold(Ingredient ingredient) {
        return ingredient.getStock().getRestockingThreshold();
    }

    @Override
    public Number getRestockAmount(Ingredient ingredient) {
        return ingredient.getStock().getRestockingAmount();
    }

    @Override
    public Map<Ingredient, Number> getIngredientStockLevels() {
        Map<Ingredient, Number> ingredientStockLevels = new HashMap<>();
        for (Ingredient ingredient : stockManagement.getIngredients()) {
            ingredientStockLevels.put(ingredient, ingredient.getStock().getCurrentStock());
        }
        return ingredientStockLevels;
    }

    @Override
    public List<Supplier> getSuppliers() {
        return stockManagement.getSuppliers();
    }

    @Override
    public Supplier addSupplier(String name, Number distance) {
        double distanceTemp = distance.doubleValue();
        Supplier supplier = new Supplier(name, distanceTemp);
        stockManagement.addSupplier(supplier);
        return supplier;
    }

    @Override
    public void removeSupplier(Supplier supplier) throws UnableToDeleteException {
        for (Ingredient ingredient : stockManagement.getIngredients()) {
            if (ingredient.getSupplier() == supplier)
                throw new UnableToDeleteException("You need to delete dependencies first");
        }
        stockManagement.getSuppliers().remove(supplier);
    }

    @Override
    public Number getSupplierDistance(Supplier supplier) {
        return supplier.getDistance();
    }

    @Override
    public List<Drone> getDrones() {
        return database.getDrones();
    }

    @Override
    public Drone addDrone(Number speed) {
        double speedTemp = speed.doubleValue();
        Drone drone = new Drone(speedTemp, stockManagement, database);
        database.addDrone(drone);
        return drone;
    }

    @Override
    public void removeDrone(Drone drone) throws UnableToDeleteException {
        if (drone.getStatus().equals(Drone.DEFAULT_STATUS))
            database.getDrones().remove(drone);
        else throw new UnableToDeleteException("Drone is doing something");
    }

    @Override
    public Number getDroneSpeed(Drone drone) {
        return drone.getSpeed();
    }

    @Override
    public String getDroneStatus(Drone drone) {
        return drone.getStatus();
    }

    @Override
    public List<Staff> getStaff() {
        return database.getStaff();
    }

    @Override
    public Staff addStaff(String name) {
        Staff staff = new Staff(name, stockManagement, database);
        database.addStaff(staff);
        return staff;
    }

    @Override
    public void removeStaff(Staff staff) throws UnableToDeleteException {
        if (staff.getStatus().equals(Drone.DEFAULT_STATUS))
            database.getStaff().remove(staff);
        else throw new UnableToDeleteException("");
    }

    @Override
    public String getStaffStatus(Staff staff) {
        return staff.getStatus();
    }

    @Override
    public List<Order> getOrders() {
        return database.getOrders();
    }

    @Override
    public void removeOrder(Order order) throws UnableToDeleteException {
        if (!order.getStatus().equals(Order.DELIVERY_STATUS))
            synchronized (database.getOrders()) {
                database.getOrders().remove(order);
            }
        else throw new UnableToDeleteException("Order is on its way to the customer.");
    }

    @Override
    public Number getOrderDistance(Order order) {
        return order.getUser().getPostcode().getDistance();
    }

    @Override
    public boolean isOrderComplete(Order order) {
        if (order.getStatus().equals(Order.DELIVERED_STATUS))
            return true;
        return false;
    }

    @Override
    public String getOrderStatus(Order order) {
        return order.getStatus();
    }

    @Override
    public Number getOrderCost(Order order) {
        return order.getCost();
    }

    @Override
    public List<Postcode> getPostcodes() {
        return database.getPostcodes();
    }

    @Override
    public void addPostcode(String code, Number distance) {
        double distanceTemp = distance.doubleValue();
        database.getPostcodes().add(new Postcode(code, distanceTemp));
    }

    @Override
    public void removePostcode(Postcode postcode) throws UnableToDeleteException {
        boolean found = false;
        for (User user : database.getUsers()) {
            if (user.getPostcode() == postcode)
                found = true;
        }
        if (found == false)
            database.getPostcodes().remove(postcode);
        else throw new UnableToDeleteException("Postcode is in use by a user");
    }

    @Override
    public List<User> getUsers() {
        return database.getUsers();
    }

    @Override
    public void removeUser(User user) throws UnableToDeleteException {
        boolean found = false;
        for (Order order : database.getOrders()) {
            if (order.getUser().getName().equals(user.getName()) && order.getStatus() != Order.DELIVERED_STATUS)
                found = true;
        }
        if (found == false) {
            for (Order order : database.getOrders()) {
                if (order.getUser() == user)
                    database.getOrders().remove(order);
            }
            database.getUsers().remove(user);
        } else throw new UnableToDeleteException("User cannot be deleted");
    }

    private List<UpdateListener> updateListeners = new ArrayList<>();

    @Override
    public void addUpdateListener(UpdateListener listener) {
        updateListeners.add(listener);
        for(Dish dish : stockManagement.getDishes())
            dish.addUpdateListener(listener);
        for(Ingredient ingredient : stockManagement.getIngredients()) {
            ingredient.addUpdateListener(listener);
        }

    }

    @Override
    public void notifyUpdate() {
        for(UpdateListener listener : updateListeners) {
            listener.updated(new UpdateEvent());
        }
    }

    public String getOrderDescription(Dish dish) {
        return dish.getDescription();
    }

    public List<Order> getOrdersUser(User user) {
        List<Order> orders = new ArrayList<>();
        if(user != null)
            for(Order order : database.getOrders())
                if (order.getUser().getName().equals(user.getName())) {
                    orders.add(order);
                }
        return orders;
    }

    public Map<Dish, Number> getBasket(User user) {
        Basket found = null;
        for (Basket basket : database.getBaskets())
            if (basket.getUser().getName().equals(user.getName())) {
                found = basket;
            }
        if (found == null)
            found = new Basket(user);
        return found.getBasket();
    }

    public void clearBasket(User user) {
        for (Basket basket : database.getBaskets())
            if (basket.getUser().getName().equals(user.getName())) {
                database.getBaskets().remove(basket);
                break;
            }
    }

    public Order checkoutBasket(User user) {
        for (Basket basket : database.getBaskets())
            if (basket.getUser().getName().equals(user.getName()) && basket.getBasket().size() != 0) {
                Map<Dish, Number> basketContent = new ConcurrentHashMap<>();
                for(Dish dish : basket.getBasket().keySet()) {
                    for (Dish dishToCompare : stockManagement.getDishes()) {
                        if (dishToCompare.getName().equals(dish.getName())) {
                            basketContent.put(dishToCompare, basket.getBasket().get(dish));
                        }
                    }
                }
                Order order = new Order(user, basketContent);
                basket.clearBasket();
                database.addOrder(order);
                database.refreshIngredientForDishes();
                return order;
            }
        return null;
    }

    public Number getBasketPrice(User user) {
        for (Basket basket : database.getBaskets())
            if (basket.getUser().getName().equals(user.getName())) {
                return basket.getTotalPrice();
            }
        return 0.00;
    }

    public void addInBasket(BasketEntry basketEntry) {
        boolean found = false;
        User user = basketEntry.getUser();
        for (Basket basket : database.getBaskets())
            if (basket.getUser().getName().equals(user.getName())) {
                found = true;
                basket.addToBasket(basketEntry.getDish(), basketEntry.getQuantity());
            }
        if (found == false) {
            Basket basket = new Basket(user);
            basket.addToBasket(basketEntry.getDish(), basketEntry.getQuantity());
            database.getBaskets().add(basket);
        }

    }
}
