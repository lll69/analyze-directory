package lll69.analyze;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.RippleDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.TypedArrayUtils;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static androidx.recyclerview.widget.RecyclerView.VERTICAL;
import static lll69.analyze.AnalyzeService.EXTRA_PATH;
import static lll69.analyze.Utils.formatSize;

public class AnalyzeActivity extends AppCompatActivity {
    MaterialToolbar toolbar;
    RecyclerView recyclerView;
    ContentLoadingProgressBar progressBar;
    String path;
    AnalyzeThread thread;
    ProgressThread progressThread;
    ArrayList<FileItem> fileItems = new ArrayList<>();
    DirItem currentItem;
    Adapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        path = getIntent().getStringExtra(EXTRA_PATH);
        if (TextUtils.isEmpty(path)) {
            finish();
            return;
        }
        setContentView(R.layout.activity_analyze);
        setSupportActionBar(toolbar = findViewById(R.id.toolbar));
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progress);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //setTitle(getString(R.string.analyze_details, (currentItem != null ? currentItem.path : path), 0.));
        toolbar.setSubtitle(" ");
        thread = null;
        recyclerView.setAdapter(adapter = new Adapter());
        recyclerView.setLayoutManager(new LinearLayoutManager(this, VERTICAL, false));

        refresh(0);
        if (thread == null) {
            startService(new Intent(this, AnalyzeService.class).putExtra(EXTRA_PATH, path));
        }
        progressThread = new ProgressThread();
        progressThread.start();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }

    void refresh(double progress) {
        if (thread == null) {
            for (AnalyzeService.TaskThread thread : App.getApp().threadList)
                if (path.equals(thread.path))
                    this.thread = thread;
        } else {
            if (currentItem == null) currentItem = thread.root;
            if (progress >= 1)
                toolbar.setSubtitle(null);
            else
                toolbar.setSubtitle(thread.currentFile + thread.currentType);
        }
        if (progress >= 1)
            setTitle(getString(R.string.analyze_finished, (currentItem != null ? currentItem.path : path)));
        else
            setTitle(getString(R.string.analyze_details, (currentItem != null ? currentItem.path : path), progress * 100));
        progressBar.setProgress((int) (progress * 10000));
        if (currentItem != null) {
            cloneList(fileItems, currentItem.fileItems);
            Collections.sort(fileItems, Collections.reverseOrder(new Comparator<FileItem>() {
                @Override
                public int compare(FileItem o1, FileItem o2) {
                    return Long.compare(o1.getSize(), o2.getSize());
                }
            }));
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressThread != null)
            progressThread.interrupt();
    }

    class ProgressThread extends Thread {
        public ProgressThread() {
            super("ProgressThread");
        }

        @Override
        public void run() {
            double progress = 0;
            while (!(progress >= 1 || (thread != null && thread.size >= 0))) {

                try {
                    sleep(16);
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
                if (thread != null && thread.root != null) {
                    progress = thread.root.getProgress();
                }
                final double p = progress;
                runOnUiThread(() -> refresh(p));
            }
            if (thread != null && thread.root != null) {
                progress = thread.root.getProgress();
            }
            final double p = progress;
            runOnUiThread(() -> refresh(p));
        }

    }

    class Adapter extends RecyclerView.Adapter<ItemHolder> {

        @NonNull
        @Override
        public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ItemHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.simple_list_item_2, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ItemHolder holder, int position) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = holder.getAdapterPosition();
                    if (thread != null && thread.root != null && fileItems != null && position >= 0) {
                        FileItem fileItem = fileItems.get(position);
                        if (fileItem instanceof DirItem) {
                            currentItem = (DirItem) fileItem;
                            refresh(thread.root.getProgress());
                        }
                    }
                }
            });
            if (thread == null || thread.root == null) {
                holder.title.setText(null);
                holder.summary.setText(null);
                return;
            }
            FileItem item = fileItems.get(position);
            holder.title.setText(item.name + (item.isFile ? "" : FileItem.SEPARATOR));
            holder.summary.setText(formatSize(item.getSize(), 3));
        }

        @Override
        public int getItemCount() {
            return fileItems.size();
        }
    }

    class ItemHolder extends RecyclerView.ViewHolder {
        public final TextView title, summary;

        public ItemHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(android.R.id.text1);
            summary = itemView.findViewById(android.R.id.text2);
        }
    }

    public static <T> ArrayList<T> cloneList(ArrayList<T> arrayList, Collection<T> list) {
        arrayList.clear();
        arrayList.ensureCapacity(list.size());
        arrayList.addAll(list);
        return arrayList;
    }

    @SuppressLint("RestrictedApi")
    public static int getAttr(Context c, int attr) {
        attr = TypedArrayUtils.getAttr(c, attr, attr);
        return attr;
    }

    @Override
    public void onBackPressed() {
        if (thread != null && thread.root != null && currentItem != null && currentItem.level > 0 && currentItem.parent != null) {
            currentItem = currentItem.parent;
            refresh(thread.root.getProgress());
        } else
            super.onBackPressed();
    }
}
