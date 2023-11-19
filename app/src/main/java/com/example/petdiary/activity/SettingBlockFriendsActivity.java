package com.example.petdiary.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petdiary.util.calbacklistener;
import com.example.petdiary.model.BlockFriendInfo;
import com.example.petdiary.adapter.BlockFriendsAdapter;
import com.example.petdiary.util.ItemTouchHelperCallback;
import com.example.petdiary.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

/**
 * 차단 친구 목록
 **/
public class SettingBlockFriendsActivity extends AppCompatActivity implements calbacklistener {

    private static com.example.petdiary.util.calbacklistener calbacklistener;
    final private String TAG = "SettingBlock";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_block_friends);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Log.e("###", FirebaseAuth.getInstance().getUid());

        db.collection("blockFriends/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/friends")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            RecyclerView recyclerView = findViewById(R.id.bfri_recyclerview);
                            LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);

                            recyclerView.setLayoutManager(layoutManager);
                            //recyclerView.addItemDecoration(new RecyclerViewDecoration(1));

                            BlockFriendsAdapter adapter = new BlockFriendsAdapter(getApplicationContext(), calbacklistener);

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String friendUid = document.getData().get("friendUid").toString();
                                adapter.addItem(new BlockFriendInfo(friendUid));
                            }
                            recyclerView.setAdapter(adapter);
                            ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelperCallback(adapter));
                            helper.attachToRecyclerView(recyclerView);
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

    }

    public static void setlistener(calbacklistener listener) {

        calbacklistener = listener;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void refresh(boolean check) {

    }

    @Override
    public void friendContents(boolean check) {

    }
}
