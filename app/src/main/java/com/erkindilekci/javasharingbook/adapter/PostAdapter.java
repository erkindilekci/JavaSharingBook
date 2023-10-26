package com.erkindilekci.javasharingbook.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.erkindilekci.javasharingbook.R;
import com.erkindilekci.javasharingbook.databinding.RecyclerRowBinding;
import com.erkindilekci.javasharingbook.model.Post;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private Context mContext;
    private List<Post> posts;

    public PostAdapter(Context mContext, List<Post> posts) {
        this.mContext = mContext;
        this.posts = posts;
    }

    public class PostViewHolder extends RecyclerView.ViewHolder {
        private RecyclerRowBinding binding;

        public PostViewHolder(RecyclerRowBinding b) {
            super(b.getRoot());
            binding = b;
        }
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PostViewHolder(RecyclerRowBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post current = posts.get(position);

        Glide.with(mContext)
                .load(current.downloadUrl())
                .placeholder(R.drawable.loading_img)
                .fitCenter()
                .into(holder.binding.recyclerViewImageView)
        ;

        holder.binding.recyclerViewEmailText.setText(current.userEmail());
        holder.binding.recyclerViewCommentText.setText(current.comment());
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }
}
