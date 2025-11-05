package com.example.phoneshopapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.phoneshopapp.R;
import com.example.phoneshopapp.model.Review;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {
    private Context context;
    private List<Review> reviewList;
    private SimpleDateFormat dateFormat;

    public ReviewAdapter(Context context, List<Review> reviewList) {
        this.context = context;
        this.reviewList = reviewList;
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviewList.get(position);
        
        holder.textUserName.setText(review.getUserName());
        holder.textComment.setText(review.getComment());
        
        // Format date
        if (review.getDate() != null) {
            holder.textDate.setText(dateFormat.format(review.getDate()));
        } else {
            holder.textDate.setVisibility(View.GONE);
        }
        
        // Update star display based on rating
        updateStarDisplay(holder, review.getRating());
        
        // Show variant info if available
        String variantInfo = review.getFormattedVariantInfo();
        if (variantInfo != null && !variantInfo.isEmpty()) {
            holder.layoutVariantInfo.setVisibility(View.VISIBLE);
            holder.textVariantInfo.setText(variantInfo);
        } else {
            holder.layoutVariantInfo.setVisibility(View.GONE);
        }
        
        // Show verified purchase badge if verified
        if (review.isVerifiedPurchase()) {
            holder.layoutVerifiedBadge.setVisibility(View.VISIBLE);
        } else {
            holder.layoutVerifiedBadge.setVisibility(View.GONE);
        }
        
        // Set avatar (you can load from URL later with Glide)
        holder.imageAvatar.setImageResource(R.drawable.ic_person_24);
    }

    private void updateStarDisplay(ReviewViewHolder holder, float rating) {
        ImageView[] stars = {holder.star1, holder.star2, holder.star3, holder.star4, holder.star5};
        
        for (int i = 0; i < stars.length; i++) {
            if (i < rating) {
                stars[i].setImageResource(R.drawable.ic_star_filled);
            } else {
                stars[i].setImageResource(R.drawable.ic_star_empty);
            }
        }
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    public void updateReviews(List<Review> newReviews) {
        this.reviewList = newReviews;
        notifyDataSetChanged();
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        ImageView imageAvatar;
        TextView textUserName;
        TextView textComment;
        TextView textDate;
        View layoutVariantInfo;
        TextView textVariantInfo;
        View layoutVerifiedBadge;
        TextView textVerified;
        ImageView star1, star2, star3, star4, star5;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            imageAvatar = itemView.findViewById(R.id.imageAvatar);
            textUserName = itemView.findViewById(R.id.textUserName);
            textComment = itemView.findViewById(R.id.textComment);
            textDate = itemView.findViewById(R.id.textReviewDate);
            layoutVariantInfo = itemView.findViewById(R.id.layoutVariantInfo);
            textVariantInfo = itemView.findViewById(R.id.textVariantInfo);
            layoutVerifiedBadge = itemView.findViewById(R.id.layoutVerifiedBadge);
            textVerified = itemView.findViewById(R.id.textVerified);
            star1 = itemView.findViewById(R.id.star1);
            star2 = itemView.findViewById(R.id.star2);
            star3 = itemView.findViewById(R.id.star3);
            star4 = itemView.findViewById(R.id.star4);
            star5 = itemView.findViewById(R.id.star5);
        }
    }
}