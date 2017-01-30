package com.jeffthefate.dmbquiz;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Created by jeff on 1/28/2017.
 */

public class Log {

    public Log() {}

    private String androidVersion;
    private String appPackage;
    private String deviceModel;
    private String packageVersion;
    private String stacktrace;

    public String getAndroidVersion() {
        return androidVersion;
    }

    public void setAndroidVersion(String androidVersion) {
        this.androidVersion = androidVersion;
    }

    public String getAppPackage() {
        return appPackage;
    }

    public void setAppPackage(String appPackage) {
        this.appPackage = appPackage;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    public String getPackageVersion() {
        return packageVersion;
    }

    public void setPackageVersion(String packageVersion) {
        this.packageVersion = packageVersion;
    }

    public String getStacktrace() {
        return stacktrace;
    }

    public void setStacktrace(String stacktrace) {
        this.stacktrace = stacktrace;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Log log = (Log) o;

        return new EqualsBuilder()
                .append(androidVersion, log.androidVersion)
                .append(appPackage, log.appPackage)
                .append(deviceModel, log.deviceModel)
                .append(packageVersion, log.packageVersion)
                .append(stacktrace, log.stacktrace)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(androidVersion)
                .append(appPackage)
                .append(deviceModel)
                .append(packageVersion)
                .append(stacktrace)
                .toHashCode();
    }

    @Override
    public String toString() {
        return "Log{" +
                "androidVersion='" + androidVersion + '\'' +
                ", appPackage='" + appPackage + '\'' +
                ", deviceModel='" + deviceModel + '\'' +
                ", packageVersion='" + packageVersion + '\'' +
                ", stacktrace='" + stacktrace + '\'' +
                '}';
    }
}
