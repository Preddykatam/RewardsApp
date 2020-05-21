package com.charter.rewards.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.catalina.core.ApplicationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.charter.rewards.model.CustomerRewards;
import com.charter.rewards.model.MonthRewards;
import com.charter.rewards.model.TransactionVO;

/*
 * NOTE:
 * Assumption -1: 'Total rewards of a customer' is assumed to be total reward points of each month for customer,
 * but not reward points of total amount of all the months.
 * 
 */

@RestController
@RequestMapping("rewards")
public class RewardsController {

	private static final Logger log = LoggerFactory.getLogger(RestController.class);

	private static Map<String, List<TransactionVO>> tranxnMap = new HashMap<String, List<TransactionVO>>();

	@Value("classpath:static/transactions.txt")
	private static String filepath;

	static {
		loadTransactionsData(filepath);
	}

	@GetMapping(value = "/test")
	public String healthCheck() {
		return "Health check success.";
	}

	private static void loadTransactionsData(String fileName) {
		try {
			if (filepath == null) {
				filepath = ClassLoader.getSystemResource("static/transactions.txt").getFile();
				log.info("Load resource filepath:" + filepath);
			}

			File file = new File(filepath);
			FileInputStream fis = new FileInputStream(file);
			InputStreamReader isr = new InputStreamReader(fis);
			BufferedReader br = new BufferedReader(isr);

			String line;
			TransactionVO transactionVO = null;
			while ((line = br.readLine()) != null) {
				String[] txn = line.split(",");
				if (txn == null || txn.length < 3)
					continue;
				String cust_id = txn[0];
				transactionVO = new TransactionVO(txn[0], Double.valueOf(txn[1]), txn[2]);
				if (tranxnMap.containsKey(cust_id)) {
					tranxnMap.get(cust_id).add(transactionVO);
				} else {
					List<TransactionVO> txnList = new ArrayList<TransactionVO>();
					txnList.add(transactionVO);
					tranxnMap.put(cust_id, txnList);
				}
			}
			log.info("Loaded transactions successfully.");
			br.close();
		} catch (Exception ex) {
			log.error("Unable to load transaction data." + ex.getMessage());
			ex.printStackTrace();
		}
	}

	@GetMapping(value = "/{cust_id}")
	public ResponseEntity<List<CustomerRewards>> getCustomerRewards(@PathVariable("cust_id") String customerId) {
		log.debug("Customer id:" + customerId);

		List<CustomerRewards> rewardsResp = new ArrayList<CustomerRewards>();
		try {
			if (customerId == null || customerId.isEmpty()) {
				customerId = "all";
				log.info("Customer id defaulting to 'all'.");
			}

			if (tranxnMap == null && tranxnMap.isEmpty())
				loadTransactionsData(filepath);

			log.debug("loaded Mao:" + tranxnMap.entrySet());

			if (tranxnMap != null && !tranxnMap.isEmpty()) {
				CustomerRewards custRewards = null;
				if ("ALL".equalsIgnoreCase(customerId)) {
					for (Map.Entry<String, List<TransactionVO>> entry : tranxnMap.entrySet()) {

						custRewards = calculateRewards(entry.getValue());
						rewardsResp.add(custRewards);
					}
				} else {
					if (!tranxnMap.containsKey(customerId)) {
						List<MonthRewards> allMonthsRewards = new ArrayList<>();
						rewardsResp.add(new CustomerRewards("Customer Not Found..!", allMonthsRewards, 0));
						return ResponseEntity.badRequest().body(rewardsResp);
					}
					rewardsResp.add(calculateRewards(tranxnMap.get(customerId)));
				}
			}
		} catch (Exception ex) {
			log.error("Unable to calculate rewards." + ex.getMessage());
			ex.printStackTrace();
		}
		return ResponseEntity.ok().body(rewardsResp);
	}

	private CustomerRewards calculateRewards(List<TransactionVO> transactions) {
		log.info("Calculate rewards:" + transactions.size());

		List<MonthRewards> allMonthsRewards = new ArrayList<>();
		// Approach - 1: SORT the list per date and iterate through the list.

		// Approach-2: Create a map of <date,List<trxnAmount> and then create month
		// rewards.
		Map<String, List<Double>> monthlyTranxns = new HashMap<String, List<Double>>();

		String date = null;
		String yearmonth = null;
		for (TransactionVO transVO : transactions) {
			date = transVO.getTxnDate();
			yearmonth = date.substring(0, date.lastIndexOf("-"));
			if (monthlyTranxns.containsKey(yearmonth)) {

				monthlyTranxns.get(yearmonth).add(transVO.getTxnAmount());
			} else {
				List<Double> txnList = new ArrayList<>();
				txnList.add(transVO.getTxnAmount());
				monthlyTranxns.put(transVO.getTxnDate(), txnList);
			}
		}

		log.debug("MonthlyTxnsMap:" + monthlyTranxns);

		double monthlytotalAmount = 0.0;
		long monthlyRewardsPoints = 0;
		long totalCustRewards = 0;
		for (Map.Entry<String, List<Double>> mtrans : monthlyTranxns.entrySet()) {

			monthlytotalAmount = getTotalAmountForMonth(mtrans.getValue());
			monthlyRewardsPoints = calcRewardPoints(monthlytotalAmount);
			log.debug("Month-year:" + mtrans.getKey() + ". Rewards for this month:" + monthlyRewardsPoints);
			MonthRewards monthRewards = new MonthRewards(mtrans.getKey(), monthlyRewardsPoints);
			allMonthsRewards.add(monthRewards);
			totalCustRewards = totalCustRewards + monthlyRewardsPoints;
		}
		return new CustomerRewards(transactions.get(0).getCustomerID(), allMonthsRewards, totalCustRewards);
	}

	private double getTotalAmountForMonth(List<Double> value) {

		double sum = 0.0;
		for (Double amt : value) {
			sum = sum + amt;
		}
		log.debug("TotalAmount:" + sum);
		return sum;
	}

	private long calcRewardPoints(double amount) {

		long sum = 0;
		long amt = (new Double(amount)).longValue();
		if (amt > 100) {
			long above100 = amt - 100;
			sum = sum + above100 * 2;
			amt = 100;
		}
		if (amt > 50) {
			long above50 = amt - 50;
			sum = sum + above50 * 1;
		}
		log.debug("Amount:" + amount + ",Rewards:" + sum);
		return sum;
	}
}
