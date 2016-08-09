package stuhorner.com.buckit;

import android.app.Activity;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.content.Intent;
import android.os.Bundle;
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
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;

import java.util.HashSet;
import java.util.LinkedList;

public class MatchActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private LinkedList<User> users = new LinkedList<>();
    private LinkedList<User> userQueue = new LinkedList<>();
    private HashSet<String> loadingQueue = new HashSet<>();
    private ImageButton nextButton;
    private ImageButton chatButton;
    private ImageView nextCircle, chatCircle;
    private Button enable, enable_location;
    private ImageView mProgressView;
    private TextView emptyList;
    private SwipeFlingAdapterView flingContainer;
    private CardAdapter adapter;
    private boolean flung;
    private String item;


    public final static int CREATE_PROFILE_REQUEST = 0;
    public final static int RESULT_CHECKED = 1;
    private final static int REQUEST_CHECK_SETTINGS = 3;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;

    //Firebase references
    private DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference geoRef = FirebaseDatabase.getInstance().getReference("geoFire");
    private GeoFire geoFire = new GeoFire(geoRef);
    private GeoQuery geoQuery;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser mUser = mAuth.getCurrentUser();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match);

        googleApiClient = new GoogleApiClient.Builder(getApplicationContext(), this, this).addApi(LocationServices.API).build();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (toolbar != null) {
            toolbar.setNavigationIcon(R.drawable.ic_back);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }
        checkboxListener();

        enable = (Button) findViewById(R.id.enable_discovery);
        enable_location = (Button) findViewById(R.id.enable_location);
        mProgressView = (ImageView) findViewById(R.id.match_logo);
        emptyList = (TextView) findViewById(R.id.match_empty);
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
        Log.d("discoverable", "hiding button");
        if (enable != null) {
            enable.setVisibility(View.GONE);
        }

        initButtons();
        initFlingContainer();
        googleApiClient.connect();

        rootRef.child("users").child(mUser.getUid()).child("discoverable").setValue("1");
        SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
        editor.putBoolean("discoverable", true);
        editor.apply();
    }

    private void notDiscoverable() {
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
            addTransition(nextButton, 1);
            addTransition(chatButton, 0);
            addTransition(chatCircle, 0);
            addTransition(nextCircle, 1);
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
        Intent intent = new Intent(MatchActivity.this, CreateProfileActivity.class);
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, enable, "button");
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
                if (userQueue.isEmpty() && users.isEmpty() && loadingQueue.isEmpty()) {
                    showEmptyList(true);
                }
                else if ((!userQueue.isEmpty() || !loadingQueue.isEmpty()) && users.isEmpty()){
                    showProgress(true);
                }
                adapter.notifyDataSetChanged();

            }

            @Override
            public void onLeftCardExit(Object dataObject) {
                final Animation shake = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.wobble);
                if (flung) { nextButton.startAnimation(shake); }
                flung = false;
            }

            @Override
            public void onRightCardExit(Object dataObject) {
                final Animation shake = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.wobble);
                if (flung) { nextButton.startAnimation(shake); }
                flung = false;
            }

            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {
                Log.d("onAdapterAboutToEmpty", Boolean.toString(userQueue.isEmpty()));
                if (!userQueue.isEmpty()) {
                    Log.d("onAdapterAboutToEmpty", "poll");
                    isPendingUserValid(userQueue.poll());
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
                intent.putExtra("name", users.get(position).getName());
                intent.putExtra("uid", users.get(position).getUID());
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in, R.anim.fade_out);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == CREATE_PROFILE_REQUEST) {
            if (resultCode != RESULT_CANCELED) {
                discoverable();
                addTransition(nextButton, 1);
                addTransition(chatButton, 0);
                addTransition(nextCircle, 1);
                addTransition(chatCircle, 0);
            }
        }
        else if (requestCode == REQUEST_CHECK_SETTINGS) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    Log.d("location", "onActivityResultOK");
                    showProgress(true);
                    queryLocation();
                    break;
                case Activity.RESULT_CANCELED:
                    Log.d("location", "onActivityResultCANCELED");
                    onPermissionDenied();
                    break;
            }
        }
    }

    public void initData(Location location){
        showProgress(true);
        geoFire.setLocation(mUser.getUid(), new GeoLocation(location.getLatitude(), location.getLongitude()));
        geoQuery = geoFire.queryAtLocation(new GeoLocation(location.getLatitude(), location.getLongitude()), 220);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                Log.d("key entered", key);
                if (!key.equals(mUser.getUid())) {
                    userQueue.add(new User(key));
                    loadingQueue.add(key);
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
                if (!userQueue.isEmpty()) {
                    isPendingUserValid(userQueue.poll());
                }
                else if (users.isEmpty()){
                    showEmptyList(true);
                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void showEmptyList(boolean show) {
        showProgress(false);
        if (show && emptyList != null) {
            emptyList.setVisibility(View.VISIBLE);
        }
        else if (emptyList != null){
            emptyList.setVisibility(View.GONE);
        }
    }

    private void isPendingUserValid(final User user) {
        Query query = rootRef.child("users").child(user.getUID()).child("buckits").orderByKey().limitToFirst(1).startAt(item);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("isPendingUserValid", user.getUID());
                //if its not null, then the user is valid
                if (dataSnapshot.getValue() != null) {
                    boolean valid = false;
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        if (data.getKey().equals(item)) {
                            getPendingUserDiscoverable(user);
                            valid = true;
                        }
                    }
                    if (!valid) {
                        loadingQueue.remove(user.getUID());
                        if (!userQueue.isEmpty()) {
                            isPendingUserValid(userQueue.poll());
                        }
                        else if (users.isEmpty() && loadingQueue.isEmpty()){
                            showEmptyList(true);
                        }
                    }
                }
                else {
                    loadingQueue.remove(user.getUID());
                    if (!userQueue.isEmpty()) {
                        isPendingUserValid(userQueue.poll());
                    } else if (users.isEmpty() && loadingQueue.isEmpty()) {
                        showEmptyList(true);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getPendingUserDiscoverable(final User pendingUser) {
        Log.d("path", "getPendingUserDiscoverable");
        rootRef.child("users").child(pendingUser.getUID()).child("discoverable").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null && dataSnapshot.getValue().equals("1")) {
                    pendingUser.setName(dataSnapshot.getValue().toString());
                    getPendingUserName(pendingUser);
                }

                else {
                    loadingQueue.remove(pendingUser.getUID());
                    if (!userQueue.isEmpty()) {
                        isPendingUserValid(userQueue.poll());
                    }
                    else if (users.isEmpty() && loadingQueue.isEmpty()){
                        showEmptyList(true);
                    }
                }
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
                else {
                    loadingQueue.remove(pendingUser.getUID());
                    if (!userQueue.isEmpty()) {
                        isPendingUserValid(userQueue.poll());
                    }
                    else if (users.isEmpty() && loadingQueue.isEmpty()){
                        showEmptyList(true);
                    }
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
                else {
                    loadingQueue.remove(pendingUser.getUID());
                    if (!userQueue.isEmpty()) {
                        isPendingUserValid(userQueue.poll());
                    }
                    else if (users.isEmpty() && loadingQueue.isEmpty()){
                        showEmptyList(true);
                    }
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
                    loadingQueue.remove(pendingUser.getUID());
                }
                else {
                    loadingQueue.remove(pendingUser.getUID());
                    if (!userQueue.isEmpty()) {
                        isPendingUserValid(userQueue.poll());
                    }
                    else if (users.isEmpty() && loadingQueue.isEmpty()){
                        showEmptyList(true);
                    }
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

        if (enable != null) {
            enable.setVisibility(View.GONE);
        }
        if (nextButton != null && chatButton != null)
            addExitTransitions();
        else
            super.onBackPressed();
    }

    private void addExitTransitions() {
        Animation exit = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.button_slide_down);

        nextButton.startAnimation(exit);
        chatButton.startAnimation(exit);
        nextCircle.startAnimation(exit);
        chatCircle.startAnimation(exit);

        exit.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                nextButton.setVisibility(View.INVISIBLE);
                chatButton.setVisibility(View.INVISIBLE);
                nextCircle.setVisibility(View.INVISIBLE);
                chatCircle.setVisibility(View.INVISIBLE);
                MatchActivity.super.onBackPressed();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

    }

    private void initButtons() {
        nextButton = (ImageButton) findViewById(R.id.next_button);
        chatButton = (ImageButton) findViewById(R.id.chat_button);
        nextCircle = (ImageView) findViewById(R.id.next_circle);
        chatCircle = (ImageView) findViewById(R.id.chat_circle);
        addAnimation(nextButton);
        addAnimation(chatButton);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final Transition sharedElementEnterTransition = getWindow().getSharedElementEnterTransition();
            sharedElementEnterTransition.addListener(new Transition.TransitionListener() {
                @Override
                public void onTransitionStart(Transition transition) {}
                @Override
                public void onTransitionEnd(Transition transition) {
                    addTransition(nextButton, 1);
                    addTransition(nextCircle, 1);
                    addTransition(chatCircle, 0);
                    addTransition(chatButton, 0);

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
            nextButton.setVisibility(View.VISIBLE);
            chatButton.setVisibility(View.VISIBLE);
            chatCircle.setVisibility(View.VISIBLE);
            nextCircle.setVisibility(View.VISIBLE);
        }
    }

    private void addTransition(View button, int order) {
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
        if (button == chatButton && !users.isEmpty()) {
            flingContainer.getTopCardListener().selectLeft();
            Intent intent = new Intent(getApplicationContext(), ChatPage.class);
            intent.putExtra("name", users.get(flingContainer.getFirstVisiblePosition()).getName());
            intent.putExtra("uid", users.get(flingContainer.getFirstVisiblePosition()).getUID());
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in, R.anim.fade_out);
        }
        else if (button == nextButton && !users.isEmpty()) {
            flingContainer.getTopCardListener().selectLeft();
        }
    }

    private void checkboxListener() {
        CheckBox checkBox = (CheckBox) findViewById(R.id.checkbox);
        if (checkBox != null) {
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    Intent data = new Intent();
                    data.putExtra(BuckitList.MATCH_ITEM, getIntent().getStringExtra(BuckitList.MATCH_ITEM));
                    setResult(RESULT_CHECKED, data);
                    finish();
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

    @Override
    public void onStop() {
        if (googleApiClient != null) {
            googleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d("location", "onConnected");
        if (isLocationPermissionGranted())  {
            try {
                Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
                if (lastLocation != null) {
                    Log.d("location", lastLocation.toString());
                    initData(lastLocation);
                }
                else {
                    settingsRequest();
                }
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult c) {
        Log.d("location", "onConnectionFailed");
    }

    @Override
    public void onConnectionSuspended(int c) {
    }

    public void settingsRequest() {
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                .setNumUpdates(1)
                .setInterval(0)
                .setExpirationDuration(60 * 1000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates state = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.d("location", "success");
                        queryLocation();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        Log.d("location", "dialog");
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(MatchActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        Log.d("location", "nope");
                        break;
                }
            }
        });
    }

    private void queryLocation() {
        Log.d("location", "getLocation");
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    Log.d("location", location.toString());
                    initData(location);
                }
            });
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    public  boolean isLocationPermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v("location","Permission is granted");
                return true;
            } else {
                Log.v("location","Permission is revoked");
                requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CHECK_SETTINGS);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v("location","Permission is granted");
            return true;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        Log.d("location", "onRequestPermissionsResult");
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //on connected
                    Log.d("location", "permission granted");
                    settingsRequest();

                } else {
                    Snackbar.make(getWindow().getDecorView(), getString(R.string.permission_denied_location), Snackbar.LENGTH_LONG).show();
                    onPermissionDenied();
                }
            }
        }
    }

    private void onPermissionDenied() {
        showProgress(false);
        enable_location.setVisibility(View.VISIBLE);
        googleApiClient.disconnect();
        enable_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enable_location.setVisibility(View.GONE);
                googleApiClient.connect();
                showProgress(true);
            }
        });
    }
}
