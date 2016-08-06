package stuhorner.com.buckit;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by Stu on 1/1/2016.
 */
public class ProfileActivity extends AppCompatActivity {
    private String person_name, UID;
    private DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users");
    private FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
    private boolean editing, isProfileLoaded;
    private final static int GALLERY_REQUEST = 4;
    private ViewPager viewPager;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);
        person_name = getIntent().getStringExtra("name");
        UID = getIntent().getStringExtra("uid");
        Log.d("uid", UID);

        initData();
        setupToolbar();
        setupViewPager();
        setUpButtons();
    }

    private void setUpButtons() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (UID.equals(mUser.getUid()) || getIntent().getBooleanExtra("hide_chat_button", false)) {
            if (fab != null) {
                fab.setVisibility(View.INVISIBLE);
            }
        }
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ProfileActivity.this, ChatPage.class);
                    intent.putExtra("name", person_name);
                    intent.putExtra("uid", UID);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in, R.anim.fade_out);
                }
            });
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.p_toolbar);
        setSupportActionBar(toolbar);
        if (toolbar != null) {
            if (person_name != null) {
                Log.d("person_name", "not null");
                if (getSupportActionBar() != null)
                    getSupportActionBar().setTitle(person_name);
            } else {
                Log.d("person_name", "null");
                getName(toolbar);
            }
            toolbar.setNavigationIcon(ContextCompat.getDrawable(this, R.drawable.ic_back));
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }
    }

    private void getName(final Toolbar toolbar) {
        userRef.child(UID).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null && toolbar != null) {
                    Log.d("setting title", dataSnapshot.getValue().toString());
                    toolbar.setTitle(dataSnapshot.getValue().toString());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void setupViewPager() {
        viewPager = (ViewPager) findViewById(R.id.p_viewpager);
        ProfilePagerAdapter adapter = new ProfilePagerAdapter(getSupportFragmentManager());
        adapter.add(new ProfileDetails(), "Profile");
        adapter.add(new ProfileBuckits(), "Buck It List");
        if (viewPager != null) {
            viewPager.setAdapter(adapter);
        }
        TabLayout tabLayout = (TabLayout) findViewById(R.id.p_tabs);
        if (tabLayout != null) {
            tabLayout.setupWithViewPager(viewPager);
        }
        CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        if (collapsingToolbar != null) {
            collapsingToolbar.setTitleEnabled(false);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in, R.anim.slide_out);
    }

    private void initData() {
        userRef.child(UID).child("profilePicture").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    ImageView img = (ImageView) findViewById(R.id.backdrop);
                    byte[] bytes = Base64.decode(dataSnapshot.getValue().toString().getBytes(), Base64.DEFAULT);
                    if (img != null) {
                        img.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (UID.equals(mUser.getUid())) {
            if (editing)
                getMenuInflater().inflate(R.menu.menu_editting, menu);
            else
                getMenuInflater().inflate(R.menu.menu_edit, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d("item", Integer.toString(viewPager.getCurrentItem()));
        if (isProfileLoaded) {
            if (item.getItemId() != R.id.new_pic) {
                if (viewPager.getCurrentItem() == 0) {
                    editing = !editing;
                    invalidateOptionsMenu();
                }
            } else if (isStoragePermissionGranted()) {
                startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), GALLERY_REQUEST);
            }
            return super.onOptionsItemSelected(item);
        }
        else return false;
    }

    public void setEditing(boolean editing) {
        this.editing = editing;
    }

    public void setIsProfileLoaded(boolean isProfileLoaded) {
        this.isProfileLoaded = isProfileLoaded;
    }

    public boolean isProfileLoaded() {
        return isProfileLoaded;
    }

    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v("storage","Permission is granted");
                return true;
            } else {
                Log.v("storage","Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, GALLERY_REQUEST);
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
            Snackbar.make(findViewById(R.id.root_view), getString(R.string.update_soon), Snackbar.LENGTH_SHORT).show();
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
}