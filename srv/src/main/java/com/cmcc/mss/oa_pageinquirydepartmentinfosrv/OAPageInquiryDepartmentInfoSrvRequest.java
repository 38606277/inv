
package com.cmcc.mss.oa_pageinquirydepartmentinfosrv;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;
import com.cmcc.mss.msgheader.MsgHeader;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OA_PageInquiryDepartmentInfoSrvRequest", propOrder = {
    "msgHeader",
    "departmentNo",
    "departmentName",
    "lastupdatedatefrom",
    "lastupdatedateto",
    "attribute1INPUT",
    "attribute2INPUT",
    "attribute3INPUT",
    "attribute4INPUT",
    "attribute5INPUT"
})
public class OAPageInquiryDepartmentInfoSrvRequest {

    @XmlElement(name = "MsgHeader", required = true)
    protected MsgHeader msgHeader;
    @XmlElement(name = "Department_No", required = true, nillable = true)
    protected String departmentNo;
    @XmlElement(name = "Department_Name", required = true, nillable = true)
    protected String departmentName;
    @XmlElement(name = "LAST_UPDATE_DATE_FROM", required = true, nillable = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar lastupdatedatefrom;
    @XmlElement(name = "LAST_UPDATE_DATE_TO", required = true, nillable = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar lastupdatedateto;
    @XmlElement(name = "ATTRIBUTE1_INPUT", required = true, nillable = true)
    protected String attribute1INPUT;
    @XmlElement(name = "ATTRIBUTE2_INPUT", required = true, nillable = true)
    protected String attribute2INPUT;
    @XmlElement(name = "ATTRIBUTE3_INPUT", required = true, nillable = true)
    protected String attribute3INPUT;
    @XmlElement(name = "ATTRIBUTE4_INPUT", required = true, nillable = true)
    protected String attribute4INPUT;
    @XmlElement(name = "ATTRIBUTE5_INPUT", required = true, nillable = true)
    protected String attribute5INPUT;

    public MsgHeader getMsgHeader() {
        return msgHeader;
    }

    public void setMsgHeader(MsgHeader value) {
        this.msgHeader = value;
    }

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

    public String getATTRIBUTE1INPUT() {
        return attribute1INPUT;
    }

    public void setATTRIBUTE1INPUT(String value) {
        this.attribute1INPUT = value;
    }

    public String getATTRIBUTE2INPUT() {
        return attribute2INPUT;
    }

    public void setATTRIBUTE2INPUT(String value) {
        this.attribute2INPUT = value;
    }

    public String getATTRIBUTE3INPUT() {
        return attribute3INPUT;
    }

    public void setATTRIBUTE3INPUT(String value) {
        this.attribute3INPUT = value;
    }

    public String getATTRIBUTE4INPUT() {
        return attribute4INPUT;
    }

    public void setATTRIBUTE4INPUT(String value) {
        this.attribute4INPUT = value;
    }

    public String getATTRIBUTE5INPUT() {
        return attribute5INPUT;
    }

    public void setATTRIBUTE5INPUT(String value) {
        this.attribute5INPUT = value;
    }

}
