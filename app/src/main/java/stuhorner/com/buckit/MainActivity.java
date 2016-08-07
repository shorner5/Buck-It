package stuhorner.com.buckit;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;

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

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private ViewPager viewPager;
    private TabLayout tabLayout;
    public static boolean fabVisible = true;

    private final static int LOCATION_REQUEST = 2;
    private final static int REQUEST_CHECK_SETTINGS = 3;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private LocationReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        googleApiClient = new GoogleApiClient.Builder(getApplicationContext(), this, this).addApi(LocationServices.API).build();

        //initialize the toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(R.string.buckit_uncomp);

        //Create tabs and set name and icon
        initTabs(fab, toolbar);

        //initialize the floating action button
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, Inspire.class);
                    startActivity(intent);
                }
            });
        }
    }

    private void initTabs(final FloatingActionButton fab, final Toolbar toolbar) {
        final AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.appbar);

        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        if (tabLayout != null) {
            tabLayout.addTab(tabLayout.newTab().setContentDescription(R.string.buckit_uncomp).setIcon(R.drawable.ic_list));
            tabLayout.addTab(tabLayout.newTab().setContentDescription(R.string.messages).setIcon(R.drawable.ic_chat));
            tabLayout.addTab(tabLayout.newTab().setContentDescription(R.string.similar).setIcon(R.drawable.ic_profile));
            tabLayout.addTab(tabLayout.newTab().setContentDescription(R.string.social).setIcon(R.drawable.ic_social));
            tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

            //Color tab icons
            tabLayout.getTabAt(0).getIcon().setColorFilter(getResources().getColor(R.color.accent_color_light), PorterDuff.Mode.MULTIPLY);
            for (int i = 1; i < tabLayout.getTabCount(); i++) {
                tabLayout.getTabAt(i).getIcon().setColorFilter(Color.DKGRAY, PorterDuff.Mode.MULTIPLY);
            }
        }

        //Create the ViewPager
        viewPager = (ViewPager) findViewById(R.id.pager);
        PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);

        //Handle scrolling tabs
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout) {
            @Override
            public void onPageScrollStateChanged(int state) {
                if (appBarLayout != null) {
                    appBarLayout.setExpanded(true, true);
                }
            }
        });
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                tab.getIcon().setColorFilter(getResources().getColor(R.color.accent_color_light), PorterDuff.Mode.MULTIPLY);
                toolbar.setTitle(tab.getContentDescription());

                if (tab.getPosition() == 0) {
                    fabVisible = true;
                    fab.setVisibility(View.VISIBLE);
                    fab.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.button_slide_up));
                } else if (fabVisible) {
                    fabVisible = false;
                    fab.setVisibility(View.INVISIBLE);
                    fab.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.button_slide_down));
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                if (tab.getIcon() != null)
                    tab.getIcon().setColorFilter(Color.DKGRAY, PorterDuff.Mode.MULTIPLY);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        menu.getItem(0).getIcon().setColorFilter(getResources().getColor(android.R.color.darker_gray), PorterDuff.Mode.SRC_ATOP);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intent);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (tabLayout.getSelectedTabPosition() != 0) {
            tabLayout.smoothScrollTo(0, 0);
            viewPager.setCurrentItem(0);
        }
        else {
            super.onBackPressed();
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
                requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_REQUEST);
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
            case LOCATION_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //on connected
                    Log.d("location", "permission granted");
                    settingsRequest();

                } else {
                    Snackbar.make(getWindow().getDecorView(), getString(R.string.permission_denied_location), Snackbar.LENGTH_LONG).show();
                    receiver.permissionDenied();
                }
            }
        }
    }

    public void startLocationQuery(LocationReceiver receiver) {
        this.receiver = receiver;
        if (googleApiClient != null) {
            googleApiClient.connect();
        }
    }

    @Override
    public void onStop() {
        if (googleApiClient != null) {
            googleApiClient.disconnect();
        }
        super.onStop();
    }

    public void disconnectGoogleApiClient() {
        if (googleApiClient!= null)
            googleApiClient.disconnect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d("location", "onConnected");
        if (isLocationPermissionGranted())  {
            try {
                Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
                if (lastLocation != null) {
                    Log.d("location", lastLocation.toString());
                    receiver.initData(lastLocation);
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
                            status.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
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
                    receiver.initData(location);
                }
            });
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("mainActivity", "onActivityResult");
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    Log.d("location", "onActivityResultOK");
                    queryLocation();
                    break;
                case Activity.RESULT_CANCELED:
                    Log.d("location", "onActivityResultCANCELED");
                    receiver.permissionDenied();
                    break;
            }
        }
    }
}