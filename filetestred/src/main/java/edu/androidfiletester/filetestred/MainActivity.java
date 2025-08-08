package edu.androidfiletester.filetestred;

import android.os.Bundle;
import android.util.Log;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import edu.androidfiletester.filetestred.databinding.ActivityMainBinding;
import edu.androidfiletester.sharedfileopslib.appspecific.PrivateFileNavigator;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            PrivateFileNavigator.initPrivateStorage(this);
            String folder = PrivateFileNavigator.getPrivateFilePath();
            Log.d("AppInit", "Private folder ready at: " + folder);
        } catch (IllegalStateException e) {
            Log.e("AppInit", "Failed to init private folder", e);
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());



        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home,
                R.id.navigation_dashboard,
                R.id.navigation_notifications,
                R.id.navigation_app_specific)
                .build();
        NavController navController = Navigation.findNavController(this,
                R.id.nav_host_fragment_activity_main);

        NavigationUI.setupActionBarWithNavController(this,
                navController,
                appBarConfiguration);

        NavigationUI.setupWithNavController(binding.navView,
                navController);
    }
}