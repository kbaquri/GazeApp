package com.kbaquri.gaze.First;

/**
 * Created by kbaqu on 11-Feb-18.
 */

public class Category {

    private String mTitle;
    private String mName;
    private int mBackground;

    public Category(String title, String name, int background) {
        this.mTitle = title;
        this.mName = name;
        this.mBackground = background;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getName() {
        return mName;
    }

    public int getBackground() {
        return mBackground;
    }
}


