package stuhorner.com.buckit;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
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
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

import info.debatty.java.stringsimilarity.NormalizedLevenshtein;

public class Inspire extends AppCompatActivity {
    List<String> inspire_items = new LinkedList<>();
    AppBarLayout appBarLayout;
    InspireRVAdapter adapter;
    EditText editText;
    String newItem;
    PriorityQueue<Pair<String, Double>> buckitItems = new PriorityQueue<>(20, new Comparator<Pair<String, Double>>() {
        @Override
        public int compare(Pair<String, Double> lhs, Pair<String, Double> rhs) {
            if (lhs.second.equals(rhs.second)) {
                if (lhs.first.compareTo(rhs.first) > 0) {
                    return 1;
                }
                else {
                    return -1;
                }
            }
            if (lhs.second < rhs.second) {
                return 1;
            }
            else {
                return -1;
            }
        }
    });

    //firebase dependencies
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser mUser = mAuth.getCurrentUser();
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference rootRef = database.getReference();
    private ImageView mProgressView;

    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.inspire);
        mProgressView = (ImageView) findViewById(R.id.inspire_logo);

        initSuggestions();
        initRecyclerView();
        initAppBarLayout();
        initToolbar();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }
    }

    private void initAppBarLayout() {
        appBarLayout = (AppBarLayout) findViewById(R.id.appbar);
        if (appBarLayout != null) {
            editText = (EditText) appBarLayout.findViewById(R.id.scrolling_editText);
            addListener();
            appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
                @Override
                public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                    if (verticalOffset > -320) {
                        if (editText.getMaxLines() != 3)
                            editText.setMaxLines(3);
                        editText.setTextSize(64 - Math.abs(verticalOffset) / 8);
                    }
                    else if (editText.getMaxLines() != 1) {
                            editText.setMaxLines(1);
                    }
                }
            });
        }
    }

    private void addListener() {
        editText.setMaxLines(3);
        editText.setHorizontallyScrolling(false);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    appBarLayout.setExpanded(false);
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    getAllPotentialBuckits(editText.getText().toString());
                }
                return false;
            }
        });

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                initSearch(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void initSearch(String searchString) {
        if (inspire_items.size() > 0) {
            inspire_items.remove(0);
        }
        if (searchString.length() != 0) {
            inspire_items.add(0, String.format(getString(R.string.add_new_buckit), searchString));
        }
        else {
            inspire_items.add(0, getString(R.string.new_buckit));
        }
        adapter.notifyDataSetChanged();
    }

    private void getAllPotentialBuckits(final String searchString) {
        showProgress(true);
        inspire_items.clear();
        initSearch(searchString);
        rootRef.child("buckits_index").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                inspire_items.clear();
                initSearch(searchString);
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    String toCompare = dataSnapshot1.getKey();
                    NormalizedLevenshtein comparison = new NormalizedLevenshtein();
                    buckitItems.add(new Pair<>(toCompare, comparison.similarity(searchString, toCompare)));
                }
                for (int i = 0; i < 25 && buckitItems.size() > 0; i++) {
                    Pair<String, Double> potential = buckitItems.poll();
                    if (potential.second == 1) {
                        inspire_items.remove(0);
                    }
                    if (potential.second > 0.2) {
                        inspire_items.add(potential.first);
                    }
                }
                showProgress(false);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void initSuggestions() {
        showProgress(true);
        inspire_items.add(0, getString(R.string.new_buckit));
        Query query = rootRef.child("buckits").orderByChild("num_users").limitToLast(25);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    inspire_items.add(1, data.getKey());
                    adapter.notifyDataSetChanged();
                }
                showProgress(false);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void initRecyclerView() {
        //initialize the recyclerview
        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.inspire_rv);
        if (mRecyclerView != null) {
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            adapter = new InspireRVAdapter(inspire_items, getApplicationContext());
            mRecyclerView.setAdapter(adapter);
        }

        ItemClickSupport.addTo(mRecyclerView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, final View v) {
                newItem = (position == 0) ? editText.getText().toString() : inspire_items.get(position);
                if (newItem.equals("")) {
                    //show the keyboard
                    editText.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
                } else {
                    SharedPreferences pref = getSharedPreferences("data", MODE_PRIVATE);
                    if (position == 0 && !editText.getText().toString().equals(inspire_items.get(position)) && pref.getBoolean("create_warning", true)) {
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putBoolean("create_warning", false);
                        editor.apply();
                        AlertDialog.Builder builder = new AlertDialog.Builder(Inspire.this)
                                .setTitle("Are you sure you want to create a new Buck It?")
                                .setMessage("It's better to search for suggestions so you can find people around you with similar Buck Its")
                                .setNegativeButton("Create It", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        updateFirebase();
                                        v.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.scale_down_up));
                                    }
                                })
                                .setPositiveButton("Find Similar Buck Its", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        appBarLayout.setExpanded(false);
                                        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                                        getAllPotentialBuckits(editText.getText().toString());
                                    }
                                });
                        builder.create().show();
                    } else {
                        updateFirebase();
                        v.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.scale_down_up));
                    }
                }
            }
        });
    }

    private void updateFirebase(){
        newItem = newItem.replace("/", "")
                .replace(".", "")
                .replace("$", "")
                .replace("#", "")
                .replace("[", "")
                .replace("]", "");
        rootRef.child("users").child(mUser.getUid()).child("buckits").child(newItem).setValue(ServerValue.TIMESTAMP);
        rootRef.child("buckits").child(newItem).child("users").push().setValue(mUser.getUid());
        rootRef.child("buckits_index").child(newItem).setValue(0);

        rootRef.child("buckits").child(newItem).child("num_users").runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                if (mutableData != null) {
                    if (mutableData.getValue() == null) {
                        mutableData.setValue(1);
                    } else {
                        // Set value and report transaction success
                        int num_users = mutableData.getValue(Integer.class);
                        mutableData.setValue(++num_users);
                    }
                }
                return Transaction.success(mutableData);
            }
            @Override
            public void onComplete(DatabaseError databaseError, boolean b,DataSnapshot dataSnapshot) {}
        });

        onBackPressed();
    }

    private void showProgress(final boolean show) {
        if (show) {
            Log.d("mProgressView", "visible");
            Animation rotation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate);
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
