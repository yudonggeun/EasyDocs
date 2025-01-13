package io.github.yudonggeun;

import org.json.JSONObject;

public class InfoSpec implements JsonSpec {

    private final String title;
    private final String version;

    public InfoSpec(String title, String version) {
        this.title = title;
        this.version = version;
    }

    @Override
    public Object toJson() {
        JSONObject infoObject = new JSONObject();
        infoObject.put("title", title);
        infoObject.put("version", version);
        return infoObject;
    }
}
