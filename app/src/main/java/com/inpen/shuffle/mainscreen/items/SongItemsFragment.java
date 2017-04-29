package com.inpen.shuffle.mainscreen.items;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inpen.shuffle.R;
import com.inpen.shuffle.model.repositories.SelectedItemsRepository;
import com.inpen.shuffle.utility.CustomTypes;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SongItemsFragment extends Fragment implements ItemsContract.ItemsView {

    private static final String EXTRA_INT_ITEM_TYPE = "item_type";
    public SongItemsAdapter mItemsAdapter;
    ItemsContract.ItemsFragmentListener mActionsListener;
    @BindView(R.id.itemRecyclerView)
    RecyclerView mRecyclerView;
    @BindView(R.id.emptyView)
    TextView mEmptyView;
    @BindView(R.id.shuffleAllParent)
    ViewGroup mShuffleAllParent;
    private CustomTypes.ItemType mItemType = CustomTypes.ItemType.SONG;

    public SongItemsFragment() {
        // Required empty public constructor
    }

    public static SongItemsFragment newInstance() {

        SongItemsFragment f = new SongItemsFragment();
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActionsListener = new ItemsPresenter(getLoaderManager(),
                getContext(),
                this,
                mItemType,
                SelectedItemsRepository.getInstance());
    }

    @Override
    public void onResume() {
        super.onResume();

        mActionsListener.initialize();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_song_items, container, false);

        mItemsAdapter = new SongItemsAdapter(new ArrayList<SongItem>(0), SelectedItemsRepository.getInstance(), mItemType);

        ButterKnife.bind(this, view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // Create a grid layout with two columns
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.supportsPredictiveItemAnimations(); //TODO this line is added so tha views animate on clearitems, remove if dosent work

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mItemsAdapter);

        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
        });

        return view;
    }

    @Override
    public void onStop() {
        super.onStop();

        mActionsListener.stop();
    }

    @Override
    public void setProgressIndicator(boolean active) {
        // TODO implement
    }

    @Override
    public void showItems(@Nullable List<BaseItem> itemList) {
        mItemsAdapter.replaceData(itemList);

        if (itemList == null || itemList.size() == 0) {
            mShuffleAllParent.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
        } else {
            mShuffleAllParent.setVisibility(View.VISIBLE);
            mEmptyView.setVisibility(View.GONE);
        }
    }

    @OnClick(R.id.shuffleAllParent)
    public void onShuffleAllClicked() {
        mActionsListener.shuffleAllClicked(getActivity());
    }

    @Override
    public void clearSelection() {
        if (mItemsAdapter != null) {
            mItemsAdapter.clearSelection();
        }
    }

}
