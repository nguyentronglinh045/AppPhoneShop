package com.example.phoneshopapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.phoneshopapp.R;
import com.example.phoneshopapp.models.Banner;
import java.util.List;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerViewHolder> {
    
    private List<Banner> banners;
    private OnBannerClickListener onBannerClickListener;
    
    public interface OnBannerClickListener {
        void onBannerClick(Banner banner);
    }
    
    public BannerAdapter(List<Banner> banners) {
        this.banners = banners;
    }
    
    public void setOnBannerClickListener(OnBannerClickListener listener) {
        this.onBannerClickListener = listener;
    }
    
    @NonNull
    @Override
    public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_banner, parent, false);
        return new BannerViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
        Banner banner = banners.get(position);
        holder.bind(banner);
    }
    
    @Override
    public int getItemCount() {
        return banners != null ? banners.size() : 0;
    }
    
    class BannerViewHolder extends RecyclerView.ViewHolder {
        private ImageView bannerImage;
        private TextView bannerTitle;
        private TextView bannerSubtitle;
        private TextView bannerCta;
        
        public BannerViewHolder(@NonNull View itemView) {
            super(itemView);
            bannerImage = itemView.findViewById(R.id.bannerImage);
            bannerTitle = itemView.findViewById(R.id.bannerTitle);
            bannerSubtitle = itemView.findViewById(R.id.bannerSubtitle);
            bannerCta = itemView.findViewById(R.id.bannerCta);
        }
        
        public void bind(Banner banner) {
            bannerTitle.setText(banner.getTitle());
            bannerSubtitle.setText(banner.getSubtitle());
            bannerCta.setText(banner.getCtaText());
            
            // Set image placeholder for now (you can add Glide later for actual images)
            bannerImage.setImageResource(banner.getImageResource());
            
            // Set click listeners
            bannerCta.setOnClickListener(v -> {
                if (onBannerClickListener != null) {
                    onBannerClickListener.onBannerClick(banner);
                }
            });
            
            itemView.setOnClickListener(v -> {
                if (onBannerClickListener != null) {
                    onBannerClickListener.onBannerClick(banner);
                }
            });
        }
    }
}