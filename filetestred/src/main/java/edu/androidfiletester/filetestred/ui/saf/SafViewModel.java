package edu.androidfiletester.filetestred.ui.saf;

import android.app.Application;
import android.content.Context;
import android.net.Uri;

import androidx.documentfile.provider.DocumentFile;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import edu.androidfiletester.sharedfileopslib.shared.SafNavigator;

public class SafViewModel extends AndroidViewModel {

    public static final String RED_SHARED_TEST_FOLDER = "Red Shared Test Folder";
    private Uri rootFolderUri; // This should always be the anchor "Documents" tree URI
    private final Deque<String> currentPathSegments = new ArrayDeque<>();

    private final MutableLiveData<Boolean> isInputValid = new MutableLiveData<>(false);

    public SafViewModel(Application application) {
        super(application);
    }

    // ------------------------- NAVIGATION STATE -------------------------

    /**
     * Returns the persisted SAF root tree URI, loading from storage if needed.
     */
    public Uri getRootFolderUri(Context cntxt) throws IOException {

        if (rootFolderUri == null) {
            rootFolderUri = SafNavigator.getRootFolderUri(cntxt);
        }

        return rootFolderUri;
    }

    /**
     * Sets the current root tree URI and resets navigation path to the root.
     */
    public void setRootFolderUri(Uri treeUri) {
        this.rootFolderUri = treeUri;
        currentPathSegments.clear(); // Reset navigation path
    }

    /**
     * Navigates down into a subfolder by adding its name to the path.
     */
    public void navigateInto(String folderName) {
        currentPathSegments.addLast(folderName);
    }

    /**
     * Navigates up one folder by removing the last segment from the path.
     */
    public void navigateUp() {
        if (!currentPathSegments.isEmpty()) {
            currentPathSegments.removeLast();
        }
    }

    /**
     * Returns the current navigation path as a List of folder names.
     */
    public List<String> getCurrentPath() {
        return new ArrayList<>(currentPathSegments);
    }

    /**
     * Restore navigation state if permissions are present.
     */
    public void restoreNavigationState(Context cntxt){
        Uri rootUri = null;
        try {
            rootUri = getRootFolderUri(cntxt);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        if (rootUri != null
                && hasReadWritePermissions()
                && currentPathSegments.isEmpty()) {
            setRootFolderUri(rootUri);
            currentPathSegments.addLast(RED_SHARED_TEST_FOLDER);
        }
    }

    // ----------------------- CORE FOLDER RESOLUTION ------------------------

    /**
     * Resolves and returns the DocumentFile for the current folder based on navigation path.
     */
    private DocumentFile resolveCurrentFolder(Context cntxt) throws IOException {
        Uri treeUri = getRootFolderUri(cntxt);
        DocumentFile folder = DocumentFile.fromTreeUri(cntxt, treeUri);

        for (String segment : currentPathSegments) {
            if (folder == null) {

                return null;
            }

            folder = folder.findFile(segment);

            if (folder == null || !folder.exists() || !folder.isDirectory()) {

                return null;
            }
        }
        return folder;
    }

    // --------------------------- SHARED FOLDER CREATION/ASSERTION ----------------------------

    /**
     * Ensures our shared folder exists under the root and navigates into it.
     */
    public DocumentFile assertSharedFolder(Uri documentsTreeUri) throws IOException {
        Context ctx = getApplication().getApplicationContext();
        DocumentFile documentsRoot = DocumentFile.fromTreeUri(ctx, documentsTreeUri);

        if (documentsRoot == null || !documentsRoot.exists() || !documentsRoot.isDirectory()) {
            throw new IOException("Documents root does not exist or is not accessible");
        }

        DocumentFile sharedFolder = documentsRoot.findFile(RED_SHARED_TEST_FOLDER);

        if (sharedFolder == null || !sharedFolder.exists()) {
            sharedFolder = documentsRoot.createDirectory(RED_SHARED_TEST_FOLDER);

            if (sharedFolder == null || !sharedFolder.exists() || !sharedFolder.isDirectory()) {
                throw new IOException("Failed to create or access shared folder");
            }
        } else if (!sharedFolder.isDirectory()) {
            throw new IOException("A file with the shared folder name already exists and is not a directory");
        }

        setRootFolderUri(documentsTreeUri);
        currentPathSegments.clear();
        currentPathSegments.addLast(sharedFolder.getName());

        SafNavigator.setRootFolderUri(ctx, documentsTreeUri);

        return sharedFolder;
    }

    // ------------------------ FILE/BROWSER INTERACTION ------------------------

    /**
     * Lists all files and folders in the current directory according to navigation state.
     */
    public ArrayList<DocumentFile> listCurrentFolderContents() {
        Context cntxt = getApplication().getApplicationContext();

        try {
            DocumentFile curr = resolveCurrentFolder(cntxt);

            if (curr != null && curr.exists() && curr.isDirectory()) {

                return SafNavigator.listFolderContents(curr);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    /**
     * Reads and returns the text content of the given DocumentFile.
     */
    public String readFile(DocumentFile file){
        Context cntxt = getApplication().getApplicationContext();

        return SafNavigator.readTextFromDocumentFile(cntxt, file);
    }

    /**
     * Attempts to enter a subfolder by name; updates navigation path if it exists.
     */
    public boolean enterSubfolder(String folderName) {

        try {
            Context cntxt = getApplication().getApplicationContext();
            DocumentFile curr = resolveCurrentFolder(cntxt);

            if (curr != null) {
                DocumentFile subDir = curr.findFile(folderName);

                if (subDir != null && subDir.exists() && subDir.isDirectory()) {
                    navigateInto(folderName);

                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }


    // ------------------------ PERMISSION CHECKS ------------------------

    /**
     * Returns true if we have read and write permissions for the anchor tree URI.
     */
    public boolean hasReadWritePermissions() {
        Context cntxt = getApplication().getApplicationContext();

        try {
            Uri treeUri = getRootFolderUri(cntxt);

            return treeUri != null &&
                    SafNavigator.hasWritePermissions(cntxt, treeUri) &&
                    SafNavigator.hasReadPermission(cntxt, treeUri);
        } catch (IOException e) {
            System.out.println("Does not have read or write permissions");
        }

        return false;
    }

    // ------------------------- ENTRY CREATION ---------------------------

    /**
     * Creates a file or folder in the current directory. For files, writes the given content.
     */
    //todo: parse entry name looking for a file separator to handle being given a path (ie folder/file should create a folder with a file in it)
    public boolean createEntry(String entryName, String content, boolean isFolder) {
        try {
            Context cntxt = getApplication().getApplicationContext();
            DocumentFile currDir = resolveCurrentFolder(cntxt);

            if (currDir == null || !currDir.isDirectory()) {

                return false;
            }

            if (isFolder) {
                DocumentFile newFolder = SafNavigator.createDirectory(currDir, entryName);

                return newFolder != null && newFolder.exists() && newFolder.isDirectory();
            } else {
                DocumentFile newFile = SafNavigator.createFile(currDir, entryName);

                if (newFile != null && newFile.exists() && newFile.isFile()) {
                    return SafNavigator.writeTextToDocumentFile(cntxt, newFile, content);
                }

                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();

            return false;
        }
    }

    /**
     * Returns LiveData that reflects whether file creation input is valid.
     */
    public LiveData<Boolean> getIsInputValid() {return isInputValid;}

    /**
     * Validates the given filename for file creation input UI.
     */
    public void validateFilename(String filename){
        isInputValid.setValue(!filename.isBlank());
    }
}

