package com.example.upnext;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.upnext.Models.NewsHeadlines;

import java.util.List;

public class SavedPostsActivity extends BaseActivity {
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_saved_posts);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading saved posts...");
        progressDialog.setCancelable(false);
        progressDialog.show(); // Show loading dialog
        fetchSavedPostIds(new OnPostsFetchedListener() {
            @Override
            public void onPostsFetched(List<NewsHeadlines> posts) {
                progressDialog.dismiss(); // Dismiss loading dialog
                if (posts.isEmpty()) {
                    Toast.makeText(SavedPostsActivity.this, "No Saved posts found!", Toast.LENGTH_SHORT).show();
                } else {
                    displaySavedPosts(posts); // Display the liked posts in RecyclerView
                }
            }

            @Override
            public void onError(String errorMessage) {
                progressDialog.dismiss(); // Dismiss loading dialog
                Toast.makeText(SavedPostsActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void displaySavedPosts(List<NewsHeadlines> savedPosts) {
        RecyclerView recyclerView = findViewById(R.id.recycler_saved_posts);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize adapter with saved posts
        CustomAdapter adapter = new CustomAdapter(savedPosts, this, new SelectListener() {
            @Override
            public void OnNewsClicked(NewsHeadlines headlines) {
                startActivity(new Intent(SavedPostsActivity.this, DetailsActivity.class)
                        .putExtra("data", headlines));
            }
        });

        recyclerView.setAdapter(adapter);
    }


}
