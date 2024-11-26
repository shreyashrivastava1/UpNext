package com.example.upnext;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.upnext.Models.NewsHeadlines;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class BaseActivity extends AppCompatActivity {
    protected FirebaseAuth mAuth;               // Firebase Authentication
    protected FirebaseFirestore db;            // Firebase Firestore


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();


    }

    // 2. Save a list of posts to the 'posts' collection
    public void savePostsToFirestore(List<NewsHeadlines> headlinesList) {
        for (NewsHeadlines headline : headlinesList) {
            // Use a unique field as the document ID (e.g., title or URL)
            String documentId = headline.getUrl();
            if (documentId == null || documentId.isEmpty()) {
                documentId = headline.getTitle();
            }
            documentId = documentId.replace("/", "_")
                    .replace(".", "_")
                    .replace("#", "_")
                    .replace("[", "_")
                    .replace("]", "_");

            final String finalDocumentId = documentId;
            // Check if the post already exists in Firestore
            db.collection("posts").document(documentId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (!documentSnapshot.exists()) {
                            // Post does not exist, save it
                            db.collection("posts").document(finalDocumentId).set(headline)
                                    .addOnSuccessListener(aVoid -> {
                                        System.out.println("Post saved: " + finalDocumentId);
                                    })
                                    .addOnFailureListener(e -> {
                                        System.err.println("Error saving post: " + e.getMessage());
                                    });
                        } else {
                            // Post already exists
                            System.out.println("Duplicate post skipped: " + finalDocumentId);
                        }
                    })
                    .addOnFailureListener(e -> {
                        System.err.println("Error checking post existence: " + e.getMessage());
                    });
        }
    }

    // 3. Update a user's liked or saved posts
    public void updateUserPosts(String userId, String postId, String type) {

        // type: "likedPosts" or "savedPosts"
        db.collection("users").document(userId).update(type, FieldValue.arrayUnion(postId))
                .addOnSuccessListener(aVoid -> {
                    System.out.println("Post added to " + type + " successfully!");
                })
                .addOnFailureListener(e -> {
                    System.err.println("Error updating " + type + ": " + e.getMessage());
                });
    }


    public void saveUserInfo(String userId, String email) {

        // Create a user object as a map
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("email", email);

        userInfo.put("likedPosts", new ArrayList<String>()); // Initialize empty arrays
        userInfo.put("savedPosts", new ArrayList<String>());

        // Save or update the user document in the 'users' collection
        db.collection("users").document(userId).set(userInfo)
                .addOnSuccessListener(aVoid -> {
                    System.out.println("User information saved successfully!");
                })
                .addOnFailureListener(e -> {
                    System.err.println("Error saving user information: " + e.getMessage());
                });
    }

    // Method to fetch liked post IDs
    protected void fetchLikedPostIds(OnPostsFetchedListener listener) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> likedPostIds = (List<String>) documentSnapshot.get("likedPosts");

                        if (likedPostIds != null && !likedPostIds.isEmpty()) {
                            fetchLikedPostDetails(likedPostIds, listener); // Fetch post details
                        } else {
                            listener.onPostsFetched(new ArrayList<>()); // Empty list if no liked posts
                        }
                    } else {
                        listener.onPostsFetched(new ArrayList<>());
                    }
                })
                .addOnFailureListener(e -> {
                    listener.onError("Error fetching liked posts: " + e.getMessage());
                });
    }
    protected void fetchSavedPostIds(OnPostsFetchedListener listener) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> SavedPostIds = (List<String>) documentSnapshot.get("savedPosts");
                        if (SavedPostIds != null && !SavedPostIds.isEmpty()) {
                            fetchSavedPostDetails(SavedPostIds, listener); // Fetch post details
                        } else {
                            listener.onPostsFetched(new ArrayList<>()); // Empty list if no liked posts
                        }
                    } else {
                        listener.onPostsFetched(new ArrayList<>());
                    }
                })
                .addOnFailureListener(e -> {
                    listener.onError("Error fetching liked posts: " + e.getMessage());
                });
    }

    // Method to fetch post details using liked post IDs
    protected void fetchLikedPostDetails(List<String> likedPostIds, OnPostsFetchedListener listener) {
        db.collection("posts").whereIn(FieldPath.documentId(), likedPostIds).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<NewsHeadlines> likedPosts = new ArrayList<>();
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                        NewsHeadlines post = snapshot.toObject(NewsHeadlines.class);
                        likedPosts.add(post);
                    }
                    listener.onPostsFetched(likedPosts); // Pass the fetched posts to the listener
                })
                .addOnFailureListener(e -> {
                    listener.onError("Error fetching post details: " + e.getMessage());
                });
    }
    protected void fetchSavedPostDetails(List<String> SavedPostIds, OnPostsFetchedListener listener) {
        db.collection("posts").whereIn(FieldPath.documentId(), SavedPostIds).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<NewsHeadlines> SavedPosts = new ArrayList<>();
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                        NewsHeadlines post = snapshot.toObject(NewsHeadlines.class);
                        SavedPosts.add(post);
                    }
                    listener.onPostsFetched(SavedPosts); // Pass the fetched posts to the listener
                })
                .addOnFailureListener(e -> {
                    listener.onError("Error fetching post details: " + e.getMessage());
                });
    }



    // Listener interface for post fetching callbacks
    public interface OnPostsFetchedListener {
        void onPostsFetched(List<NewsHeadlines> posts);
        void onError(String errorMessage);
    }


}
