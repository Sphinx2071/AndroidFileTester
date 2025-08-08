package edu.androidfiletester.filetestred.ViewModelFactory;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import edu.androidfiletester.filetestred.ui.appspecific.AppSpecificViewModel;
import edu.androidfiletester.filetestred.ui.home.HomeViewModel;
import edu.androidfiletester.filetestred.ui.saf.SafViewModel;



public class ViewModelFactory implements ViewModelProvider.Factory {
    private final Application application;


    public ViewModelFactory(Application application){
        this.application = application;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(HomeViewModel.class)){
            return (T) new HomeViewModel();
        }
        else if (modelClass.isAssignableFrom(AppSpecificViewModel.class)){
            return (T) new AppSpecificViewModel(application);
        }
        else if (modelClass.isAssignableFrom(SafViewModel.class)){
            return (T) new SafViewModel(application);
        }

        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}
