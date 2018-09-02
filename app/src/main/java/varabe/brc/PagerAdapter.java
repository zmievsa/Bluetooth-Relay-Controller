package varabe.brc;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import varabe.brc.fragment.TabFragment1;
import varabe.brc.fragment.TabFragment2;

public class PagerAdapter extends FragmentStatePagerAdapter {

    public PagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                return new TabFragment1();
            case 1:
                return new TabFragment2();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }
}