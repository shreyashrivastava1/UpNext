package com.example.upnext;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.upnext.Models.NewsHeadlines;
import com.google.firebase.firestore.FieldValue;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class CustomAdapter extends RecyclerView.Adapter<CustomViewHolder> {
    private Context context;
    private List<NewsHeadlines> headlines;
    private SelectListener listener;
    private List<String> savedPosts; // Cached saved posts from Firestore

    public CustomAdapter(List<NewsHeadlines> headlines, List<String> savedPosts, Context context, SelectListener listener) {
        this.headlines = headlines;
        this.savedPosts = savedPosts; // Pass the savedPosts list from Firestore
        this.context = context;
        this.listener = listener;
    }
    public CustomAdapter(List<NewsHeadlines> headlines, Context context, SelectListener listener) {
        this.headlines = headlines;
        this.savedPosts = new ArrayList<>();
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CustomViewHolder(LayoutInflater.from(context).inflate(R.layout.headline_list_items, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder holder, int position) {

            NewsHeadlines newsItem = headlines.get(position);

            // Bind title and source
            holder.text_title.setText(newsItem.getTitle());
            holder.text_source.setText(newsItem.getSource().getName());

            // Load image with Picasso
            if (newsItem.getUrlToImage() != null) {
                Picasso.get().load(newsItem.getUrlToImage()).into(holder.img_headline);
            }

            // Determine postId
            String postId = newsItem.getUrl().replace("/", "_").replace(".", "_");

            // Check whether the adapter is in "Saved Posts Mode"
            if (context instanceof SavedPostsActivity) {
                // All posts in SavedPostsActivity are already saved
                holder.savedbtn.setImageResource(R.drawable.ic_save_filled); // Saved state
                holder.isSaved = true;

                // Handle unsave action
                holder.savedbtn.setOnClickListener(view -> {
                    SavedPostsActivity activity = (SavedPostsActivity) context;

                    // Remove from Firestore and update UI
                    activity.db.collection("users").document(activity.mAuth.getCurrentUser().getUid())
                            .update("savedPosts", FieldValue.arrayRemove(postId))
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(context, "Post unsaved", Toast.LENGTH_SHORT).show();
                                headlines.remove(position); // Remove item from the list
                                notifyItemRemoved(position); // Notify adapter
                            })
                            .addOnFailureListener(e -> Toast.makeText(context, "Failed to unsave post", Toast.LENGTH_SHORT).show());
                });
            }  else {
                // MainActivity logic: Determine the save button state dynamically
                if (savedPosts.contains(postId)) {
                    holder.savedbtn.setImageResource(R.drawable.ic_save_filled); // Saved state
                    holder.isSaved = true;
                } else {
                    holder.savedbtn.setImageResource(R.drawable.ic_save); // Unsaved state
                    holder.isSaved = false;
                }

                // Handle save/unsave action dynamically
                holder.savedbtn.setOnClickListener(view -> {
                    MainActivity activity = (MainActivity) context;

                    if (holder.isSaved) {
                        // Unsaving the post
                        holder.savedbtn.setImageResource(R.drawable.ic_save); // Unsaved state
                        holder.isSaved = false;

                        activity.db.collection("users").document(activity.mAuth.getCurrentUser().getUid())
                                .update("savedPosts", FieldValue.arrayRemove(postId))
                                .addOnSuccessListener(aVoid -> {
                                    savedPosts.remove(postId); // Update local cache
                                    Toast.makeText(context, "Post unsaved", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> Toast.makeText(context, "Failed to unsave post", Toast.LENGTH_SHORT).show());
                    } else {
                        // Saving the post
                        holder.savedbtn.setImageResource(R.drawable.ic_save_filled); // Saved state
                        holder.isSaved = true;

                        activity.db.collection("users").document(activity.mAuth.getCurrentUser().getUid())
                                .update("savedPosts", FieldValue.arrayUnion(postId))
                                .addOnSuccessListener(aVoid -> {
                                    savedPosts.add(postId); // Update local cache
                                    Toast.makeText(context, "Post saved", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> Toast.makeText(context, "Failed to save post", Toast.LENGTH_SHORT).show());
                    }
                });
            }

            // Handle card click
            holder.cardView.setOnClickListener(view -> listener.OnNewsClicked(headlines.get(holder.getAdapterPosition())));
        }


        @Override
    public int getItemCount() {
        return headlines.size();
    }
}
