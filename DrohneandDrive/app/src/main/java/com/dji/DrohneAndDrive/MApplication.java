package com.dji.DrohneAndDrive;

import android.app.Application;
import android.content.Context;
import com.secneo.sdk.Helper;

/*
DIe App muss beim ersten mal Verbindung zum Internet haben um sich bei DJI zu registrieren,
Bei Erfolg wird es lokal gespeichert, sodass bei weiteren Anwendungen keine I net verbindung notwendig ist
Die registrierung ist notwendig um das mobile sdk von DJI verwenden zu können
 */
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