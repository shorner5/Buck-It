package stuhorner.com.buckit;

import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;

public class MainActivity extends AppCompatActivity {
    ViewPager viewPager;
    TabLayout tabLayout;
    public static boolean fabVisible = true;
    int buckit_size = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        //initialize the toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(R.string.buckit_uncomp);

        //Create tabs and set name and icon
        initTabs(fab, toolbar);

        //initialize the floating action button
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, Inspire.class);
                    intent.putExtra("size", getBuckit_size());
                    startActivity(intent);
                }
            });
        }

    }

    private void initTabs(final FloatingActionButton fab, final Toolbar toolbar) {
        final AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.appbar);

        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setContentDescription(R.string.buckit_uncomp).setIcon(R.drawable.ic_list));
        tabLayout.addTab(tabLayout.newTab().setContentDescription(R.string.messages).setIcon(R.drawable.ic_chat));
        tabLayout.addTab(tabLayout.newTab().setContentDescription(R.string.similar).setIcon(R.drawable.ic_profile));
        tabLayout.addTab(tabLayout.newTab().setContentDescription(R.string.social).setIcon(R.drawable.ic_social));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        //Color tab icons
        tabLayout.getTabAt(0).getIcon().setColorFilter(getResources().getColor(R.color.accent_color_light), PorterDuff.Mode.MULTIPLY);
        for (int i = 1; i < tabLayout.getTabCount(); i++) {
            tabLayout.getTabAt(i).getIcon().setColorFilter(Color.DKGRAY, PorterDuff.Mode.MULTIPLY);
        }

        //Create the ViewPager
        viewPager = (ViewPager) findViewById(R.id.pager);
        PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);

        //Handle scrolling tabs
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout) {
            @Override
            public void onPageScrollStateChanged(int state) {
                if (appBarLayout != null) {
                    appBarLayout.setExpanded(true, true);
                }
            }
        });
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                tab.getIcon().setColorFilter(getResources().getColor(R.color.accent_color_light), PorterDuff.Mode.MULTIPLY);
                toolbar.setTitle(tab.getContentDescription());

                if (tab.getPosition() == 0) {
                    fabVisible = true;
                    fab.setVisibility(View.VISIBLE);
                    fab.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.button_slide_up));
                } else if (fabVisible) {
                    fabVisible = false;
                    fab.setVisibility(View.INVISIBLE);
                    fab.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.button_slide_down));
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                tab.getIcon().setColorFilter(Color.DKGRAY, PorterDuff.Mode.MULTIPLY);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        menu.getItem(0).getIcon().setColorFilter(getResources().getColor(R.color.accent_color_dark), PorterDuff.Mode.SRC_ATOP);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intent);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (tabLayout.getSelectedTabPosition() != 0) {
            tabLayout.smoothScrollTo(0, 0);
            viewPager.setCurrentItem(0);
        }
        else {
            super.onBackPressed();
        }
    }

    public void setBuckit_size(int size) {
        buckit_size = size;
    }

    public int getBuckit_size() {
        return buckit_size;
    }
}