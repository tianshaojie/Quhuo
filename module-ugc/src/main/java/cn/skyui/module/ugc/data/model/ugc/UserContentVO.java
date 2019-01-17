package cn.skyui.module.ugc.data.model.ugc;

import java.io.Serializable;

public class UserContentVO implements Serializable {

    private Long id;

    private String title;

    private String images;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
}