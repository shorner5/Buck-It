package stuhorner.com.buckit;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
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
public class ProfileActivity extends AppCompatActivity{
    String person_name, UID;
    FloatingActionButton fab;
    private DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users");
    private FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();

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
        fab = (FloatingActionButton) findViewById(R.id.fab);
        if (UID.equals(mUser.getUid()) || getIntent().getBooleanExtra("hide_chat_button", false)) {
            fab.setVisibility(View.INVISIBLE);
        }
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

    private void setupToolbar() {
        Toolbar toolbar = (Toolbar)findViewById(R.id.p_toolbar);
        if (toolbar != null) {
            if (person_name != null) {
                Log.d("person_name", "not null");
                toolbar.setTitle(person_name);
            }
            else {
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

    private void setupViewPager(){
        ViewPager viewPager = (ViewPager) findViewById(R.id.p_viewpager);
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
}