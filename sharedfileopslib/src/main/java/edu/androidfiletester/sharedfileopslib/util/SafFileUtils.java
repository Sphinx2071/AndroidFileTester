package edu.androidfiletester.sharedfileopslib.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.UriPermission;
import android.net.Uri;

import androidx.documentfile.provider.DocumentFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SafFileUtils {
    private static final String PREFS_NAME = "saf_prefs";
    private static final String KEY_FOLDER_URI = "folder_uri";
    private static final String SHARED_FOLDER_NAME = "Public Test Folder";

    public static void setSaveFolderUri(Context cntxt, Uri uri) throws IOException{
        SharedPreferences preferences = cntxt.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        DocumentFile parentDir = DocumentFile.fromTreeUri(cntxt, uri);
        if (parentDir != null && parentDir.exists()) {
            DocumentFile testFolder = parentDir.createDirectory(SHARED_FOLDER_NAME);
        }
        preferences.edit().putString(KEY_FOLDER_URI, uri.toString()).apply();
    }

    public static Uri getSavedFolderUri(Context cntxt) throws IOException{
        SharedPreferences preferences = cntxt.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String uriString = preferences.getString(KEY_FOLDER_URI, null);
        return uriString != null ? Uri.parse(uriString) : null;
    }

    public static boolean hasReadPermission(Context cntxt, Uri uri) throws IOException{
        List<UriPermission> permissionList = cntxt.getContentResolver().getPersistedUriPermissions();
        for(UriPermission permission : permissionList){
            if (permission.getUri().equals(uri) && permission.isReadPermission()){
                return true;
            }
        }
        return false;
    }

    public static boolean hasWritePermissions(Context cntxt, Uri uri) throws IOException{
        List<UriPermission> permissionList = cntxt.getContentResolver().getPersistedUriPermissions();
        for(UriPermission permission : permissionList){
            if (permission.getUri().equals(uri) && permission.isWritePermission()){
                return true;
            }
        }
        return false;
    }

    public static boolean docFileExistsShared(Context cntxt, Uri uri){
        DocumentFile docFile = DocumentFile.fromSingleUri(cntxt, uri);
        if(docFile == null){
            return false;
        }
        return docFile.exists();
    }

    public static boolean docFileIsDirectory(Context cntxt, Uri uri){
        DocumentFile docFile = DocumentFile.fromSingleUri(cntxt, uri);
        if(docFile == null){
            return false;
        }
        return docFile.isDirectory();
    }

    public static List<DocumentFile> listSharedFolderContents(Context cntxt, Uri folderUri) throws IOException {
        List<DocumentFile> contentList = new ArrayList<>();
        DocumentFile folder = DocumentFile.fromTreeUri(cntxt, folderUri);
        if(folder != null && folder.isDirectory()) {
            contentList.addAll(Arrays.asList(folder.listFiles()));
        }
        return contentList;
    }

    public static DocumentFile createSharedDocFile(Context cntxt, Uri parentDir, String docFileName, boolean isFolder){
        DocumentFile parent = DocumentFile.fromTreeUri(cntxt, parentDir);
        DocumentFile docFile = null;
        if(parent != null && parent.isDirectory()){
            if (isFolder) {
                docFile = parent.createDirectory(docFileName);
            }
            else{
                docFile = parent.createFile("text/plain", docFileName);
            }
        }
        return docFile;
    }

    public static boolean deleteSharedDocFile(Context cntxt, Uri uri){
        DocumentFile docFile = DocumentFile.fromSingleUri(cntxt, uri);
        if(docFile == null){
            return false;
        }
        return docFile.delete();
    }

    public static String readSharedDocFile(Context cntxt, Uri uri) throws IOException {
        try (InputStream is = cntxt.getContentResolver().openInputStream(uri)){
            if (is == null){
                throw new IOException("Unable to open file");
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    public static void writeSharedDocFile(Context cntxt, Uri uri, String content) throws IOException {
        try (OutputStream os = cntxt.getContentResolver().openOutputStream(uri, "wa")){
            if (os != null) {
                os.write(content.getBytes(StandardCharsets.UTF_8));

            }
        }
    }


}
