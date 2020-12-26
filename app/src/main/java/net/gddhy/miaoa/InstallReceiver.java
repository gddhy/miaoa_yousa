package net.gddhy.miaoa;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class InstallReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())){
            context.startService(new Intent(context,SoundPlayService.class).setData(intent.getData()));
        }
    }
}
