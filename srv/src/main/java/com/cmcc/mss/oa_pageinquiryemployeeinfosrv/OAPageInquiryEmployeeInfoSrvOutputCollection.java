
package com.cmcc.mss.oa_pageinquiryemployeeinfosrv;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OA_PageInquiryEmployeeInfoSrvOutputCollection", propOrder = {
    "oaPageInquiryEmployeeInfoSrvOutputItem"
})
public class OAPageInquiryEmployeeInfoSrvOutputCollection {

    @XmlElement(name = "OA_PageInquiryEmployeeInfoSrvOutputItem")
    protected List<OAPageInquiryEmployeeInfoSrvOutputItem> oaPageInquiryEmployeeInfoSrvOutputItem;

    /**
     * Gets the value of the oaPageInquiryEmployeeInfoSrvOutputItem property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the oaPageInquiryEmployeeInfoSrvOutputItem property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getOAPageInquiryEmployeeInfoSrvOutputItem().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link OAPageInquiryEmployeeInfoSrvOutputItem }
     * 
     * 
     */
    public List<OAPageInquiryEmployeeInfoSrvOutputItem> getOAPageInquiryEmployeeInfoSrvOutputItem() {
        if (oaPageInquiryEmployeeInfoSrvOutputItem == null) {
            oaPageInquiryEmployeeInfoSrvOutputItem = new ArrayList<OAPageInquiryEmployeeInfoSrvOutputItem>();
        }
        return this.oaPageInquiryEmployeeInfoSrvOutputItem;
    }

}
