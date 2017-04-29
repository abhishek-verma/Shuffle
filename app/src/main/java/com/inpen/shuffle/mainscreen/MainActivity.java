package com.inpen.shuffle.mainscreen;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.inpen.shuffle.R;
import com.inpen.shuffle.mainscreen.fab.FabFragment;
import com.inpen.shuffle.mainscreen.items.ItemsFragment;
import com.inpen.shuffle.mainscreen.items.SongItemsFragment;
import com.inpen.shuffle.utility.CustomTypes;
import com.yalantis.guillotine.animation.GuillotineAnimation;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity
        implements MainScreenContract.MainView {

    private static final int PERMISSION_REQUEST_CODE = 0;
    private static final long RIPPLE_DURATION = 250;


    MainScreenContract.ActivityActionsListener mActivityActionsListener;

    @BindView(R.id.root)
    FrameLayout mRootView;
    @BindView(R.id.viewpager)
    ViewPager mViewPager;
    @BindView(R.id.tab_layout)
    TabLayout mTabLayout;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.content_hamburger)
    View contentHamburger;

    private MyPagerAdapter mFragmentAdapter;
    private FabFragment mFabFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        if (savedInstanceState == null) {
            setupFabFragment();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        mActivityActionsListener = new MainPresenter(this);
        mActivityActionsListener.init(this);

        setupAdapterAndViewPager();
        setupGuillotineMenu();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

//        getSupportFragmentManager()
//                .beginTransaction()
//                .remove(mFabFragment)
//                .commitAllowingStateLoss();
//
//        mFabFragment.onDestroy();
//        mFabFragment = null;
    }

    @Override
    protected void onStop() {
        mActivityActionsListener.stop(this);
        super.onStop();
    }

    private void setupAdapterAndViewPager() {
        //Set up the Adapter
        mFragmentAdapter = new MyPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mFragmentAdapter);
        mViewPager.setOffscreenPageLimit(4);

        //Set up the Tablayout
        mTabLayout.setupWithViewPager(mViewPager);
    }

    private void setupGuillotineMenu() {

        View guillotineMenu = LayoutInflater.from(this).inflate(R.layout.guillotine, null);
        mRootView.addView(guillotineMenu);

        new GuillotineAnimation.GuillotineBuilder(guillotineMenu, guillotineMenu.findViewById(R.id.guillotine_hamburger), contentHamburger)
                .setStartDelay(RIPPLE_DURATION)
                .setActionBarViewForAnimation(mToolbar)
                .setClosedOnStart(true)
                .build();
    }

    private void setupFabFragment() {
        if (mFabFragment == null) {
            mFabFragment = FabFragment.newInstance();
        }

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fabFragmentContainer, mFabFragment)
                .commit();
    }

    @Override
    public void connectToSession(MediaSessionCompat.Token token)
            throws RemoteException {
        MediaControllerCompat mediaController = new MediaControllerCompat(this, token);
        setSupportMediaController(mediaController);
    }

    @Override
    public boolean hasPermissions() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }


    @Override
    public void getPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                // TODO Explain to the user why we need to read the contacts
            }

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            mActivityActionsListener.gotPermissionResult(this, true);
        }
    }

    @Override
    public AppCompatActivity getActivityContext() {
        return this;
    }

    public class MyPagerAdapter extends FragmentPagerAdapter {

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            CustomTypes.ItemType itemType;

            switch (position) {
                case 0:
                    itemType = CustomTypes.ItemType.ALBUM_KEY;
                    break;
                case 1:
                    itemType = CustomTypes.ItemType.ARTIST_KEY;
                    break;
                case 2:
                    //For song
                    return SongItemsFragment.newInstance();
                case 3:
                    itemType = CustomTypes.ItemType.FOLDER;
                    break;
                case 4:
                    itemType = CustomTypes.ItemType.PLAYLIST;
                    break;
                default:
                    itemType = CustomTypes.ItemType.ALBUM_KEY;
            }

            return ItemsFragment.newInstance(itemType);
        }

        @Override
        public int getCount() {
            return 5;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            String title;

            switch (position) {
                case 0:
                    title = "Albums";
                    break;
                case 1:
                    title = "Artists";
                    break;
                case 2:
                    title = "Songs";
                    break;
                case 3:
                    title = "Folders";
                    break;
                case 4:
                    title = "Playlist";
                    break;
                default:
                    title = "Error";
            }

            return title;
        }
    }
}
