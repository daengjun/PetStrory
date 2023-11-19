package com.example.petdiary.adapter;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.petdiary.R;
import com.example.petdiary.activity.Main_Expand_ImageView;
import com.example.petdiary.model.Chat;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    private ArrayList<Chat> mDataset;
    String stMyEmail = "";
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();
    StorageReference pathReference;
    String nick;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView textView;
        public ImageView imageView;
        public TextView nickView;
        public ImageView profile;

        public MyViewHolder(View v) {
            super(v);
            textView = v.findViewById(R.id.tvChat);
            imageView = v.findViewById(R.id.ivChat);
            nickView = v.findViewById(R.id.nickChat);
            profile = v.findViewById(R.id.chatProfile);
        }
    }

    @Override
    public int getItemViewType(int position) {
//        return super.getItemViewType(position);
        if (mDataset.get(position).getEmail().equals(stMyEmail)) {
            if(mDataset.get(position).getImage() == null){
                return 1;
            }else if(mDataset.get(position).getText() == null)
                return 2;

        } else {
            if(mDataset.get(position).getImage() == null) {
                return 3;
            }else if(mDataset.get(position).getText() == null)
                return 4;
        }
        return 0;
    }

    public MyAdapter(ArrayList<Chat> myDataset, String stEmail, String nick) {
        mDataset = myDataset;
        this.stMyEmail = stEmail;
        this.nick = nick;
    }

    @Override
    public MyAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {

        View v =  LayoutInflater.from(parent.getContext())
                .inflate(R.layout.right_text_view, parent, false);
        if(viewType == 2){
            v =  LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.right_image_view, parent, false);
        }
        else if(viewType == 3){
            v =  LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.my_text_view, parent, false);
        }
        else if(viewType == 4){
            v =  LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.my_image_view, parent, false);
        }

        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (mDataset.get(position).getImage() == null) {

            if(holder.nickView != null){
                holder.nickView.setText(nick);

                db.collection("users")
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        String s = document.get("profileImg").toString();
                                        String ss = document.get("email").toString();
                                        if(ss.equals(mDataset.get(position).getEmail())) {
                                            if (s.length() > 0) {
                                                ImageView profileImage = (ImageView) holder.profile.findViewById(R.id.chatProfile);
                                                Glide.with(holder.profile.getContext()).load(s).centerCrop().override(500).into(profileImage);
                                                //Glide.with(holder.profile.getContext()).load(s).centerCrop().into(profileImage);
                                            }
                                        }
                                    }
                                } else {

                                }
                            }
                        });

            }
            holder.textView.setText(mDataset.get(position).getText());

        } else if (mDataset.get(position).getText() == null) {
            if(holder.nickView != null){
                holder.nickView.setText(nick);
                db.collection("users")
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        String s = document.get("profileImg").toString();
                                        String ss = document.get("email").toString();
                                        if(ss.equals(mDataset.get(position).getEmail())) {
                                            if (s.length() > 0) {
                                                ImageView profileImage = (ImageView) holder.profile.findViewById(R.id.chatProfile);
                                                Glide.with(holder.profile.getContext()).load(s).centerCrop().override(500).into(profileImage);
                                                //Glide.with(holder.profile.getContext()).load(s).centerCrop().into(profileImage);
                                            }
                                        }
                                    }
                                } else {

                                }
                            }
                        });

            }

            Glide.with(holder.imageView.getContext()).load(mDataset.get(position).getImage()).into(holder.imageView);

            holder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Intent intent = new Intent(holder.itemView.getContext(), Main_Expand_ImageView.class);
                    intent.putExtra("imageUrl1", mDataset.get(position).getImage());
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    holder.itemView.getContext().startActivity(intent);
                }
            });

        }
    }
    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
