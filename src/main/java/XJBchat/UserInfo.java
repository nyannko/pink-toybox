package XJBchat;

import java.util.Set;

public class UserInfo {
    private String name;
    private String message;
    private String newClient; // change var name
    private Set<String> onlineList;

    public boolean isRegisterStatus() {
        return registerStatus;
    }

    public void setRegisterStatus(boolean registerStatus) {
        this.registerStatus = registerStatus;
    }

    private boolean registerStatus;

    public UserInfo() {}

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

    public String getNewClient() {
        return newClient;
    }

    public void setNewClient(String newClient) {
        this.newClient = newClient;
    }

    public Set<String> getOnlineList() {
        return onlineList;
    }

    public void setOnlineList(Set<String> onlineList) {
        this.onlineList = onlineList;
    }
}
