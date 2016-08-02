package stuhorner.com.buckit;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
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
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

import info.debatty.java.stringsimilarity.NormalizedLevenshtein;

public class Inspire extends AppCompatActivity {
    List<String> inspire_items = new LinkedList<>();
    AppBarLayout appBarLayout;
    InspireRVAdapter adapter;
    EditText editText;
    boolean suggestionsReady = false, itemAdded = false;
    String newItem;
    ArrayList<String> mBuckitItems = new ArrayList<>();
    PriorityQueue<Pair<String, Double>> buckitItems = new PriorityQueue<Pair<String, Double>>(20, new Comparator<Pair<String, Double>>() {
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
        initEditText();
        initBuckIts();
    }

    private void initBuckIts() {
        rootRef.child("users").child(mUser.getUid()).child("buckits").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mBuckitItems = (ArrayList<String>)dataSnapshot.getValue();
                suggestionsReady = true;

                if (itemAdded && (mBuckitItems == null || !mBuckitItems.contains(newItem))) {
                    updateFirebase();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

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

    private void initEditText() {
        InputFilter filter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {

                if (source instanceof SpannableStringBuilder) {
                    SpannableStringBuilder sourceAsSpannableBuilder = (SpannableStringBuilder)source;
                    for (int i = end - 1; i >= start; i--) {
                        char currentChar = source.charAt(i);
                        if (!Character.isLetterOrDigit(currentChar) && !Character.isSpaceChar(currentChar)) {
                            sourceAsSpannableBuilder.delete(i, i+1);
                        }
                    }
                    return source;
                } else {
                    StringBuilder filteredStringBuilder = new StringBuilder();
                    for (int i = start; i < end; i++) {
                        char currentChar = source.charAt(i);
                        if (Character.isLetterOrDigit(currentChar) || Character.isSpaceChar(currentChar)) {
                            filteredStringBuilder.append(currentChar);
                        }
                    }
                    return filteredStringBuilder.toString();
                }
            }
        };

        editText.setFilters(new InputFilter[] { filter });
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
                Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();
                while (iterator.hasNext()) {
                    String toCompare = iterator.next().getKey();
                    NormalizedLevenshtein comparison = new NormalizedLevenshtein();
                    buckitItems.add(new Pair<String, Double>(toCompare, comparison.similarity(searchString, toCompare)));
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
                Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();
                while (iterator.hasNext()) {
                    inspire_items.add(1, iterator.next().getKey());
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
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new InspireRVAdapter(inspire_items, getApplicationContext());
        mRecyclerView.setAdapter(adapter);

        ItemClickSupport.addTo(mRecyclerView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                newItem = (position == 0) ? editText.getText().toString() : inspire_items.get(position);
                if (newItem.equals("")) {
                    //show the keyboard
                    editText.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
                }
                else {
                    itemAdded = true;
                    if (suggestionsReady && (mBuckitItems == null || !mBuckitItems.contains(newItem))) {
                        updateFirebase();
                        v.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.scale_down_up));
                    }
                    else {
                        Snackbar.make(recyclerView, String.format(getString(R.string.repeat), newItem), Snackbar.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void updateFirebase(){
        rootRef.child("users").child(mUser.getUid()).child("buckits").child(Integer.toString(getIntent().getIntExtra("size", 0))).setValue(newItem);
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
