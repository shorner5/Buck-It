package stuhorner.com.buckit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.LinkedList;

public class SettingsActivity extends AppCompatActivity {
    //Firebase references
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser mUser = mAuth.getCurrentUser();
    private DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users/" + mUser.getUid());

    private final static int PROFILE_DIMEN = 200;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        initToolbar();
        initCompletedBuckits();
        initProfile();
        initSearchSettings();

        Button logOut = (Button) findViewById(R.id.setting_logout);
        if (logOut != null) {
            logOut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAuth.signOut();
                    getSharedPreferences("data", MODE_PRIVATE).edit().clear().apply();
                    Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
        }
    }

    private void initProfile() {
        final ImageView profilePic = (ImageView) findViewById(R.id.profile_image);
        final TextView name = (TextView) findViewById(R.id.profile_name);
        CardView profile = (CardView) findViewById(R.id.profile_view);
        if (profile != null) {
            profile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(SettingsActivity.this, ProfileActivity.class);
                    intent.putExtra("name", name != null ? name.getText().toString() : null);
                    intent.putExtra("uid", mUser.getUid());
                    startActivity(intent);
                }
            });
        }
        userRef.child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null && name != null) {
                    name.setText(dataSnapshot.getValue().toString());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        userRef.child("profilePicSmall").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null && profilePic != null) {
                    byte[] bytes = Base64.decode(dataSnapshot.getValue().toString().getBytes(), Base64.DEFAULT);
                    profilePic.setImageBitmap(resizeImage(BitmapFactory.decodeByteArray(bytes,0, bytes.length), PROFILE_DIMEN, PROFILE_DIMEN));

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
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

    private void initSearchSettings() {
        final SharedPreferences pref = getSharedPreferences("data", MODE_PRIVATE);
        final SharedPreferences.Editor editor = pref.edit();
        final Switch discoverable = (Switch) findViewById(R.id.settings_discoverable);
        if (discoverable != null) {
            discoverable.setChecked(pref.getBoolean("discoverable", false));
            discoverable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (pref.getBoolean("profile_created", false)) {
                        editor.putBoolean("discoverable", isChecked);
                        editor.apply();
                        userRef.child("discoverable").setValue((isChecked) ? "1" : "0");
                    } else {
                        discoverable.setChecked(false);
                        Intent intent = new Intent(SettingsActivity.this, CreateProfileActivity.class);
                        startActivityForResult(intent, MatchActivity.CREATE_PROFILE_REQUEST);
                    }
                }
            });
        }
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getString(R.string.action_settings));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MatchActivity.CREATE_PROFILE_REQUEST) {
            if (resultCode != RESULT_CANCELED) {
                setDiscoverable();
            }
        }
    }

    private void setDiscoverable() {
        final Switch discoverable = (Switch) findViewById(R.id.settings_discoverable);
        if (discoverable != null) discoverable.setChecked(true);
    }

    private void initCompletedBuckits() {
        RecyclerView completedList = (RecyclerView) findViewById(R.id.settings_completed_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        final LinkedList<String> bucket_items = new LinkedList<>();
        final RVAdapter adapter= new RVAdapter(getSupportFragmentManager(), bucket_items, true);
        if (completedList != null) {
            completedList.setLayoutManager(layoutManager);
            completedList.setAdapter(adapter);
        }
        userRef.child("completed").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    bucket_items.add(dataSnapshot.getKey());
                    adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                bucket_items.remove(dataSnapshot.getKey());
                adapter.notifyDataSetChanged();
                if (bucket_items.isEmpty()) {
                    TextView textView = (TextView) findViewById(R.id.no_data_completed);
                    if (textView != null) {
                        textView.setVisibility(View.VISIBLE);
                    }
                }
            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) { }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
        userRef.child("completed").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (bucket_items.isEmpty()) {
                    TextView textView = (TextView) findViewById(R.id.no_data_completed);
                    if (textView != null) {
                        textView.setVisibility(View.VISIBLE);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }
}
