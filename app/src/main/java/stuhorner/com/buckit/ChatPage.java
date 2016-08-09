package stuhorner.com.buckit;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.lang.CharSequence;
import java.lang.String;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Stu on 12/24/2015.
 */
public class ChatPage extends AppCompatActivity {
    List<Message> messages = new ArrayList<>();
    String UID;
    ImageButton sendButton;
    EditText editText;
    ChatMessageAdapter adapter;
    ListView listView;
    boolean active = true;
    DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
    FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_page);
        UID = getIntent().getStringExtra("uid");

        Toolbar toolbar = (Toolbar) findViewById(R.id.chat_toolbar);
        setSupportActionBar(toolbar);
        if (toolbar != null) {
            toolbar.setNavigationIcon(ContextCompat.getDrawable(this, R.drawable.ic_back));
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
            if (getIntent().getStringExtra("name") != null && getSupportActionBar() != null)
                getSupportActionBar().setTitle(getIntent().getStringExtra("name"));
            else
                getName(toolbar);
        }

        initMessages();
        initButton();

        editText = (EditText) findViewById(R.id.chat_edit_text);
        listView = (ListView) findViewById(R.id.chat_messages);
        adapter = new ChatMessageAdapter(ChatPage.this, messages);
        if (listView != null) {
            listView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_NORMAL);
            listView.setAdapter(adapter);

        }

        if (editText != null) {
            editText.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) {
                }

                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() > 0) {
                        sendButton.setAlpha(1f);
                    } else {
                        sendButton.setAlpha(.2f);
                    }
                }
            });
        }
    }

    private void getName(final Toolbar toolbar) {
        rootRef.child("users").child(UID).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null && getSupportActionBar() != null) {
                    toolbar.setTitle(dataSnapshot.getValue().toString());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void initButton() {
        sendButton = (ImageButton) findViewById(R.id.chat_sendButton);
        sendButton.setColorFilter(getResources().getColor(R.color.accent_color_light), PorterDuff.Mode.MULTIPLY);
        sendButton.setAlpha(.2f);

        //sendButton animation
        final Animation scaleDown = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.scale_down);
        scaleDown.setFillAfter(true);
        final Animation scaleUp = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.scale_up);
        scaleUp.setFillAfter(true);

        sendButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        if (editText != null && editText.getText().length() > 0) {
                            sendButton.startAnimation(scaleDown);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        if (editText != null && editText.getText().length() > 0) {
                            sendButton.startAnimation(scaleUp);
                            //launch chat bubble
                            Message messageToSend = new Message(editText.getText().toString(), mUser.getUid());
                            sendMessage(messageToSend);
                            editText.setText(null);
                        }
                        break;
                }
                return false;
            }
        });
    }

    private void sendMessage(Message messageToSend) {
        rootRef.child("users").child(UID).child("social").child(mUser.getUid()).setValue(0);
        rootRef.child("users").child(mUser.getUid()).child("social").child(UID).setValue(0);
        rootRef.child("messages").child(mUser.getUid()).child(UID).child("metadata").child("last_message_time").setValue(ServerValue.TIMESTAMP);
        rootRef.child("messages").child(UID).child(mUser.getUid()).child("metadata").child("last_message_time").setValue(ServerValue.TIMESTAMP);
        rootRef.child("messages").child(mUser.getUid()).child(UID).child("metadata").child("last_message").setValue(messageToSend);
        rootRef.child("messages").child(UID).child(mUser.getUid()).child("metadata").child("last_message").setValue(messageToSend);
        rootRef.child("messages").child(mUser.getUid()).child(UID).push().setValue(messageToSend);
        rootRef.child("messages").child(UID).child(mUser.getUid()).push().setValue(messageToSend);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        getIcon(menu.getItem(0));
        return super.onCreateOptionsMenu(menu);
    }

    private void getIcon(final MenuItem item) {
        rootRef.child("users").child(UID).child("profilePicSmall").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    byte[] bytes = Base64.decode(dataSnapshot.getValue().toString(), Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    item.setIcon(resizeImage(bitmap, 300, 300));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private Drawable resizeImage(Bitmap bitmap, int w, int h)
    {
        // load the origial Bitmap
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        // calculate the scale
        float scaleWidth = ((float) w) / width;
        float scaleHeight = ((float) h) / height;
        // create a matrix for the manipulation
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,width, height, matrix, true);
        return new BitmapDrawable(resizedBitmap);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.chat_profile) {
            Intent intent = new Intent(ChatPage.this, ProfileActivity.class);
            intent.putExtra("uid", UID);
            intent.putExtra("name", getIntent().getStringExtra("name"));
            intent.putExtra("hide_chat_button", true);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in, R.anim.fade_out);
        }
        return false;
    }

    public void onBackPressed() {
        super.onBackPressed();
        active = false;
        sendButton.getBackground().clearColorFilter();
        overridePendingTransition(R.anim.fade_in, R.anim.slide_out);
    }

    private void initMessages() {
        rootRef.child("messages").child(mUser.getUid()).child(UID).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (!dataSnapshot.getKey().equals("metadata") && !dataSnapshot.child("body").getValue().toString().equals("")) {
                    Message message = new Message(dataSnapshot.child("body").getValue().toString(), dataSnapshot.child("sender").getValue().toString());
                    messages.add(message);
                    adapter.notifyDataSetChanged();
                }

                //update seen time
                if (active)
                    rootRef.child("messages").child(mUser.getUid()).child(UID).child("metadata").child("seen_message_time").setValue(ServerValue.TIMESTAMP);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

}
