package stuhorner.com.buckit;

import android.content.Intent;
import android.location.Location;
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
import android.widget.Button;
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

import java.util.HashSet;
import java.util.LinkedList;

public class NearMeFragment extends Fragment implements LocationReceiver {
    private ImageView mProgressView;
    private TextView emptyList;
    private Button button;
    private NearMeAdapter adapter;
    private LinkedList<NearMeHolder> users = new LinkedList<>();
    private HashSet<String> userQueue = new HashSet<>();
    private DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
    private GeoFire geoFire = new GeoFire(rootRef.child("geoFire"));
    private FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_near_me, container, false);
        button = (Button) v.findViewById(R.id.enable_location);
        initRecyclerView(v);
        ((MainActivity)getActivity()).startLocationQuery(this);
        return v;
    }

    @Override
    public void initData(Location location) {
        Log.d("location", "initData");
        geoFire.setLocation(mUser.getUid(), new GeoLocation(location.getLatitude(), location.getLongitude()));
        GeoQuery query = geoFire.queryAtLocation(new GeoLocation(location.getLatitude(), location.getLongitude()), 220);
        query.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (!key.equals(mUser.getUid())) {
                    if (userQueue.size() < 100) {
                        getPendingUserDiscoverable(key);
                        userQueue.add(key);
                    }
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
                if (userQueue.isEmpty()) {
                    showEmptyList(true);
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void getPendingUserDiscoverable(final String UID) {
        Log.d("path", "getPendingUserDiscoverable");
        rootRef.child("users").child(UID).child("discoverable").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null && dataSnapshot.getValue().equals("1")) {
                    getName(UID);
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

    private void getName(final String UID) {
        rootRef.child("users").child(UID).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    NearMeHolder user = new NearMeHolder();
                    user.setName(dataSnapshot.getValue().toString());
                    user.setUid(UID);
                    getBuckitList(user);
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

    private void getBuckitList(final NearMeHolder user) {
        Query query = rootRef.child("users").child(user.getUid()).child("buckits").orderByKey().limitToFirst(2);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        user.addItem(data.getKey());
                    }
                }
                getImage(user);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getImage(final NearMeHolder user) {
        rootRef.child("users").child(user.getUid()).child("profilePicSmall").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    user.setImg(dataSnapshot.getValue().toString());
                    users.add(user);
                    adapter.notifyDataSetChanged();
                    showProgress(false);
                }
                else {
                    userQueue.remove(user.getUid());
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

    private void initRecyclerView(View v) {
        RecyclerView mRecyclerView = (RecyclerView) v.findViewById(R.id.near_me_recyclerview);
        mProgressView = (ImageView) v.findViewById(R.id.logo);
        emptyList = (TextView) v.findViewById(R.id.list_empty);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new NearMeAdapter(getContext(), users);
        mRecyclerView.setAdapter(adapter);
        showProgress(true);

        ItemClickSupport.addTo(mRecyclerView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                Intent intent = new Intent (getActivity(), ProfileActivity.class);
                intent.putExtra("uid", users.get(position).getUid());
                intent.putExtra("name", users.get(position).getName());
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.slide_in, R.anim.fade_out);
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        ((MainActivity)getActivity()).disconnectGoogleApiClient();
    }

    @Override
    public void permissionDenied() {
        showProgress(false);
        button.setVisibility(View.VISIBLE);
        ((MainActivity)getActivity()).disconnectGoogleApiClient();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button.setVisibility(View.GONE);
                ((MainActivity)getActivity()).startLocationQuery(NearMeFragment.this);
                showProgress(true);
            }
        });
    }

}
