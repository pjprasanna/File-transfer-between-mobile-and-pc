package abi.apps.transyfile;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Collections;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private static TextView mobIPText;
    private static EditText ipEditText;
    private static Button sendBtn;
    private static Button receiveBtn;
    private final static String TAG = "Abishek";
    private static String path = "";
    public String serverIp;
    private String ipAddress;


    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ipEditText = findViewById(R.id.ip);
        sendBtn = findViewById(R.id.send);
        receiveBtn = findViewById(R.id.receive);
        mobIPText = findViewById(R.id.mobIP);

        try {
            ipAddress = getIPAddress(getApplicationContext());
        } catch (SocketException e) {
            e.printStackTrace();
        }

        mobIPText.setText("This Device's IP: " + ipAddress);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                serverIp = ipEditText.getText().toString();

                if(checkPermission()){

                    Intent intent = new Intent(MainActivity.this, FileExplorer.class);
                    String path = Environment.getExternalStorageDirectory().getAbsolutePath();
                    intent.putExtra("path", path);
                    intent.putExtra("ip", serverIp);
                    startActivity(intent);

                }
                else {
                    requestPermission();
                }

            }
        });

        receiveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ReceiveFile rs = new ReceiveFile();
                rs.start();
                while(rs.isAlive());
                Toast.makeText(MainActivity.this, "File Received and saved to Downloads", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getIPAddress(Context context) throws SocketException {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if(networkInfo.getType() == ConnectivityManager.TYPE_WIFI){
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
            return Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());
        }

        else if(networkInfo.getType() == ConnectivityManager.TYPE_MOBILE){
            Log.i("fgh", "yes");
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());


            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());

                for (InetAddress addr : addrs) {
                    String sAddr = addr.getHostAddress();

                    if(sAddr.contains("192.168."))
                            return sAddr;

                }
            }
        }

        return "";
    }

    private boolean checkPermission(){

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            return Environment.isExternalStorageManager();

        int readCheck = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int writeCheck = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return readCheck == PackageManager.PERMISSION_GRANTED && writeCheck == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            try{
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setData(Uri.parse(String.format("package:%s", getApplicationContext().getPackageName())));
                startActivity(intent);
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
        else
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 30);
    }

    static class ReceiveFile extends Thread{

        private ServerSocket ServerSoc;
        private Socket Soc;
        private DataInputStream din;
        private FileOutputStream fout;
        private StringBuilder filepath;
        private DataOutputStream dout;
        private File fp;

        @Override
        public void run() {
            try {
                ServerSoc = new ServerSocket(1908);
                Soc = ServerSoc.accept();

                din = new DataInputStream(Soc.getInputStream());

                if(Soc.isConnected())
                    Log.i("Abishek", "Yes Connected");

                filepath = new StringBuilder(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/" + din.readUTF());

                int i = 1;
                fp = new File(filepath.toString());

                while (fp.exists()) {
                    if (i == 1)
                        filepath.insert(filepath.indexOf("."), "(" + i + ")");
                    else {
                        int j = filepath.lastIndexOf(String.valueOf(i - 1));
                        filepath.replace(j, j + 1, "" + i + "");
                    }
                    fp = new File(filepath.toString());
                    i++;
                }

                Log.i("Abishek", filepath.toString());

                fout = new FileOutputStream(fp);

                String tmp;
                try {
                    while (!(tmp = din.readUTF()).equals("xxxxxxxxxxxxxxxxxxxxxx"))
                        fout.write(Integer.parseInt(tmp));
                } catch (Exception ex) {
                }

                dout = new DataOutputStream(Soc.getOutputStream());
                dout.writeUTF("Done");
                
                Log.i("Abishek", "File Written");

                fout.flush();
                fout.close();
                din.close();
                Soc.close();
            } catch (IOException ie) {}
        }
    }
}