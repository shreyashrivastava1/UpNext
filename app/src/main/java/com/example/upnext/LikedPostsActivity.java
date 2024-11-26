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

import java.util.ArrayList;
import java.util.List;

public class LikedPostsActivity extends BaseActivity {

    private ProgressDialog progressDialog;
    List<NewsHeadlines> savedPosts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_liked_posts);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading liked posts...");
        progressDialog.setCancelable(false);
        progressDialog.show(); // Show loading dialog

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        fetchLikedPostIds(new OnPostsFetchedListener() {
            @Override
            public void onPostsFetched(List<NewsHeadlines> posts) {
                progressDialog.dismiss(); // Dismiss loading dialog
                if (posts.isEmpty()) {
                    Toast.makeText(LikedPostsActivity.this, "No liked posts found!", Toast.LENGTH_SHORT).show();
                } else {
                    displayLikedPosts(posts); // Display the liked posts in RecyclerView
                }
            }

            @Override
            public void onError(String errorMessage) {
                progressDialog.dismiss(); // Dismiss loading dialog
                Toast.makeText(LikedPostsActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });



    }

    private void displayLikedPosts(List<NewsHeadlines> likedPosts) {
        RecyclerView recyclerView = findViewById(R.id.recycler_liked_posts);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        CustomAdapter adapter = new CustomAdapter(likedPosts, this, new SelectListener() {
            @Override
            public void OnNewsClicked(NewsHeadlines headlines) {
                startActivity(new Intent(LikedPostsActivity.this, DetailsActivity.class)
                        .putExtra("data", headlines));
            }
        });

        recyclerView.setAdapter(adapter);
    }


}
