package com.example.myapplication.model;

import java.util.List;

public class LoginResponse {
    public OAuth oauth;
    public UserInfo userInfo;
    public List<String> permissions;
    public String apiVersion;
    public boolean showPasswordPrompt;

    public static class OAuth {
        public String access_token;
        public int expires_in;
        public String token_type;
        public String scope;
        public String refresh_token;
    }

    public static class UserInfo {
        public int personalNo;
        public String firstName;
        public String lastName;
        public String displayName;
        public boolean active;
        public String businessUnit;
    }
}