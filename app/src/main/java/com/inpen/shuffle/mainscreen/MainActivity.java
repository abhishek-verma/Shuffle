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
import android.view.Menu;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.inpen.shuffle.R;
import com.inpen.shuffle.mainscreen.fab.FabFragment;
import com.inpen.shuffle.mainscreen.items.ItemsFragment;
import com.inpen.shuffle.mainscreen.items.SongItemsFragment;
import com.inpen.shuffle.utility.CustomTypes;
import com.yarolegovich.slidingrootnav.SlidingRootNav;
import com.yarolegovich.slidingrootnav.SlidingRootNavBuilder;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity
        implements MainScreenContract.MainView {

    private static final int PERMISSION_REQUEST_CODE = 0;
    private static final long RIPPLE_DURATION = 250;


    MainScreenContract.ActivityActionsListener mActivityActionsListener;

    @BindView(R.id.viewpager)
    ViewPager mViewPager;
    @BindView(R.id.tab_layout)
    TabLayout mTabLayout;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.content_hamburger)
    ImageView mContentHamburger;

    private MyPagerAdapter mFragmentAdapter;

    private FabFragment mFabFragment;
    private SlidingRootNav navMenuDrawer;

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
        setupNavigationMenu();
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.main_screen_menu, menu);
        mActivityActionsListener.setupSearch(menu);
        return super.onCreateOptionsMenu(menu);
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

    private void setupNavigationMenu() {

        navMenuDrawer = new SlidingRootNavBuilder(this)
                .withMenuLayout(R.layout.navbar_menu)
                .inject();

        mContentHamburger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navMenuDrawer.openMenu();
            }
        });

        LinearLayout profileGrp = (LinearLayout) findViewById(R.id.profile_group);

        profileGrp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
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
