package stuhorner.com.buckit;

import android.os.Build;
import android.support.v4.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.transition.Transition;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;

import com.lorentzos.flingswipe.SwipeFlingAdapterView;

import java.util.ArrayList;
import java.util.List;

public class MatchActivity extends AppCompatActivity {
    List<String> user_names;
    ImageButton yesButton;
    ImageButton noButton;
    ImageButton chatButton;
    SwipeFlingAdapterView flingContainer;
    boolean flung = false;
    public final static String PERSON_NAME = "com.stuhorner.buckit.PERSON_NAME";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.match);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        TextView textView = (TextView)findViewById(R.id.match_text);
        if (textView != null)
            textView.setText(getIntent().getStringExtra(BuckitList.MATCH_ITEM));

        initButtons();
        initData();
        checkboxListener();
        initFlingContainer();
    }

    private void initFlingContainer() {
        //handle the cards
        flingContainer = (SwipeFlingAdapterView) findViewById(R.id.swipecards);
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,R.layout.card_item, R.id.card_title, user_names);
        flingContainer.setAdapter(arrayAdapter);

        //handle card swiping
        flingContainer.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {
            @Override
            public void removeFirstObjectInAdapter() {
                user_names.remove(0);
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onLeftCardExit(Object dataObject) {
                final Animation shake = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.wobble);
                if (flung) { noButton.startAnimation(shake); }
                flung = false;
            }

            @Override
            public void onRightCardExit(Object dataObject) {
                final Animation shake = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.wobble);
                if (flung) { yesButton.startAnimation(shake); }
                flung = false;
            }

            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {
                user_names.add("Kanye West");
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onScroll(float f) {
                if (f == 1.0 || f == -1.0) {
                    flung = true;
                }
            }
        });
        //handle tapping the cards
        flingContainer.setOnItemClickListener(new SwipeFlingAdapterView.OnItemClickListener() {
            @Override
            public void onItemClicked(int position, Object dataObject) {
                Intent intent = new Intent(MatchActivity.this, ProfileActivity.class);
                intent.putExtra(PERSON_NAME, user_names.get(position));
                startActivityForResult(intent, 1);
                overridePendingTransition(R.anim.slide_in, R.anim.fade_out);
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == 1) {
            if (resultCode == ProfileActivity.RESULT_YES){
                //swipe right
                flingContainer.getTopCardListener().selectRight();
            }
            else if (resultCode == ProfileActivity.RESULT_NO ) {
                //swipe left
                flingContainer.getTopCardListener().selectLeft();
            }
        }
    }

    private void initData(){
        user_names = new ArrayList<String>();
        for (int i = 1;i < 11; i++) {
            user_names.add("User " + i);
        }
    }

    @Override
    public void onBackPressed(){
        Animation animation1 = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.button_slide_down);
        Animation animation2 = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.button_slide_down);
        Animation animation3 = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.button_slide_down);

        yesButton.startAnimation(animation1);
        chatButton.startAnimation(animation2);
        noButton.startAnimation(animation3);

        animation1.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                yesButton.setVisibility(View.INVISIBLE);
                noButton.setVisibility(View.INVISIBLE);
                chatButton.setVisibility(View.INVISIBLE);
                MatchActivity.super.onBackPressed();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

    }

    private void initButtons() {
        yesButton = (ImageButton) findViewById(R.id.yes_button);
        noButton = (ImageButton) findViewById(R.id.no_button);
        chatButton = (ImageButton) findViewById(R.id.chat_button);
        addAnimation(yesButton);
        addAnimation(noButton);
        addAnimation(chatButton);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final Transition sharedElementEnterTransition = getWindow().getSharedElementEnterTransition();
            sharedElementEnterTransition.addListener(new Transition.TransitionListener() {
                @Override
                public void onTransitionStart(Transition transition) {}
                @Override
                public void onTransitionEnd(Transition transition) {
                    Log.d("Transition", "end");

                    Animation noAnim = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.button_slide_up);
                    noAnim.setFillAfter(true);
                    Animation chatAnim = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.button_slide_up);
                    chatAnim.setStartOffset(50);
                    chatAnim.setFillAfter(true);
                    Animation yesAnim = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.button_slide_up);
                    yesAnim.setStartOffset(100);
                    yesAnim.setFillAfter(true);

                    noButton.startAnimation(noAnim);
                    chatButton.startAnimation(chatAnim);
                    yesButton.startAnimation(yesAnim);

                    yesButton.setVisibility(View.VISIBLE);
                    noButton.setVisibility(View.VISIBLE);
                    chatButton.setVisibility(View.VISIBLE);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        sharedElementEnterTransition.removeListener(this);
                    }
                }
                @Override
                public void onTransitionCancel(Transition transition) {}
                @Override
                public void onTransitionPause(Transition transition) {}
                @Override
                public void onTransitionResume(Transition transition) {}
            });
        }
        else {
            yesButton.setVisibility(View.VISIBLE);
            noButton.setVisibility(View.VISIBLE);
            chatButton.setVisibility(View.VISIBLE);
        }
    }

    private void addAnimation(final ImageButton button) {
        final Animation scaleDown = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.scale_down);
        scaleDown.setFillAfter(true);
        final Animation scaleUp = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.scale_up);
        scaleUp.setFillAfter(true);

        button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        button.startAnimation(scaleDown);
                        break;
                    case MotionEvent.ACTION_UP:
                        button.startAnimation(scaleUp);
                        handleButtonPress(button);
                        break;
                }
                return false;
            }
        });
    }

    private void handleButtonPress(ImageButton button) {
        if (button == chatButton) {
            Intent intent = new Intent(getApplicationContext(), ChatPage.class);
            intent.putExtra(PERSON_NAME, user_names.get(flingContainer.getFirstVisiblePosition()));
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in, R.anim.fade_out);
        }
        else if (button == yesButton) {
            flingContainer.getTopCardListener().selectRight();
        }
        else if (button == noButton) {
            flingContainer.getTopCardListener().selectLeft();
        }
    }

    private void checkboxListener() {
        CheckBox checkBox = (CheckBox) findViewById(R.id.checkbox);
        if (checkBox != null) {
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    FragmentManager fm = getSupportFragmentManager();
                    Checked check = Checked.newInstance(getIntent().getStringExtra(BuckitList.MATCH_ITEM));
                    check.show(fm, "hello");
                }
            });
        }
    }

}
