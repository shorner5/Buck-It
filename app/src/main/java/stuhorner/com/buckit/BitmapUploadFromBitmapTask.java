package stuhorner.com.buckit;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Created by Stu on 4/17/2016.
 */
class BitmapUploadFromBitmapTask extends AsyncTask<String, Void, String> {
    private Bitmap bitmap;
    private String savePath;
    private DatabaseReference userRef = FirebaseDatabase.getInstance().getReference();
    private final static int BITMAP_WIDTH = 500;
    private final static int BITMAP_HEIGHT = 320;

    public BitmapUploadFromBitmapTask(Bitmap bitmap, String savePath) {
        this.savePath = savePath;
        this.bitmap = bitmap;
    }

    @Override
    protected String doInBackground(String... params) {
        Log.d("bitmapUploadFromBitmap", savePath);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap = compressBitmap(bitmap);
        if (bitmap != null) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 50, baos);
        }
        byte[] bytes = baos.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    @Override
    protected void onPostExecute(String result) {
        if (result != null) {
            Log.d("bitmapUploadFromBitmap", "uploading");
            userRef.child(savePath).setValue(result);
        }
    }

    private Bitmap compressBitmap(Bitmap bitmap) {
        return (bitmap.getWidth() > BITMAP_WIDTH && bitmap.getHeight() > BITMAP_HEIGHT)
                ? Bitmap.createScaledBitmap(bitmap, BITMAP_WIDTH, BITMAP_HEIGHT, true)
                : bitmap;
    }
}

