package com.di72nn.stuff.wallabag.apiwrapper.models;

import com.di72nn.stuff.wallabag.apiwrapper.WallabagService;
import com.squareup.moshi.Json;

/**
 * The {@code ExistsWithIdResponse} class represents the response to
 * the {@link WallabagService#articleExistsWithIdCall(String)} call.
 */
public class ExistsWithIdResponse {

    /** The ID of the article if it exists, {@code null} otherwise. */
    @Json(name = "exists")
    public Integer id;

    @Override
    public String toString() {
        return "ExistsWithIdResponse{" +
                "id=" + id +
                '}';
    }

}
