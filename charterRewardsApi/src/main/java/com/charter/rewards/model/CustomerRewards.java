package com.charter.rewards.model;

import java.util.List;

public class CustomerRewards {

	private String custId;
	
	private List<MonthRewards> monthlyrewards;
	
	private long totalRewards;

	public CustomerRewards(String custId, List<MonthRewards> monthlyrewards, long totalRewards) {
		super();
		this.custId = custId;
		this.monthlyrewards = monthlyrewards;
		this.totalRewards = totalRewards;
	}

	public String getCustId() {
		return custId;
	}

	public void setCustId(String custId) {
		this.custId = custId;
	}

	public List<MonthRewards> getMonthlyrewards() {
		return monthlyrewards;
	}

	public void setMonthlyrewards(List<MonthRewards> monthlyrewards) {
		this.monthlyrewards = monthlyrewards;
	}

	public long getTotalRewards() {
		return totalRewards;
	}

	public void setTotalRewards(long totalRewards) {
		this.totalRewards = totalRewards;
	}
}
