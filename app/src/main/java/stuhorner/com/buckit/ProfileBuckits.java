package stuhorner.com.buckit;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Stu on 3/23/2016.
 */
public class ProfileBuckits extends Fragment {
    private List<String> bucket_items = new LinkedList<>();
    private DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users");
    private FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
    private ProfileBuckitsAdapter adapter;
    private ImageView mProgressView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.profile_buckits, container, false);
        initRecyclerView(view);
        mProgressView = (ImageView) view.findViewById(R.id.logo);
        initData();

        return view;
    }

    private void initRecyclerView(View view) {
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.p_recycler);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity().getBaseContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        adapter = new ProfileBuckitsAdapter(bucket_items);
        recyclerView.setAdapter(adapter);

        ItemClickSupport.addTo(recyclerView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                Snackbar.make(v, String.format(getResources().getString(R.string.add_confirm), bucket_items.get(position)), Snackbar.LENGTH_SHORT).show();
                v.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.scale_down_up));
                userRef.child(mUser.getUid()).child("buckits").child(bucket_items.get(position)).setValue(ServerValue.TIMESTAMP);
            }
        });
    }

    private void initData(){
        showProgress(true);
        String UID = getActivity().getIntent().getStringExtra("uid");
        userRef.child(UID).child("buckits").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        bucket_items.add(data.getKey());
                        adapter.notifyDataSetChanged();
                    }
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
