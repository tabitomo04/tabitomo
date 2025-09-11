package com.koreatravel.tabitomo;

public interface PathConstants {
    String BASE = "";

    // Authentication
    String AUTH = "/auth";
    String SIGNUP = AUTH + "/signup";
    String LOGIN = AUTH + "/login";
    String LOGOUT = AUTH + "/logout";
    String VERIFY_EMAIL = AUTH + "/verify-email";
    String FORGOT_PASSWORD = AUTH + "/forgot-password";
    String REQUEST_PASSWORD_RESET = AUTH + "/request-password-reset";
    String RESET_PASSWORD = AUTH + "/reset-password";
    String CHECK_EMAIL = AUTH + "/check-email";
    String CHECK_NICKNAME = AUTH + "/check-nickname";
    String CURRENT_USER = AUTH + "/me";
    String LOGIN_SUCCESS = LOGIN + "/success";
    String LOGIN_ERROR = LOGIN + "-error";
    String LOGOUT_SUCCESS = LOGOUT + "/success";
    
    // Member
    String MEMBER = "/member";
    
    // Question
    String QUESTION = "/question";
    String QUESTION_START = QUESTION + "/start";
    String QUESTION_FORM = QUESTION + "/form";
    String QUESTION_SUBMIT = QUESTION + "/submit";
    String QUESTION_COMPLETE = QUESTION + "/complete";
    public static final String MEMBER_INFO = MEMBER + "/info";
    public static final String MEMBER_QUESTION = MEMBER + "/question";
    public static final String MEMBER_QUESTION_START = MEMBER_QUESTION + "/start";
    public static final String MEMBER_QUESTION_FORM = MEMBER_QUESTION + "/form";
    public static final String MEMBER_QUESTION_COMPLETE = MEMBER_QUESTION + "/complete";

    // 여행지
    public static final String TRAVEL = "/travel";
    public static final String TRAVEL_LIST = TRAVEL + "/list";
    public static final String TRAVEL_DETAIL = TRAVEL + "/detail";

    public static final String TRAVEL_SIGHT = TRAVEL + "/sight";
    public static final String TRAVEL_SIGHT_LIST = TRAVEL_SIGHT + "/list";
    public static final String TRAVEL_SIGHT_DETAIL = TRAVEL_SIGHT + "/detail";

    public static final String TRAVEL_CITY = TRAVEL + "/city";
    public static final String TRAVEL_CITY_LIST = TRAVEL_CITY + "/list";
    public static final String TRAVEL_CITY_DETAIL = TRAVEL_CITY + "/detail";

    public static final String FAVORITE = TRAVEL + "/favorite";
    public static final String FAVORITE_PLACE_LIST = FAVORITE + "/list";

    //여행
    public static final String TRIP = TRAVEL + "/trip";
    public static final String TRIP_LIST = TRIP + "/list";
    public static final String TRIP_DETAIL = TRIP + "/detail";
    public static final String TRIP_COMPLETE = TRIP + "/complete";

    // 하단에 추가 바람!

    // 여행 스토리북
    public static final String STORYBOOK = "/storybook";
    public static final String STORYBOOK_LIST = STORYBOOK + "/list";
    public static final String STORYBOOK_DETAIL = STORYBOOK + "/detail";
    public static final String STORYBOOK_WRITE = STORYBOOK + "/write";
    public static final String STORYBOOK_UPDATE = STORYBOOK + "/update";
    public static final String STORYBOOK_DELETE = STORYBOOK + "/delete";
}