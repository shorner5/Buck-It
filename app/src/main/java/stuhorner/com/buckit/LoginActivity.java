package stuhorner.com.buckit;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends Activity {
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference rootRef = database.getReference();

    // UI references.
    private EditText mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private Button mEmailSignInButton;
    private Animation rotation;

    private static final int MINIMUM_PASSWORD_LENGTH = 6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mEmailView = (EditText) findViewById(R.id.email);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                    attemptLogin();
                    return true;
            }
        });

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    showProgress(false);
                    Intent MainActivityIntent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(MainActivityIntent);
                    finish();
                } else {
                    Log.d("firebaseAuth", "signed out");
                }
            }
        };

        mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mEmailSignInButton.getText().toString().equals(getString(R.string.action_sign_in))) {
                    attemptLogin();
                }
                else {
                    EditText confirm = (EditText) findViewById(R.id.confirm_password);
                    if (confirm.getText().toString().equals(mPasswordView.getText().toString())){
                        Log.d("calling", "createNewUser");
                        createNewUser(mEmailView.getText().toString(), mPasswordView.getText().toString());
                    }
                    else {
                        confirm.setError(getString(R.string.error_incorrect_password));
                    }
                }
            }
        });

        mProgressView = findViewById(R.id.logo);
        rotation = AnimationUtils.loadAnimation(this, R.anim.rotate);
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password) || !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            authenticateUser(email, password);
        }
    }

    private boolean isEmailValid(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= MINIMUM_PASSWORD_LENGTH;
    }

    /**
     * Shows the progress UI
     */
    private void showProgress(final boolean show) {
        if (show) {
            rotation.setRepeatCount(Animation.INFINITE);
            mProgressView.startAnimation(rotation);
        }
        else {
            rotation.setRepeatCount(0);
        }

    }

    private void authenticateUser(final String email, final String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        showProgress(false);
                        if (!task.isSuccessful()) {
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                mPasswordView.setError(getString(R.string.error_incorrect_password));
                                mPasswordView.requestFocus();
                            } catch (FirebaseAuthUserCollisionException e) {
                                //incorrect password
                                mPasswordView.setError(getString(R.string.error_incorrect_password));
                                mPasswordView.requestFocus();
                            } catch (FirebaseAuthInvalidUserException e) {
                                createConfirmation(email, password);
                            } catch (Exception e) {
                                e.getMessage();
                            }
                        }
                    }
                });
    }

    private void createConfirmation(final String email, final String password) {
        final EditText confirm = (EditText) findViewById(R.id.confirm_password);
        confirm.setVisibility(View.VISIBLE);
        confirm.requestFocus();
        mEmailView.setVisibility(View.GONE);
        mEmailSignInButton.setText(getString(R.string.register));
        confirm.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (confirm.getText().toString().equals(mPasswordView.getText().toString())){
                    Log.d("calling", "createNewUser");
                    createNewUser(email, password);
                    return true;
                }
                else {
                    confirm.setError(getString(R.string.error_non_match_password));
                    confirm.requestFocus();
                    return false;
                }
            }
        });
    }

    private void createNewUser(String email, String password) {
        showProgress(true);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d("task", "completed");
                        if (mAuth.getCurrentUser() != null) {
                            Log.d("new user created", mAuth.getCurrentUser().getUid());
                            rootRef.child("users_index").push().setValue(mAuth.getCurrentUser().getUid());
                            rootRef.child("users").child(mAuth.getCurrentUser().getUid()).child("email").setValue(mAuth.getCurrentUser().getEmail());
                        }
                        if (!task.isSuccessful()) {
                            showProgress(false);
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthWeakPasswordException e) {
                                mPasswordView.setError(getString(R.string.error_invalid_password));
                                mPasswordView.requestFocus();
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                mEmailView.setError(getString(R.string.error_invalid_email));
                                mEmailView.setVisibility(View.VISIBLE);
                                mEmailView.requestFocus();
                            } catch (FirebaseAuthUserCollisionException e) {
                                //incorrect password
                                mEmailView.setError(getString(R.string.error_invalid_email));
                                mEmailView.setVisibility(View.VISIBLE);
                                mEmailView.requestFocus();
                            } catch (Exception e) {
                                e.getMessage();
                            }
                        }
                    }
                });
    }

    @Override
    public void onBackPressed() {
        if (mEmailView.getVisibility() == View.GONE) {
            mEmailView.setVisibility(View.VISIBLE);
        }
    }
}
