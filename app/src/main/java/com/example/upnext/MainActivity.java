package com.example.upnext;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.upnext.Models.NewsApiResponse;
import com.example.upnext.Models.NewsHeadlines;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity implements SelectListener {
    NewsHeadlines headlines;
    // Declare RecyclerView and adapter for displaying news
    RecyclerView recyclerView;
    CustomAdapter adapter;
    FirebaseAuth mAuth;
    private ProgressDialog progressDialog;
    private List<String> savedPosts = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase SDK
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        checkAuthentication();

        // Enable Edge-to-Edge UI compatibility
        EdgeToEdge.enable(this);

        // Set the layout file for this activity
        setContentView(R.layout.activity_main);

        // Adjust padding for the root view to handle system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets; // Return the insets to ensure proper handling

        });

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);


        // Create a RequestManager object to fetch news headlines
        RequestManager manager = new RequestManager(MainActivity.this);

        // Fetch general category news headlines without any specific query
        manager.getNewsHeadlines(listener, "general", null);
    }

    private void checkAuthentication() {
        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(MainActivity.this, Activity_Login.class));
            finish();
        }
    }

    // Listener for handling API responses
    private final onFetchDataListener<NewsApiResponse> listener = new onFetchDataListener<NewsApiResponse>() {

        @Override
        public void onFetchData(List<NewsHeadlines> list, String message) {
            // Display the fetched news in the RecyclerView
            showNews(list);

            // Save posts to Firestore
            savePostsToFirestore(list);
        }

        @Override
        public void onError(String message) {
            // Log the error message for debugging purposes
            android.util.Log.e("MainActivity", "Error fetching news: " + message);
        }
    };

    private void showNews(List<NewsHeadlines> list) {
        progressDialog.show();

        // Pass a Runnable as a callback
        fetchSavedPosts(() -> {
            recyclerView = findViewById(R.id.recycler_main);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new GridLayoutManager(MainActivity.this, 1));
            adapter = new CustomAdapter(list, savedPosts, MainActivity.this, MainActivity.this);
            recyclerView.setAdapter(adapter);
            progressDialog.dismiss();
        });
    }

    @Override
    public void OnNewsClicked(NewsHeadlines headlines) {
        if (headlines != null) {
            startActivity(new Intent(MainActivity.this, DetailsActivity.class)
                    .putExtra("data", headlines));
        } else {
            Toast.makeText(this, "Error: No news data available", Toast.LENGTH_SHORT).show();
        }
    }

    // Inflate the menu for the toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);

        return true;
    }

    // Handle toolbar menu item clicks
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_liked_posts) {// Open the "Liked Posts" activity
            startActivity(new Intent(this, LikedPostsActivity.class));
            return true;
        } else if (itemId==R.id.action_saved_posts) {
            startActivity(new Intent(this, SavedPostsActivity.class));
            return true;
        } else if (itemId == R.id.action_logout) {// Log out the user
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, Activity_Login.class));
            finish();
            return true;
        } else if (itemId == R.id.account) {
            deleteAccount();
            return true;


        }
        return super.onOptionsItemSelected(item);
    }
    private void fetchSavedPosts(Runnable onComplete) {
        if (mAuth.getCurrentUser() == null) {
            System.err.println("User is not logged in!");
            onComplete.run(); // Proceed without fetching
            return;
        }

        String uid = mAuth.getCurrentUser().getUid();
        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        savedPosts = (List<String>) documentSnapshot.get("savedPosts");
                        if (savedPosts == null) savedPosts = new ArrayList<>();
                    }
                    onComplete.run();
                })
                .addOnFailureListener(e -> {
                    System.err.println("Error fetching savedPosts: " + e.getMessage());
                    onComplete.run();
                });
    }
    public void deleteAccount() {
        new AlertDialog.Builder(this)
                .setMessage("Delete this account?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    Delete(userId);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    protected void Delete(String userId) {
        // Delete user data from Firestore
        db.collection("users").document(userId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    System.out.println("User data deleted successfully from Firestore!");

                    // Delete the user from Firebase Authentication
                    FirebaseAuth.getInstance().getCurrentUser()
                            .delete()
                            .addOnSuccessListener(aVoidAuth -> {
                                System.out.println("User account deleted from Firebase Authentication!");
                                Toast.makeText(this, "Account deleted successfully!", Toast.LENGTH_SHORT).show();

                                // Navigate to SignUp activity
                                Intent i = new Intent(MainActivity.this, Activity_SignUp.class);
                                startActivity(i);
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                System.err.println("Error deleting user from Firebase Authentication: " + e.getMessage());
                                Toast.makeText(this, "Failed to delete account from Firebase Auth!", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    System.err.println("Error deleting user document: " + e.getMessage());
                    Toast.makeText(this, "Failed to delete account data!", Toast.LENGTH_SHORT).show();
                });
    }



}
