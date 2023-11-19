package com.example.petdiary.util;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;
import android.view.WindowManager;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class util {

    public static String nickName;

    /**
     * 다이어 로그 생성
     **/
    public static Dialog createDialog(int layoutId, Context context) {

        final Dialog customDia = new Dialog(context);
        customDia.requestWindowFeature(Window.FEATURE_NO_TITLE);
        WindowManager.LayoutParams params = customDia.getWindow().getAttributes();
        customDia.setContentView(layoutId);
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        customDia.getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);

        return customDia;
    }


    /**
     * 이메일 정규식
     **/
    public static boolean isValidEmail(String email) {
        boolean err = false;
        String regex = "^[_a-z0-9-]+(.[_a-z0-9-]+)*@(?:\\w+\\.)+\\w+$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(email);
        if (m.matches()) {
            err = true;
        }
        return err;
    }

    /**
     * 비밀 번호 정규식
     **/
    public static boolean isValidPassword(String password) {
        boolean err = false;
        String regex = "^(?=.*[0-9])(?=.*[a-z])(?=.*\\W)(?=\\S+$).{8,20}$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(password);
        if (m.matches()) {
            err = true;
        }
        return err;
    }

    /**
     * 닉네임 정규식
     **/
    public static boolean isValidNickName(final String nickName) {
        boolean err = false;
        String regex = "^[a-zA-Z0-9]*$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(nickName);
        if (m.matches()) {
            if (nickName.length() > 1 && nickName.length() < 9) {
                err = true;
            }
        }
        return err;
    }


}
