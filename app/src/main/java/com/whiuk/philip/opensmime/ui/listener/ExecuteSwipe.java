package com.whiuk.philip.opensmime.ui.listener;

import android.view.View;

import com.daimajia.swipe.SimpleSwipeListener;
import com.daimajia.swipe.SwipeLayout;

import com.whiuk.philip.opensmime.KeyInfo;
import com.whiuk.philip.opensmime.R;
import com.whiuk.philip.opensmime.ui.adapter.KeyAdapter;

/**
 * Execute methods if hands are released.
 */
public class ExecuteSwipe extends SimpleSwipeListener {
    private KeyAdapter keyAdapter;
    private KeyInfo keyInfo;

    public ExecuteSwipe(KeyAdapter keyAdapter, KeyInfo keyInfo) {
        this.keyAdapter = keyAdapter;
        this.keyInfo = keyInfo;
    }

    @Override
    public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {
        layout.setDragDistance(0);
        View delete_icon = layout.findViewById(R.id.delete_icon);
        View share_icon = layout.findViewById(R.id.share_icon);
        if (delete_icon.isShown()) {
            delete_icon.setVisibility(View.INVISIBLE);
            keyAdapter.deleteCertificate(keyInfo);
        }

        if (share_icon.isShown()) {
            share_icon.setVisibility(View.INVISIBLE);
            keyAdapter.shareCertificate(keyInfo);
        }
        super.onHandRelease(layout, xvel, yvel);
    }
}
