package stuhorner.com.buckit;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

/**
 * Created by Owner on 8/1/2016.
 */
public class User {
    String name;
    Bitmap profilePicture;
    int age;
    String UID;

    public User() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Bitmap getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        byte[] bytes = Base64.decode(profilePicture.getBytes(), Base64.DEFAULT);
        this.profilePicture = BitmapFactory.decodeByteArray(bytes,0, bytes.length, options);
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }
}
