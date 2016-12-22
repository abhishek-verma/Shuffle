package com.inpen.shuffle.mainscreen.items;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.inpen.shuffle.R;
import com.inpen.shuffle.model.repositories.SelectedItemsRepository;
import com.inpen.shuffle.utility.CustomTypes;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.google.gson.internal.$Gson$Preconditions.checkNotNull;

public class ItemsFragment extends Fragment implements ItemsContract.ItemsView {

    private static final String EXTRA_INT_ITEM_TYPE = "item_type";
    public ItemsAdapter mItemsAdapter;
    ItemsContract.ItemsFragmentListener mActionsListener;
    @BindView(R.id.itemRecyclerView)
    RecyclerView mRecyclerView;
    @BindView(R.id.emptyView)
    TextView mEmptyView;
    private CustomTypes.ItemType mItemType;

    public ItemsFragment() {
        // Required empty public constructor
    }

    public static ItemsFragment newInstance(@NonNull CustomTypes.ItemType itemType) {

        ItemsFragment f = new ItemsFragment();
        Bundle args = new Bundle();
        args.putInt(EXTRA_INT_ITEM_TYPE, checkNotNull(CustomTypes.ItemType.toInt(itemType)));

        f.setArguments(args);
        f.setRetainInstance(true);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handleArguments();

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

    private void handleArguments() {

        Bundle b = getArguments();

        if (b != null) {
            mItemType = CustomTypes.ItemType.fromInt(getArguments().getInt(EXTRA_INT_ITEM_TYPE));
        } else {
            mItemType = CustomTypes.ItemType.ALBUM_KEY;
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_items, container, false);

        mItemsAdapter = new ItemsAdapter(new ArrayList<Item>(0), SelectedItemsRepository.getInstance(), mItemType);

        ButterKnife.bind(this, view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // Create a grid layout with two columns
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mItemsAdapter);

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
    public void showItems(@Nullable List<Item> itemList) {
        mItemsAdapter.replaceData(itemList);

        if (itemList == null || itemList.size() == 0) {
            mRecyclerView.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
        } else {
            mRecyclerView.setVisibility(View.VISIBLE);
            mEmptyView.setVisibility(View.GONE);
        }
    }

    @Override
    public void clearSelection() {
        if (mItemsAdapter != null) {
            mItemsAdapter.clearSelection();
        }
    }

}
