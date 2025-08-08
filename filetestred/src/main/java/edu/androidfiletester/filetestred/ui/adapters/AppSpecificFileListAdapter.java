package edu.androidfiletester.filetestred.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.List;

import edu.androidfiletester.filetestred.R;

public class AppSpecificFileListAdapter extends RecyclerView.Adapter<AppSpecificFileListAdapter.AppSpecificFileViewHolder> {

    private List<File> files;
    private OnFileClickListener fileClickListener;
    private OnOverflowClickListener overflowClickListener;


    public interface OnFileClickListener {
        void onFileClick(File file);
        void onFileLongClick(File file);
    }

    public interface OnOverflowClickListener {
        void onOverflowClick(View anchor, File file);
    }

    public AppSpecificFileListAdapter(List<File> files) {
        this.files = files;
    }

    public void setFiles(List<File> newFiles) {
        this.files = newFiles;
        notifyDataSetChanged();
    }

    public void setOnFileClickListener(OnFileClickListener listener) {
        this.fileClickListener = listener;
    }


    public void setOnOverflowClickListener(OnOverflowClickListener listener){
        this.overflowClickListener = listener;
    }

    @NonNull
    @Override
    public AppSpecificFileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.file_item, parent, false);
        return new AppSpecificFileViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull AppSpecificFileViewHolder holder, int position) {
        File file = files.get(position);
        holder.tvFileName.setText(file.getName());

        holder.itemView.setOnClickListener(v -> {
            if (fileClickListener != null) fileClickListener.onFileClick(file);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if(fileClickListener != null) fileClickListener.onFileLongClick(file);
            return true;
        });

        holder.btnOverflow.setOnClickListener(v -> {
            if (overflowClickListener != null) overflowClickListener.onOverflowClick(v, file);
        });

        if (file.isDirectory()) {
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

    static class AppSpecificFileViewHolder extends RecyclerView.ViewHolder {
        TextView tvFileName;
        ImageButton btnOverflow;
        ImageView ivFileIcon;
        ImageView ivFolderIcon;

        AppSpecificFileViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFileName = itemView.findViewById(R.id.tv_file_name);
            btnOverflow = itemView.findViewById(R.id.btn_file_overflow);
            ivFileIcon = itemView.findViewById(R.id.iv_file_icon);
            ivFolderIcon = itemView.findViewById(R.id.iv_folder_icon);
        }
    }
}

