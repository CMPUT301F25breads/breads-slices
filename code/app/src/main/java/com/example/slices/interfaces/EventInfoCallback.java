package com.example.slices.interfaces;

import com.example.slices.models.EventInfo;

public interface EventInfoCallback {
    void onSuccess(EventInfo eventInfo);
    void onFailure(Exception e);

}
