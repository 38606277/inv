
package com.cmcc.mss.oa_pageinquirydeptemprelationinfosrv;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.cmcc.mss.oa_pageinquirydeptemprelationinfosrv package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _OAPageInquiryDeptEmpRelationInfoSrvRequest_QNAME = new QName("http://mss.cmcc.com/OA_PageInquiryDeptEmpRelationInfoSrv", "OA_PageInquiryDeptEmpRelationInfoSrvRequest");
    private final static QName _OAPageInquiryDeptEmpRelationInfoSrvResponse_QNAME = new QName("http://mss.cmcc.com/OA_PageInquiryDeptEmpRelationInfoSrv", "OA_PageInquiryDeptEmpRelationInfoSrvResponse");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.cmcc.mss.oa_pageinquirydeptemprelationinfosrv
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link OAPageInquiryDeptEmpRelationInfoSrvRequest }
     * 
     */
    public OAPageInquiryDeptEmpRelationInfoSrvRequest createOAPageInquiryDeptEmpRelationInfoSrvRequest() {
        return new OAPageInquiryDeptEmpRelationInfoSrvRequest();
    }

    /**
     * Create an instance of {@link OAPageInquiryDeptEmpRelationInfoSrvResponse }
     * 
     */
    public OAPageInquiryDeptEmpRelationInfoSrvResponse createOAPageInquiryDeptEmpRelationInfoSrvResponse() {
        return new OAPageInquiryDeptEmpRelationInfoSrvResponse();
    }

    /**
     * Create an instance of {@link OAPageInquiryDeptEmpRelationInfoSrvOutputCollection }
     * 
     */
    public OAPageInquiryDeptEmpRelationInfoSrvOutputCollection createOAPageInquiryDeptEmpRelationInfoSrvOutputCollection() {
        return new OAPageInquiryDeptEmpRelationInfoSrvOutputCollection();
    }

    /**
     * Create an instance of {@link OAPageInquiryDeptEmpRelationInfoSrvOutputItem }
     * 
     */
    public OAPageInquiryDeptEmpRelationInfoSrvOutputItem createOAPageInquiryDeptEmpRelationInfoSrvOutputItem() {
        return new OAPageInquiryDeptEmpRelationInfoSrvOutputItem();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link OAPageInquiryDeptEmpRelationInfoSrvRequest }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mss.cmcc.com/OA_PageInquiryDeptEmpRelationInfoSrv", name = "OA_PageInquiryDeptEmpRelationInfoSrvRequest")
    public JAXBElement<OAPageInquiryDeptEmpRelationInfoSrvRequest> createOAPageInquiryDeptEmpRelationInfoSrvRequest(OAPageInquiryDeptEmpRelationInfoSrvRequest value) {
        return new JAXBElement<OAPageInquiryDeptEmpRelationInfoSrvRequest>(_OAPageInquiryDeptEmpRelationInfoSrvRequest_QNAME, OAPageInquiryDeptEmpRelationInfoSrvRequest.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link OAPageInquiryDeptEmpRelationInfoSrvResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mss.cmcc.com/OA_PageInquiryDeptEmpRelationInfoSrv", name = "OA_PageInquiryDeptEmpRelationInfoSrvResponse")
    public JAXBElement<OAPageInquiryDeptEmpRelationInfoSrvResponse> createOAPageInquiryDeptEmpRelationInfoSrvResponse(OAPageInquiryDeptEmpRelationInfoSrvResponse value) {
        return new JAXBElement<OAPageInquiryDeptEmpRelationInfoSrvResponse>(_OAPageInquiryDeptEmpRelationInfoSrvResponse_QNAME, OAPageInquiryDeptEmpRelationInfoSrvResponse.class, null, value);
    }

}
