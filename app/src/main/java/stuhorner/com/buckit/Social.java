package stuhorner.com.buckit;

import android.os.Bundle;
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
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * Created by Stu on 12/14/2015.
 */
public class Social extends Fragment {
    private LinkedList<SocialPostHolder> social_items = new LinkedList<>();
    private HashSet<String> userQueue = new HashSet<>();
    private SocialRVAdapter adapter;
    private DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
    private FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
    private ImageView mProgressView;
    private TextView emptyList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.social, container,false);
        initRecyclerView(v);
        initData();

        return v;
    }

    private void initData() {
        Query query = rootRef.child("users").child(mUser.getUid()).child("social").orderByValue().limitToFirst(25);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    initSocialItems(data.getKey());
                    userQueue.add(data.getKey());
                }
                initSocialItems(mUser.getUid());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void initSocialItems(final String UID) {
        rootRef.child("social").child(UID).
                addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        Log.d("title", data.getKey());
                        SocialPostHolder post = new SocialPostHolder();
                        post.setTitle(data.getKey());
                        post.setUID(UID);
                        if (data.child("time").getValue() != null) {
                            post.setTime(Long.parseLong(data.child("time").getValue().toString()));
                            Log.d("time", data.child("time").getValue().toString());
                        }
                        if (data.child("story").getValue() != null){
                            post.setStory(data.child("story").getValue().toString());
                            Log.d("story", data.child("story").getValue().toString());
                        }
                        if (data.child("img").getValue() != null){
                            post.setImg(data.child("img").getValue().toString());
                            Log.d("img", data.child("img").getValue().toString());
                        }
                        if (data.child("likes").getValue() != null) {
                            post.setLikes(Integer.parseInt(data.child("likes").getValue().toString()));
                            Log.d("likes", data.child("likes").getValue().toString());
                        }
                        if (data.child("likedBy").child(mUser.getUid()).getValue() != null) {
                            post.setLiked(true);
                        }
                        else {
                            post.setLiked(false);
                        }
                        social_items.add(post);
                    }
                    Collections.sort(social_items, new Comparator<SocialPostHolder>() {
                        @Override
                        public int compare(SocialPostHolder o1, SocialPostHolder o2) {
                            if (o1.getTime() < o2.getTime())
                                return 1;
                            else
                                return -1;
                        }
                    });
                    adapter.notifyDataSetChanged();
                    Log.d("adapter", "notified datasetchanged");
                    showEmptyList(false);
                }
                else {
                    userQueue.remove(UID);
                    if (userQueue.isEmpty()) {
                        showEmptyList(true);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void initRecyclerView(View v) {
        RecyclerView mRecyclerView = (RecyclerView) v.findViewById(R.id.social_rv);
        mProgressView = (ImageView) v.findViewById(R.id.logo);
        emptyList = (TextView) v.findViewById(R.id.list_empty);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new SocialRVAdapter(getActivity(), social_items);
        mRecyclerView.setAdapter(adapter);
        showProgress(true);

    }

    private void showProgress(final boolean show) {
        if (show) {
            Log.d("mProgressView", "visible");
            Animation rotation = AnimationUtils.loadAnimation(getContext(), R.anim.rotate);
            rotation.setRepeatCount(Animation.INFINITE);
            mProgressView.startAnimation(rotation);
            mProgressView.setVisibility(View.VISIBLE);
        } else {
            Log.d("mProgressView", "invisible");
            mProgressView.setVisibility(View.INVISIBLE);
            mProgressView.clearAnimation();
        }
    }

    private void showEmptyList(boolean show) {
        showProgress(false);
        if (show)
            emptyList.setVisibility(View.VISIBLE);
        else
            emptyList.setVisibility(View.GONE);
    }
}
