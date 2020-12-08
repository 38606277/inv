
package com.cmcc.mss.oa_pageinquiryemployeeinfosrv;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OA_PageInquiryEmployeeInfoSrvOutputItem", propOrder = {
    "employeeNo",
    "loginName",
    "employeeName",
    "employeeLevel",
    "mobile",
    "mail",
    "employeeOrder",
    "employeeStatus",
    "areaName",
    "userNum",
    "lastUpdateTime",
    "reference1",
    "reference2",
    "reference3",
    "reference4",
    "reference5"
})
public class OAPageInquiryEmployeeInfoSrvOutputItem {

    @XmlElement(name = "Employee_No", required = true, nillable = true)
    protected String employeeNo;
    @XmlElement(name = "Login_Name", required = true, nillable = true)
    protected String loginName;
    @XmlElement(name = "Employee_Name", required = true, nillable = true)
    protected String employeeName;
    @XmlElement(name = "Employee_Level", required = true, nillable = true)
    protected String employeeLevel;
    @XmlElement(name = "Mobile", required = true, nillable = true)
    protected String mobile;
    @XmlElement(name = "Mail", required = true, nillable = true)
    protected String mail;
    @XmlElement(name = "Employee_Order", required = true, nillable = true)
    protected String employeeOrder;
    @XmlElement(name = "Employee_Status", required = true, nillable = true)
    protected String employeeStatus;
    @XmlElement(name = "Area_Name", required = true, nillable = true)
    protected String areaName;
    @XmlElement(name = "User_Num", required = true, nillable = true)
    protected String userNum;
    @XmlElement(name = "Last_Update_Time", required = true, nillable = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar lastUpdateTime;
    @XmlElement(name = "REFERENCE1", required = true, nillable = true)
    protected String reference1;
    @XmlElement(name = "REFERENCE2", required = true, nillable = true)
    protected String reference2;
    @XmlElement(name = "REFERENCE3", required = true, nillable = true)
    protected String reference3;
    @XmlElement(name = "REFERENCE4", required = true, nillable = true)
    protected String reference4;
    @XmlElement(name = "REFERENCE5", required = true, nillable = true)
    protected String reference5;

    public String getEmployeeNo() {
        return employeeNo;
    }

    public void setEmployeeNo(String value) {
        this.employeeNo = value;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String value) {
        this.loginName = value;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String value) {
        this.employeeName = value;
    }

    public String getEmployeeLevel() {
        return employeeLevel;
    }

    public void setEmployeeLevel(String value) {
        this.employeeLevel = value;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String value) {
        this.mobile = value;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String value) {
        this.mail = value;
    }

    public String getEmployeeOrder() {
        return employeeOrder;
    }

    public void setEmployeeOrder(String value) {
        this.employeeOrder = value;
    }

    public String getEmployeeStatus() {
        return employeeStatus;
    }

    public void setEmployeeStatus(String value) {
        this.employeeStatus = value;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String value) {
        this.areaName = value;
    }

    public String getUserNum() {
        return userNum;
    }

    public void setUserNum(String value) {
        this.userNum = value;
    }

    public XMLGregorianCalendar getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(XMLGregorianCalendar value) {
        this.lastUpdateTime = value;
    }

    public String getREFERENCE1() {
        return reference1;
    }

    public void setREFERENCE1(String value) {
        this.reference1 = value;
    }

    public String getREFERENCE2() {
        return reference2;
    }

    public void setREFERENCE2(String value) {
        this.reference2 = value;
    }

    public String getREFERENCE3() {
        return reference3;
    }

    public void setREFERENCE3(String value) {
        this.reference3 = value;
    }

    public String getREFERENCE4() {
        return reference4;
    }

    public void setREFERENCE4(String value) {
        this.reference4 = value;
    }

    public String getREFERENCE5() {
        return reference5;
    }

    public void setREFERENCE5(String value) {
        this.reference5 = value;
    }

}
