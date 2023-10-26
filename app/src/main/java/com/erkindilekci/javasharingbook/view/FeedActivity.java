package com.erkindilekci.javasharingbook.view;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.erkindilekci.javasharingbook.R;
import com.erkindilekci.javasharingbook.adapter.PostAdapter;
import com.erkindilekci.javasharingbook.databinding.ActivityFeedBinding;
import com.erkindilekci.javasharingbook.model.Post;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Map;

public class FeedActivity extends AppCompatActivity {

    private ActivityFeedBinding binding;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    private ArrayList<Post> posts;

    private PostAdapter postAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFeedBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        binding.toolbar2.setTitle("Activity Feed");
        binding.toolbar2.setTitleTextColor(Color.WHITE);

        setSupportActionBar(binding.toolbar2);

        posts = new ArrayList<>();

        getData();

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        postAdapter = new PostAdapter(this, posts);
        binding.recyclerView.setAdapter(postAdapter);
    }

    private void getData() {
        firestore.collection("Posts").orderBy("date", Query.Direction.DESCENDING).addSnapshotListener((value, error) -> {
            if (error != null) {
                Toast.makeText(this, error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            } else {
                if (value != null) {
                    for (DocumentSnapshot document : value.getDocuments()) {
                        Map<String, Object> data = document.getData();

                        String userEmail = (String) data.get("userEmail");
                        String comment = (String) data.get("comment");
                        String downloadUrl = (String) data.get("downloadUrl");

                        posts.add(new Post(userEmail, comment, downloadUrl));
                    }

                    postAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.feed_activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_sign_out) {
            mAuth.signOut();

            Intent intent = new Intent(this, MainActivity.class);
            finish();
            startActivity(intent);

            return true;
        } else if (item.getItemId() == R.id.action_add_post) {
            Intent intent = new Intent(this, UploadActivity.class);
            startActivity(intent);

            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}