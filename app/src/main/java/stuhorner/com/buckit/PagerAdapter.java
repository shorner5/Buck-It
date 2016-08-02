package stuhorner.com.buckit;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class PagerAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;

    public PagerAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                BuckitList tab1 = new BuckitList();
                return tab1;
            case 1:
                Chat tab2 = new Chat();
                return tab2;
            case 2:
                Social tab3 = new Social();
                return tab3;
            case 3:
                MatchesFragment tab4 = new MatchesFragment();
                return tab4;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}