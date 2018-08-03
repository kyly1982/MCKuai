package com.mckuai.imc.Widget.autoupdate.internal;


import com.mckuai.imc.Widget.autoupdate.ResponseParser;
import com.mckuai.imc.Widget.autoupdate.Version;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class SimpleJSONParser implements ResponseParser {

    @Override
    public Version parser(String response) {
        try {
            JSONTokener jsonParser = new JSONTokener(response);
            JSONObject json = (JSONObject) jsonParser.nextValue();
//            boolean success = json.getBoolean("success");
            Version version = null;
            if (json.has("state") && json.has("dataObject")) {
                JSONObject dataField = json.getJSONObject("dataObject");
                int code = dataField.getInt("code");
                String name = dataField.getString("name");
                String feature = dataField.getString("feature");
                String targetUrl = dataField.getString("targetUrl");
                version = new Version(code, name, feature, targetUrl);
            }
            return version;
        } catch (JSONException exp) {
            exp.printStackTrace();
            return null;
        }
    }

}
