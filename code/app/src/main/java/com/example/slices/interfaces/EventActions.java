package com.example.slices.interfaces;

import androidx.annotation.NonNull;
import com.example.slices.Event;

/**
 * EventActions
 * affects the event_card.xml when Join or Leave are clicked
 *      -Raj (@R.P.)
 */
public interface EventActions {
    void onJoinClicked(@NonNull Event e);
    void onLeaveClicked(@NonNull Event e);
}