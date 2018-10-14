package food;

import common.Model;

import java.io.Serializable;
import java.util.concurrent.locks.*;

public class Ingredient extends Model implements Serializable {

	private String name;
    private Lock lock = new ReentrantLock(true);
	private String unit;
	private Supplier supplier;
	private Stock stock;
	
	public Ingredient(String name, String unit, Supplier supplier, Stock stock) {
		this.name = name;
		this.setUnit(unit);
		this.setSupplier(supplier);
		this.setStock(stock);
	}
	
	@Override
	public String getName() {
		return this.name;
	}

	public String getUnit() {
		return this.unit;
	}

	public void setUnit(String unit) {
		this.notifyUpdate("unit", this.unit, unit);
		this.unit = unit;
	}

	public Supplier getSupplier() {
		return this.supplier;
	}

	public void setSupplier(Supplier supplier) {
		this.notifyUpdate("supplier", this.supplier, supplier);
		this.supplier = supplier;
	}

	public Stock getStock() {
		return stock;
	}

	public void setStock(Stock stock) {
		this.notifyUpdate();
		this.stock = stock;
	}

    public Lock getLock() {
        return lock;
    }
}
