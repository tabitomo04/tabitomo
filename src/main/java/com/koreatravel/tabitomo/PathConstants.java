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
    String MEMBER_INFO = MEMBER + "/info";
    String MEMBER_PROFILE = MEMBER + "/profile";
    String MEMBER_UPDATE = MEMBER + "/update";
    String MEMBER_CHANGE_PASSWORD = MEMBER + "/change-password";
    String MYPAGE = MEMBER + "/mypage";
    
    // Question
    String QUESTION = "/question";
    String QUESTION_START = QUESTION + "/start";
    String QUESTION_FORM = QUESTION + "/form";
    String QUESTION_SUBMIT = QUESTION + "/submit";
    String QUESTION_COMPLETE = QUESTION + "/complete";
    String MEMBER_QUESTION = MEMBER + "/question";
    String MEMBER_QUESTION_START = MEMBER_QUESTION + "/start";
    String MEMBER_QUESTION_FORM = MEMBER_QUESTION + "/form";
    String MEMBER_QUESTION_COMPLETE = MEMBER_QUESTION + "/complete";

    // Travel
    String TRAVEL = "/travel";
    String TRAVEL_LIST = TRAVEL + "/list";
    String TRAVEL_DETAIL = TRAVEL + "/detail";
    String TRAVEL_SIGHT = TRAVEL + "/sight";
    String TRAVEL_SIGHT_LIST = TRAVEL_SIGHT + "/list";
    String TRAVEL_SIGHT_DETAIL = TRAVEL_SIGHT + "/detail";
    String TRAVEL_CITY = TRAVEL + "/city";
    String TRAVEL_CITY_LIST = TRAVEL_CITY + "/list";
    String TRAVEL_CITY_DETAIL = TRAVEL_CITY + "/detail";

    // Favorites
    String FAVORITE = "/favorite";
    String FAVORITE_PLACE = FAVORITE + "/place";
    String FAVORITE_PLACE_LIST = FAVORITE_PLACE + "/list";
    String FAVORITE_ADD = FAVORITE + "/add";
    String FAVORITE_REMOVE = FAVORITE + "/remove";

    // Trip
    String TRIP = "/trip";
    String TRIP_NEW = TRIP + "/new";
    String TRIP_LIST = TRIP + "/list";
    String TRIP_DETAIL = TRIP + "/{tripId}";
    String TRIP_UPDATE = TRIP + "/{tripId}/update";
    String TRIP_DELETE = TRIP + "/{tripId}/delete";
    String TRIP_COMPLETE = TRIP + "/complete";
    String TRIP_RECOMMEND = TRIP + "/recommend";
    String TRIP_SAVE = TRIP + "/save";
    String TRIP_SHARE = TRIP + "/{tripId}/share";

    // Storybook
    String STORYBOOK = "/storybook";
    String STORYBOOK_LIST = STORYBOOK + "/list";
    String STORYBOOK_DETAIL = STORYBOOK + "/{id}";
    String STORYBOOK_WRITE = STORYBOOK + "/write";
    String STORYBOOK_UPDATE = STORYBOOK + "/{id}/update";
    String STORYBOOK_DELETE = STORYBOOK + "/{id}/delete";
    String STORYBOOK_SAVE = STORYBOOK + "/save";
    String STORYBOOK_EDIT = STORYBOOK + "/{id}/edit";
    String STORYBOOK_EDITOR = STORYBOOK + "/editor";
    String STORYBOOK_VIEW = STORYBOOK + "/view";
    String STORYBOOK_TEMPSAVE = STORYBOOK + "/tempsave";
    String STORYBOOK_TEMPSAVE_LIST = STORYBOOK_TEMPSAVE + "/list";
    String STORYBOOK_TEMPSAVE_DELETE = STORYBOOK_TEMPSAVE + "/delete";
    String STORYBOOK_LIKE = STORYBOOK + "/like";

    // Chat
    String CHAT = "/chat";
    String CHAT_SEND = CHAT + "/send";
    String CHAT_HISTORY = CHAT + "/history";
    String CHAT_FEEDBACK = CHAT + "/feedback";

    // API Endpoints
    String API = "/api";
    String API_EMAIL = API + "/email";
    String API_EMAIL_VERIFY = API_EMAIL + "/verify";
    String API_EMAIL_SEND_VERIFICATION = API_EMAIL + "/send-verification";
    
    String API_TRANSLATE = API + "/translate";
    String API_TRANSLATE_TEXT = API_TRANSLATE + "/text";
    
    String API_REFERENCE = API + "/reference";
    String API_REFERENCE_COUNTRIES = API_REFERENCE + "/countries";
    String API_REFERENCE_LANGUAGES = API_REFERENCE + "/languages";
    
    String API_FAVORITES = API + "/favorites";
    String API_FAVORITES_ADD = API_FAVORITES + "/add";
    String API_FAVORITES_REMOVE = API_FAVORITES + "/remove";
    String API_FAVORITES_LIST = API_FAVORITES + "/list";
    
    // Editor
    String EDITOR = "/editor";
    String EDITOR_SAVE = EDITOR + "/save";
    String EDITOR_LOAD = EDITOR + "/load";
    String EDITOR_DELETE = EDITOR + "/delete";
    
    // Upload
    String UPLOAD = "/upload";
    String UPLOAD_IMAGE = UPLOAD + "/image";
    String UPLOAD_FILE = UPLOAD + "/file";
}