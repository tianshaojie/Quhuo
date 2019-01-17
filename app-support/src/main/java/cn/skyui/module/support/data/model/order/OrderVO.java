package cn.skyui.module.support.data.model.order;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by tiansj on 2017/8/29.
 */
public class OrderVO implements Serializable {

    private Long id;

    private String sn;

    private Integer amount;

    private Integer coin;

    private Integer status;

    private Integer payType;

    private Integer payStatus;

    private Date payTime;

    private Date createTime;

    private Date updateTime;

    private String goodsName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
