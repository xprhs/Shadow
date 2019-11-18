package com.tencent.shadow.sample.task;

import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.tencent.shadow.sample.introduce_shadow_lib.BuildConfig;
import com.tencent.shadow.sample.util.DownloadUtil;

/**
 * 负责下载更新包
 */
public class Downloader {
    //下载的文件保存路径
    private String savePath = "";
    //要下载的文件列表
    private List<File> files = new ArrayList<>();

    private Worker worker = new Worker();

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }

    /**
     * 获取下载文件保存路径
     * @return
     */
    public String getSavePath() {
        return this.savePath;
    }

    /**
     * 获取下载文件列表
     * @return
     */
    public List<File> getFiles() {
        return this.files;
    }

    /**
     * 清空下载文件列表
     */
    public void clearFiles(){
        this.files.clear();
    }

    public void shutdown(){
        this.worker.shutdown();
    }
    public void shutdownnow(){
        this.worker.shutdownnow();
    }

    public Future take() throws InterruptedException{
        return this.worker.take();
    }

    public Future poll(){
        return this.worker.poll();
    }

    public Future poll(long timeout, TimeUnit unit) throws InterruptedException{
        return this.worker.poll(timeout,unit);
    }
    /**
     * 下载单文件
     * @param file
     * @return
     */
    public Future<File> download(File file){
        this.clearFiles();
        this.getFiles().add(file);
        Future<File> ff = this.worker.submitTask(new Task(file));
        return ff;
    }

    /**
     * 下载单文件，并设置下载文件保存地址
     * @param file
     * @param savePath
     * @return
     */
    public Future<File> download(File file,String savePath){
        this.clearFiles();
        this.getFiles().add(file);
        this.setSavePath(savePath);
        Future<File> ff = this.worker.submitTask(new Task(file));
        return ff;
    }

    /**
     * 下载多文件
     * @param files
     * @return
     */
    public List<Future<File>> download(List<File> files){
        this.clearFiles();
        this.getFiles().addAll(files);
        List<Future<File>> ffs = new ArrayList<>();
        for(File file : files){
            Future<File> ff = this.worker.submitTask(new Task(file));
            ffs.add(ff);
        }
        return ffs;
    }

    /**
     * 下载多文件，并设置下载文件保存地址
     * @param files
     * @param savePath
     * @return
     */
    public List<Future<File>> download(List<File> files,String savePath){
        this.clearFiles();
        this.getFiles().addAll(files);
        this.setSavePath(savePath);
        List<Future<File>> ffs = new ArrayList<>();
        for(File file : files){
            Future<File> ff = this.worker.submitTask(new Task(file));
            ffs.add(ff);
        }
        return ffs;
    }

    private class Task implements Callable{
        private File file;
        public Task(File file){
            this.file = file;
        }
        @Override
        public File call() throws Exception {
            String fileUrl = BuildConfig.RESOURCESHOST+this.file.getPath();
            // url服务器地址，saveurl是下载路径，fileName表示的是文件名字
            DownloadUtil.getInstance().download(null, fileUrl,savePath,this.file.getName(),  new DownloadUtil.OnDownloadListener() {
                @Override
                public void onDownloadSuccess() {
                    Log.d("Downloader","DownloadSuccess");
                }

                @Override
                public void onDownloading(int progress) {
                    Log.d("Downloader","DownloadProgress:"+progress);
                }

                @Override
                public void onDownloadFailed(Exception e) {
                    Log.d("Downloader","DownloadFailed:"+e.getMessage());
                }
            });


            return new File(savePath+this.file.getName());
        }
    }

}
