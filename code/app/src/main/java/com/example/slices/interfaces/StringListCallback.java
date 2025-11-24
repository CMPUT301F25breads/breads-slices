package com.example.slices.interfaces;

import java.util.List;

public interface StringListCallback {
    void onSuccess(List<String> strings);
    void onFailure(Exception e);

}
