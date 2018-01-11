package com.app.ssoft.securebrowser;

import java.io.Serializable;

/**
 * Created by Shekahar.Shrivastava on 27-Nov-17.
 */

public class BookmarkDetails implements Serializable {
    public String bookmarkTitle;
    public String bookmarkUrl;

    @Override
    public String toString() {
        return "BookmarkDetails{" +
                "bookmarkTitle='" + bookmarkTitle + '\'' +
                ", bookmarkUrl='" + bookmarkUrl + '\'' +
                '}';
    }
}
