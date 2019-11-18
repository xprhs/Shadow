package com.tencent.shadow.sample.introduce_shadow_lib;

import android.app.Application;

import com.tencent.shadow.dynamic.host.PluginManagerUpdater;
import com.tencent.shadow.sample.task.Downloader;

import java.io.File;
import java.util.concurrent.Future;

/**
 * 这个Updater没有任何升级能力。直接将指定路径作为其升级结果。
 */
public class FixedPathPmUpdater implements PluginManagerUpdater {

    final private File apk;
    private boolean updating = false;
    private Application application;
    private Downloader downloader = new Downloader();

    FixedPathPmUpdater(File apk) {
        this.apk = apk;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public Downloader getDownloader() {
        return downloader;
    }

    public void setUpdating(boolean updating) {
        this.updating = updating;
    }

    @Override
    public boolean wasUpdating() {
        return this.updating;
    }

    @Override
    public Future<File> update() {
        String path = this.application.getApplicationContext().getFilesDir().getAbsolutePath();

        Future<File> ff = null;
        try{
            ff = this.downloader.download(InitApplication.getManagerList().get(0),path);
        }catch(Exception e){

        }finally {
            return ff;
        }
    }

    @Override
    public File getLatest() {
        if(apk.exists()){
            return apk;
        }else{
            return null;
        }
    }

    @Override
    public Future<Boolean> isAvailable(final File file) {
        return null;
    }
    public void shutdown(){
        this.downloader.shutdown();
    }
    public void shutdownnow(){
        this.downloader.shutdownnow();
    }
}