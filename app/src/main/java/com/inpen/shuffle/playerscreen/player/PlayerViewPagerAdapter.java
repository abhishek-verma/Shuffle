package com.inpen.shuffle.playerscreen.player;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.inpen.shuffle.model.repositories.QueueRepository;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by Abhishek on 12/28/2016.
 */

public class PlayerViewPagerAdapter extends FragmentStatePagerAdapter {

    public PlayerViewPagerAdapter(FragmentManager fm) {
        super(fm);

        EventBus.getDefault().register(this);
    }

    @Override
    public int getCount() {
        return QueueRepository.getInstance().getSize();
    }

    @Override
    public Fragment getItem(int position) {
        return PlayerFragment.newInstance(position);
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onQueueContentsUpdated(QueueRepository.QueueContentsChangedEvent event) {
        notifyDataSetChanged();
    }
}
