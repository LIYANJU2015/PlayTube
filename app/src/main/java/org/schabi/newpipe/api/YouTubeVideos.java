package org.schabi.newpipe.api;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by liyanju on 2017/12/10.
 */

public class YouTubeVideos {

    public String nextPageToken;

    public ArrayList<Snippet> items = new ArrayList<>();

    public static class Snippet {

        public String publishedAt;
        public String channelId;
        public String title;
        public String description;
        public ThumbnailsBean thumbnails;
        public String channelTitle;
        public String categoryId;

        public String vid;

        public ContentDetails contentDetails;

        public Statistics statistics;

        public static class Statistics {
            private String viewCount;
            private String likeCount;
            private String dislikeCount;
            private String favoriteCount;
            private String commentCount;

            public String getViewCount() {
                return viewCount;
            }

            public void setViewCount(String viewCount) {
                this.viewCount = viewCount;
            }

            public String getLikeCount() {
                return likeCount;
            }

            public void setLikeCount(String likeCount) {
                this.likeCount = likeCount;
            }

            public String getDislikeCount() {
                return dislikeCount;
            }

            public void setDislikeCount(String dislikeCount) {
                this.dislikeCount = dislikeCount;
            }

            public String getFavoriteCount() {
                return favoriteCount;
            }

            public void setFavoriteCount(String favoriteCount) {
                this.favoriteCount = favoriteCount;
            }

            public String getCommentCount() {
                return commentCount;
            }

            public void setCommentCount(String commentCount) {
                this.commentCount = commentCount;
            }
        }

        public static class ContentDetails {
            public String duration;
            public String dimension;
            public String definition;
            public String caption;
            public boolean licensedContent;
            public String projection;
        }


        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (obj instanceof Snippet
                    && (((Snippet) obj).title.equals(title) && (((Snippet) obj).description
                    .equals(description)))) {
                return true;
            }
            return false;
        }

        public static class ThumbnailsBean {

            @SerializedName("default")
            private DefaultBean defaultX;
            private MediumBean medium;
            private HighBean high;
            private StandardBean standard;
            private MaxresBean maxres;

            public DefaultBean getDefaultX() {
                return defaultX;
            }

            public void setDefaultX(DefaultBean defaultX) {
                this.defaultX = defaultX;
            }

            public MediumBean getMedium() {
                return medium;
            }

            public void setMedium(MediumBean medium) {
                this.medium = medium;
            }

            public HighBean getHigh() {
                return high;
            }

            public void setHigh(HighBean high) {
                this.high = high;
            }

            public StandardBean getStandard() {
                return standard;
            }

            public void setStandard(StandardBean standard) {
                this.standard = standard;
            }

            public MaxresBean getMaxres() {
                return maxres;
            }

            public void setMaxres(MaxresBean maxres) {
                this.maxres = maxres;
            }

            public static class DefaultBean {
                /**
                 * url : https://i.ytimg.com/vi/6ajP1v4Dgfs/default.jpg
                 * width : 120
                 * height : 90
                 */

                private String url;
                private int width;
                private int height;

                public String getUrl() {
                    return url;
                }

                public void setUrl(String url) {
                    this.url = url;
                }

                public int getWidth() {
                    return width;
                }

                public void setWidth(int width) {
                    this.width = width;
                }

                public int getHeight() {
                    return height;
                }

                public void setHeight(int height) {
                    this.height = height;
                }
            }

            public static class MediumBean {
                /**
                 * url : https://i.ytimg.com/vi/6ajP1v4Dgfs/mqdefault.jpg
                 * width : 320
                 * height : 180
                 */

                private String url;
                private int width;
                private int height;

                public String getUrl() {
                    return url;
                }

                public void setUrl(String url) {
                    this.url = url;
                }

                public int getWidth() {
                    return width;
                }

                public void setWidth(int width) {
                    this.width = width;
                }

                public int getHeight() {
                    return height;
                }

                public void setHeight(int height) {
                    this.height = height;
                }
            }

            public static class HighBean {
                /**
                 * url : https://i.ytimg.com/vi/6ajP1v4Dgfs/hqdefault.jpg
                 * width : 480
                 * height : 360
                 */

                private String url;
                private int width;
                private int height;

                public String getUrl() {
                    return url;
                }

                public void setUrl(String url) {
                    this.url = url;
                }

                public int getWidth() {
                    return width;
                }

                public void setWidth(int width) {
                    this.width = width;
                }

                public int getHeight() {
                    return height;
                }

                public void setHeight(int height) {
                    this.height = height;
                }
            }

            public static class StandardBean {
                /**
                 * url : https://i.ytimg.com/vi/6ajP1v4Dgfs/sddefault.jpg
                 * width : 640
                 * height : 480
                 */

                private String url;
                private int width;
                private int height;

                public String getUrl() {
                    return url;
                }

                public void setUrl(String url) {
                    this.url = url;
                }

                public int getWidth() {
                    return width;
                }

                public void setWidth(int width) {
                    this.width = width;
                }

                public int getHeight() {
                    return height;
                }

                public void setHeight(int height) {
                    this.height = height;
                }
            }

            public static class MaxresBean {
                /**
                 * url : https://i.ytimg.com/vi/6ajP1v4Dgfs/maxresdefault.jpg
                 * width : 1280
                 * height : 720
                 */

                private String url;
                private int width;
                private int height;

                public String getUrl() {
                    return url;
                }

                public void setUrl(String url) {
                    this.url = url;
                }

                public int getWidth() {
                    return width;
                }

                public void setWidth(int width) {
                    this.width = width;
                }

                public int getHeight() {
                    return height;
                }

                public void setHeight(int height) {
                    this.height = height;
                }
            }
        }
    }
}
