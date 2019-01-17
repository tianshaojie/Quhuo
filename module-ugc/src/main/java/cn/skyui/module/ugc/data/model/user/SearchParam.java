package cn.skyui.module.ugc.data.model.user;

import java.io.Serializable;

public class SearchParam implements Serializable {

    private Integer pageNum;
    private Integer pageSize;

    private Long loginUid;
    private String keyword;

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Long getLoginUid() {
        return loginUid;
    }

    public void setLoginUid(Long loginUid) {
        this.loginUid = loginUid;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
}
