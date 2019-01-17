package cn.skyui.module.ugc.data.model.ugc;

import com.chad.library.adapter.base.entity.MultiItemEntity;

/**
 * Created by tiansj on 2018/4/14.
 */

public class PhotoItem implements MultiItemEntity {

    public static final int ADD = 1;
    public static final int IMG = 2;
    private int itemType;

    private String imagePath;

    public PhotoItem(int itemType) {
        this.itemType = itemType;
    }

    @Override
    public int getItemType() {
        return itemType;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
