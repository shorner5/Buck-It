package stuhorner.com.buckit;

import android.content.SharedPreferences;
import android.os.Build;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.util.Pair;
import android.transition.Transition;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;
import java.util.LinkedList;

public class MatchActivity extends AppCompatActivity {
    LinkedList<User> users = new LinkedList<>();
    LinkedList<User> userQueue = new LinkedList<>();
    LinkedList<String> potentialUserQueue = new LinkedList<>();
    private ImageButton yesButton;
    private ImageButton noButton;
    private ImageButton chatButton;
    private ImageView mProgressView;
    SwipeFlingAdapterView flingContainer;
    CardAdapter adapter;
    boolean flung = false;
    private String item;

    public final static String PERSON_NAME = "com.stuhorner.buckit.PERSON_NAME";
    public final static int CREATE_PROFILE_REQUEST = 0;
    public final static int VIEW_PROFILE_REQUEST = 1;

    //Firebase references
    DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
    DatabaseReference geoRef = FirebaseDatabase.getInstance().getReference("geoFire");
    GeoFire geoFire = new GeoFire(geoRef);
    GeoQuery geoQuery;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser mUser = mAuth.getCurrentUser();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.match);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        checkboxListener();

        mProgressView = (ImageView) findViewById(R.id.match_logo);
        TextView textView = (TextView) findViewById(R.id.match_text);
        item = getIntent().getStringExtra(BuckitList.MATCH_ITEM);
        if (textView != null)
            textView.setText(item);

        final SharedPreferences pref = getSharedPreferences("data", MODE_PRIVATE);
        if (!pref.getBoolean("discoverable", false)) {
            notDiscoverable();
        }
        else {
            discoverable();
        }

    }

    private void discoverable() {
        Button enable = (Button) findViewById(R.id.enable_discovery);
        if (enable != null) {
            enable.setVisibility(View.GONE);
        }

        initButtons();
        initData();
        initFlingContainer();
    }

    private void notDiscoverable() {
        final Button enable = (Button) findViewById(R.id.enable_discovery);
        if (enable != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                final Transition sharedElementEnterTransition = getWindow().getSharedElementEnterTransition();
                sharedElementEnterTransition.addListener(new Transition.TransitionListener() {
                    @Override
                    public void onTransitionStart(Transition transition) {}
                    @Override
                    public void onTransitionEnd(Transition transition) {
                        enable.setVisibility(View.VISIBLE);
                        enable.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                isProfileDataSet();
                            }
                        });
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            sharedElementEnterTransition.removeListener(this);
                        }
                    }
                    @Override
                    public void onTransitionCancel(Transition transition) {}
                    @Override
                    public void onTransitionPause(Transition transition) {}
                    @Override
                    public void onTransitionResume(Transition transition) {}
                });
            }
        }
    }

    private void isProfileDataSet() {
        SharedPreferences pref = getSharedPreferences("data", MODE_PRIVATE);
        if (pref.getBoolean("profile_created", false)) {
            discoverable();
            addTransition(noButton, 0);
            addTransition(chatButton, 1);
            addTransition(yesButton, 2);
            Button enable = (Button) findViewById(R.id.enable_discovery);
            if (enable != null) {
                enable.setVisibility(View.INVISIBLE);
            }
        }
        else {
            createProfile();
        }
    }

    private void createProfile() {
        Button enable = (Button) findViewById(R.id.enable_discovery);
        Intent intent = new Intent(MatchActivity.this, CreateProfileActivity.class);
        Pair<View, String> p3 = Pair.create((View) enable, "button");

        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, p3);
        startActivityForResult(intent,CREATE_PROFILE_REQUEST, options.toBundle());
    }

    private void initFlingContainer() {
        //handle the cards
        flingContainer = (SwipeFlingAdapterView) findViewById(R.id.swipecards);
        adapter = new CardAdapter(this, users);
        flingContainer.setAdapter(adapter);

        //handle card swiping
        flingContainer.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {
            @Override
            public void removeFirstObjectInAdapter() {
                users.remove(0);
                adapter.notifyDataSetChanged();
                if (users.isEmpty() && (!userQueue.isEmpty() || !potentialUserQueue.isEmpty())) {
                    showProgress(true);
                }
                else if (userQueue.isEmpty() && users.isEmpty() && potentialUserQueue.isEmpty()){
                    showEmptyList(true);
                }
            }

            @Override
            public void onLeftCardExit(Object dataObject) {
                final Animation shake = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.wobble);
                if (flung) { noButton.startAnimation(shake); }
                flung = false;
            }

            @Override
            public void onRightCardExit(Object dataObject) {
                final Animation shake = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.wobble);
                if (flung) { yesButton.startAnimation(shake); }
                flung = false;
            }

            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {
                Log.d("onAdapterAboutToEmpty", Boolean.toString(userQueue.isEmpty()));
                if (!userQueue.isEmpty()) {
                    Log.d("onAdapterAboutToEmpty", "poll");
                    getPendingUserName(userQueue.poll());
                }
            }

            @Override
            public void onScroll(float f) {
                if (f == 1.0 || f == -1.0) {
                    flung = true;
                }
            }
        });
        //handle tapping the cards
        flingContainer.setOnItemClickListener(new SwipeFlingAdapterView.OnItemClickListener() {
            @Override
            public void onItemClicked(int position, Object dataObject) {
                Intent intent = new Intent(MatchActivity.this, ProfileActivity.class);
                intent.putExtra(PERSON_NAME, users.get(position).getUID());
                startActivityForResult(intent, VIEW_PROFILE_REQUEST);
                overridePendingTransition(R.anim.slide_in, R.anim.fade_out);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == VIEW_PROFILE_REQUEST) {
            if (resultCode == ProfileActivity.RESULT_YES){
                //swipe right
                flingContainer.getTopCardListener().selectRight();
            }
            else if (resultCode == ProfileActivity.RESULT_NO ) {
                //swipe left
                flingContainer.getTopCardListener().selectLeft();
            }
        }
        else if (requestCode == CREATE_PROFILE_REQUEST) {
            if (resultCode != RESULT_CANCELED) {
                discoverable();
                addTransition(noButton, 0);
                addTransition(chatButton, 1);
                addTransition(yesButton, 2);
            }
        }
    }

    private void initData(){
        showProgress(true);
        geoQuery = geoFire.queryAtLocation(new GeoLocation(37.7832, -122.4056), 220);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                Log.d("key entered", key);
                if (!key.equals(mUser.getUid())) {
                    isPendingUserValid(key);
                    potentialUserQueue.add(key);
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if (userQueue.isEmpty() && potentialUserQueue.isEmpty() && users.isEmpty()) {
                    Log.d("userQueue", "isEmpty");
                    showEmptyList(true);
                }
                else if (!userQueue.isEmpty()){
                    Log.d("onGeoQueryReady", "poll");
                    getPendingUserName(userQueue.poll());
                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void showEmptyList(boolean show) {
        TextView emptyList = (TextView) findViewById(R.id.match_empty);
        showProgress(false);
        if (show && emptyList != null) {
            emptyList.setVisibility(View.VISIBLE);
        }
        else if (emptyList != null){
            emptyList.setVisibility(View.GONE);
        }
    }

    private void isPendingUserValid(final String key) {
        Query query = rootRef.child("users").child(key).child("buckits").orderByValue().limitToFirst(1).startAt(item);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //if its not null, then the user is valid
                if (dataSnapshot.getValue() != null) {
                    Log.d(dataSnapshot.getKey(), dataSnapshot.getValue().toString());
                    User pendingUser = new User();
                    pendingUser.setUID(key);
                    Log.d("pendingUser", "VALID");
                    userQueue.add(pendingUser);
                    if (users.isEmpty()) {
                        getPendingUserName(userQueue.poll());
                    }
                }
                potentialUserQueue.removeFirst();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getPendingUserName(final User pendingUser) {
        Log.d("path", "getPendingUserName");
        rootRef.child("users").child(pendingUser.getUID()).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    pendingUser.setName(dataSnapshot.getValue().toString());
                    getPendingUserAge(pendingUser);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getPendingUserAge(final User pendingUser) {
        Log.d("path", "getPendingUserAge");
        rootRef.child("users").child(pendingUser.getUID()).child("age").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    pendingUser.setAge(Integer.parseInt(dataSnapshot.getValue().toString()));
                    getPendingUserPic(pendingUser);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getPendingUserPic(final User pendingUser) {
        Log.d("path", "getPendingUserPic");
        rootRef.child("users").child(pendingUser.getUID()).child("profilePicture").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    pendingUser.setProfilePicture(dataSnapshot.getValue().toString());
                    users.add(pendingUser);
                    adapter.notifyDataSetChanged();
                    showProgress(false);
                    showEmptyList(false);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        if (geoQuery != null)
            geoQuery.removeAllListeners();
        showProgress(false);
        showEmptyList(false);
        Button enable = (Button) findViewById(R.id.enable_discovery);

        if (enable != null) {
            enable.setVisibility(View.INVISIBLE);
        }
        if (yesButton != null && chatButton != null && noButton != null)
            addExitTransitions();
        else
            super.onBackPressed();
    }

    private void addExitTransitions() {
        Animation animation1 = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.button_slide_down);
        Animation animation2 = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.button_slide_down);
        Animation animation3 = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.button_slide_down);

        yesButton.startAnimation(animation1);
        chatButton.startAnimation(animation2);
        noButton.startAnimation(animation3);

        animation1.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                yesButton.setVisibility(View.INVISIBLE);
                noButton.setVisibility(View.INVISIBLE);
                chatButton.setVisibility(View.INVISIBLE);
                MatchActivity.super.onBackPressed();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

    }

    private void initButtons() {
        yesButton = (ImageButton) findViewById(R.id.yes_button);
        noButton = (ImageButton) findViewById(R.id.no_button);
        chatButton = (ImageButton) findViewById(R.id.chat_button);
        addAnimation(yesButton);
        addAnimation(noButton);
        addAnimation(chatButton);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final Transition sharedElementEnterTransition = getWindow().getSharedElementEnterTransition();
            sharedElementEnterTransition.addListener(new Transition.TransitionListener() {
                @Override
                public void onTransitionStart(Transition transition) {}
                @Override
                public void onTransitionEnd(Transition transition) {
                    addTransition(noButton, 0);
                    addTransition(chatButton, 1);
                    addTransition(yesButton, 2);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        sharedElementEnterTransition.removeListener(this);
                    }
                }
                @Override
                public void onTransitionCancel(Transition transition) {}
                @Override
                public void onTransitionPause(Transition transition) {}
                @Override
                public void onTransitionResume(Transition transition) {}
            });
        }
        else {
            yesButton.setVisibility(View.VISIBLE);
            noButton.setVisibility(View.VISIBLE);
            chatButton.setVisibility(View.VISIBLE);
        }
    }

    private void addTransition(ImageButton button, int order) {
        Animation anim = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.button_slide_up);
        anim.setFillAfter(true);
        anim.setStartOffset(order * 50);

        button.startAnimation(anim);
        button.setVisibility(View.VISIBLE);
    }

    private void addAnimation(final ImageButton button) {
        final Animation scaleDown = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.scale_down);
        scaleDown.setFillAfter(true);
        final Animation scaleUp = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.scale_up);
        scaleUp.setFillAfter(true);

        button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        button.startAnimation(scaleDown);
                        break;
                    case MotionEvent.ACTION_UP:
                        button.startAnimation(scaleUp);
                        handleButtonPress(button);
                        break;
                }
                return false;
            }
        });
    }

    private void handleButtonPress(ImageButton button) {
        if (button == chatButton) {
            Intent intent = new Intent(getApplicationContext(), ChatPage.class);
            intent.putExtra(PERSON_NAME, users.get(flingContainer.getFirstVisiblePosition()).getName());
            intent.putExtra("uid", users.get(flingContainer.getFirstVisiblePosition()).getUID());
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in, R.anim.fade_out);
        }
        else if (button == yesButton && !users.isEmpty()) {
            flingContainer.getTopCardListener().selectRight();
        }
        else if (button == noButton && !users.isEmpty()) {
            flingContainer.getTopCardListener().selectLeft();
        }
    }

    private void checkboxListener() {
        CheckBox checkBox = (CheckBox) findViewById(R.id.checkbox);
        if (checkBox != null) {
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    FragmentManager fm = getSupportFragmentManager();
                    Checked check = Checked.newInstance(getIntent().getStringExtra(BuckitList.MATCH_ITEM), true);
                    check.show(fm, "hello");
                }
            });
        }
    }

    private void showProgress(final boolean show) {
        if (show) {
            Log.d("mProgressView", "visible");
            Animation rotation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate);
            rotation.setRepeatCount(Animation.INFINITE);
            mProgressView.startAnimation(rotation);
            mProgressView.setVisibility(View.VISIBLE);
        } else {
            Log.d("mProgressView", "invisible");
            mProgressView.setVisibility(View.GONE);
            mProgressView.clearAnimation();
        }
    }
}
