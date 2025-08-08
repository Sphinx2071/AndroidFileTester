package edu.androidfiletester.sharedfileopslib.shared;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.UriPermission;
import android.net.Uri;

import androidx.documentfile.provider.DocumentFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Central static utility class for all SAF (Storage Access Framework) navigation and file operations.
 * Handles SAF permissions, persistent root URI storage, and directory/file manipulation logic.
 * Designed for reuse in multiple apps—this should be your only stop for direct SAF/DocumentFile calls.
 */
public class SafNavigator {

    private static final String PREFS_NAME = "saf_prefs";
    private static final String KEY_FOLDER_URI = "folder_uri";

    /**
     * Persistently saves the tree URI for the "root" folder selected by the user.
     *
     * @param cntxt         Application or Activity Context (cannot be null)
     * @param rootFolder    The SAF tree URI to persist
     * @throws IOException if SharedPreferences are unavailable
     */
    public static void setRootFolderUri(Context cntxt, Uri rootFolder) throws IOException{
        SharedPreferences preferences = cntxt.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        preferences.edit().putString(KEY_FOLDER_URI, rootFolder.toString()).apply();
    }

    /**
     * Retrieves the persisted root folder tree URI from SharedPreferences, if previously saved.
     *
     * @param cntxt     Application or Activity Context (cannot be null)
     * @return The persisted root folder URI, or null if unset.
     * @throws IOException if SharedPreferences are unavailable
     */
    public static Uri getRootFolderUri(Context cntxt) throws IOException{
        SharedPreferences preferences = cntxt.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String rootUriString = preferences.getString(KEY_FOLDER_URI, null);
        Uri rootUri = null;

        if(rootUriString != null){
            rootUri = Uri.parse(rootUriString);
        }

        return rootUri;
    }

    /**
     * Checks if the app has read permissions for the specified SAF tree URI.
     *
     * @param cntxt     Application or Activity Context (cannot be null)
     * @param rootUri   The SAF URI to check (should be a persisted tree URI)
     * @return true if this app has read permission for the URI, false otherwise
     * @throws IOException if ContentResolver fails or unavailable
     */
    public static boolean hasReadPermission(Context cntxt, Uri rootUri) throws IOException{
        List<UriPermission> permissionList = cntxt.getContentResolver().getPersistedUriPermissions();

        for(UriPermission permission : permissionList){

            if (permission.getUri().equals(rootUri) && permission.isReadPermission()){
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the app has write permissions for the specified SAF tree URI.
     *
     * @param cntxt     Application or Activity Context (cannot be null)
     * @param rootUri   The SAF URI to check (should be a persisted tree URI)
     * @return true if this app has write permission for the URI, false otherwise
     * @throws IOException if ContentResolver fails or unavailable
     */
    public static boolean hasWritePermissions(Context cntxt, Uri rootUri) throws IOException{
        List<UriPermission> permissionList = cntxt.getContentResolver().getPersistedUriPermissions();

        for(UriPermission permission : permissionList){

            if (permission.getUri().equals(rootUri) && permission.isWritePermission()){
                return true;
            }
        }
        return false;
    }

    /**
     * Lists contents of a SAF folder anchored by the given tree URI.
     *
     * @param docfile       DocumentFile to list contents of
     * @return ArrayList of DocumentFiles in the folder, or empty list if not accessible
     */
    public static ArrayList<DocumentFile> listFolderContents(DocumentFile docfile) {
        ArrayList<DocumentFile> contentList = new ArrayList<>();

        if (docfile != null && docfile.exists() && docfile.isDirectory()) {
            DocumentFile[] files = docfile.listFiles();

            if (files != null) {
                contentList.addAll(Arrays.asList(files));
            }
        }
        return contentList;
    }

    /**
     * Creates a new directory with the given name under the specified parent folder URI.
     *
     * @param docfile       DocumentFile to create a directory in
     * @param dirName       Desired name for the new directory
     * @return DocumentFile for new directory, or null if creation failed
     */
    public static DocumentFile createDirectory(DocumentFile docfile, String dirName) {


        if (docfile != null && docfile.isDirectory()) {
            return docfile.createDirectory(dirName);
        }

        return null;
    }

    /**
     * Creates a new plain text file with the given name under the specified parent folder URI.
     *
     * @param docFile       DocumentFile to create a file in
     * @param fileName      Name of the new file to create
     * @return DocumentFile for the new file, or null if creation failed
     */
    public static DocumentFile createFile(DocumentFile docFile, String fileName) {

        if (docFile != null && docFile.isDirectory()) {
            return docFile.createFile("text/plain", fileName);
        }

        return null;
    }


    /**
     * Reads plain text content from the given DocumentFile.
     *
     * @param cntxt     Context (cannot be null)
     * @param file      DocumentFile to read (must already exist and be a file)
     * @return The text content (with line endings preserved), or null on error/missing file
     */
    public static String readTextFromDocumentFile(Context cntxt, DocumentFile file) {

        if (file == null || !file.exists() || !file.isFile()) {
            return null;
        }

        StringBuilder builder = new StringBuilder();
        try (InputStream is = cntxt.getContentResolver().openInputStream(file.getUri());
             InputStreamReader reader = new InputStreamReader(is);
             BufferedReader bufferedReader = new BufferedReader(reader)) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                builder.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return builder.toString();
    }

    /**
     * Writes the given plain text to the specified DocumentFile (must already exist and be a file).
     *
     * @param cntxt     Context (cannot be null)
     * @param file      DocumentFile to write to
     * @param content   The text to write
     * @return true if write was successful, false otherwise
     */
    public static boolean writeTextToDocumentFile(Context cntxt, DocumentFile file, String content){

        if (file == null || !file.exists() || !file.isFile()){
            return false;
        }

        try (OutputStream outputStream = cntxt.getContentResolver().openOutputStream(file.getUri());
             OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream)) {
            outputStreamWriter.write(content);
            outputStreamWriter.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Attempts to resolve and return the parent directory's URI (tree/document) for a document/file.
     * (This is strictly for advanced use and should be called with caution!)
     *
     * @param cntxt         Context (cannot be null)
     * @param folderUri     URI for a SAF file/folder (may be the root Documents tree URI or any subfolder's URI)
     * @return The parent directory's URI, or null if at root or unable to resolve
     * @throws IOException If the given URI is invalid or not found
     */
    public static Uri getParentDirUri(Context cntxt, Uri folderUri) throws IOException {
        DocumentFile docFile = DocumentFile.fromSingleUri(cntxt, folderUri);

        if(docFile == null || !docFile.exists()){
            throw new IOException();
        }

        List<String> uriParts = folderUri.getPathSegments();
        int treeIndex = uriParts.indexOf("tree");
        int docIndex = uriParts.indexOf("document");
        String parentDocId = uriParts.get(docIndex + 1);
        int lastSlash = parentDocId.lastIndexOf('/');

        if (lastSlash == -1) {
            return null;
        }

        parentDocId = Uri.encode(parentDocId.substring(0, lastSlash));

        Uri.Builder parentFolderUriBuilder = new Uri.Builder()
                .scheme(folderUri.getScheme())
                .authority(folderUri.getAuthority())
                .appendPath("tree")
                .appendPath(uriParts.get(treeIndex + 1))
                .appendPath("document")
                .appendPath(parentDocId);

        Uri parentFolderUri = parentFolderUriBuilder.build();

        if(!docFile.exists() && !docFile.isDirectory()){
            return null;
        }

        return parentFolderUri;
    }
}
