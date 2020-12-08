
package com.cmcc.mss.oa_pageinquirydepartmentinfosrv;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.cmcc.mss.oa_pageinquirydepartmentinfosrv package. 
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

    private final static QName _OAPageInquiryDepartmentInfoSrvRequest_QNAME = new QName("http://mss.cmcc.com/OA_PageInquiryDepartmentInfoSrv", "OA_PageInquiryDepartmentInfoSrvRequest");
    private final static QName _OAPageInquiryDepartmentInfoSrvResponse_QNAME = new QName("http://mss.cmcc.com/OA_PageInquiryDepartmentInfoSrv", "OA_PageInquiryDepartmentInfoSrvResponse");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.cmcc.mss.oa_pageinquirydepartmentinfosrv
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link OAPageInquiryDepartmentInfoSrvRequest }
     * 
     */
    public OAPageInquiryDepartmentInfoSrvRequest createOAPageInquiryDepartmentInfoSrvRequest() {
        return new OAPageInquiryDepartmentInfoSrvRequest();
    }

    /**
     * Create an instance of {@link OAPageInquiryDepartmentInfoSrvResponse }
     * 
     */
    public OAPageInquiryDepartmentInfoSrvResponse createOAPageInquiryDepartmentInfoSrvResponse() {
        return new OAPageInquiryDepartmentInfoSrvResponse();
    }

    /**
     * Create an instance of {@link OAPageInquiryDepartmentInfoSrvOutputCollection }
     * 
     */
    public OAPageInquiryDepartmentInfoSrvOutputCollection createOAPageInquiryDepartmentInfoSrvOutputCollection() {
        return new OAPageInquiryDepartmentInfoSrvOutputCollection();
    }

    /**
     * Create an instance of {@link OAPageInquiryDepartmentInfoSrvOutputItem }
     * 
     */
    public OAPageInquiryDepartmentInfoSrvOutputItem createOAPageInquiryDepartmentInfoSrvOutputItem() {
        return new OAPageInquiryDepartmentInfoSrvOutputItem();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link OAPageInquiryDepartmentInfoSrvRequest }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mss.cmcc.com/OA_PageInquiryDepartmentInfoSrv", name = "OA_PageInquiryDepartmentInfoSrvRequest")
    public JAXBElement<OAPageInquiryDepartmentInfoSrvRequest> createOAPageInquiryDepartmentInfoSrvRequest(OAPageInquiryDepartmentInfoSrvRequest value) {
        return new JAXBElement<OAPageInquiryDepartmentInfoSrvRequest>(_OAPageInquiryDepartmentInfoSrvRequest_QNAME, OAPageInquiryDepartmentInfoSrvRequest.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link OAPageInquiryDepartmentInfoSrvResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mss.cmcc.com/OA_PageInquiryDepartmentInfoSrv", name = "OA_PageInquiryDepartmentInfoSrvResponse")
    public JAXBElement<OAPageInquiryDepartmentInfoSrvResponse> createOAPageInquiryDepartmentInfoSrvResponse(OAPageInquiryDepartmentInfoSrvResponse value) {
        return new JAXBElement<OAPageInquiryDepartmentInfoSrvResponse>(_OAPageInquiryDepartmentInfoSrvResponse_QNAME, OAPageInquiryDepartmentInfoSrvResponse.class, null, value);
    }

}
