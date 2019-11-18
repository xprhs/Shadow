package com.tencent.shadow.sample.introduce_shadow_lib;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.tencent.shadow.core.common.LoggerFactory;
import com.tencent.shadow.dynamic.host.DynamicPluginManager;
import com.tencent.shadow.dynamic.host.DynamicRuntime;
import com.tencent.shadow.dynamic.host.PluginManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import static android.os.Process.myPid;

public class InitApplication {
    private static List<File> managerList = new ArrayList<File>();
    private static List<File> pluginList = new ArrayList<File>();

    private static List<Future<File>> preManagerList = new ArrayList<Future<File>>();

    public static List<File> getManagerList() {
        return managerList;
    }

    public static List<File> getPluginList() {
        return pluginList;
    }
    /**
     * 这个PluginManager对象在Manager升级前后是不变的。它内部持有具体实现，升级时更换具体实现。
     */
    private static PluginManager sPluginManager;

    public static PluginManager getPluginManager() {
        return sPluginManager;
    }

    public static void onApplicationCreate(Application application) {
        if (isProcess(application, ":plugin")) {
            //在全动态架构中，Activity组件没有打包在宿主而是位于被动态加载的runtime，
            //为了防止插件crash后，系统自动恢复crash前的Activity组件，此时由于没有加载runtime而发生classNotFound异常，导致二次crash
            //因此这里恢复加载上一次的runtime
            DynamicRuntime.recoveryRuntime(application);
        }

        //Log接口Manager也需要使用，所以主进程也初始化。
        LoggerFactory.setILoggerFactory(new AndroidLoggerFactory());

        initManagerList();

        FixedPathPmUpdater fixedPathPmUpdater
                = new FixedPathPmUpdater(new File(application.getApplicationContext().getFilesDir().getAbsolutePath()+"/sample-manager-debug.apk"));
        fixedPathPmUpdater.setApplication(application);
        boolean needWaitingUpdate
                = fixedPathPmUpdater.wasUpdating()//之前正在更新中，暗示更新出错了，应该放弃之前的缓存
                || fixedPathPmUpdater.getLatest() == null;//没有本地缓存
        if (needWaitingUpdate) {
            try {
                Future<File> update = fixedPathPmUpdater.update();
                fixedPathPmUpdater.setUpdating(true);
                update = fixedPathPmUpdater.getDownloader().take();//阻塞主线程
                preManagerList.clear();
                preManagerList.add(update);
                if(update.isDone()){
                    Log.d("update","done");
                    update.get();//这里是阻塞的，需要业务自行保证更新Manager足够快。
                    fixedPathPmUpdater.shutdown();
                }
//                Log.d("update","update");
//                Thread.sleep(3000);
            } catch (Exception e) {
                throw new RuntimeException("Sample程序不容错", e);
            }
            fixedPathPmUpdater.setUpdating(false);
        }
        try{
            while(fixedPathPmUpdater.getLatest() == null){
                Thread.sleep(1000);
            }
            sPluginManager = new DynamicPluginManager(fixedPathPmUpdater);
        }catch(Exception e){
            Log.d("pluginManager",e.getMessage());
        }finally{
            //尝试先返回保证app正常运行，在后面检测manager为初始化时重新初始化
            return ;
        }
    }

    private static boolean isProcess(Context context, String processName) {
        String currentProcName = null;
        ActivityManager manager =
                (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo processInfo : manager.getRunningAppProcesses()) {
            if (processInfo.pid == myPid()) {
                currentProcName = processInfo.processName;
                break;
            }
        }
        return processName.equals(currentProcName);
    }
    private static void initManagerList(){
        File f = new File("Shadow/dynamic/apks/sample-manager-debug.apk");
        managerList.add(f);
    }
}
