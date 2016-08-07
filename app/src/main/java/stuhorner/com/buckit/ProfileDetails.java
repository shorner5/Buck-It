package stuhorner.com.buckit;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by Stu on 3/23/2016.
 */
public class ProfileDetails extends Fragment {
    private DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users");
    private FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
    private TextView textView;
    private EditText editText;
    private ImageView mProgressView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.profile_details, container, false);
        setHasOptionsMenu(true);
        textView = (TextView) view.findViewById(R.id.p_text);
        mProgressView = (ImageView) view.findViewById(R.id.logo);
        editText = (EditText) view.findViewById(R.id.p_edittext);
        initEditText();
        initData();
        textView.setMovementMethod(new ScrollingMovementMethod());

        return view;
    }
    private void initData(){
        showProgress(true);
        userRef.child(getActivity().getIntent().getStringExtra("uid")).child("profileText").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (isAdded()) {
                    ((ProfileActivity)getActivity()).setIsProfileLoaded(true);
                    if (dataSnapshot.getValue() != null && textView != null) {
                        textView.setText(dataSnapshot.getValue().toString());
                    } else if (textView != null) {
                        textView.setText(getString(R.string.no_profile_text));
                    }
                    showProgress(false);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void initEditText() {
        editText.setLines(30);
        editText.setHorizontallyScrolling(false);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                saveText();
                ((ProfileActivity)getActivity()).setEditing(false);
                getActivity().invalidateOptionsMenu();
                return true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (((ProfileActivity)getActivity()).isProfileLoaded()) {
            switch (item.getItemId()) {
                case R.id.edit:
                    editText.setText(textView.getText());
                    textView.setVisibility(View.INVISIBLE);
                    editText.setVisibility(View.VISIBLE);
                    editText.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
                    break;
                case R.id.action_done:
                    saveText();
                    break;
            }
            return super.onOptionsItemSelected(item);
        }
        else return false;
    }

    private void saveText() {
        textView.setText(editText.getText().toString());
        textView.setVisibility(View.VISIBLE);
        editText.setVisibility(View.INVISIBLE);
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        userRef.child(mUser.getUid()).child("profileText").setValue(textView.getText().toString());
    }

    private void showProgress(final boolean show) {
        if (show) {
            Log.d("mProgressView", "visible");
            Animation rotation = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate);
            rotation.setRepeatCount(Animation.INFINITE);
            mProgressView.startAnimation(rotation);
            mProgressView.setVisibility(View.VISIBLE);
        } else {
            Log.d("mProgressView", "invisible");
            mProgressView.setVisibility(View.INVISIBLE);
            mProgressView.clearAnimation();
        }
    }
}
