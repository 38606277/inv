package root.report.budget.bean;

import java.math.BigDecimal;

public class BudgetAccount {
	private String company_id;
	private String company_name;
	private String budget_account_name;
	private String budget_account_code;
	private BigDecimal transmit_budget_amount;
	private BigDecimal approved_budget_amount;
	private BigDecimal account_amount;
	private BigDecimal claim_amount;
	
	public String getCompany_id() {
		return company_id;
	}
	public void setCompany_id(String company_id) {
		this.company_id = company_id;
	}
	public String getCompany_name() {
		return company_name;
	}
	public void setCompany_name(String company_name) {
		this.company_name = company_name;
	}
	public String getBudget_account_name() {
		return budget_account_name;
	}
	public void setBudget_account_name(String budget_account_name) {
		this.budget_account_name = budget_account_name;
	}
	public String getBudget_account_code() {
		return budget_account_code;
	}
	public void setBudget_account_code(String budget_account_code) {
		this.budget_account_code = budget_account_code;
	}
	public BigDecimal getTransmit_budget_amount() {
		return transmit_budget_amount;
	}
	public void setTransmit_budget_amount(BigDecimal transmit_budget_amount) {
		this.transmit_budget_amount = transmit_budget_amount;
	}
	public BigDecimal getApproved_budget_amount() {
		return approved_budget_amount;
	}
	public void setApproved_budget_amount(BigDecimal approved_budget_amount) {
		this.approved_budget_amount = approved_budget_amount;
	}
	public BigDecimal getAccount_amount() {
		return account_amount;
	}
	public void setAccount_amount(BigDecimal account_amount) {
		this.account_amount = account_amount;
	}
	public BigDecimal getClaim_amount() {
		return claim_amount;
	}
	public void setClaim_amount(BigDecimal claim_amount) {
		this.claim_amount = claim_amount;
	}
	
}
