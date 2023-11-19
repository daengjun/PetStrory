package com.example.petdiary.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.petdiary.model.Data;
import com.example.petdiary.adapter.MyPageAdapter;
import com.example.petdiary.adapter.Mypage_petAdapter;
import com.example.petdiary.model.PetData;
import com.example.petdiary.util.RecyclerDecoration;
import com.example.petdiary.activity.*;
import com.bumptech.glide.Glide;
import com.example.petdiary.R;
import com.example.petdiary.adapter.HorizontalSpaceItemDecoration;
import com.example.petdiary.util.calbacklistener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.app.Activity.RESULT_OK;


public class FragmentMy extends Fragment implements calbacklistener {

    private static final String TAG = "MyPage_Fragment";
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private BottomNavigationView bottomNavigationView;
    private ViewGroup viewGroup;
    private TextView profileName;
    private TextView profileMemo;
    private String profileImgName;
    private ImageView profileEditImg;
    boolean contentCheck;

    Map<String, String> userInfo = new HashMap<>();   // 이거 여기서 선언할게 아니라 받아와야함
    //Map<String, String> petInfo = new HashMap<>();
    ArrayList<Data> postList = new ArrayList<Data>();
    ArrayList<Data> selectedPostList = new ArrayList<Data>();
    ArrayList<PetData> petList = new ArrayList<PetData>();
    int listCount = 0;


    // 사진 리사이클뷰 선언
    RecyclerView recyclerView;
    RecyclerView.Adapter adapter;
    RecyclerView.LayoutManager layoutManager;


    // 펫 정보 리사이클뷰 선언
    RecyclerView petRecyclerView;
    RecyclerView.Adapter petAdapter;
    String choicePetId;

    boolean editPet = false;

    @Override
    public void refresh(boolean check) {
        if (!check) {
            ((MainActivity) getActivity()).refresh(false);
        }
        contentCheck = check;
    }

    @Override
    public void friendContents(boolean check) {
    }

    public interface StringCallback {
        void callback(String choice, int click);

    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_mypage, container, false);
        mSwipeRefreshLayout = (SwipeRefreshLayout) viewGroup.findViewById(R.id.swipe_layout);
        profileEditImg = viewGroup.findViewById(R.id.profile_image);
        profileName = viewGroup.findViewById(R.id.profile_name);
        profileMemo = viewGroup.findViewById(R.id.profile_memo);

        ImageView petAddBtn = viewGroup.findViewById(R.id.profile_petAddBtn);
        final ImageView profileImage = viewGroup.findViewById(R.id.profile_image);

        SettingBookMarkActivity.setlistener(this);
        SettingBlockFriendsActivity.setlistener(this);

        //////////////////////////////////// 유저 정보 가져 오기
        getUserInfo();

        petRecyclerView = (RecyclerView) viewGroup.findViewById(R.id.pet_recyclerView);
        petRecyclerView.setHasFixedSize(true);

        //////////////////////////////////// 애완 동물 정보 가져 오기
        getPetInfo();

        //////////////////////////////////// 게시물 정보 가져 오기
        loadPostsAfterCheck(false);

        //////////////////////////////////// 사진 RecyclerView setting
        setPicRecyclerView();

        //////////////////////////////////// 프로필 이미지 수정 이벤트 추가
        profileImage.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //  setImg();
                setProfileImg(profileImgName);
                String userId = "IAmTarget";//"IAmUser"
                String targetId = "IAmTarget";

                Intent intent = new Intent(getContext(), ProfileEditActivity.class);
                intent.putExtra("targetId", targetId);
                intent.putExtra("userId", userId);
                intent.putExtra("userImage", profileImgName);// userInfo.get("profileImg")); // 임시로 넣은 이미지
                intent.putExtra("userName", profileName.getText().toString());// userInfo.get("nickName"));//userName.getText().toString());
                intent.putExtra("userMemo", profileMemo.getText().toString());//userInfo.get("memo"));//userMemo.getText().toString());

                startActivityForResult(intent, 0);
                return true;
            }


        });


        // 펫 추가 버튼
        petAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), AnimalProfileActivity.class);
                intent.putExtra("isAddMode", true);
                intent.putExtra("isEditMode", false);
                intent.putExtra("petId", "");
                intent.putExtra("petMaster", "");
                intent.putExtra("userId", "");
                intent.putExtra("petImage", "");

                //startActivity(intent);
                startActivityForResult(intent, 1);
            }
        });

        // 새로 고침시 Update
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                postList.clear();
                loadPostsAfterCheck(false);
                mSwipeRefreshLayout.setRefreshing(false);  // 로딩 애니메이션 사라짐

            }
        });

        //////////////////////////////////// 최상단으로 가기 이벤트 추가
        moveTop();

        int horizontalSpaceWidth = getResources().getDimensionPixelSize(R.dimen.horizontal_space_width); // 원하는 간격 크기
        petRecyclerView.addItemDecoration(new HorizontalSpaceItemDecoration(horizontalSpaceWidth));

        return viewGroup;
    }


    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {

            case 0:  // 프로필 수정 리턴 값
                if (resultCode == RESULT_OK) {
                    setProfileImg(data.getStringExtra("profileImg"));
                    TextView toolbarNick = getActivity().findViewById(R.id.toolbar_nickName);
                    profileName.setText(data.getStringExtra("nickName"));
                    profileMemo.setText(data.getStringExtra("memo"));
                    toolbarNick.setText(profileName.getText().toString());
                    ImageView hambugerProfileImg = getActivity().findViewById(R.id.user_icon);
                    Glide.with(this).load(data.getStringExtra("profileImg")).centerCrop().override(500).into(hambugerProfileImg);
                    profileImgName = data.getStringExtra("profileImg");
                    editPet = true;
                }
                break;
            case 1: // 펫 추가, 수정
                if (resultCode == RESULT_OK) {
                    editPet = true;
                    getPetInfo();
                }
                break;

        }
    }

    //////////////////////////////////// 프로필 이미지, 닉네임, 메모 가져 오기,
    private void getUserInfo() {
        //  유저
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    userInfo.put("nickName", document.getString("nickName"));
                    userInfo.put("profileImg", document.getString("profileImg"));
                    userInfo.put("memo", document.getString("memo"));

                    profileName.setText(userInfo.get(("nickName")));
                    profileMemo.setText(userInfo.get(("memo")));
                    profileImgName = document.getString("profileImg");
                    if (profileImgName.length() > 0) {
                        setProfileImg(profileImgName);
                    }
                    //setImg();
                } else {
                    Log.w(TAG, "Error getting documents.", task.getException());
                }
            }
        });
    }


    //////////////////////////////////// 펫 정보 로드
    private void getPetInfo() {
        //  유저
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("pets").document(uid).collection("pets")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()) {
                            petList.clear();

                            for (QueryDocumentSnapshot document : task.getResult()) {

                                Map<String, Object> data = document.getData();
                                // 이름 이미지 메모
                                PetData pet = new PetData(
                                        document.getId(),
                                        data.get("petName").toString(),
                                        data.get("profileImg").toString(),
                                        data.get("petMemo").toString(),
                                        data.get("master").toString());
                                petList.add(pet);

                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                        setPetRecyclerView();
                        petAdapter.notifyDataSetChanged();
                    }
                });

    }


    //////////////////////////////////// 개인 게시물 로드. 체크시 이전 게시물 개수와 비교후 update
    //////////////////////////////////// 체크 안하면 그냥 update
    private void loadPostsAfterCheck(final boolean needCheck) {
        //  유저
        postList.clear();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Query query = db.collection("post").whereEqualTo("uid", uid);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    int resultCount = task.getResult().size();
                    if (needCheck)
                        if (listCount == resultCount)
                            return;

                    for (QueryDocumentSnapshot document : task.getResult()) {

                        Data dataList = new Data();
                        dataList.setPostID(document.getId());
                        dataList.setUid(document.getData().get("uid").toString());
                        dataList.setContent(document.getData().get("content").toString());
                        dataList.setDate(document.getData().get("date").toString());
                        dataList.setImageUrl1(document.getData().get("imageUrl1").toString());
                        dataList.setImageUrl2(document.getData().get("imageUrl2").toString());
                        dataList.setImageUrl3(document.getData().get("imageUrl3").toString());
                        dataList.setImageUrl4(document.getData().get("imageUrl4").toString());
                        dataList.setImageUrl5(document.getData().get("imageUrl5").toString());
                        dataList.setNickName(document.getData().get("nickName").toString());
                        dataList.setFavoriteCount(Integer.parseInt(document.getData().get("favoriteCount").toString()));
                        postList.add(0, dataList);
                    }
                    adapter.notifyDataSetChanged();
                    listCount = resultCount;

                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });

    }


    ////////////////////////////////////  특정 펫 게시물 로드
    private void loadSelectedPosts(String petId) {
        //  유저
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Query query = db.collection("post").whereEqualTo("petsID", petId);
        //query.get
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    int resultCount = task.getResult().size();
                    postList.clear();

                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Data dataList = new Data();
                        dataList.setPostID(document.getId());
                        Log.d(TAG, "onComplete: getid" + document.getId());
                        dataList.setUid(document.getData().get("uid").toString());
                        dataList.setDate(document.getData().get("date").toString());
                        dataList.setContent(document.getData().get("content").toString());
                        dataList.setImageUrl1(document.getData().get("imageUrl1").toString());
                        dataList.setImageUrl2(document.getData().get("imageUrl2").toString());
                        dataList.setImageUrl3(document.getData().get("imageUrl3").toString());
                        dataList.setImageUrl4(document.getData().get("imageUrl4").toString());
                        dataList.setImageUrl5(document.getData().get("imageUrl5").toString());
                        dataList.setNickName(document.getData().get("nickName").toString());
                        postList.add(0, dataList);
                    }
                    adapter.notifyDataSetChanged();

                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });

    }

    private void setProfileImg(String profileImg) {

        if(profileImg!=null&&!profileImg.isEmpty()){
            Glide.with(this).load(profileImg).centerCrop().override(500).into(profileEditImg);
        }
    }

    /**
     * 최상단 이동 이벤트
     **/
    private void moveTop() {
        bottomNavigationView = getActivity().findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemReselectedListener(new BottomNavigationView.OnNavigationItemReselectedListener() {
            @Override
            public void onNavigationItemReselected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.tab4) {
                    recyclerView.smoothScrollToPosition(0);
                }
            }
        });
    }

    //////////////////////////////////// 사진 RecyclerView setting
    private void setPicRecyclerView() {
        recyclerView = (RecyclerView) viewGroup.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);

        int columnNum = 3;
        adapter = new MyPageAdapter(postList, columnNum, getContext(), this);
        recyclerView.setAdapter(adapter); // RecyclerView 어댑터 연결
        layoutManager = new GridLayoutManager(getContext(), columnNum);
        recyclerView.setLayoutManager(layoutManager);

        // RecyclerView 간격 추가
        RecyclerDecoration spaceDecoration = new RecyclerDecoration(10);
        recyclerView.addItemDecoration(spaceDecoration);
    }

    //////////////////////////////////// 펫 RecyclerView setting
    private void setPetRecyclerView() {

        // 프로필 바꾸고 돌아 왔을때
        if (petAdapter != null && editPet) {
            editPet = false;
            ((MainActivity) getActivity()).refresh(editPet);
            petRecyclerView.setAdapter(petAdapter);
            petAdapter.notifyDataSetChanged();
            return;
        }

        if (petAdapter != null) {
            petRecyclerView.setAdapter(petAdapter);
            petAdapter.notifyDataSetChanged();
            return;
        }

        petAdapter = new Mypage_petAdapter(petList, getContext(), getActivity(), new StringCallback() {
            @Override
            public void callback(String choice, int click) {
                choicePetId = choice;

                if (click == 0) {
                    ((MainActivity) getActivity()).refresh(false);
                } else {
                    ((MainActivity) getActivity()).refresh(true);
                }
                /* 동물 등록 하고 나서 전체 데이터 새로 고침 */

                if (choice.equals(""))
                    loadPostsAfterCheck(false);
                else
                    loadSelectedPosts(choicePetId);
            }
        });

        petRecyclerView.setAdapter(petAdapter);
        petAdapter.notifyDataSetChanged();
    }


    //////////////////////////////////// 화면 처음 활성화 됐을 시 행동
    @Override
    public void onResume() {
        super.onResume();
        getPetInfo();
        if (mSwipeRefreshLayout != null) {
            if (contentCheck) {
                ((MainActivity) getActivity()).refresh(true);
                contentCheck = false;
            }
        }
    }

    public void myPageRefresh() {

        loadPostsAfterCheck(false);
        getPetInfo();


    }


}


