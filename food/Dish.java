package food;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import common.Model;

public class Dish extends Model implements Serializable {

	private String name;
	private String description;
	private Number price;
	private Map<Ingredient, Number> recipe;
	private Stock stock;
	private Lock lock = new ReentrantLock(true);
	
	public Dish(String name, String description, Number price, HashMap<Ingredient, Number> recipe, Stock stock) {
		this.name = name;
		this.description = description;
		this.price = price;
		this.recipe = recipe;
		this.setStock(stock);
	}
	
	@Override
	public String getName() {
		return this.name;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.notifyUpdate("desciption", this.description, description);
		this.description = description;
	}

	public Number getPrice() {
		return this.price;
	}

	public void setPrice(double price) {
		this.notifyUpdate("price", this.price, price);
		this.price = price;
	}

	public Map<Ingredient, Number> getRecipe() {
		return this.recipe;
	}

	public void setRecipe(Map<Ingredient, Number> recipe) {
		this.notifyUpdate("recipe", this.recipe, recipe);
		this.recipe = (HashMap<Ingredient, Number>) recipe;
		for(Ingredient ingredient : recipe.keySet()) {
		    System.out.println(ingredient.getName() + " " + recipe.get(ingredient));
        }
	}

	public Stock getStock() {

	    return stock;
	}

	public void setStock(Stock stock) {
		this.notifyUpdate("stock", this.stock, stock);
		this.stock = stock;
	}

    public Lock getLock() {
        return lock;
    }
}
