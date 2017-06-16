package com.inpen.shuffle.model.repositories;

import android.support.v7.widget.SearchView;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Abhishek on 6/15/2017.
 */

public class SearchRepositiory implements SearchView.OnQueryTextListener {

    ///////////////////////////////////////////////////////////////////////////
    // Static fields
    ///////////////////////////////////////////////////////////////////////////

    public static SearchRepositiory mSearchRepositioryInstance;

    public static SearchRepositiory getInstance() {
        if (mSearchRepositioryInstance == null)
            mSearchRepositioryInstance = new SearchRepositiory();

        return mSearchRepositioryInstance;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Regular fields and methods
    ///////////////////////////////////////////////////////////////////////////

    public void setSearchView(SearchView searchView) {
        searchView.setOnQueryTextListener(this);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        EventBus.getDefault().post(new SearchQueryChangedEvent(newText));
        return true;
    }

    public class SearchQueryChangedEvent {
        public String searchTerm;

        public SearchQueryChangedEvent(String searchTerm) {
            this.searchTerm = searchTerm;
        }
    }
}
