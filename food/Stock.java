package food;

import common.Model;

import java.io.Serializable;

public class Stock extends Model implements Serializable {

	private String name;
	private Number currentStock;
	private Number restockingThreshold;
	private Number restockingAmount;

	public Stock(Number currentStock, Number restockingThreshold, Number restockingAmount) {
		this.setCurrentStock(currentStock);
		this.setRestockingThreshold(restockingThreshold);
		this.setRestockingAmount(restockingAmount);
	}

    public Stock(Number restockingThreshold, Number restockingAmount) {
        this.setCurrentStock(0.00);
        this.setRestockingThreshold(restockingThreshold);
        this.setRestockingAmount(restockingAmount);
    }
	
	@Override
	public String getName() {
		return String.valueOf(currentStock);
	}

	public Number getCurrentStock() {
		return this.currentStock;
	}

	public void setCurrentStock(Number currentStock) {
		this.notifyUpdate();
		double currentStockTemp = currentStock.doubleValue();
		//System.out.println(currentStockTemp);
		this.currentStock = currentStockTemp;
	}

	public Number getRestockingThreshold() {
		return this.restockingThreshold;
	}

	public void setRestockingThreshold(Number restockingThreshold) {
		this.notifyUpdate("restockingThreshold", this.restockingThreshold, restockingThreshold);
        double restockingThresholdTemp = restockingThreshold.doubleValue();
		//System.out.println(restockingThresholdTemp);
		this.restockingThreshold = restockingThresholdTemp;
	}

	public Number getRestockingAmount() {
		return restockingAmount;
	}

	public void setRestockingAmount(Number restockingAmount) {
		this.notifyUpdate();
        double restockingAmountTemp = restockingAmount.doubleValue();
        //System.out.println(restockingAmountTemp);
		this.restockingAmount = restockingAmountTemp;
	}

	public boolean checkStock() {
		if((double) this.currentStock > (double) this.restockingThreshold) {
			return true;
		}
		return false;
	}
}
