package com.inpen.shuffle.utility;

import android.text.TextUtils;

import com.inpen.shuffle.mainscreen.items.Item;
import com.inpen.shuffle.model.MutableMediaMetadata;

/**
 * Created by Abhishek on 3/26/2017.
 */

public class BaseItem implements Comparable<BaseItem> {

    public String id;
    public String imagePath;
    public String title;

    public BaseItem(String id, String title, String imagePath) {
        this.id = id;
        this.title = title;
        this.imagePath = imagePath;
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
        if (o == null || o.getClass() != BaseItem.class) {
            return false;
        }

        Item that = (Item) o;

        return TextUtils.equals(id, that.id);
    }

    @Override
    public int compareTo(BaseItem item) {
        return title.compareTo(item.title);
    }
}
