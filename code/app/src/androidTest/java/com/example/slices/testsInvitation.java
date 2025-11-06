package com.example.slices;

import com.example.slices.models.Invitation;

import org.junit.Test;

public class testsInvitation {

    @Test
    public void testCreateInvitation() {
        // Create a new invitation
        Invitation invitation = new Invitation("Test Invitation", "Test Message", 1, 2, 3, 4);
        assert invitation.getTitle().equals("Test Invitation");
        assert invitation.getBody().equals("Test Message");
        assert invitation.getId() == 1;
        assert invitation.getRecipientId() == 2;
        assert invitation.getSenderId() == 3;
        assert invitation.getEventId() == 4;
    }


}
