package com.geraud.android.gps1.Models;

public class Branch {
    private String companyId
            ,workingTime
            ,postalAddress
            ,managingDirector
            ,name
            ,type;
    private ContactPerson contactPerson;

    public Branch(String companyId, String workingTime, String postalAddress, String managingDirector, String name, String type, ContactPerson contactPerson) {
        this.companyId = companyId;
        this.workingTime = workingTime;
        this.postalAddress = postalAddress;
        this.managingDirector = managingDirector;
        this.name = name;
        this.type = type;
        this.contactPerson = contactPerson;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public String getWorkingTime() {
        return workingTime;
    }

    public void setWorkingTime(String workingTime) {
        this.workingTime = workingTime;
    }

    public String getPostalAddress() {
        return postalAddress;
    }

    public void setPostalAddress(String postalAddress) {
        this.postalAddress = postalAddress;
    }

    public String getManagingDirector() {
        return managingDirector;
    }

    public void setManagingDirector(String managingDirector) {
        this.managingDirector = managingDirector;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ContactPerson getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(ContactPerson contactPerson) {
        this.contactPerson = contactPerson;
    }
}
