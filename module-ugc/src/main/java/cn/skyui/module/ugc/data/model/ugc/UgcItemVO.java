package cn.skyui.module.ugc.data.model.ugc;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by tiansj on 2018/4/10.
 */

public class UgcItemVO implements Serializable, Cloneable {

    private Long id;

    private Long uid;

    private Integer type;

    private String title;

    private String images;

    private String image;

    private Date createTime;

    private String video;

    private String cover;

    private Long pv = 0L;

    private Long uv = 0L;

    private Long likes = 0L;

    private Long comments = 0L;

    private double distance;

    private String poiname;

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public String getPoiname() {
        return poiname;
    }

    public void setPoiname(String poiname) {
        this.poiname = poiname;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getVideo() {
        return video;
    }

    public void setVideo(String video) {
        this.video = video;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public Long getPv() {
        return pv;
    }

    public void setPv(Long pv) {
        this.pv = pv;
    }

    public Long getUv() {
        return uv;
    }

    public void setUv(Long uv) {
        this.uv = uv;
    }

    public Long getLikes() {
        return likes;
    }

    public void setLikes(Long likes) {
        this.likes = likes;
    }

    public Long getComments() {
        return comments;
    }

    public void setComments(Long comments) {
        this.comments = comments;
    }

    @Override
    public Object clone() {
        UgcItemVO itemVO = null;
        try{
            itemVO = (UgcItemVO)super.clone();
        }catch(CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return itemVO;
    }


}
