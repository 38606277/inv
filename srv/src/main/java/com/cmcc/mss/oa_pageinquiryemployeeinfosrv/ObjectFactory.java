
package com.cmcc.mss.oa_pageinquiryemployeeinfosrv;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.cmcc.mss.oa_pageinquiryemployeeinfosrv package. 
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

    private final static QName _OAPageInquiryEmployeeInfoSrvRequest_QNAME = new QName("http://mss.cmcc.com/OA_PageInquiryEmployeeInfoSrv", "OA_PageInquiryEmployeeInfoSrvRequest");
    private final static QName _OAPageInquiryEmployeeInfoSrvResponse_QNAME = new QName("http://mss.cmcc.com/OA_PageInquiryEmployeeInfoSrv", "OA_PageInquiryEmployeeInfoSrvResponse");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.cmcc.mss.oa_pageinquiryemployeeinfosrv
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link OAPageInquiryEmployeeInfoSrvRequest }
     * 
     */
    public OAPageInquiryEmployeeInfoSrvRequest createOAPageInquiryEmployeeInfoSrvRequest() {
        return new OAPageInquiryEmployeeInfoSrvRequest();
    }

    /**
     * Create an instance of {@link OAPageInquiryEmployeeInfoSrvResponse }
     * 
     */
    public OAPageInquiryEmployeeInfoSrvResponse createOAPageInquiryEmployeeInfoSrvResponse() {
        return new OAPageInquiryEmployeeInfoSrvResponse();
    }

    /**
     * Create an instance of {@link OAPageInquiryEmployeeInfoSrvOutputCollection }
     * 
     */
    public OAPageInquiryEmployeeInfoSrvOutputCollection createOAPageInquiryEmployeeInfoSrvOutputCollection() {
        return new OAPageInquiryEmployeeInfoSrvOutputCollection();
    }

    /**
     * Create an instance of {@link OAPageInquiryEmployeeInfoSrvOutputItem }
     * 
     */
    public OAPageInquiryEmployeeInfoSrvOutputItem createOAPageInquiryEmployeeInfoSrvOutputItem() {
        return new OAPageInquiryEmployeeInfoSrvOutputItem();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link OAPageInquiryEmployeeInfoSrvRequest }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mss.cmcc.com/OA_PageInquiryEmployeeInfoSrv", name = "OA_PageInquiryEmployeeInfoSrvRequest")
    public JAXBElement<OAPageInquiryEmployeeInfoSrvRequest> createOAPageInquiryEmployeeInfoSrvRequest(OAPageInquiryEmployeeInfoSrvRequest value) {
        return new JAXBElement<OAPageInquiryEmployeeInfoSrvRequest>(_OAPageInquiryEmployeeInfoSrvRequest_QNAME, OAPageInquiryEmployeeInfoSrvRequest.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link OAPageInquiryEmployeeInfoSrvResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mss.cmcc.com/OA_PageInquiryEmployeeInfoSrv", name = "OA_PageInquiryEmployeeInfoSrvResponse")
    public JAXBElement<OAPageInquiryEmployeeInfoSrvResponse> createOAPageInquiryEmployeeInfoSrvResponse(OAPageInquiryEmployeeInfoSrvResponse value) {
        return new JAXBElement<OAPageInquiryEmployeeInfoSrvResponse>(_OAPageInquiryEmployeeInfoSrvResponse_QNAME, OAPageInquiryEmployeeInfoSrvResponse.class, null, value);
    }

}
