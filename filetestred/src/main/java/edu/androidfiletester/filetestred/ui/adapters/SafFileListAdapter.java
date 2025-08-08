package edu.androidfiletester.filetestred.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.androidfiletester.filetestred.R;

public class SafFileListAdapter extends RecyclerView.Adapter<SafFileListAdapter.SafFileViewHolder>{

    private List<DocumentFile> files;
    private SafFileListAdapter.OnFileClickListener fileClickListener;
    private OnOverflowClickListener overflowClickListener;

    public interface OnFileClickListener {
        void onFileClick(DocumentFile documentFile);
        void onFileLongClick(DocumentFile documentFile);
    }

    public interface OnOverflowClickListener {
        void onOverflowClick(View anchor, DocumentFile documentFile);
    }

    public SafFileListAdapter(List<DocumentFile> files) {
        this.files = files;
    }

    public void setFiles(List<DocumentFile> newFiles) {
        this.files = newFiles;
        notifyDataSetChanged();
    }

    public void setOnFileClickListener(SafFileListAdapter.OnFileClickListener fileClickListener) {
        this.fileClickListener = fileClickListener;
    }

    @NonNull
    @Override
    public SafFileListAdapter.SafFileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.file_item, parent, false);
        return new SafFileListAdapter.SafFileViewHolder(itemView);
    }
    @Override
    public void onBindViewHolder(@NonNull SafFileListAdapter.SafFileViewHolder holder, int position) {
        DocumentFile documentFile = files.get(position);
        holder.tvFileName.setText(documentFile.getName());

        holder.itemView.setOnClickListener(v -> {
            if (fileClickListener != null) fileClickListener.onFileClick(documentFile);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if(fileClickListener != null) fileClickListener.onFileLongClick(documentFile);
            return true;
        });

        if (documentFile.isDirectory()) {
            holder.ivFileIcon.setVisibility(View.GONE);
            holder.ivFolderIcon.setVisibility(View.VISIBLE);
        } else {
            holder.ivFileIcon.setVisibility(View.VISIBLE);
            holder.ivFolderIcon.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return files != null ? files.size() : 0;
    }

    static class SafFileViewHolder extends RecyclerView.ViewHolder {
        TextView tvFileName;
        ImageButton btnOverflow;
        ImageView ivFileIcon;
        ImageView ivFolderIcon;

        SafFileViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFileName = itemView.findViewById(R.id.tv_file_name);
            btnOverflow = itemView.findViewById(R.id.btn_file_overflow);
            ivFileIcon = itemView.findViewById(R.id.iv_file_icon);
            ivFolderIcon = itemView.findViewById(R.id.iv_folder_icon);
        }
    }
}
