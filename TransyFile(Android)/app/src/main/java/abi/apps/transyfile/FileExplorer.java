package abi.apps.transyfile;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.SyncStateContract;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.lang.ref.WeakReference;
import java.net.InetAddress;

public class FileExplorer extends AppCompatActivity {

    private static String path;
    private static File root;
    private static File[] filesAndFolders;
    private static RecyclerView recyclerView;
    private static TextView noFilesView;
    public String serverIp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_explorer);

        path = getIntent().getStringExtra("path");
        serverIp = getIntent().getStringExtra("ip");


        root = new File(path);

        filesAndFolders = root.listFiles();

        recyclerView = findViewById(R.id.recycler_view);
        noFilesView = findViewById(R.id.no_files_view);

        if(filesAndFolders.length == 0 || filesAndFolders == null){
            noFilesView.setVisibility(View.VISIBLE);
            return;
        }

        noFilesView.setVisibility(View.INVISIBLE);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new MyAdapter(getApplicationContext(), filesAndFolders, serverIp));
    }


}