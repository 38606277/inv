package root.fnd.entity;

import java.io.Serializable;
import java.util.List;

public class TreeNode implements Serializable {
    private String id;
    private String pid;
    private String value;
    private String name;
    private List<TreeNode> children;

    public TreeNode() {
    }

    public TreeNode(String id, String pid, String value, String name) {
        super();
        this.id = id;
        this.pid = pid;
        this.value = value;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public List<TreeNode> getChildren() {
        return children;
    }

    public void setChildren(List<TreeNode> children) {
        this.children = children;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
