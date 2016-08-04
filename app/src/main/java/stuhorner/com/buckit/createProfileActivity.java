package stuhorner.com.buckit;

import android.*;
import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CreateProfileActivity extends AppCompatActivity {
    int view = 0;
    private final static int VIEW_NAME = 0;
    private final static int VIEW_AGE = 1;
    private final static int VIEW_PROFILE_PICTURE = 2;
    private final static int GALLERY_REQUEST = 3;

    //UI components
    Button button;
    EditText editText;
    Toolbar toolbar;
    TextView textView;

    //Firebase references
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser mUser = mAuth.getCurrentUser();
    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users/" + mUser.getUid());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_profile);
        button = (Button) findViewById(R.id.button);
        editText = (EditText) findViewById(R.id.edit_text);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        textView = (TextView) findViewById(R.id.textview);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }

        isProfileCreated();
        showProgress(true);

    }

    private void isProfileCreated() {
        userRef.child("profile_created").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null && dataSnapshot.getValue().toString().equals("1")) {
                    SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
                    editor.putBoolean("profile_created", true);
                    editor.apply();
                    setResult(RESULT_OK);
                    finish();
                }
                else {
                    ScrollView scrollView = (ScrollView) findViewById(R.id.scroll_view);
                    if (scrollView != null) {
                        scrollView.setVisibility(View.VISIBLE);
                    }
                    viewChanges(view);
                    handleButtonProgression();
                    showProgress(false);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void handleButtonProgression() {
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                editText.setError(null);
                handleButtonPress(view);
                return true;
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    editText.setError(null);
                    handleButtonPress(view);
            }
        });
    }

    private void handleButtonPress(int position) {
        switch (position) {
            case VIEW_NAME:
                if (editText.getText().length() > 1) {
                    setName(editText.getText().toString());
                    updateViews(++view);
                }
                else {
                    editText.setError(getString(R.string.error_field_required));
                }
                break;
            case VIEW_AGE:
                if (editText.getText().length() == 0) {
                    editText.setError(getString(R.string.error_field_required));
                }
                else if (Integer.parseInt(editText.getText().toString()) < 18) {
                    editText.setError(getString(R.string.age_minimum));
                }
                else {
                    setAge(Integer.parseInt(editText.getText().toString()));
                    updateViews(++view);
                }
                break;
            case VIEW_PROFILE_PICTURE:
                if (isStoragePermissionGranted()) {
                    startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), GALLERY_REQUEST);
                }
                break;
        }
    }

    private void updateViews(final int view) {
        editText.animate().alpha(0).setDuration(200).setListener(null);
        editText.setText("");
        textView.animate().alpha(0).setDuration(200).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                Log.d("animation", "ended");
                viewChanges(view);
            }
        });
    }

    private void viewChanges(int view) {
        Log.d("view", Integer.toString(view));
        switch (view) {
            case VIEW_NAME:
                button.setText(getString(R.string.next));
                textView.setText(getString(R.string.name));
                editText.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
                textView.animate().alpha(1.0f).setDuration(200).setListener(null);
                editText.animate().alpha(1.0f).setDuration(200).setListener(null);
                InputMethodManager keyboard = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                keyboard.showSoftInput(editText, 0);
                break;
            case VIEW_AGE:
                button.setText(getString(R.string.next));
                textView.setText(getString(R.string.age));
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                textView.animate().alpha(1.0f).setDuration(200).setListener(null);
                editText.animate().alpha(1.0f).setDuration(200).setListener(null);
                InputMethodManager input = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                input.showSoftInput(editText, 0);
                break;
            case VIEW_PROFILE_PICTURE:
                button.setText(getString(R.string.pic));
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                if (isStoragePermissionGranted()) {
                    startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), GALLERY_REQUEST);
                }
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setName(String name) {
        userRef.child("name").setValue(name);
    }

    private void setAge(int age) {
        userRef.child("age").setValue(age);
    }

    @Override
    public void onBackPressed() {
        switch (view) {
            case VIEW_NAME:
                super.onBackPressed();
                break;
            case VIEW_AGE:
                updateViews(--view);
                break;
            case VIEW_PROFILE_PICTURE:
                viewChanges(--view);
                break;
        }
    }

    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v("storage","Permission is granted");
                return true;
            } else {
                Log.v("storage","Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, GALLERY_REQUEST);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v("storage","Permission is granted");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case GALLERY_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), GALLERY_REQUEST);
                } else {
                    Snackbar.make(getWindow().getDecorView(), getString(R.string.permission_denied), Snackbar.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST && resultCode == Activity.RESULT_OK) {
            //upload image
            Uri selectedImage = data.getData();
            BitmapUploadTask task = new BitmapUploadTask(getPathFromURI(selectedImage), "users/" + mUser.getUid() + "/profilePicture");
            task.execute();
            BitmapCompressAndUploadTask compressTask = new BitmapCompressAndUploadTask(getPathFromURI(selectedImage), "users/" + mUser.getUid() + "/profilePicSmall");
            compressTask.execute();

            //save the result
            userRef.child("discoverable").setValue("1");
            userRef.child("profile_created").setValue("1");
            SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
            editor.putBoolean("profile_created", true);
            editor.apply();
            setResult(RESULT_OK);
            finish();
        }
    }

    private String getPathFromURI(Uri uri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = getApplicationContext().getContentResolver().query(uri,proj,null,null,null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void showProgress(final boolean show) {
        ImageView mProgressView = (ImageView) findViewById(R.id.logo);
        if (show && mProgressView != null) {
            Log.d("mProgressView", "visible");
            Animation rotation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate);
            rotation.setRepeatCount(Animation.INFINITE);
            mProgressView.startAnimation(rotation);
            mProgressView.setVisibility(View.VISIBLE);
        } else if (mProgressView != null){
            Log.d("mProgressView", "invisible");
            mProgressView.setVisibility(View.GONE);
            mProgressView.clearAnimation();
        }
    }

}
