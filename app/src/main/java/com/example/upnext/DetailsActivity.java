package com.example.upnext;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.upnext.Models.NewsHeadlines;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class DetailsActivity extends BaseActivity {
    NewsHeadlines headlines;
    TextView txt_title,txt_author,txt_time,txt_detail,txt_content;
    ImageView img_news;
    boolean isLiked = false;
    ImageView likeButton;
    private List<String> likedPosts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        headlines =(NewsHeadlines) getIntent().getSerializableExtra("data");
        likeButton = findViewById(R.id.likeButton); // Initialize here
       String postID = returnId(headlines.getUrl());
        likeButton.setOnClickListener(v -> isLiked(v, postID));
        checkIfPostIsLiked(postID);
        txt_title = findViewById(R.id.text_detail_title);
        txt_author=findViewById(R.id.text_detail_author);
        txt_content = findViewById(R.id.text_detail_content);
        txt_time = findViewById(R.id.text_detail_time);
        txt_detail = findViewById(R.id.text_detail_detail);
        img_news = findViewById(R.id.img_detail_news);

        txt_title.setText(headlines.getTitle());
        txt_author.setText(headlines.getAuthor());
        txt_time.setText(headlines.getPublishedAt());
        txt_detail.setText(headlines.getDescription());
        txt_content.setText(headlines.getContent());
        Picasso.get().load(headlines.getUrlToImage()).into(img_news);
        fetchLikedPosts();










    }
    private void fetchLikedPosts() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        likedPosts = (List<String>) documentSnapshot.get("likedPosts");
                        if (likedPosts == null) likedPosts = new ArrayList<>();
                        checkIfPostIsLiked(returnId(headlines.getUrl())); // Check for the current post
                    }
                })
                .addOnFailureListener(e -> {
                    System.err.println("Error fetching likedPosts: " + e.getMessage());
                });
    }
    private void checkIfPostIsLiked(String postId) {
        if (likedPosts.contains(postId)) {
            // Post is liked
            likeButton.setImageResource(R.drawable.ic_heart_filled);
            isLiked = true;
        } else {
            // Post is not liked
            likeButton.setImageResource(R.drawable.ic_like);
            isLiked = false;
        }
    }

    public String returnId(String url){
        return url.replace("/", "_")
                .replace(".", "_")
                .replace("#", "_")
                .replace("[", "_")
                .replace("]", "_");
    }
    public void isLiked(View v, String postId) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(likeButton, "scaleX", 1f, 1.5f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(likeButton, "scaleY", 1f, 1.5f, 1f);
        scaleX.setDuration(300); // 300ms animation
        scaleY.setDuration(300);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY);
        animatorSet.start();
        String uid = null;

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            System.err.println("No user is currently signed in!");
        }


         if (uid != null) {
        if (isLiked) {
            likeButton.setImageResource(R.drawable.ic_like); // Unliked state
            isLiked = false;
            // Remove postId from the likedPosts array
            db.collection("users").document(uid).update("likedPosts", FieldValue.arrayRemove(postId))
                    .addOnSuccessListener(aVoid -> {
                        System.out.println("Post removed from liked posts: " + postId);
                    })
                    .addOnFailureListener(e -> {
                        System.err.println("Error removing post from liked posts: " + e.getMessage());
                    });
        } else {
            likeButton.setImageResource(R.drawable.ic_heart_filled); // Liked state
            isLiked = true;

            // Add postId to the likedPosts array
            updateUserPosts(uid, postId, "likedPosts");
        }
    }}

}



