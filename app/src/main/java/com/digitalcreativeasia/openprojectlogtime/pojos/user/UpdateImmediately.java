
package com.digitalcreativeasia.openprojectlogtime.pojos.user;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UpdateImmediately {

    @SerializedName("href")
    @Expose
    private String href;
    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("method")
    @Expose
    private String method;

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

}
