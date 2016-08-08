package stuhorner.com.buckit;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
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
 * Created by Stu on 8/3/2016.
 */
public class BitmapCompressAndUploadTask extends AsyncTask<String, Void, String> {
    private Bitmap bitmap;
    private String savePath;
    private DatabaseReference userRef = FirebaseDatabase.getInstance().getReference();
    private final static int BITMAP_WIDTH = 216;
    private final static int BITMAP_HEIGHT = 138;

    public BitmapCompressAndUploadTask(Bitmap bitmap, String savePath) {
        this.savePath = savePath;
        this.bitmap = bitmap;
    }

    @Override
    protected String doInBackground(String... params) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap = getCircleBitmap(compress(bitmap));
        if (bitmap != null) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 50, baos);
        }
        byte[] bytes = baos.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    @Override
    protected void onPostExecute(String result) {
        if (result != null) {
            userRef.child(savePath).setValue(result);
        }
    }

    private Bitmap compress(Bitmap bitmap) {
        return (bitmap.getWidth() > BITMAP_WIDTH && bitmap.getHeight() > BITMAP_HEIGHT)
                ? Bitmap.createScaledBitmap(bitmap,BITMAP_WIDTH, BITMAP_HEIGHT, true)
                : bitmap;
    }

    private Bitmap getCircleBitmap(Bitmap bitmap) {
        final Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(output);

        final int color = Color.RED;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawOval(rectF, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        bitmap.recycle();

        return output;
    }
}

