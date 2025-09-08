package com.example.literatureuniverse.model.cloudinary;

import com.google.gson.annotations.SerializedName;

public class UploadResponse {
    @SerializedName("secure_url")
    private String secureUrl;

    public String getSecureUrl() {
        return secureUrl;
    }
}
