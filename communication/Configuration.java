package communication;

import food.Dish;
import food.Ingredient;
import food.Stock;
import food.Supplier;
import kitchen.Drone;
import kitchen.Staff;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class Configuration {

    private Database database;
    private StockManagement stockManagement;
    private String filename;

    public Configuration(Database database, StockManagement stockManagement, String filename) {
        this.database = database;
        this.stockManagement = stockManagement;
        this.filename = filename;
    }

    public void loadConfiguration() throws FileNotFoundException {
        BufferedReader reader;
        reader = new BufferedReader(new FileReader(filename));
        String line = null;
        try {
            line = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (line != null) {
            String[] parts = new String[100];
            parts = line.split(":");
            switch (parts[0]) {
                case "SUPPLIER":
                    this.addSupplier(parts);
                    break;
                case "INGREDIENT":
                    this.addIngredient(parts);
                    break;
                case "DISH":
                    this.addDish(parts);
                    break;
                case "POSTCODE":
                    this.addPostcode(parts);
                    break;
                case "USER":
                    this.addUser(parts);
                    break;
                case "STAFF":
                    this.addStaff(parts);
                    break;
                case "ORDER":
                    this.addOrder(parts);
                    break;
                case "STOCK":
                    this.addStock(parts);
                    break;
                case "DRONE":
                    this.addDrone(parts);
                    break;
                default:
                    break;
            }
            // read next line
            try {
                line = reader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addOrder(String[] parts){
        User userToAdd = null;
        for(User user : database.getUsers()) {
            if (user.getName().equals(parts[1])) {
                userToAdd = user;
                break;
            }
        }
        Order order = new Order(userToAdd);
        String[] content = parts[2].split(",");

        for(int i = 0; i < content.length; i++) {
            String[] dishAndQuantity = content[i].split(" \\* ");
            for (Dish dish : stockManagement.getDishes()) {
                if (dish.getName().equals(dishAndQuantity[1])) {
                    order.addEntry(dish, Double.parseDouble(dishAndQuantity[0]));
                    break;
                }
            }
        }
        database.addOrder(order);
    }

    public void addSupplier(String[] parts){

        Supplier supplier = new Supplier(parts[1], Double.parseDouble(parts[2]));
        stockManagement.addSupplier(supplier);
    }

    public void addIngredient(String[] parts) {

        Supplier ingredientSupplier = null;
        for(Supplier supplier : stockManagement.getSuppliers())
            if(supplier.getName().equals(parts[3])) {
                ingredientSupplier = supplier;
                break;
            }
        Stock ingredientStock = new Stock(Double.parseDouble(parts[4]), Double.parseDouble(parts[5]));
        Ingredient ingredient = new Ingredient(parts[1], parts[2], ingredientSupplier, ingredientStock);

        stockManagement.addIngredient(ingredient);

    }

    public void addDish(String[] parts) {
        HashMap<Ingredient, Number> recipeToAdd = new HashMap<>();
        String[] recipe = parts[6].split(",");
        for(int i = 0; i < recipe.length; i++) {
            String[] ingredientToAdd = recipe[i].split(" \\* ");
            for (Ingredient ingredient : stockManagement.getIngredients()) {
                if (ingredient.getName().equals(ingredientToAdd[1])) {
                    recipeToAdd.put(ingredient, Double.parseDouble(ingredientToAdd[0]));
                    break;
                }
            }
        }
        Stock dishStock = new Stock(Double.parseDouble(parts[4]), Double.parseDouble(parts[5]));
        Dish dish = new Dish(parts[1], parts[2], Double.parseDouble(parts[3]), recipeToAdd, dishStock);

        stockManagement.addDish(dish);

    }

    public void addUser(String[] parts) {
        Postcode postcode = null;
        //System.out.println(parts.length);
        for(Postcode postcodeIncrement : database.getPostcodes()) {
            if (postcodeIncrement.getName().equals(parts[4])) {
                postcode = postcodeIncrement;
                break;
            }
        }
        User user = new User(parts[1], parts[2], parts[3], postcode);

        database.addUser(user);
    }

    public void addPostcode(String[] parts) {
        Postcode postcode = new Postcode(parts[1], Double.parseDouble(parts[2]));


        database.addPostcode(postcode);
    }

    public void addStaff(String[] parts) {
        Staff staff = new Staff(parts[1], stockManagement, database);

        database.addStaff(staff);
    }

    public void addDrone(String[] parts) {
        Drone drone = new Drone(Double.parseDouble(parts[1]), stockManagement, database);

        database.addDrone(drone);
    }

    public void addStock(String[] parts) {
        boolean added = false;
        if(added == false) {
            for (Dish dish : stockManagement.getDishes()) {
                if (dish.getName().equals(parts[1])) {
                    added = true;
                    dish.getStock().setCurrentStock(Double.parseDouble(parts[2]));
                    break;
                }
            }
        }
        if(added == false) {
            for (Ingredient ingredient : stockManagement.getIngredients()) {
                if (ingredient.getName().equals(parts[1])) {
                    //added = true;
                    ingredient.getStock().setCurrentStock(Double.parseDouble(parts[2]));
                    break;
                }
            }
        }
    }

}
