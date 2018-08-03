package com.mckuai.imc.Widget.autoupdate.internal;


import com.mckuai.imc.Widget.autoupdate.Version;

public interface ResponseCallback {
    void onFoundLatestVersion(Version version);

    void onCurrentIsLatest();
}
