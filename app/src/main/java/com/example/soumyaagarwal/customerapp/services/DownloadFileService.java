package com.example.soumyaagarwal.customerapp.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;

import java.io.File;

public class DownloadFileService extends IntentService
{

    String TaskId;
    String url;
    public DownloadFileService() {
        super("Upload");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {

            TaskId = intent.getStringExtra("TaskId");
            url = intent.getStringExtra("url");
            downloadFile(url,TaskId);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void downloadFile(final String url, final String task_id) {

        String h = url;
        StorageReference mStorageRef = FirebaseStorage.getInstance().getReferenceFromUrl(url);
        File rootPath = new File(Environment.getExternalStorageDirectory(), "MeChat/Images");
        if (!rootPath.exists()) {
            rootPath.mkdirs();
        }
        String uriSting = System.currentTimeMillis() + ".jpg";

        final File localFile = new File(rootPath, uriSting);

        mStorageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot)
            {
                Log.e("firebase ", ";local tem file created  created " + localFile.toString());
                Toast.makeText(DownloadFileService.this, "Downloaded Quotation", Toast.LENGTH_SHORT).show();
                stopSelf();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception)
            {
                Toast.makeText(DownloadFileService.this, "Download Failed", Toast.LENGTH_SHORT).show();
                stopSelf();
                Log.e("firebase ", ";local tem file not created  created " + exception.toString());
            }
        }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            double fprogress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                            long bytes = taskSnapshot.getBytesTransferred();

                            String progress = String.format("%.2f", fprogress);
                            int constant = 1000;
                            if(bytes%constant == 0)
                            {
                                android.support.v4.app.NotificationCompat.Builder mBuilder =
                                        new NotificationCompat.Builder(getApplicationContext())
                                                .setSmallIcon(android.R.drawable.stat_sys_download)
                                                .setContentTitle("Downloading " + task_id + "Quotation.pdf")
                                                .setContentText(" " + progress + "% completed" );

                                NotificationManager mNotificationManager =
                                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                                mNotificationManager.notify(100, mBuilder.build());
                            }
                        }
                    });
                }
            }
