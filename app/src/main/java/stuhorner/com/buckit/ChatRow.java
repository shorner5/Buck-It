package stuhorner.com.buckit;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

/**
 * Created by Stu on 6/22/2016.
 */
public class ChatRow {
    private String UID;
    private String name;
    private String subtitle;
    private boolean newMessage;
    private Bitmap chatIcon;

    public ChatRow(String UID) {
        this.UID = UID;
        this.name = "";
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setNewMessage(boolean newMessage) {
        this.newMessage = newMessage;
    }

    public boolean isNewMessage() {
        return newMessage;
    }

    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }

    public Bitmap getChatIcon() {
        return chatIcon;
    }

    public void setChatIcon(String chatIcon) {
        byte[] bytes = Base64.decode(chatIcon.getBytes(), Base64.DEFAULT);
        this.chatIcon = BitmapFactory.decodeByteArray(bytes,0, bytes.length);
    }
}
