
package com.cmcc.mss.oa_pageinquirydeptemprelationinfosrv;

import java.math.BigDecimal;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OA_PageInquiryDeptEmpRelationInfoSrvResponse", propOrder = {
    "errorFlag",
    "errorMessage",
    "totalrecord",
    "totalpage",
    "pagesize",
    "currentpage",
    "instanceid",
    "oaPageInquiryDeptEmpRelationInfoSrvOutputCollection"
})
public class OAPageInquiryDeptEmpRelationInfoSrvResponse {

    @XmlElement(name = "ErrorFlag", required = true, nillable = true)
    protected String errorFlag;
    @XmlElement(name = "ErrorMessage", required = true, nillable = true)
    protected String errorMessage;
    @XmlElement(name = "TOTAL_RECORD", required = true, nillable = true)
    protected BigDecimal totalrecord;
    @XmlElement(name = "TOTAL_PAGE", required = true, nillable = true)
    protected BigDecimal totalpage;
    @XmlElement(name = "PAGE_SIZE", required = true, nillable = true)
    protected BigDecimal pagesize;
    @XmlElement(name = "CURRENT_PAGE", required = true, nillable = true)
    protected BigDecimal currentpage;
    @XmlElement(name = "INSTANCE_ID", required = true, nillable = true)
    protected String instanceid;
    @XmlElement(name = "OA_PageInquiryDeptEmpRelationInfoSrvOutputCollection", required = true)
    protected OAPageInquiryDeptEmpRelationInfoSrvOutputCollection oaPageInquiryDeptEmpRelationInfoSrvOutputCollection;

    public String getErrorFlag() {
        return errorFlag;
    }

    public void setErrorFlag(String value) {
        this.errorFlag = value;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String value) {
        this.errorMessage = value;
    }

    public BigDecimal getTOTALRECORD() {
        return totalrecord;
    }

    public void setTOTALRECORD(BigDecimal value) {
        this.totalrecord = value;
    }

    public BigDecimal getTOTALPAGE() {
        return totalpage;
    }

    public void setTOTALPAGE(BigDecimal value) {
        this.totalpage = value;
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

    public String getINSTANCEID() {
        return instanceid;
    }

    public void setINSTANCEID(String value) {
        this.instanceid = value;
    }

    public OAPageInquiryDeptEmpRelationInfoSrvOutputCollection getOAPageInquiryDeptEmpRelationInfoSrvOutputCollection() {
        return oaPageInquiryDeptEmpRelationInfoSrvOutputCollection;
    }

    public void setOAPageInquiryDeptEmpRelationInfoSrvOutputCollection(OAPageInquiryDeptEmpRelationInfoSrvOutputCollection value) {
        this.oaPageInquiryDeptEmpRelationInfoSrvOutputCollection = value;
    }

}
