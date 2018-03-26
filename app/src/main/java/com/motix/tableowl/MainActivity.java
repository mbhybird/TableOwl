package com.motix.tableowl;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.setThreadPolicy(
                new StrictMode
                        .ThreadPolicy
                        .Builder()
                        .permitAll()
                        .build());

        StrictMode.setVmPolicy(
                new StrictMode
                        .VmPolicy
                        .Builder()
                        .build());

        SFTPUtils sftpUtils = new SFTPUtils("47.93.25.51", "root", "l3f4qiZR1sREmwK6");
        ChannelSftp sftp = sftpUtils.connect();
        try {
            Vector<ChannelSftp.LsEntry> files = sftpUtils.listFiles("/var/www/cx");
            for (ChannelSftp.LsEntry file : files) {
                System.out.println(file.getFilename());
            }
        } catch (SftpException e) {
            e.printStackTrace();
        }

        String path = Environment.getExternalStorageDirectory() + "/temp.txt";
        File f = new File(path);
        try {
             f.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        try {
//            sftp.chmod(777,"var/www/cx");
//        } catch (SftpException e) {
//            e.printStackTrace();
//        }
        sftpUtils.uploadFile("/var/www/cx", "temp.txt", Environment.getExternalStorageDirectory().getPath(), "temp.txt");

    }
}
