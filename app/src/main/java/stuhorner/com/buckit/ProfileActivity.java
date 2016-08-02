package stuhorner.com.buckit;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;

/**
 * Created by Stu on 1/1/2016.
 */
public class ProfileActivity extends AppCompatActivity{
    String person_name;
    ImageButton noButton;
    ImageButton chatButton;
    ImageButton yesButton;
    public final static String PERSON_NAME = "com.stuhorner.buckit.PERSON_NAME";
    public final static int RESULT_NO = 1;
    public final static int RESULT_YES = 2;
    int result = 0;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);
        person_name = getIntent().getStringExtra(MatchActivity.PERSON_NAME);

        setupToolbar();
        setupViewPager();
        setupCollapsingToolbar();
        setUpButtons();

    }

    private void setUpButtons() {
        noButton = (ImageButton)findViewById(R.id.p_no_button);
        chatButton = (ImageButton)findViewById(R.id.p_chat_button);
        yesButton = (ImageButton)findViewById(R.id.p_yes_button);
        noButton.setImageResource(R.drawable.ic_no);
        chatButton.setImageResource(R.drawable.ic_chatbutton);
        yesButton.setImageResource(R.drawable.ic_yes);

        addAnimation(noButton);
        addAnimation(yesButton);
        addAnimation(chatButton);
    }

    private void setupToolbar() {
        Toolbar toolbar = (Toolbar)findViewById(R.id.p_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(person_name);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(ContextCompat.getDrawable(this, R.drawable.ic_back));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
    private void setupViewPager(){
        final ViewPager viewPager = (ViewPager) findViewById(R.id.p_viewpager);
        setupViewPager(viewPager);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.p_tabs);
        tabLayout.setupWithViewPager(viewPager);
    }
    private void setupCollapsingToolbar(){
        final CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitleEnabled(false);
    }
    private void setupViewPager(ViewPager viewPager) {
        ProfilePagerAdapter adapter = new ProfilePagerAdapter(getSupportFragmentManager());
        adapter.add(new ProfileDetails(), "Profile");
        adapter.add(new ProfileBuckits(), "Buck It List");
        viewPager.setAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
        setResult(result);
        super.onBackPressed();
        if (result == RESULT_NO)
            overridePendingTransition(R.anim.fade_in, R.anim.slide_out_left);
        else
            overridePendingTransition(R.anim.fade_in, R.anim.slide_out);
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
            intent.putExtra(PERSON_NAME, person_name);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in, R.anim.fade_out);
        }
        else if (button == yesButton) {
            result = RESULT_YES;
            onBackPressed();
        }
        else if (button == noButton) {
            result = RESULT_NO;
            onBackPressed();
        }
    }
}