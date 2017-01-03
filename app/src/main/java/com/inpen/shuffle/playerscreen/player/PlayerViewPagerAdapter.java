package com.inpen.shuffle.playerscreen.player;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.inpen.shuffle.model.repositories.QueueRepository;

/**
 * Created by Abhishek on 12/28/2016.
 */

public class PlayerViewPagerAdapter extends FragmentStatePagerAdapter {

    public PlayerViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public int getCount() {
        return QueueRepository.getInstance().getSize();
    }

    @Override
    public Fragment getItem(int position) {
        return PlayerFragment.newInstance(position);
    }
}
