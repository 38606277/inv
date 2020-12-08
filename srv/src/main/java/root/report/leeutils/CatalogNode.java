package root.report.leeutils;

import java.io.Serializable;
import java.util.List;

public class CatalogNode implements Serializable {
    private String catalogId;
    private String catalogPid;
    private String catalogName;
    private List<CatalogNode> children;

    public CatalogNode() {
    }

    public CatalogNode(String id, String pid, String name) {
        super();
        this.catalogId = id;
        this.catalogPid = pid;
        this.catalogName = name;
    }



    public List<CatalogNode> getChildren() {
        return children;
    }

    public void setChildren(List<CatalogNode> children) {
        this.children = children;
    }

    public String getCatalogId() {
        return catalogId;
    }

    public void setCatalogId(String catalogId) {
        this.catalogId = catalogId;
    }

    public String getCatalogPid() {
        return catalogPid;
    }

    public void setCatalogPid(String catalogPid) {
        this.catalogPid = catalogPid;
    }

    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }
}
