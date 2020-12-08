
package com.cmcc.mss.oa_pageinquirydepartmentinfosrv;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OA_PageInquiryDepartmentInfoSrvOutputItem", propOrder = {
    "departmentNo",
    "departmentName",
    "departmentParentNo",
    "areaName",
    "companyName",
    "departmentLevel",
    "mail",
    "departmentOrder",
    "departmentStatus",
    "lastUpdateTime",
    "reference1",
    "reference2",
    "reference3",
    "reference4",
    "reference5"
})
public class OAPageInquiryDepartmentInfoSrvOutputItem {

    @XmlElement(name = "Department_No", required = true, nillable = true)
    protected String departmentNo;
    @XmlElement(name = "Department_Name", required = true, nillable = true)
    protected String departmentName;
    @XmlElement(name = "Department_Parent_No", required = true, nillable = true)
    protected String departmentParentNo;
    @XmlElement(name = "Area_Name", required = true, nillable = true)
    protected String areaName;
    @XmlElement(name = "Company_Name", required = true, nillable = true)
    protected String companyName;
    @XmlElement(name = "Department_Level", required = true, nillable = true)
    protected String departmentLevel;
    @XmlElement(name = "Mail", required = true, nillable = true)
    protected String mail;
    @XmlElement(name = "Department_Order", required = true, nillable = true)
    protected String departmentOrder;
    @XmlElement(name = "Department_Status", required = true, nillable = true)
    protected String departmentStatus;
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

    public String getDepartmentNo() {
        return departmentNo;
    }

    public void setDepartmentNo(String value) {
        this.departmentNo = value;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String value) {
        this.departmentName = value;
    }

    public String getDepartmentParentNo() {
        return departmentParentNo;
    }

    public void setDepartmentParentNo(String value) {
        this.departmentParentNo = value;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String value) {
        this.areaName = value;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String value) {
        this.companyName = value;
    }

    public String getDepartmentLevel() {
        return departmentLevel;
    }

    public void setDepartmentLevel(String value) {
        this.departmentLevel = value;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String value) {
        this.mail = value;
    }

    public String getDepartmentOrder() {
        return departmentOrder;
    }

    public void setDepartmentOrder(String value) {
        this.departmentOrder = value;
    }

    public String getDepartmentStatus() {
        return departmentStatus;
    }

    public void setDepartmentStatus(String value) {
        this.departmentStatus = value;
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
