// 
// Decompiled by Procyon v0.5.30
// 

package me.retrorealms.practiceserver.utils;

import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

public class JsonBuilder {
    private JsonObject json;

    public JsonBuilder() {
        this.initiateData();
    }

    public JsonBuilder(final String key, final Object value) {
        this.initiateData();
        this.json.addProperty(key, value.toString());
    }

    public JsonBuilder(final HashMap<String, Object> data) {
        this.initiateData();
        for (final Map.Entry<String, Object> x : data.entrySet()) {
            this.json.addProperty((String) x.getKey(), x.getValue().toString());
        }
    }

    private void initiateData() {
        this.json = new JsonObject();
    }

    public JsonBuilder setData(final String key, final Object value) {
        this.json.addProperty(key, value.toString());
        return this;
    }

    public JsonObject getJson() {
        return this.json;
    }

    @Override
    public String toString() {
        return this.json.toString();
    }
}
