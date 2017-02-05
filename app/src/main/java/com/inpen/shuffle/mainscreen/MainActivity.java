package com.inpen.shuffle.mainscreen;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
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

import com.inpen.shuffle.R;
import com.inpen.shuffle.mainscreen.fab.FabFragment;
import com.inpen.shuffle.mainscreen.items.ItemsFragment;
import com.inpen.shuffle.utility.CustomTypes;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity
        implements MainScreenContract.MainView {

    private static final int PERMISSION_REQUEST_CODE = 0;


    MainScreenContract.ActivityActionsListener mActivityActionsListener;

    @BindView(R.id.viewpager)
    ViewPager mViewPager;
    @BindView(R.id.tab_layout)
    TabLayout mTabLayout;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    private MyPagerAdapter mFragmentAdapter;
    private FabFragment mFabFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

    }

    @Override
    protected void onStart() {
        mActivityActionsListener = new MainPresenter(this);
        mActivityActionsListener.init(this);

        setupFabFragment();

        setupAdapterAndViewPager();
        super.onStart();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        getSupportFragmentManager()
                .beginTransaction()
                .remove(mFabFragment)
                .commit();

        mFabFragment.onDestroy();
        mFabFragment = null;

        super.onSaveInstanceState(outState);
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
//            = CustomTypes.ItemType.ALBUM_ID;

            switch (position) {
                case 0:
                    itemType = CustomTypes.ItemType.ALBUM_KEY;
                    break;
                case 1:
                    itemType = CustomTypes.ItemType.ARTIST_KEY;
                    break;
                case 2:
                    itemType = CustomTypes.ItemType.FOLDER;
                    break;
                case 3:
                    itemType = CustomTypes.ItemType.PLAYLIST;
                    break;
                default:
                    itemType = CustomTypes.ItemType.ALBUM_KEY;
            }

            return ItemsFragment.newInstance(itemType);
        }

        @Override
        public int getCount() {
            return 4;
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
                    title = "Folders";
                    break;
                case 3:
                    title = "Playlist";
                    break;
                default:
                    title = "Albums";
            }

            return title;
        }
    }
}
