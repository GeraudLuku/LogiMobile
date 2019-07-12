package com.geraud.android.gps1.Models;

import java.io.Serializable;

public class PromotionMessage implements Serializable {
    private String title, body, branchId, companyId;
    private long timestamp;

    public PromotionMessage(){}

    public PromotionMessage(String title, String body, String branchId, long timestamp, String companyId) {
        this.title = title;
        this.body = body;
        this.branchId = branchId;
        this.companyId = companyId;
        this.timestamp = timestamp;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }
}
