package edu.androidfiletester.filetestblue;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.util.List;

import edu.androidfiletester.filetestblue.databinding.ActivityMainBinding;
import edu.androidfiletester.sharedfileopslib.util.SafFileUtils;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private ActivityResultLauncher<Intent> folderPickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        folderPickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result ->{
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null){
                        Uri uri = result.getData().getData();
                        if (uri != null){
                            this.getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                            SharedPreferences preferences = this.getSharedPreferences("saf_prefs", Context.MODE_PRIVATE);
                            preferences.edit().putString("folder_uri", uri.toString()).apply();

                            String decodedUri = Uri.decode(uri.toString());
                            Log.d("FileTestBlue", uri.toString()); //content://com.android.externalstorage.documents/tree/primary%3ADocuments%2FBlue%20Test%20Folder
                            Log.d("FileTestBlue", decodedUri); //content://com.android.externalstorage.documents/tree/primary:Documents/Blue Test Folder
                            List<String> paths = uri.getPathSegments();
                            for (String segment : paths){
                                Log.d("FileTestBlue",  segment); // tree /n primary:Documents/Blue Test Folder
                            }
                        }
                    }
                }
        );

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        folderPickerLauncher.launch(intent);
    }

}