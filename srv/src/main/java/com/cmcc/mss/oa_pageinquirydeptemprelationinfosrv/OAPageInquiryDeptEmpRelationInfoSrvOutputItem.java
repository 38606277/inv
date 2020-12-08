
package com.cmcc.mss.oa_pageinquirydeptemprelationinfosrv;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OA_PageInquiryDeptEmpRelationInfoSrvOutputItem", propOrder = {
    "employeeNo",
    "departmentNo",
    "relationStatus",
    "lastUpdateTime"
})
public class OAPageInquiryDeptEmpRelationInfoSrvOutputItem {

    @XmlElement(name = "Employee_No", required = true, nillable = true)
    protected String employeeNo;
    @XmlElement(name = "Department_No", required = true, nillable = true)
    protected String departmentNo;
    @XmlElement(name = "Relation_Status", required = true, nillable = true)
    protected String relationStatus;
    @XmlElement(name = "Last_Update_Time", required = true, nillable = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar lastUpdateTime;

    public String getEmployeeNo() {
        return employeeNo;
    }

    public void setEmployeeNo(String value) {
        this.employeeNo = value;
    }

    public String getDepartmentNo() {
        return departmentNo;
    }

    public void setDepartmentNo(String value) {
        this.departmentNo = value;
    }

    public String getRelationStatus() {
        return relationStatus;
    }

    public void setRelationStatus(String value) {
        this.relationStatus = value;
    }

    public XMLGregorianCalendar getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(XMLGregorianCalendar value) {
        this.lastUpdateTime = value;
    }

}
