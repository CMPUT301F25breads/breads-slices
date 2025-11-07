package com.example.slices.interfaces;

import com.example.slices.models.Invitation;

import java.util.List;

/**
 * Interface for invitation list callbacks
 * @author Ryan Haubrich
 * @version 1.0
 */
public interface InvitationListCallback {
    void onSuccess(List<Invitation> invitations);
    void onFailure(Exception e);
}
