package com.example.slices.interfaces;

import com.example.slices.models.EventInfo;

public interface EventInfoChangeListener {
    void onEventInfoChanged(EventInfo eventInfo, DBWriteCallback callback);
}
