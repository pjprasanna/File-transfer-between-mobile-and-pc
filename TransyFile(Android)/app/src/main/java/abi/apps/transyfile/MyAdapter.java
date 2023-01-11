package abi.apps.transyfile;

import android.content.Context;
import android.content.Intent;
import android.nfc.Tag;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

    Context context;
    File[] filesAndFolders;

    private static Socket Soc;
    public String serverIp;
    public static boolean sent = false;
    
    public MyAdapter(Context context, File[] filesAndFolders, String serverIp){
        this.context = context;
        this.filesAndFolders = filesAndFolders;
        this.serverIp = serverIp;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.file_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        File selectedFile = filesAndFolders[position];

//        int fileSize = Integer.parseInt(String.valueOf(selectedFile.length() / (1024)));
//        if(fileSize < 1024)
//            holder.fileSizeView.setText(fileSize + " KB");
//        else if(fileSize < 1024 * 1024)
//            holder.fileNameView.setText(fileSize / 1024 + " MB");
//        else
//            holder.fileSizeView.setText(fileSize / (1024 * 1024) + " GB");

        holder.fileNameView.setText(selectedFile.getName());

        if(selectedFile.isDirectory()){
            holder.iconView.setImageResource(R.drawable.ic_baseline_folder_24);
            holder.fileSizeView.setText(null);
        }
        else
            holder.iconView.setImageResource(R.drawable.ic_baseline_file_open_24);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(selectedFile.isDirectory()){
                    Intent intent = new Intent(context, FileExplorer.class);
                    String path = selectedFile.getAbsolutePath();
                    intent.putExtra("path", path);
                    intent.putExtra("ip", serverIp);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
                else {
                    PopupMenu popupMenu = new PopupMenu(context, holder.itemView);
                    popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {

                            if (menuItem.getTitle().toString().equals("Send")) {
                                Thread thread = new Thread(new SendFile(selectedFile.getAbsolutePath(), serverIp, selectedFile.getName()));
                                thread.start();
                                while(thread.isAlive());
                                if(sent)
                                    Toast.makeText(context, "File Sent Successfully", Toast.LENGTH_SHORT).show();
                            }
                            return true;
                        }
                    });
                    popupMenu.show();
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return filesAndFolders.length;
    }

    static class ViewHolder extends RecyclerView.ViewHolder{

        TextView fileNameView, fileSizeView;
        ImageView iconView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            fileNameView = itemView.findViewById(R.id.file_name_view);
            fileSizeView = itemView.findViewById(R.id.file_size_view);
            iconView = itemView.findViewById(R.id.icon_view);
        }
    }
    
    static class SendFile implements Runnable{

        private String path;
        private String ipAddr;
        private String fileName;

        public SendFile(String path, String ipAddr, String fileName) {
            this.path = path;
            this.ipAddr = ipAddr;
            this.fileName = fileName;
        }

        @Override
        public void run() {

            try {

                Soc = new Socket(ipAddr, 1908);
                FileInputStream fin = new FileInputStream(path);
                DataOutputStream dout = new DataOutputStream(Soc.getOutputStream());

                dout.writeUTF(fileName);


                int readByte;
                while((readByte = fin.read()) != -1)
                    dout.writeUTF(String.valueOf(readByte));

                dout.writeUTF("xxxxxxxxxxxxxxxxxxxxxx");

                DataInputStream din = new DataInputStream(Soc.getInputStream());

                while(!din.readUTF().equals("Done"));
                sent = true;
                
                fin.close();
                dout.flush();
                din.close();
                Soc.close();

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

}
