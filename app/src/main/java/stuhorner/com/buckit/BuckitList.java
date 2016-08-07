package stuhorner.com.buckit;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
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
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Created by Stu on 12/14/2015.
 */
public class BuckitList extends Fragment {
    public final static String MATCH_ITEM = "com.stuhorner.buckit.MATCH_ITEM";
    private final static int MATCH_STARTED = 2;
    private LinkedList<String> bucket_items = new LinkedList<>();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser mUser = mAuth.getCurrentUser();
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference rootRef = database.getReference();
    private RVAdapter adapter;

    //UI components
    private RecyclerView rv;
    private TextView no_data;
    private TextView no_data_subtitle;
    private ImageView mProgressView;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.buckit_list, container, false);

        //initiate the buckit_list
        rv = (RecyclerView) view.findViewById(R.id.rv);
        no_data = (TextView) view.findViewById(R.id.buckit_no_data);
        no_data_subtitle = (TextView) view.findViewById(R.id.buckit_no_data_subtitle);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        rv.setLayoutManager(layoutManager);

        mProgressView = (ImageView) view.findViewById(R.id.list_logo);
        initData();

        //initialize the adapter for the recyclerview
        adapter = new RVAdapter(getFragmentManager(), bucket_items, false);
        rv.setAdapter(adapter);
        addListListener();

        return view;
    }

    private void addListListener() {
        //listen for clicks
        ItemClickSupport.addTo(rv).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                //transition to activity_match activity
                Intent intent = new Intent(getActivity(), MatchActivity.class);
                intent.putExtra(MATCH_ITEM, bucket_items.get(position));
                Pair<View, String> p1 = Pair.create(v, "item_title");
                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), p1);
                startActivityForResult(intent, MATCH_STARTED, options.toBundle());
            }
        });

        //listen for swipes and long presses
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                int removeIndex = viewHolder.getAdapterPosition();
                String removeText = bucket_items.get(removeIndex);

                Snackbar.make(getView(), "Removed '" + bucket_items.get(removeIndex) + "'", Snackbar.LENGTH_SHORT).show();

                //remove item from the list and update the list's size
                bucket_items.remove(removeIndex);
                adapter.notifyItemRemoved(removeIndex);
                BuckitList.this.removeItemFromFirebase(removeText);

                if (bucket_items.size() == 0) {
                    showEmptyList(true);
                }
            }

            @Override
            public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                return super.getSwipeDirs(recyclerView, viewHolder);
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(rv);
    }

    public void removeItemFromFirebase(final String removeItem) {
        //remove the item from the user's list
        rootRef.child("users").child(mUser.getUid()).child("buckits").child(removeItem).setValue(null);

        //remove the user from the item's list of users
        Query query = rootRef.child("buckits").child(removeItem).child("users").orderByValue().startAt(mUser.getUid()).limitToFirst(1);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("key", dataSnapshot.getChildren().iterator().next().getKey());
                rootRef.child("buckits").child(removeItem).child("users").child(dataSnapshot.getChildren().iterator().next().getKey()).setValue(null);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //decrease the number of users by 1
        rootRef.child("buckits").child(removeItem).child("num_users").runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                if (mutableData != null) {
                    if (mutableData.getValue() == null) {
                        mutableData.setValue(1);
                    } else {
                        // Set value and report transaction success
                        int num_users = mutableData.getValue(Integer.class);
                        mutableData.setValue(--num_users);
                    }
                }
                return Transaction.success(mutableData);
            }
            @Override
            public void onComplete(DatabaseError databaseError, boolean b,DataSnapshot dataSnapshot) {}
        });
    }

    private void initData() {
        showProgress(true);
        if (mUser != null) {
            rootRef.child("users").child(mUser.getUid()).child("buckits").addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    if (dataSnapshot.getKey() != null) {
                        bucket_items.add(dataSnapshot.getKey());
                        showProgress(false);
                        showEmptyList(false);
                        adapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getKey() != null) {
                        bucket_items.remove(dataSnapshot.getKey());
                        adapter.notifyDataSetChanged();
                        if (bucket_items.isEmpty()) {
                            showEmptyList(true);
                        }
                    }
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });

            rootRef.child("users").child(mUser.getUid()).child("buckits").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (bucket_items.isEmpty()) {
                        Log.d("bucket_items", "isEmpty");
                        showEmptyList(true);
                        showProgress(false);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
    }

    private void showEmptyList(boolean show) {
        if (show) {
            no_data.setVisibility(View.VISIBLE);
            no_data_subtitle.setVisibility(View.VISIBLE);
        } else {
            no_data.setVisibility(View.GONE);
            no_data_subtitle.setVisibility(View.GONE);
        }
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MATCH_STARTED) {
            if (resultCode != Activity.RESULT_CANCELED) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                Checked check = Checked.newInstance(data.getStringExtra(BuckitList.MATCH_ITEM), true);
                check.show(fm, "hello");
            }
        }
    }
}
