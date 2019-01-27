package com.armandasalmd.weeklyroutine.classes;

public interface Dismissible {
    interface OnDismissedListener {
        void onDismissed();
    }

    void dismiss(OnDismissedListener listener);
}