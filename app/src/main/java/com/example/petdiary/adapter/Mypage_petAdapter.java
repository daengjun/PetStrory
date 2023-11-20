package com.example.petdiary.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.petdiary.model.PetData;
import com.example.petdiary.R;
import com.example.petdiary.activity.AnimalProfileActivity;
import com.example.petdiary.fragment.FragmentMy;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;


public class Mypage_petAdapter extends RecyclerView.Adapter<Mypage_petAdapter.MyPagePetViewHolder> {

    private ArrayList<PetData> arrayList;
    Activity activity;
    private Context context;

    private int squareSize;
    //private int columnNum;

    private FragmentMy.StringCallback stringCallback;
    int prePosition = -1;

    private SparseBooleanArray selectedItems = new SparseBooleanArray();

    public Mypage_petAdapter(ArrayList<PetData> arrayList, Context context, Activity activity, FragmentMy.StringCallback callback) {
        this.arrayList = arrayList;
        this.context = context;
        this.activity = activity;
        this.stringCallback = callback;
        //this.columnNum = columnNum;

    }


    @NonNull
    @Override
    public MyPagePetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.mypage_pet_item, parent, false);
        MyPagePetViewHolder holder = new MyPagePetViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyPagePetViewHolder holder, int position) {

        String url = arrayList.get(position).getImageUrl();

        if (!url.equals("")) {
            holder.postImage.setPadding(0,0,0,0);
            Glide.with(context).load(url).centerCrop().override(500).into(holder.postImage);
        } else {
            Glide.with(context).load(R.drawable.baseline_pets_24).centerCrop().override(500).into(holder.postImage);
        }

        holder.changeFrameState(selectedItems.get(position));
    }

    @Override
    public int getItemCount() {
        return (arrayList != null ? arrayList.size() : 0);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    public class MyPagePetViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView frame;
        ImageView postImage;
        int frameWidth;

        public MyPagePetViewHolder(@NonNull final View itemView) {
            super(itemView);

            frame = itemView.findViewById(R.id.my_page_pet_imageView);
            this.postImage = itemView.findViewById(R.id.my_page_pet_image);
            frameWidth = itemView.getLayoutParams().width / 10;

            itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();

                    if (selectedItems.get(position)) {
                        selectedItems.delete(position);// 펼쳐진 Item을 클릭 시
                        stringCallback.callback("", 0);
                    } else {
                        selectedItems.delete(prePosition); // 직전의 클릭됐던 Item의 클릭상태를 지움
                        selectedItems.put(position, true); // 클릭한 Item의 position을 저장
                        stringCallback.callback(arrayList.get(position).getPetId(), 0);
                    }

                    // 해당 포지션의 변화를 알림
                    if (prePosition != -1) notifyItemChanged(prePosition);
                    notifyItemChanged(position);

                    // 클릭된 position 저장
                    prePosition = position;

                }
            });

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            final String uid = user.getUid();

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int position = getAdapterPosition();

                    Intent intent = new Intent(context, AnimalProfileActivity.class);
                    PetData pet = arrayList.get(position);

                    intent.putExtra("isAddMode", false);
                    intent.putExtra("isEditMode", false);
                    intent.putExtra("petId", pet.getPetId());
                    intent.putExtra("petMaster", pet.getPetMaster());

                    intent.putExtra("userId", uid);

                    intent.putExtra("petImage", pet.getImageUrl());
                    intent.putExtra("name", pet.getName());
                    intent.putExtra("memo", pet.getMemo());

                    activity.startActivityForResult(intent, 1);
                    return false;
                }
            });
        }

        private void changeFrameState(final boolean isOpen) {
            if (isOpen) {
                frame.setStrokeColor(Color.parseColor("#626267"));
            } else {
                frame.setStrokeColor(Color.parseColor("#ACACAE"));
            }
        }
    }
}