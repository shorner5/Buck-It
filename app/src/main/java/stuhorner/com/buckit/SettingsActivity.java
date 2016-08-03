package stuhorner.com.buckit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SettingsActivity extends AppCompatActivity {
    //Firebase references
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser mUser = mAuth.getCurrentUser();
    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users/" + mUser.getUid());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        initToolbar();
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

    private void initSearchSettings() {
        final SharedPreferences pref = getSharedPreferences("data", MODE_PRIVATE);
        final SharedPreferences.Editor editor = pref.edit();
        final Switch discoverable = (Switch) findViewById(R.id.settings_discoverable);
        if (discoverable != null) {
            discoverable.setChecked(pref.getBoolean("discoverable", false));
            discoverable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (pref.getBoolean("profile_created", false) || !isChecked) {
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
        final SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
        final Switch discoverable = (Switch) findViewById(R.id.settings_discoverable);
        if (discoverable != null) discoverable.setChecked(true);
        editor.putBoolean("discoverable", true);
        editor.apply();
        userRef.child("discoverable").setValue("1");
        userRef.child("profile_created").setValue("1");
    }
}
