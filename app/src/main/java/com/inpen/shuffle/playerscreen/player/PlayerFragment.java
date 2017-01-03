package com.inpen.shuffle.playerscreen.player;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.media.MediaMetadataCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.inpen.shuffle.R;
import com.inpen.shuffle.utility.BlurTransformation;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PlayerFragment extends Fragment implements PlayerFragmentContract.PlayerFragmentView {

    public static final String EXTRA_INT_INDEX = "index";
    @BindView(R.id.albumArt)
    ImageView mAlbumArtView;
    @BindView(R.id.songTitle)
    TextView mTitleView;
    @BindView(R.id.songArtist)
    TextView mArtistView;
    private PlayerFragmentContract.PlayerFragmentListener mListener;

    public PlayerFragment() {
        // Required empty public constructor
    }

    public static PlayerFragment newInstance(int position) {

        Bundle args = new Bundle();
        args.putInt(EXTRA_INT_INDEX, position);

        PlayerFragment fragment = new PlayerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mListener = new PlayerPresenter(this, getArguments());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ViewGroup parentView = (ViewGroup) inflater.inflate(R.layout.fragment_player, container, false);

        ButterKnife.bind(this, parentView);

        mListener.init();

        return parentView;
    }

    @Override
    public void initView(MediaMetadataCompat metadata) {
        String title = metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE);
        String artist = metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST);
        String albumArtUrl = metadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI);

        mTitleView.setText(title);
        mArtistView.setText(artist);

        Glide.with(getContext())
                .load(albumArtUrl)
                .transform(new BlurTransformation(getContext()))
                .error(getResources().getDrawable(R.drawable.ic_loading_circle, null))
                .into(mAlbumArtView);
    }

    @Override
    public void updateUIState(boolean playing) {

    }
}
