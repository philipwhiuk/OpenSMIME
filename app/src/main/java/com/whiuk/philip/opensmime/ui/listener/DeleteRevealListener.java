package com.whiuk.philip.opensmime.ui.listener;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;

import com.daimajia.swipe.SwipeLayout;

import com.whiuk.philip.opensmime.App;
import com.whiuk.philip.opensmime.R;

/**
 * Detect delete swipe
 */
public class DeleteRevealListener implements SwipeLayout.OnRevealListener {

    @Override
    public void onReveal(View view, SwipeLayout.DragEdge dragEdge, float v, int i) {
        SharedPreferences sharedPreferences = App.getPreferences();
        View delete_icon = view.findViewById(R.id.delete_icon);
        float deleteDistance = sharedPreferences.getInt("delete_distance", 30) / 100.0f;
        if (dragEdge != SwipeLayout.DragEdge.Right) {
            return;
        }
        if (v <= deleteDistance && delete_icon.isShown()) {
            delete_icon.setVisibility(View.INVISIBLE);
        } else if (v > deleteDistance && !delete_icon.isShown()) {
            delete_icon.setVisibility(View.VISIBLE);
        }
    }
}
