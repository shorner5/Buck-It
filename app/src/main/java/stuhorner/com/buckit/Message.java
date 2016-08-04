package stuhorner.com.buckit;

/**
 * Created by Stu on 6/21/2016.
 */
public class Message {
    String body;
    String sender;

    public Message(String body, String sender) {
        this.body = body;
        this.sender = sender;
    }

    public String getSender() {
        return this.sender;
    }

    public String getBody() {
        return this.body;
    }

}
