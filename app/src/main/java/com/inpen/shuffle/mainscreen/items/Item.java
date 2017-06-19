package com.inpen.shuffle.mainscreen.items;

import android.text.TextUtils;

import com.inpen.shuffle.utility.BaseItem;

/**
 * Created by Abhishek on 10/27/2016.
 */

public class Item extends BaseItem {

    public int count;

    public Item(String id, String title, String imagePath, int count) {
        super(id, title, imagePath);
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
        if (o == null || o.getClass() != Item.class) {
            return false;
        }

        Item that = (Item) o;

        return TextUtils.equals(id, that.id);
    }

}
