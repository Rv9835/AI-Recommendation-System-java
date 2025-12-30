package entity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Item {
    /**
     * Simple DTO representing an item in the catalog. Use `builder()` to construct instances.
     */
    private final String id;
    private final String name;
    private final String address;
    private final String imageUrl;
    private final String url;
    private final double lat;
    private final double lon;
    private final String description;
    private final Set<String> categories;

    private Item(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.address = builder.address;
        this.imageUrl = builder.imageUrl;
        this.url = builder.url;
        this.lat = builder.lat;
        this.lon = builder.lon;
        this.description = builder.description;
        this.categories = builder.categories == null ? Collections.emptySet() : Collections.unmodifiableSet(builder.categories);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getUrl() {
        return url;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public String getDescription() {
        return description;
    }

    public Set<String> getCategories() {
        return categories;
    }

    public JSONObject toJSONObject() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("id", id);
            obj.put("name", name);
            obj.put("address", address);
            obj.put("image_url", imageUrl);
            obj.put("url", url);
            obj.put("lat", lat);
            obj.put("lon", lon);
            obj.put("description", description);
            obj.put("categories", new JSONArray(categories));
        } catch (JSONException e) {
            // Safe to ignore serialization issues for DTO
        }
        return obj;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String name;
        private String address;
        private String imageUrl;
        private String url;
        private double lat;
        private double lon;
        private String description;
        private Set<String> categories = new HashSet<>();

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setAddress(String address) {
            this.address = address;
            return this;
        }

        public Builder setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
            return this;
        }

        public Builder setUrl(String url) {
            this.url = url;
            return this;
        }

        public Builder setLat(double lat) {
            this.lat = lat;
            return this;
        }

        public Builder setLon(double lon) {
            this.lon = lon;
            return this;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder setCategories(Set<String> categories) {
            if (categories != null) {
                this.categories = new HashSet<>(categories);
            }
            return this;
        }

        public Item build() {
            return new Item(this);
        }
    }
}
