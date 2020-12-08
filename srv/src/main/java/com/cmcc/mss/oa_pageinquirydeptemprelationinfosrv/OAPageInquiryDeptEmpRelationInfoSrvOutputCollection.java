
package com.cmcc.mss.oa_pageinquirydeptemprelationinfosrv;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OA_PageInquiryDeptEmpRelationInfoSrvOutputCollection", propOrder = {
    "oaPageInquiryDeptEmpRelationInfoSrvOutputItem"
})
public class OAPageInquiryDeptEmpRelationInfoSrvOutputCollection {

    @XmlElement(name = "OA_PageInquiryDeptEmpRelationInfoSrvOutputItem")
    protected List<OAPageInquiryDeptEmpRelationInfoSrvOutputItem> oaPageInquiryDeptEmpRelationInfoSrvOutputItem;

    /**
     * Gets the value of the oaPageInquiryDeptEmpRelationInfoSrvOutputItem property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the oaPageInquiryDeptEmpRelationInfoSrvOutputItem property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getOAPageInquiryDeptEmpRelationInfoSrvOutputItem().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link OAPageInquiryDeptEmpRelationInfoSrvOutputItem }
     * 
     * 
     */
    public List<OAPageInquiryDeptEmpRelationInfoSrvOutputItem> getOAPageInquiryDeptEmpRelationInfoSrvOutputItem() {
        if (oaPageInquiryDeptEmpRelationInfoSrvOutputItem == null) {
            oaPageInquiryDeptEmpRelationInfoSrvOutputItem = new ArrayList<OAPageInquiryDeptEmpRelationInfoSrvOutputItem>();
        }
        return this.oaPageInquiryDeptEmpRelationInfoSrvOutputItem;
    }

}
