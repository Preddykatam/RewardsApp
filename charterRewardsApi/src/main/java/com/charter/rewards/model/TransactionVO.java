package com.charter.rewards.model;

public class TransactionVO {
	
	public String customerID;
	
	public double txnAmount;
	
	public String txnDate;

	public TransactionVO(String customerID, double txnAmount, String txnDate) {
		//super();
		this.customerID = customerID;
		this.txnAmount = txnAmount;
		this.txnDate = txnDate;
	}

	public String getCustomerID() {
		return customerID;
	}

	public void setCustomerID(String customerID) {
		this.customerID = customerID;
	}

	public double getTxnAmount() {
		return txnAmount;
	}

	public void setTxnAmount(double txnAmount) {
		this.txnAmount = txnAmount;
	}

	public String getTxnDate() {
		return txnDate;
	}

	public void setTxnDate(String txnDate) {
		this.txnDate = txnDate;
	}
}
