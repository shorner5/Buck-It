package stuhorner.com.buckit;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Base64;

import java.util.ArrayList;

/**
 * Created by Stu on 8/5/2016.
 */
public class NearMeHolder {
    private Bitmap img;
    private String name;
    private String uid;
    private ArrayList<String> items = new ArrayList<>(2);

    public NearMeHolder() {
    }

    public Bitmap getImg() {
        return img;
    }

    public void setImg(String img) {
        byte[] bytes = Base64.decode(img.getBytes(), Base64.DEFAULT);
        this.img = resizeImage(BitmapFactory.decodeByteArray(bytes, 0, bytes.length), 200, 200);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getUid() {
        return uid;
    }

    public ArrayList<String> getItems() {
        return items;
    }

    public void addItem(String item) {
        items.add(item);
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    private Bitmap resizeImage(Bitmap bitmap, int w, int h)
    {
        // load the origial Bitmap
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        // calculate the scale
        float scaleWidth = ((float) w) / width;
        float scaleHeight = ((float) h) / height;
        // create a matrix for the manipulation
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        return Bitmap.createBitmap(bitmap, 0, 0,width, height, matrix, true);
    }
}
