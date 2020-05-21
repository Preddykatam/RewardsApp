package com.charter.rewards.model;

public class MonthRewards {
	
	private String month;
	
	private long totalRewards;

	public MonthRewards(String month, long totalRewards) {
		super();
		this.month = month;
		this.totalRewards = totalRewards;
	}

	public String getMonth() {
		return month;
	}

	public void setMonth(String month) {
		this.month = month;
	}

	public long getTotalRewards() {
		return totalRewards;
	}

	public void setTotalRewards(long totalRewards) {
		this.totalRewards = totalRewards;
	}
}
