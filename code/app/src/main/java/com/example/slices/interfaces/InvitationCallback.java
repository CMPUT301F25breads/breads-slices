package com.example.slices.interfaces;

import com.example.slices.models.Invitation;

/**
 * Interface for invitation callbacks
 * @author Ryan Haubrich
 * @version 1.0
 */
public interface InvitationCallback {
    public void onSuccess(Invitation result);
    public void onFailure(Exception e);
}
