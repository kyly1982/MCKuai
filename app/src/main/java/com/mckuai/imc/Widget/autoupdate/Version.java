package com.mckuai.imc.Widget.autoupdate;

public class Version {

    /**
     * 版本号 e.g: 13
     */
    public final int code;

    /**
     * 版本名 e.g: 1.0.9
     */
    public final String name;

    /**
     * 此版本特性 e.g: Fixed bugs
     */
    public final String feature;

    /**
     * 此版本APK下载地址
     */
    public final String targetUrl;

    public Version(int code, String name, String feature, String targetUrl) {
        this.code = code;
        this.name = name;
        this.feature = feature;
        this.targetUrl = targetUrl;
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer("VERSION -> ");
        buffer.append("Code:").append(code).append(", ");
        buffer.append("Name:").append(name).append(", ");
        buffer.append("Feature:").append(feature).append(", ");
        buffer.append("TargetUrl:").append(targetUrl);
        return buffer.toString();
    }

}
