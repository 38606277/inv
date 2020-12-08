package root.report.util;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.xml.sax.SAXException;

public class XmlUtil
{
    //解析指定路径下xml文件
    public static Document parseXmlToDom(String sqlPath) throws SAXException, DocumentException
    {
        SAXReader reader = new SAXReader();
        reader.setValidation(false);
        reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        return reader.read(new File(sqlPath));
    }
    
    public static Document parseXmlToDom(File file) throws SAXException, DocumentException
    {
        SAXReader reader = new SAXReader();
        reader.setValidation(false);
        reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        return reader.read(file);
    }
    public static Document parseXmlToDom(URL url) throws SAXException, DocumentException
    {
        SAXReader reader = new SAXReader();
        reader.setValidation(false);
        reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        return reader.read(url);
    }
    public static Document parseXmlToDom(InputStream in) throws SAXException, DocumentException
    {
        SAXReader reader = new SAXReader();
        reader.setValidation(false);
        reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        return reader.read(in);
    }
}
