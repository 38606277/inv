package root.report.service.webchat;

public class WebChatMenu {
}
class Button {
    private String name;

    public void setName(String name){
        this.name = name;
    }
    public String getName(){
        return name;
    }

}



class ClickButton extends Button {
    private String type;
    private String key;
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }
}

class ViewButton extends Button{

    public String type;
    public String url;
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
}



class ComplexButton extends Button{
    private Button[] sub_button;

    public Button[] getSub_button() {
        return sub_button;
    }

    public void setSub_button(Button[] sub_button) {
        this.sub_button = sub_button;
    }

}


class Menu {
    private Button[] button;

    public Button[] getButton() {
        return button;
    }
    public void setButton(Button[] button) {
        this.button = button;
    }
}

