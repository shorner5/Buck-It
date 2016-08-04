package stuhorner.com.buckit;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

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
    private TextView textView;
    private ImageView mProgressView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.profile_details, container, false);
        textView = (TextView) view.findViewById(R.id.p_text);
        mProgressView = (ImageView) view.findViewById(R.id.logo);
        initData();
        textView.setMovementMethod(new ScrollingMovementMethod());

        return view;
    }
    private void initData(){
        showProgress(true);
        userRef.child("profileText").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null && textView != null) {
                    textView.setText(dataSnapshot.getValue().toString());
                }
                else if (textView != null) {
                    textView.setText(getString(R.string.no_profile_text));
                }
                showProgress(false);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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
