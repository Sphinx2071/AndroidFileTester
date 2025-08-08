package edu.androidfiletester.filetestred.ui.appspecific;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.androidfiletester.filetestred.R;
import edu.androidfiletester.filetestred.ViewModelFactory.ViewModelFactory;
import edu.androidfiletester.filetestred.databinding.FragmentAppSpecificBinding;
import edu.androidfiletester.filetestred.ui.adapters.AppSpecificFileListAdapter;
import edu.androidfiletester.sharedfileopslib.appspecific.PrivateFileNavigator;
import edu.androidfiletester.sharedfileopslib.util.fileTester;

public class AppSpecificFragment extends Fragment {

    private FragmentAppSpecificBinding binding;
    private AppSpecificViewModel appSpecificViewModel;
    private AppSpecificFileListAdapter appSpecificFileListAdapter;;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        ViewModelFactory factory = new ViewModelFactory(requireActivity().getApplication());

        appSpecificViewModel = new ViewModelProvider(this, factory).get(AppSpecificViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState){
        binding = FragmentAppSpecificBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        appSpecificFileListAdapter = new AppSpecificFileListAdapter(new ArrayList<>());

        binding.fileList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.fileList.setAdapter(appSpecificFileListAdapter);

        setupClickListeners();
    }

    // helper methods
    private void setupClickListeners(){
        binding.btnBrowsePrivate.setOnClickListener(v ->{
            updateAppSpecificFileList();
        });

        binding.btnCreate.setOnClickListener(v ->{
            showCreateDialog();
        });

        appSpecificFileListAdapter.setOnFileClickListener(new AppSpecificFileListAdapter.OnFileClickListener() {
            @Override
            public void onFileClick(File file) {
                if(file.isDirectory()){
                    appSpecificViewModel.setCurrDir(file);
                    updateAppSpecificFileList();
                } else {
                    showFileContent(file);
                }
            }

            @Override
            public void onFileLongClick(File file) {
                new AlertDialog.Builder(requireContext())
                        .setTitle(file.getName())
                        .setMessage("Are you sure you want to delete this file?")
                        .setPositiveButton("Yes", (dialog, which) ->{
                            appSpecificViewModel.deleteFileIO(file);
                            updateAppSpecificFileList();
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });
    }

    private void updateAppSpecificFileList(){
        List<File> fileList = appSpecificViewModel.listFilesIO();
        binding.tvCurrentPath.setText(appSpecificViewModel.getCurrDir().getValue().getName());

        appSpecificFileListAdapter.setFiles(fileList);
    }

    private void showFileContent(File file){
        String content = appSpecificViewModel.readFileIO(file);

        new AlertDialog.Builder(requireContext())
                .setTitle(file.getName())
                .setMessage(content)
                .setPositiveButton("OK", null)
                .show();
    }

    // dialog views
    private void showCreateDialog(){
        View createDialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_create, null);
        Button btnCreateFile = createDialogView.findViewById(R.id.create_file);
        EditText etFileName = createDialogView.findViewById(R.id.et_name);
        EditText etFileContent = createDialogView.findViewById(R.id.et_content);
        TextView etResultText = createDialogView.findViewById(R.id.result_text);
        Button btnDone = createDialogView.findViewById(R.id.btn_done);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Create New File")
                .setView(createDialogView)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.show();


        etFileName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                appSpecificViewModel.validateFilename(etFileName.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        appSpecificViewModel.getIsInputValid().observe(getViewLifecycleOwner(), btnCreateFile::setEnabled);

        btnCreateFile.setOnClickListener(v ->{

            try{
                String filename = etFileName.getText().toString().trim();
                String fileContent = etFileContent.getText().toString().trim();
                boolean created = appSpecificViewModel.createFile(filename);

                if(created) {
                    etResultText.setText("File created successfully");
                    appSpecificViewModel.writeFileIO(filename, fileContent);
                    etFileName.setText("");
                    etFileContent.setText("");
                    btnCreateFile.setEnabled(false);
                    updateAppSpecificFileList();
                }
                else {
                    etResultText.setText("File not created, check name");
                    btnCreateFile.setEnabled(true);
                }
            } catch (IOException e) {
                etResultText.setText("Error creating file");
                e.printStackTrace();
            }
            etResultText.setVisibility(View.VISIBLE);
            btnDone.setVisibility(View.VISIBLE);

            btnDone.setOnClickListener(view ->{
                etResultText.setVisibility(View.GONE);
                btnDone.setVisibility(View.GONE);
                dialog.dismiss();
            });
        });
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        binding = null;
    }
}
