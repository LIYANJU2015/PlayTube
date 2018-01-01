package org.schabi.newpipe.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

/**
 * Created by liyanju on 2017/11/19.
 */

public class YoutubeSnippetDeserializer implements JsonDeserializer<YouTubeVideos> {


    @Override
    public YouTubeVideos deserialize(JsonElement jsonElement, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        final JsonObject jsonObject = jsonElement.getAsJsonObject();
        YouTubeVideos youTubeVideos = new YouTubeVideos();
        youTubeVideos.nextPageToken = jsonObject.get("nextPageToken").getAsString();
        JsonArray jsonArray = jsonObject.get("items").getAsJsonArray();
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject item = jsonArray.get(i).getAsJsonObject();
            YouTubeVideos.Snippet snippet = context.deserialize(item.get("snippet").getAsJsonObject(),
                    YouTubeVideos.Snippet.class);
            snippet.statistics = context.deserialize(item.get("statistics").getAsJsonObject(),
                    YouTubeVideos.Snippet.Statistics.class);
            snippet.contentDetails = context.deserialize(item.get("contentDetails").getAsJsonObject(),
                    YouTubeVideos.Snippet.ContentDetails.class);
            snippet.vid = item.get("id").getAsString();
            youTubeVideos.items.add(snippet);
        }
        return youTubeVideos;
    }
}
