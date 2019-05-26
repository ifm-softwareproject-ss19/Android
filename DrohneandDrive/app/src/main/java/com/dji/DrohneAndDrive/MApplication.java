package com.dji.DrohneAndDrive;

import android.app.Application;
import android.content.Context;

import com.secneo.sdk.Helper;

public class MApplication extends Application {
    private boolean registerAp=false;

    public boolean isRegisterAp() {
        return registerAp;
    }

    public void setRegisterAp(boolean registerAp) {
        this.registerAp = registerAp;
    }

    @Override
    protected void attachBaseContext(Context paramContext) {
        super.attachBaseContext(paramContext);
        Helper.install(MApplication.this);
    }
}