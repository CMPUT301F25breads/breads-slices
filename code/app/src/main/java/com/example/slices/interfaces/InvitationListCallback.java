package com.example.slices.interfaces;

import com.example.slices.models.Invitation;

import java.util.List;

public interface InvitationListCallback {
    void onSuccess(List<Invitation> invitations);
    void onFailure(Exception e);
}
