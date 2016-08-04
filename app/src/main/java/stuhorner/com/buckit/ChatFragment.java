package stuhorner.com.buckit;

import android.content.Intent;
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
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Stu on 12/14/2015.
 */
public class ChatFragment extends Fragment {
    List<ChatRow> chatRows = new LinkedList<>();
    ChatAdapter adapter;
    ImageView mProgressView;
    TextView mEmptyList;
    private FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
    private DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chats, container, false);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.chat_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mProgressView = (ImageView) view.findViewById(R.id.chat_logo);
        mEmptyList = (TextView) view.findViewById(R.id.chat_list_empty);
        showProgress(true);
        initData();
        adapter = new ChatAdapter(chatRows, getContext());
        recyclerView.setAdapter(adapter);

        ItemClickSupport.addTo(recyclerView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                //transition to page activity
                if (position < chatRows.size()) {
                    Intent intent = new Intent(getActivity(), ChatPage.class);
                    intent.putExtra("uid", chatRows.get(position).getUID());
                    intent.putExtra("name", chatRows.get(position).getName());
                    getActivity().startActivity(intent);
                    getActivity().overridePendingTransition(R.anim.slide_in, R.anim.fade_out);
                }
            }
        });
        return view;
    }

    private void initData() {
        Query query = rootRef.child("messages").child(mUser.getUid()).orderByChild("metadata/last_message_time");
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                ChatRow row = new ChatRow(dataSnapshot.getKey());
                chatRows.add(0, row);
                getName(row);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String key) {
                //move the row to the front
                int index = findIndexFromKey(dataSnapshot.getKey());
                Log.d("index", Integer.toString(index));
                Log.d("key", dataSnapshot.getKey());
                ChatRow row = chatRows.get(index);
                chatRows.remove(index);
                chatRows.add(0, row);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError errror) {
            }
        });

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (chatRows.isEmpty()) {
                    showProgress(false);
                    showEmptyList(true);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
    }

    private void getName(final ChatRow row) {
        rootRef.child("users").child(row.getUID()).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    row.setName(dataSnapshot.getValue().toString());
                    getNewMessage(row);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
    }

    private void getNewMessage(final ChatRow row) {
        rootRef.child("messages").child(mUser.getUid()).child(row.getUID()).child("metadata").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if ((dataSnapshot.child("seen_message_time").getValue() != null && dataSnapshot.child("last_message_time").getValue() != null)) {
                    if ((long) dataSnapshot.child("seen_message_time").getValue() < (long) dataSnapshot.child("last_message_time").getValue()) {
                        row.setNewMessage(true);
                    } else {
                        row.setNewMessage(false);
                    }
                }
                else {
                    row.setNewMessage(true);
                }
                getSubtitle(row);
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
            }
        });
    }

    private void getSubtitle(final ChatRow row) {
        rootRef.child("messages").child(mUser.getUid()).child(row.getUID()).child("metadata").child("last_message").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (isAdded()) {
                    if (dataSnapshot.child("body").getValue() != null) {
                        row.setSubtitle(dataSnapshot.child("body").getValue().toString());
                    }
                    getPicture(row);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
    }

    private void getPicture(final ChatRow row) {
        rootRef.child("users").child(row.getUID()).child("profilePicSmall").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    row.setChatIcon(dataSnapshot.getValue().toString());
                    adapter.notifyDataSetChanged();
                    showProgress(false);
                    showEmptyList(false);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private int findIndexFromKey(String key) {
        for (int i = 0; i < chatRows.size(); i++) {
            Log.d("AT " + Integer.toString(i), chatRows.get(i).getUID());
            if (chatRows.get(i).getUID().equals(key)) {
                return i;
            }
        }
        return 0;
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
            mProgressView.setVisibility(View.GONE);
            mProgressView.clearAnimation();
        }
    }

    private void showEmptyList(boolean show) {
        if (show)
            mEmptyList.setVisibility(View.VISIBLE);
        else
            mEmptyList.setVisibility(View.GONE);
    }
}