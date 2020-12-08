
package com.cmcc.mss.msgheader;

import java.math.BigDecimal;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MsgHeader", propOrder = {
    "sourcesystemid",
    "sourcesystemname",
    "userid",
    "username",
    "submitdate",
    "pagesize",
    "currentpage",
    "totalrecord"
})
public class MsgHeader {

    @XmlElement(name = "SOURCESYSTEMID", required = true, nillable = true)
    protected String sourcesystemid;
    @XmlElement(name = "SOURCESYSTEMNAME", required = true, nillable = true)
    protected String sourcesystemname;
    @XmlElement(name = "USERID", required = true, nillable = true)
    protected String userid;
    @XmlElement(name = "USERNAME", required = true, nillable = true)
    protected String username;
    @XmlElement(name = "SUBMITDATE", required = true, nillable = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar submitdate;
    @XmlElement(name = "PAGE_SIZE", required = true, nillable = true)
    protected BigDecimal pagesize;
    @XmlElement(name = "CURRENT_PAGE", required = true, nillable = true)
    protected BigDecimal currentpage;
    @XmlElement(name = "TOTAL_RECORD", required = true, nillable = true)
    protected BigDecimal totalrecord;

    public String getSOURCESYSTEMID() {
        return sourcesystemid;
    }

    public void setSOURCESYSTEMID(String value) {
        this.sourcesystemid = value;
    }

    public String getSOURCESYSTEMNAME() {
        return sourcesystemname;
    }

    public void setSOURCESYSTEMNAME(String value) {
        this.sourcesystemname = value;
    }

    public String getUSERID() {
        return userid;
    }

    public void setUSERID(String value) {
        this.userid = value;
    }

    public String getUSERNAME() {
        return username;
    }

    public void setUSERNAME(String value) {
        this.username = value;
    }

    public XMLGregorianCalendar getSUBMITDATE() {
        return submitdate;
    }

    public void setSUBMITDATE(XMLGregorianCalendar value) {
        this.submitdate = value;
    }

    public BigDecimal getPAGESIZE() {
        return pagesize;
    }

    public void setPAGESIZE(BigDecimal value) {
        this.pagesize = value;
    }

    public BigDecimal getCURRENTPAGE() {
        return currentpage;
    }

    public void setCURRENTPAGE(BigDecimal value) {
        this.currentpage = value;
    }

    public BigDecimal getTOTALRECORD() {
        return totalrecord;
    }

    public void setTOTALRECORD(BigDecimal value) {
        this.totalrecord = value;
    }

}
