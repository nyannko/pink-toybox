package XJBchat;

public class UserInfo {
    private String name;
    private String message;

    public UserInfo(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public String getMessage() {
        return this.message;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
