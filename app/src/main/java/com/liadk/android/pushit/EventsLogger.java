package com.liadk.android.pushit;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.Date;

public class EventsLogger {

    private FirebaseAnalytics mFirebaseAnalytics;

    public EventsLogger(Context context) {
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
    }

    public void log(String event, String attr, String val) {
        Bundle params = new Bundle();

        params.putString("device", Build.DEVICE);
        params.putString("date", getDate());
        params.putString(attr, val);
        mFirebaseAnalytics.logEvent(event, params);
    }

    public void log(String event, String attr1, String val1, String attr2, String val2) {
        Bundle params = new Bundle();

        params.putString("device", Build.DEVICE);
        params.putString("date", getDate());
        params.putString(attr1, val1);
        params.putString(attr2, val2);
        mFirebaseAnalytics.logEvent(event, params);
    }

    public void log(String event, String attr1, String val1, String attr2, String val2, String attr3, String val3) {
        Bundle params = new Bundle();

        params.putString("device", Build.DEVICE);
        params.putString("date", getDate());
        params.putString(attr1, val1);
        params.putString(attr2, val2);
        params.putString(attr3, val3);
        mFirebaseAnalytics.logEvent(event, params);
    }

    public void log(String event, String attr1, String val1, String attr2, String val2, String attr3, String val3, String attr4, String val4) {
        Bundle params = new Bundle();

        params.putString("device", Build.DEVICE);
        params.putString("date", getDate());
        params.putString(attr1, val1);
        params.putString(attr2, val2);
        params.putString(attr3, val3);
        params.putString(attr4, val4);
        mFirebaseAnalytics.logEvent(event, params);
    }

    public void log(String event, String attr1, String val1, String attr2, String val2, String attr3, String val3, String attr4, String val4, String attr5, String val5) {
        Bundle params = new Bundle();

        params.putString("device", Build.DEVICE);
        params.putString("date", getDate());
        params.putString(attr1, val1);
        params.putString(attr2, val2);
        params.putString(attr3, val3);
        params.putString(attr4, val4);
        params.putString(attr5, val5);
        mFirebaseAnalytics.logEvent(event, params);
    }

    public void log(String event, String attr1, String val1, String attr2, String val2, String attr3, String val3, String attr4, String val4, String attr5, String val5, String attr6, String val6) {
        Bundle params = new Bundle();

        params.putString("device", Build.DEVICE);
        params.putString("date", getDate());
        params.putString(attr1, val1);
        params.putString(attr2, val2);
        params.putString(attr3, val3);
        params.putString(attr4, val4);
        params.putString(attr5, val5);
        params.putString(attr6, val6);
        mFirebaseAnalytics.logEvent(event, params);
    }

    public void log(String event, String attr, boolean val) {
        log(event, attr, val+"");
    }

    public void log(String event, String attr1, boolean val1, String attr2, boolean val2, String attr3, boolean val3, String attr4, boolean val4) {
        log(event, attr1, val1+"", attr2, val2+"", attr3, val3+"", attr4, val4+"");
    }

    public void log(String event, String attr1, boolean val1, String attr2, boolean val2, String attr3, boolean val3, String attr4, boolean val4, String attr5, boolean val5) {
        log(event, attr1, val1+"", attr2, val2+"", attr3, val3+"", attr4, val4+"", attr5, val5+"");
    }

    private String getDate() {
        return DateFormat.format("d/M/yyyy HH:mm:ss", new Date()).toString();
    }
}
