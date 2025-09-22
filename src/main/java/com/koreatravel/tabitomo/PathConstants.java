package com.koreatravel.tabitomo;   

public interface PathConstants {
    public static final String BASE = "";

    public static final String AUTH = BASE + "/auth";
    public static final String LOGIN = AUTH + "/login";
    public static final String SIGNUP = AUTH + "/signup";
    public static final String RESET_PASSWORD = AUTH + "/reset-password";
    public static final String LOGOUT = AUTH + "/logout";

    // 멤버
    public static final String MEMBER = BASE + "/member";
    public static final String MYPAGE = "/mypage";
    public static final String MEMBER_INFO = MEMBER + "/info";
    public static final String MEMBER_UPDATE = MEMBER + "/update";
    public static final String MEMBER_DELETE = MEMBER + "/delete";

    // 질문 페이지
    public static final String QUESTION = BASE + "/question";
    public static final String QUESTION_START = QUESTION + "/start";
    public static final String QUESTION_FORM = QUESTION + "/form";
    public static final String QUESTION_COMPLETE = QUESTION + "/complete";

    // 여행


    // 여행 스토리북
    public static final String STORYBOOK = "/storybook";
    public static final String STORYBOOK_LIST = STORYBOOK + "/list";
    public static final String STORYBOOK_DETAIL = STORYBOOK + "/detail";
    public static final String STORYBOOK_WRITE = STORYBOOK + "/write";
    public static final String STORYBOOK_UPDATE = STORYBOOK + "/update";
    public static final String STORYBOOK_DELETE = STORYBOOK + "/delete";
    public static final String STORYBOOK_TEMPDELETE = STORYBOOK + "/tempdelete";
    public static final String STORYBOOK_SAVE = STORYBOOK + "/save";
    public static final String STORYBOOK_TEMPSAVE = STORYBOOK + "/tempsave";
}