
package com.cmcc.mss.oa_pageinquirydeptemprelationinfosrv;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;
import com.cmcc.mss.msgheader.MsgHeader;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OA_PageInquiryDeptEmpRelationInfoSrvRequest", propOrder = {
    "msgHeader",
    "employeeNo",
    "departmentNo",
    "lastupdatedatefrom",
    "lastupdatedateto"
})
public class OAPageInquiryDeptEmpRelationInfoSrvRequest {

    @XmlElement(name = "MsgHeader", required = true)
    protected MsgHeader msgHeader;
    @XmlElement(name = "Employee_No", required = true, nillable = true)
    protected String employeeNo;
    @XmlElement(name = "Department_No", required = true, nillable = true)
    protected String departmentNo;
    @XmlElement(name = "LAST_UPDATE_DATE_FROM", required = true, nillable = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar lastupdatedatefrom;
    @XmlElement(name = "LAST_UPDATE_DATE_TO", required = true, nillable = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar lastupdatedateto;

    public MsgHeader getMsgHeader() {
        return msgHeader;
    }

    public void setMsgHeader(MsgHeader value) {
        this.msgHeader = value;
    }

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

    public XMLGregorianCalendar getLASTUPDATEDATEFROM() {
        return lastupdatedatefrom;
    }

    public void setLASTUPDATEDATEFROM(XMLGregorianCalendar value) {
        this.lastupdatedatefrom = value;
    }

    public XMLGregorianCalendar getLASTUPDATEDATETO() {
        return lastupdatedateto;
    }

    public void setLASTUPDATEDATETO(XMLGregorianCalendar value) {
        this.lastupdatedateto = value;
    }

}
