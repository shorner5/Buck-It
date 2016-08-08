package stuhorner.com.buckit;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.theartofdev.edmodo.cropper.CropImageView;

/**
 * Created by Stu on 8/7/2016.
 */
public class CropImageActivity extends AppCompatActivity implements CropImageView.OnGetCroppedImageCompleteListener {
    private FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
    private CropImageView cropImageView;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_crop_image);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        initCropImageView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_crop, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_done:
                onGetCroppedImageComplete(cropImageView, cropImageView.getCroppedImage(), null);
                break;
            case R.id.action_rotate:
                cropImageView.rotateImage(-90);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initCropImageView() {
        cropImageView = (CropImageView) findViewById(R.id.cropImageView);
        if (cropImageView != null) {
            Log.d("crop", "not null");
            cropImageView.setOnGetCroppedImageCompleteListener(this);
            cropImageView.setImageUriAsync(Uri.parse(getIntent().getStringExtra("uri")));
        }
    }

    @Override
    public void onGetCroppedImageComplete(CropImageView view, Bitmap bitmap, Exception error) {
        Log.d("menu_crop image", "complete");
        BitmapUploadFromBitmapTask task = new BitmapUploadFromBitmapTask(bitmap, "users/" + mUser.getUid() + "/profilePicture");
        task.execute();
        BitmapCompressAndUploadTask compressTask = new BitmapCompressAndUploadTask(bitmap, "users/" + mUser.getUid() + "/profilePicSmall");
        compressTask.execute();
        Toast.makeText(CropImageActivity.this, getString(R.string.update_soon), Toast.LENGTH_SHORT).show();
        onBackPressed();
    }
}
