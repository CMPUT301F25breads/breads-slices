package com.example.slices.interfaces;

import com.example.slices.models.Invitation;

public interface InvitationCallback {
    public void onSuccess(Invitation result);
    public void onFailure(Exception e);
}
