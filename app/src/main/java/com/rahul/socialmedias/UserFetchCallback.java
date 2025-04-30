package com.rahul.socialmedias;

import com.rahul.socialmedias.Model.User;

public interface UserFetchCallback {
    void onUserFetched(User user);
    void onError(String errorMessage);
}
