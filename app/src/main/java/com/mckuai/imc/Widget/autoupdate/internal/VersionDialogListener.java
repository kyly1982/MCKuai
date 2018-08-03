package com.mckuai.imc.Widget.autoupdate.internal;

public interface VersionDialogListener {
    void doUpdate(boolean laterOnWifi);

    void doIgnore();
}
