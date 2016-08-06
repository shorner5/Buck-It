package stuhorner.com.buckit;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

/**
 * Created by Stu on 8/4/2016.
 */
public class SocialPostHolder {
    private String title;
    private String story;
    private Bitmap img;
    private int likes = 0;
    private long time;
    private String UID;
    private boolean liked;

    public SocialPostHolder() {

    }

    public String getStory() {
        return story;
    }

    public void setStory(String story) {
        this.story = story;
    }

    public Bitmap getImg() {
        return img;
    }

    public void setImg(String img) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        byte[] bytes = Base64.decode(img.getBytes(), Base64.DEFAULT);
        this.img = BitmapFactory.decodeByteArray(bytes,0, bytes.length, options);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public void incrementLikes() {
        this.likes++;
    }

    public void decrementLikes() {
        this.likes--;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }

    public String getUID() {
        return UID;
    }

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }
}
