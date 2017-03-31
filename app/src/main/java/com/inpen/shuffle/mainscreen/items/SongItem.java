package com.inpen.shuffle.mainscreen.items;

import android.text.TextUtils;

/**
 * Created by Abhishek on 10/27/2016.
 */

public class SongItem extends BaseItem {
    public String artist;

    public SongItem(String id, String title, String imagePath, String artist) {
        super(id, title, imagePath);
        this.artist = artist;
    }

    @Override
    public String toString() {
        return "id:" + id + " title:" + title;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || o.getClass() != SongItem.class) {
            return false;
        }

        SongItem that = (SongItem) o;

        return TextUtils.equals(id, that.id);
    }

}
