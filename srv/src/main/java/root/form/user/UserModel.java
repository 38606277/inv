package root.form.user;

public class UserModel
{
    private int id;           //主键
    private String userId;       //用户ID
    private String userName;     //用户名称
    private String encryptPwd;//用户密码
    private int isAdmin;      //是否是管理员
    private String isAdminText;//管理员、用户
    private String creationDate; //创建时间
    private String startDate;    //起始时间
    private String endDate;      //结束时间
    private String description;  //备注
    private String regisType;//注册来源(import)
    private String icon;
    
    public int getId()
    {
        return id;
    }
    public void setId(int id)
    {
        this.id = id;
    }
    public String getUserId()
    {
        return userId;
    }
    public void setUserId(String userId)
    {
        this.userId = userId;
    }
    public String getUserName()
    {
        return userName;
    }
    public void setUserName(String userName)
    {
        this.userName = userName;
    }
    public String getEncryptPwd()
    {
        return encryptPwd;
    }
    public void setEncryptPwd(String encryptPwd)
    {
        this.encryptPwd = encryptPwd;
    }
    public int getIsAdmin()
    {
        return isAdmin;
    }
    public void setIsAdmin(int isAdmin)
    {
        this.isAdmin = isAdmin;
    }
    public String getIsAdminText()
    {
        return isAdminText;
    }
    public void setIsAdminText(String isAdminText)
    {
        this.isAdminText = isAdminText;
    }
    public String getCreationDate()
    {
        return creationDate;
    }
    public void setCreationDate(String creationDate)
    {
        this.creationDate = creationDate;
    }
    public String getStartDate()
    {
        return startDate;
    }
    public void setStartDate(String startDate)
    {
        this.startDate = startDate;
    }
    public String getEndDate()
    {
        return endDate;
    }
    public void setEndDate(String endDate)
    {
        this.endDate = endDate;
    }
    public String getDescription()
    {
        return description;
    }
    public void setDescription(String description)
    {
        this.description = description;
    }
    public String getRegisType()
    {
        return regisType;
    }
    public void setRegisType(String regisType)
    {
        this.regisType = regisType;
    }
    public String getIcon()
    {
        return icon;
    }
    public void setIcon(String icon)
    {
        this.icon = icon;
    }
    @Override
    public String toString()
    {
        return "UserModel [id=" + id + ", userId=" + userId + ", userName=" + userName + ", encryptPwd=" + encryptPwd
                + ", isAdmin=" + isAdmin + ", isAdminText=" + isAdminText + ", creationDate=" + creationDate
                + ", startDate=" + startDate + ", endDate=" + endDate + ", description=" + description + ", regisType="
                + regisType + "]";
    }
}
