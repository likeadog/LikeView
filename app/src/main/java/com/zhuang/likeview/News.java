package com.zhuang.likeview;

/**
 * Created by zhuang on 2017/8/3.
 */

public class News {
    private String title;
    private String content;
    private boolean hasLike;
    private int likeCount;

    public void addLikeCount(){
        likeCount++;
    }

    public void delLikeCount(){
        likeCount--;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isHasLike() {
        return hasLike;
    }

    public void setHasLike(boolean hasLike) {
        this.hasLike = hasLike;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }
}
