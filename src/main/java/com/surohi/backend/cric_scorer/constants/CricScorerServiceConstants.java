package com.surohi.backend.cric_scorer.constants;

public class CricScorerServiceConstants {
    public static final String USER_DETAIL_TABLE = "user_detail";
    public static final String PUBLIC_SCHEMA = "public";

    // Column Name Constants
    public static final String ID = "id";
    public static final String USER_NAME = "user_name";
    public static final String FIRST_NAME = "first_name";
    public static final String LAST_NAME = "last_name";
    public static final String GENDER = "gender";
    public static final String DATE_OF_BIRTH = "date_of_birth";
    public static final String PASSWORD = "password";
    public static final String EMAIL_ID = "email_id";
    public static final String PHONE_NUMBER = "phone_number";
    public static final String UNIQUE_IDENTIFIER = "unique_identifier";
    public static final String CREATED_AT = "created_at";
    public static final String UPDATED_AT = "updated_at";

    //End Point
    public static final String BASE_URL = "/newEra/crick-scorer";
    public static final String USER_REGISTRATION_URL = "/user/register";
    public static final String AUTH_LOGIN_URL = "/auth/login";
    public static final String AUTH_LOGOUT_URL = "/auth/logout";
    public static final String PROFILE_CREATION_URL = "/profile/creation";
}
