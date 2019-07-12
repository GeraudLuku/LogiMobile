package com.geraud.android.gps1.Models;

public class Subscription {
    private String branchId,companyId;

    public Subscription(){}

    public Subscription(String branchId, String companyId) {
        this.branchId = branchId;
        this.companyId = companyId;
    }

    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }
}
