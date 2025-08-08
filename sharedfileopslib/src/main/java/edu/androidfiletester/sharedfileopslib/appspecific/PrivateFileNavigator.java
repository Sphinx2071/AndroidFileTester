package edu.androidfiletester.sharedfileopslib.appspecific;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PrivateFileNavigator {

    private static String PRIVATE_FILE_PATH = null;
    private static final String PRIVATE_FOLDER_NAME = "Private Test Folder";

    public PrivateFileNavigator(){

    }

    public static void initPrivateStorage(Context context) {
        // Resolve new path
        PRIVATE_FILE_PATH = context.getFilesDir().getAbsolutePath() + File.separator + PRIVATE_FOLDER_NAME;
        File folder = new File(PRIVATE_FILE_PATH);

        if (!folder.exists() && !folder.mkdirs()) {
            throw new IllegalStateException("Failed to create private folder at: " + PRIVATE_FILE_PATH);
        }
    }

    public static String getPrivateFilePath(){
        return PRIVATE_FILE_PATH + File.separator;
    }

    public static boolean createFileIO(String filepath) throws IOException{
        File file = new File(PRIVATE_FILE_PATH + File.separator + filepath);
        File parent = file.getParentFile();
        if(!parent.exists())
            parent.mkdirs();

        return file.createNewFile();
    }

    public static boolean fileExistsIO(String filepath){
        File file = new File(PRIVATE_FILE_PATH, filepath);
        return file.exists();
    }

    public static void writeFileIO(String filepath, String content) throws IOException{
        File file = new File(PRIVATE_FILE_PATH, filepath);
        try (FileOutputStream outputStream = new FileOutputStream(file)){
            outputStream.write(content.getBytes(StandardCharsets.UTF_8));
        }
    }

    public static String readFileIO(File file) throws IOException{
        try (FileInputStream inputStream = new FileInputStream(file)){
            byte[] data = new byte[(int) file.length()];
            int read = inputStream.read(data);
            if(read != data.length){
                throw new IOException("Could not read file");
            }
            return new String(data, StandardCharsets.UTF_8);
        }
    }

    public static List<File> listFilesIO(File dir) throws IOException{
        File[] fileList = dir.listFiles();
        return fileList != null ? Arrays.asList(fileList) : Collections.emptyList();
    }

    public static boolean deleteFileIO(File file){
        return file.delete();
    }
}
