package edu.androidfiletester.filetestred.ui.tcpTransfer;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.io.*;
import java.net.*;
import java.util.*;

import edu.androidfiletester.filetestred.databinding.FragmentTcpTransferBinding;
import edu.androidfiletester.sharedfileopslib.appspecific.PrivateFileNavigator;

public class TcpTransferFragment extends Fragment {
    private String serverIp = "10.0.2.2";
    private int serverPort = 3000;

    private FragmentTcpTransferBinding binding;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentTcpTransferBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupClickListeners();
        createFilesToSend();
    }

    public void setupClickListeners(){

        binding.btnTcpSend.setOnClickListener(v ->{
            ArrayList<File> sendList = new ArrayList<>(getPrivateFilesToSend());
            System.out.println(serverPort);
            new Thread(() -> sendFilesOrFolders(sendList)).start();
        });

        binding.btnTcpGet.setOnClickListener(v -> {
            File saveTo = new File(requireContext().getFilesDir(), "test.txt");
            downloadFileFromServer("test.txt", saveTo);
        });
    }

    public void createFilesToSend(){
        try {
            PrivateFileNavigator.createFileIO("testFolder/TestFile.txt");
            PrivateFileNavigator.writeFileIO("testFolder/TestFile.txt", "test text");
            PrivateFileNavigator.writeFileIO("topLevelTestFile.txt", "some more test txt");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<File> getPrivateFilesToSend(){
        File file = new File(PrivateFileNavigator.getPrivateFilePath());
        try {
            ArrayList<File> fileList = new ArrayList<>(PrivateFileNavigator.listFilesIO(file));
            System.out.println(fileList);
            return fileList;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendFilesOrFolders(List<File> filesToSend) {
        try (Socket socket = new Socket(serverIp, serverPort);
             OutputStream out = socket.getOutputStream()) {
            for (File f : filesToSend) {
                if (f.isDirectory()) {
                    sendFolder(f, out);
                } else {
                    sendFile(f, out);
                }
            }
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
            new Handler(Looper.getMainLooper()).post(() -> {
                // update UI with error
            });
        }
    }


    private void sendFolder(File folder, OutputStream out) throws IOException {
        writeLine(out, "uploadFolder " + folder.getName());
        File[] items = folder.listFiles();
        if (items != null) {
            for (File f : items) {
                if (f.isDirectory()) {
                    sendFolder(f, out);
                } else {
                    sendFile(f, out);
                }
            }
        }
        writeLine(out, "endFolder");
    }

    private void sendFile(File file, OutputStream out) throws IOException {
        writeLine(out, "uploadFile " + file.getName());
        System.out.println("uploadFile " + file.getName());
        writeLine(out, "fileSize " + file.length());
        System.out.println("fileSize " + file.length());
        try (InputStream fin = new FileInputStream(file)) {
            byte[] buf = new byte[4096];
            int len;
            while ((len = fin.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
        }
    }

    private void downloadFileFromServer(String fileName, File saveTo) {
        new Thread(() -> {
            try (Socket socket = new Socket(serverIp, serverPort);
                 InputStream in = socket.getInputStream();
                 OutputStream out = socket.getOutputStream();
                 FileOutputStream fout = new FileOutputStream(saveTo)) {

                // 1. Send download request
                writeLine(out, "downloadFile " + fileName);

                // 2. Read fileSize line
                String fileSizeLine = readLine(in); // e.g., "fileSize 1323"
                if (fileSizeLine == null || !fileSizeLine.startsWith("fileSize ")) {
                    // handle error
                    return;
                }
                long expectedSize = Long.parseLong(fileSizeLine.substring("fileSize ".length()).trim());

                // 3. Read bytes
                byte[] buffer = new byte[4096];
                long totalRead = 0;
                while (totalRead < expectedSize) {
                    int toRead = (int)Math.min(buffer.length, expectedSize - totalRead);
                    int count = in.read(buffer, 0, toRead);
                    if (count == -1) break; // connection lost
                    fout.write(buffer, 0, count);
                    totalRead += count;
                }

                // 4. Read the endFile marker (assuming it is on a new line)
                String endLine = readLine(in);
                if (!"endFile".equals(endLine)) {
                    // handle protocol error
                    return;
                }

                // Optional: update UI, notify user
            } catch (Exception e) {
                e.printStackTrace();
                new Handler(Looper.getMainLooper()).post(() -> {
                    // update UI with error
                });
            }
        }).start();
    }

    // Helper to read lines from InputStream
    private String readLine(InputStream in) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int b;
        while ((b = in.read()) != -1) {
            if (b == '\n') break;
            baos.write(b);
        }
        return baos.size() == 0 ? null : baos.toString("UTF-8").replace("\r", "").trim();
    }



    private void writeLine(OutputStream out, String line) throws IOException {
        out.write((line + "\n").getBytes("UTF-8"));
    }
}

