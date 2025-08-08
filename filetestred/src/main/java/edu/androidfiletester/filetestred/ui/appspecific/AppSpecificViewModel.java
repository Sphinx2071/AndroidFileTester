package edu.androidfiletester.filetestred.ui.appspecific;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import edu.androidfiletester.sharedfileopslib.appspecific.PrivateFileNavigator;

public class AppSpecificViewModel extends AndroidViewModel {

    private final MutableLiveData<Boolean> isInputValid = new MutableLiveData<>(false);

    private final MutableLiveData<File> currDir = new MutableLiveData<>(new File(PrivateFileNavigator.getPrivateFilePath()));

    public LiveData<File> getCurrDir() {return currDir;}

    public LiveData<Boolean> getIsInputValid() {return isInputValid;}

    public AppSpecificViewModel(@NonNull Application application) {
        super(application);

    }

    public List<File> listFilesIO() {
        File currDir = getCurrDir().getValue();
        try {
            return PrivateFileNavigator.listFilesIO(currDir);
        }
        catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public String privateFilePath(){
        return PrivateFileNavigator.getPrivateFilePath();
    }

    public boolean createFile(String filename) throws IOException{
        return PrivateFileNavigator.createFileIO(filename);
    }

    public void writeFileIO(String fileName, String fileContent){
        try{
            PrivateFileNavigator.writeFileIO(fileName, fileContent);
        }
        catch(IOException e){
            e.printStackTrace();
        }

    }

    public String readFileIO(File file){
        String fileContent;
        try{
            fileContent = PrivateFileNavigator.readFileIO(file);
        }
        catch(IOException e){
            fileContent = "Unable to display file contents";
        }
        return fileContent;
    }

    public boolean deleteFileIO(File file){
        return PrivateFileNavigator.deleteFileIO(file);
    }

    public void validateFilename(String filename){
        if (!filename.isBlank()){
            isInputValid.setValue(true);
        }
    }

    public void setCurrDir(File dir){
        if (dir.isDirectory()){
            currDir.setValue(dir);
        }
    }
}