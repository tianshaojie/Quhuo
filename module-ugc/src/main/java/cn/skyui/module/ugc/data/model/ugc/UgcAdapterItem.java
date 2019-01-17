package cn.skyui.module.ugc.data.model.ugc;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import cn.skyui.module.ugc.data.model.user.SimpleUserVO;

/**
 * Created by tiansj on 2018/4/14.
 */

public class UgcAdapterItem implements MultiItemEntity {

    public static final int VIDEO = 1;
    public static final int IMAGE = 2;
    private int itemType;

    private SimpleUserVO user;
    private UgcItemVO ugcItem;

    public UgcAdapterItem(int itemType) {
        this.itemType = itemType;
    }

    @Override
    public int getItemType() {
        return itemType;
    }

    public UgcItemVO getUgcItem() {
        return ugcItem;
    }

    public void setUgcItem(UgcItemVO ugcItem) {
        this.ugcItem = ugcItem;
    }

    public SimpleUserVO getUser() {
        return user;
    }

    public void setUser(SimpleUserVO user) {
        this.user = user;
    }
}
