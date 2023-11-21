package com.example.petdiary.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.example.petdiary.model.Data;
import com.example.petdiary.activity.Expand_contentsView;
import com.example.petdiary.util.OnSingleClickListener;
import com.example.petdiary.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class ViewPageAdapterSub extends PagerAdapter implements com.example.petdiary.util.calbacklistener {

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    String uid = user.getUid();
    private ArrayList<String> images = new ArrayList<String>();
    private LayoutInflater inflater;
    private Context context;
    private Data arrayList;
    com.example.petdiary.util.calbacklistener calbacklistener;
    private DatabaseReference mDatabase;
    private FirebaseDatabase firebaseDatabase;

    public ViewPageAdapterSub(Data arrayList, String uri1, Context context, com.example.petdiary.util.calbacklistener calbacklistener) {
        if (uri1.length() > 0) {
            images.add(uri1);
        }
        this.context = context;
        this.arrayList = arrayList;
        this.calbacklistener = calbacklistener;
    }

    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((LinearLayout) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        inflater = (LayoutInflater) context.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.slider_sub, container, false);
        ImageView imageView = (ImageView) v.findViewById(R.id.imageView);
        Glide.with(context).load(images.get(position)).centerCrop().override(500).into(imageView);


        imageView.setOnClickListener(new View.OnClickListener() {
            private static final long CLICK_TIME_INTERVAL = 1000; // 클릭 간격을 조절하세요 (밀리초 단위)
            private long lastClickTime = 0;

            @Override
            public void onClick(View v) {
                long currentTime = System.currentTimeMillis();

                // 현재 시간과 마지막 클릭 시간 간의 차이를 계산
                if (currentTime - lastClickTime >= CLICK_TIME_INTERVAL) {
                    // 일정 시간이 지난 후에 클릭 처리
                    goPost(arrayList);

                    // 마지막 클릭 시간 갱신
                    lastClickTime = currentTime;
                }
            }
        });

        container.addView(v);
        return v;
    }

    private void goPost(final Data arrayList) {

        final long startTime = System.currentTimeMillis();

        final Intent intent = new Intent(context, Expand_contentsView.class);
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        final ArrayList<String> mainSource = new ArrayList<>();

        mainSource.clear();

        mDatabase = FirebaseDatabase.getInstance().getReference("friend/" + uid);
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    mainSource.add(postSnapshot.getKey());
                }
                db.collection("user-checked/" + uid + "/bookmark")
                        .whereEqualTo("postID", arrayList.getPostID())
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    intent.putExtra("bookmark", "unchecked");
                                    for (final QueryDocumentSnapshot document : task.getResult()) {
                                        if (arrayList.getPostID().equals(document.getData().get("postID").toString())) {
                                            intent.putExtra("bookmark", "checked");
                                            break;
                                        }
                                    }
                                    db.collection("user-checked/" + uid + "/like")
                                            .whereEqualTo("postID", arrayList.getPostID())
                                            .get()
                                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        intent.putExtra("postLike", "unchecked");
                                                        for (final QueryDocumentSnapshot document : task.getResult()) {
                                                            if (arrayList.getPostID().equals(document.getData().get("postID").toString())) {
                                                                intent.putExtra("postLike", "checked");
                                                                break;
                                                            }
                                                        }
                                                        boolean chkFriend = false;
                                                        for (int i = 0; i < mainSource.size(); i++) {
                                                            if (arrayList.getUid().equals(mainSource.get(i))) {
                                                                chkFriend = true;
                                                                break;
                                                            }
                                                        }
                                                        if (chkFriend) {
                                                            intent.putExtra("friend", "checked");
                                                        } else {
                                                            intent.putExtra("friend", "unchecked");
                                                        }
                                                        intent.putExtra("postID", arrayList.getPostID());
                                                        intent.putExtra("nickName", arrayList.getNickName());
                                                        intent.putExtra("uid", arrayList.getUid());
                                                        intent.putExtra("imageUrl1", arrayList.getImageUrl1());
                                                        intent.putExtra("imageUrl2", arrayList.getImageUrl2());
                                                        intent.putExtra("imageUrl3", arrayList.getImageUrl3());
                                                        intent.putExtra("imageUrl4", arrayList.getImageUrl4());
                                                        intent.putExtra("imageUrl5", arrayList.getImageUrl5());
                                                        intent.putExtra("favoriteCount", arrayList.getFavoriteCount());
                                                        intent.putExtra("date", arrayList.getDate());
                                                        intent.putExtra("content", arrayList.getContent());
                                                        intent.putExtra("postID", arrayList.getPostID());
                                                        intent.putExtra("category", arrayList.getCategory());
                                                        intent.putExtra("favoriteCount", arrayList.getFavoriteCount());
                                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                        context.startActivity(intent);

                                                        Expand_contentsView.setListener(calbacklistener);

                                                        long endTime = System.currentTimeMillis();
                                                        long elapsedTime = endTime - startTime;

                                                        double seconds = (double) elapsedTime / 1000.0;

                                                        Log.d("MyApp", "코드 실행에 걸린 시간: " + seconds + "초");

                                                    } else {
                                                        Log.d("###", "Error getting documents: ", task.getException());
                                                    }
                                                }
                                            });
                                } else {
                                    Log.d("###", "Error getting documents: ", task.getException());
                                }
                            }
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.invalidate();
    }

    @Override
    public void refresh(boolean check) {

    }

    @Override
    public void friendContents(boolean check) {
    }
}


