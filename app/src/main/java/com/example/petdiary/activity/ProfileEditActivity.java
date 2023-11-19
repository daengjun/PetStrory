

package com.example.petdiary.activity;

import android.app.Activity;
import android.content.Intent;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.petdiary.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 내 프로필 편집
 **/

public class ProfileEditActivity extends AppCompatActivity {
    private ImageView editIcon;
    private ImageView userImage;
    private EditText userName;
    private EditText userMemo;
    private Button saveBtn;
    private Button cancelBtn;
    private String userId;
    private String targetId;
    private boolean isEditMode = false;
    private boolean isPressedSaveBtn = false;
    private String preImage;
    private String preName;
    private String preMemo;
    private String postImgPath = null;  // 갤러리 눌러서 가져온 파일 이름

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24);

        editIcon = findViewById(R.id.userPage_editIcon);
        userImage = findViewById(R.id.userPage_Image);
        userName = findViewById(R.id.userPage_name);
        userMemo = findViewById(R.id.userPage_memo);
        saveBtn = findViewById(R.id.userPage_save);
        cancelBtn = findViewById(R.id.userPage_cancel);

        Intent intent = getIntent();
        userId = intent.getExtras().getString("userId");
        targetId = intent.getExtras().getString("targetId");
        userName.setText(intent.getExtras().getString("userName"));
        userMemo.setText(intent.getExtras().getString("userMemo"));
        preImage = intent.getExtras().getString("userImage");
        postImgPath = preImage;
        setProfileImg(preImage);

        userImage.setOnClickListener(onClickListener);
        editIcon.setOnClickListener(onClickListener);
        cancelBtn.setOnClickListener(onClickListener);
        saveBtn.setOnClickListener(onClickListener);

        if (userId.equals(targetId)) {
            setEditIcon(true);
        } else {
            setEditIcon(false);
        }
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.userPage_editIcon:
                    if (editIcon.isClickable()) {
                        isEditMode = true;
                        preName = userName.getText().toString();
                        preMemo = userMemo.getText().toString();
                        setEditIcon(false);
                        setEditMode(true);
                    }
                    break;
                case R.id.userPage_Image:
                    if (isEditMode)
                        startPopupActivity();
                    break;
                case R.id.userPage_save:



                    if (userName.getText().toString().isEmpty()) {
                        Toast.makeText(ProfileEditActivity.this, "닉네임을 입력 해주세요", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    isPressedSaveBtn = true;
                    isEditMode = false;

                    setProfileImg(postImgPath);
                    saveDataToFirebase();
                    setEditIcon(true);
                    setEditMode(false);

                    preName = userName.getText().toString();
                    preMemo = userMemo.getText().toString();

                    preImage = postImgPath;

                    break;
                case R.id.userPage_cancel:
                    // isImageEdit = false;
                    isEditMode = false;
                    postImgPath = null;
                    postImgPath = preImage;
                    setProfileImg(preImage);
                    userName.setText(preName);
                    userMemo.setText(preMemo);
                    setEditIcon(true);
                    setEditMode(false);
                    break;
            }
        }
    };

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 0:
                if (resultCode == RESULT_OK) {

                    Log.d("갤러리에서 사진을 선택했따.", "onActivityResult: ");
                    postImgPath = data.getStringExtra("postImgPath");
                    setProfileImg(postImgPath);
                }
                break;
        }
    }

    private void setEditIcon(boolean isShown) {
        if (isShown)
            editIcon.setVisibility(View.VISIBLE);
        else
            editIcon.setVisibility(View.INVISIBLE);

        editIcon.setClickable(isShown);
    }

    private void setEditMode(boolean isEditMode) {
        if (isEditMode) {
            //  isEdit = true;
            userName.setBackground(getBaseContext().getResources().getDrawable(R.drawable.text_shape_coners));
            userName.setHint("사용자 닉네임 입력");
            userName.setGravity(Gravity.CENTER);
            userName.setFocusableInTouchMode(true);

            userMemo.setBackground(getBaseContext().getResources().getDrawable(R.drawable.text_shape_coners));
            userMemo.setHint("사용자 정보를 입력 해주세요");
            userMemo.setFocusableInTouchMode(true);
            userMemo.setGravity(Gravity.TOP | Gravity.START);

            saveBtn.setVisibility(View.VISIBLE);
            cancelBtn.setVisibility(View.VISIBLE);
        } else {
            // isEdit = false;
            userName.setBackground(null);
            userName.setFocusableInTouchMode(false);
            userName.setFocusable(false);
            userName.setHint("");
            userName.setGravity(Gravity.CENTER);

            userMemo.setBackground(null);
            userMemo.setFocusableInTouchMode(false);
            userMemo.setFocusable(false);
            userMemo.setHint("");
            userMemo.setGravity(Gravity.CENTER_HORIZONTAL);

            saveBtn.setVisibility(View.INVISIBLE);
            cancelBtn.setVisibility(View.INVISIBLE);
        }
    }


    // 프로필 이미지 변경 함수
    private void setProfileImg(String profileImg) {
        Activity activity = ProfileEditActivity.this;
        if (activity.isFinishing())
            return;

        Log.d("IR", "setProfileImg: " + profileImg);

        if(profileImg!=null&&!profileImg.isEmpty()){
            Glide.with(this).load(profileImg).centerCrop().override(500).into(userImage);
        }
    }


    // 선택 팝업 열기
    private void startPopupActivity() {
        Intent intent = new Intent(getApplicationContext(), ImageChoicePopupActivity.class);
        startActivityForResult(intent, 0);
    }

    @Override
    public void onBackPressed() {
        if (isPressedSaveBtn) {
            Intent intent = new Intent();
            intent.putExtra("profileImg", postImgPath);
            intent.putExtra("nickName", userName.getText().toString());
            intent.putExtra("memo", userMemo.getText().toString());

            setResult(Activity.RESULT_OK, intent);
            finish();
        }
        super.onBackPressed();
    }

    private void saveDataToFirebase() {
        // 이미지 변경시
        if (postImgPath.compareTo(preImage) != 0)
            setProfileImageToFirebase();

        // 텍스트 변경시
        if (preMemo.compareTo(userMemo.getText().toString()) != 0 || preName.compareTo(userName.getText().toString()) != 0)
            setProfileTextToFirebase();
    }

    private void setProfileImageToFirebase() {
        // String postImgPath = preImage;
        final String[] profileImg = new String[1];

        // 파이어베이스 스토리지에 이미지 저장
        FirebaseStorage storage = FirebaseStorage.getInstance();
        final StorageReference storageRef = storage.getReference();
        final UploadTask[] uploadTask = new UploadTask[1];

        // 로컬 파일에서 업로드(스토리지)
        final Uri file = Uri.fromFile(new File(postImgPath));
        StorageReference riversRef = storageRef.child("users/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "_profileImage.jpg");
        uploadTask[0] = riversRef.putFile(file);

        uploadTask[0].addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                // 파이어베이스의 스토리지에 저장한 이미지의 다운로드 경로를 가져옴
                final StorageReference ref = storageRef.child("users/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "_profileImage.jpg");
                uploadTask[0] = ref.putFile(file);

                Task<Uri> urlTask = uploadTask[0].continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        return ref.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            profileImg[0] = downloadUri.toString();

                            postImgPath = profileImg[0];

                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            String uid = user.getUid();

                            // 클라우드 파이어스토어의 users에 프로필 이미지 주소 저장
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            DocumentReference documentUserReference = db.collection("users").document(uid);

                            String temp = userName.getText().toString();
                            documentUserReference
                                    .update("profileImg", profileImg[0])
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d("ProfileEditActivity", "DocumentSnapshot successfully updated!");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w("ProfileEditActivity", "Error updating document", e);
                                        }
                                    });
                            setProfileImg(postImgPath);
                        } else {
                            // 서버에 저장 실패시
                        }
                    }
                });

            }
        });

    }

    // 닉네임, 메모 저장
    private void setProfileTextToFirebase() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();

        //  클라우드 파이어스토어의 users에 프로필 이미지 주소 저장
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference documentUserReference = db.collection("users").document(uid);

        documentUserReference
                .update(
                        "nickName", userName.getText().toString(),
                        "memo", userMemo.getText().toString()
                )
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("ProfileEditActivity", "DocumentSnapshot successfully updated!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("ProfileEditActivity", "Error updating document", e);
                    }
                });
    }
}
