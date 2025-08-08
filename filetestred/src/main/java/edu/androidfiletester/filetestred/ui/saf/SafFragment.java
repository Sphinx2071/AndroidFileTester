package edu.androidfiletester.filetestred.ui.saf;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.androidfiletester.filetestred.R;
import edu.androidfiletester.filetestred.ViewModelFactory.ViewModelFactory;
import edu.androidfiletester.filetestred.databinding.FragmentSafBinding;
import edu.androidfiletester.filetestred.ui.adapters.SafFileListAdapter;

public class SafFragment extends Fragment {
    private FragmentSafBinding binding;
    private SafViewModel safViewModel;
    private SafFileListAdapter safFileListAdapter;
    private ActivityResultLauncher<Intent> folderPickerLauncher;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        ViewModelFactory factory = new ViewModelFactory(requireActivity().getApplication());
        safViewModel = new ViewModelProvider(this, factory).get(SafViewModel.class);

        folderPickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),result ->{

                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null){
                        Uri userPickedUri = result.getData().getData();

                        if (userPickedUri != null){
                            ContentResolver resolver = requireContext().getContentResolver();
                            int flags = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;

                            resolver.takePersistableUriPermission(userPickedUri, flags);
                            try {
                                safViewModel.assertSharedFolder(userPickedUri);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                            verifyFolderPermissions();
                        }
                    }
                }
        );
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState){
        binding = FragmentSafBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        safFileListAdapter = new SafFileListAdapter(new ArrayList<>());
        binding.fileList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.fileList.setAdapter(safFileListAdapter);

        setupClickListeners();
    }

    private void setupClickListeners(){
        safViewModel.restoreNavigationState(requireContext());

        binding.btnSafBrowse.setOnClickListener(v -> {
            verifyFolderPermissions();
        });

        binding.btnSafCreate.setOnClickListener(v ->{
            showCreateDialog();
        });

        safFileListAdapter.setOnFileClickListener(new SafFileListAdapter.OnFileClickListener() {
            @Override
            public void onFileClick(DocumentFile documentFile) {

                if(documentFile.isDirectory()){
                    safViewModel.navigateInto(documentFile.getName());
                    updateSafFileList();
                } else {
                    showFileContent(documentFile);
                }
            }

            @Override
            public void onFileLongClick(DocumentFile documentFile) {

                new AlertDialog.Builder(requireContext())
                        .setTitle(documentFile.getName())
                        .setMessage("Are you sure you want to delete this file?")
                        .setPositiveButton("Yes", (dialog, which) ->{
                            documentFile.delete();
                            updateSafFileList();
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });
    }

    /**
     * Ensures user has SAF permissions and the shared folder is set up.
     */
    private void verifyFolderPermissions() {
        Context cntxt = requireContext();
        Uri rootTreeUri = null;

        try {
            rootTreeUri = safViewModel.getRootFolderUri(cntxt);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (rootTreeUri == null || !safViewModel.hasReadWritePermissions()) {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            folderPickerLauncher.launch(intent);
            return;
        }
        updateSafFileList();
    }

    /**
     * Updates the UI list to show files in the current SAF folder path.
     */
    private void updateSafFileList() {
        List<String> pathSegments = safViewModel.getCurrentPath();
        String path = "Documents";

        if (!pathSegments.isEmpty()) {
            path += "/" + String.join("/", pathSegments);
        }

        binding.tvCurrentPath.setText(path);
        List<DocumentFile> fileList = safViewModel.listCurrentFolderContents();
        safFileListAdapter.setFiles(fileList);
    }

    /**
     * Displays the content of the selected file in a dialog.
     */
    private void showFileContent(DocumentFile documentFile){
        String content = safViewModel.readFile(documentFile);

        new AlertDialog.Builder(requireContext())
                .setTitle(documentFile.getName())
                .setMessage(content)
                .setPositiveButton("OK", null)
                .show();
    }

    private void showCreateDialog(){
        View createDialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_create, null);
        Button btnCreateFile = createDialogView.findViewById(R.id.create_file);
        EditText etFileName = createDialogView.findViewById(R.id.et_name);
        EditText etFileContent = createDialogView.findViewById(R.id.et_content);
        TextView etResultText = createDialogView.findViewById(R.id.result_text);
        Button btnDone = createDialogView.findViewById(R.id.btn_done);
        RadioButton rbIsFolder = createDialogView.findViewById(R.id.rb_folder);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Create New File")
                .setView(createDialogView)
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();

        dialog.setOnDismissListener(dlg -> safViewModel.validateFilename(""));

        etFileName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                safViewModel.validateFilename(etFileName.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        safViewModel.getIsInputValid().observe(getViewLifecycleOwner(), btnCreateFile::setEnabled);

        btnCreateFile.setEnabled(true);

        btnCreateFile.setOnClickListener(v ->{

            String filename = etFileName.getText().toString().trim();
            boolean isFolder = rbIsFolder.isChecked();
            String fileContent = etFileContent.getText().toString().trim();

            if(safViewModel.createEntry(filename, fileContent, isFolder)) {
                etResultText.setText("File created successfully");
                etFileName.setText("");
                etFileContent.setText("");
                btnCreateFile.setEnabled(false);
                updateSafFileList();
            } else {
                etResultText.setText("File not created, check name");
                btnCreateFile.setEnabled(true);
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
