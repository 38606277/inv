package root.report.leedemo;

import java.util.List;

public class AccountCaptionVo {

        private String captionName;
        private String  captionCode;
        private Long  parentId;
        private String  systematicSubjects;
        private String  lendingDirection;
        private List<AccountCaptionVo> children;
        private String  mnemonicCode;


    public String getCaptionName() {
        return captionName;
    }

    public void setCaptionName(String captionName) {
        this.captionName = captionName;
    }

    public String getCaptionCode() {
        return captionCode;
    }

    public void setCaptionCode(String captionCode) {
        this.captionCode = captionCode;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getSystematicSubjects() {
        return systematicSubjects;
    }

    public void setSystematicSubjects(String systematicSubjects) {
        this.systematicSubjects = systematicSubjects;
    }

    public String getLendingDirection() {
        return lendingDirection;
    }

    public void setLendingDirection(String lendingDirection) {
        this.lendingDirection = lendingDirection;
    }

    public List<AccountCaptionVo> getChildren() {
        return children;
    }

    public void setChildren(List<AccountCaptionVo> children) {
        this.children = children;
    }

    public String getMnemonicCode() {
        return mnemonicCode;
    }

    public void setMnemonicCode(String mnemonicCode) {
        this.mnemonicCode = mnemonicCode;
    }
}
