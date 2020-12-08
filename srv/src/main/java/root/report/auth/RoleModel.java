package root.report.auth;

/**
 * Created by Administrator on 2018/10/8.
 */
public class RoleModel {
    private int roleId;           //主键
    private String roleName;       //用户名称
    private String enabled;         //是否啟用
    private String enabledText;
    private String createdDate; //创建时间
    private String createdBy;    //創建人
    private String lastUpdatedDate;      //最后修改时间
    private String lastUpdatedBy;  //最后修改人
    public int getRoleId()
    {
        return roleId;
    }
    public void setRoleId(int roleId)
    {
        this.roleId = roleId;
    }
    public String getRoleName()
    {
        return roleName;
    }
    public void setRoleName(String roleName)
    {
        this.roleName = roleName;
    }
    public String getEnabled()
    {
        return enabled;
    }
    public void setEnabled(String enabled)
    {
        this.enabled = enabled;
    }
    public String getEnabledText()
    {
        return enabledText;
    }
    public void setEnabledText(String enabledText)
    {
        this.enabledText = enabledText;
    }
    public String getCreatedDate()
    {
        return createdDate;
    }
    public void setCreatedDate(String createdDate)
    {
        this.createdDate = createdDate;
    }
    public String getCreatedBy()
    {
        return createdBy;
    }
    public void setCreatedBy(String createdBy)
    {
        this.createdBy = createdBy;
    }
    public String getLastUpdatedDate()
    {
        return lastUpdatedDate;
    }
    public void setLastUpdatedDate(String lastUpdatedDate)
    {
        this.lastUpdatedDate = lastUpdatedDate;
    }
    public String getLastUpdatedBy()
    {
        return lastUpdatedBy;
    }
    public void setLastUpdatedBy(String lastUpdatedBy)
    {
        this.lastUpdatedBy = lastUpdatedBy;
    }

}
