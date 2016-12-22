package com.inpen.shuffle.mainscreen.items;

import android.text.TextUtils;

import com.inpen.shuffle.model.MutableMediaMetadata;

/**
 * Created by Abhishek on 10/27/2016.
 */

public class Item implements Comparable<Item> {

    public String id;
    public String imagePath;
    public String title;
    public int count;

    public Item(String id, String title, String imagePath, int count) {
        this.id = id;
        this.title = title;
        this.imagePath = imagePath;
        this.count = count;
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
        if (o == null || o.getClass() != MutableMediaMetadata.class) {
            return false;
        }

        Item that = (Item) o;

        return TextUtils.equals(id, that.id);
    }

    @Override
    public int compareTo(Item item) {
        return title.compareTo(item.title);
    }

}
