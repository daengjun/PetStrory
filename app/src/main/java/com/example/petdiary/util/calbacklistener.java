package com.example.petdiary.util;

/**
 * 각각의 프래그먼트 새로 고침 하는 콜백 리스너
 **/
public interface calbacklistener {
    void refresh(boolean check);

    // 친구 삭제 했을때 게시글 새로 고침
    void friendContents(boolean check);
}


