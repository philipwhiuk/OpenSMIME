package com.whiuk.philip.opensmime.ui.listener;

import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import com.whiuk.philip.opensmime.App;
import com.whiuk.philip.opensmime.KeyInfo;
import com.whiuk.philip.opensmime.OpenSMIME;
import com.whiuk.philip.opensmime.R;
import com.whiuk.philip.opensmime.ui.adapter.KeyAdapter;

/**
 * Click listener for popup menu.
 */
public class PopupMenuClickListener implements PopupMenu.OnMenuItemClickListener, View.OnClickListener {
    private KeyAdapter keyAdapter;
    private KeyInfo keyInfo;

    public PopupMenuClickListener(KeyAdapter keyAdapter, KeyInfo keyInfo) {
        this.keyAdapter = keyAdapter;
        this.keyInfo = keyInfo;
    }

    @Override
    public void onClick(View v) {
        PopupMenu popup = new PopupMenu(App.getContext(), v);
        popup.setOnMenuItemClickListener(this);
        popup.inflate(R.menu.card_context);
        popup.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int id = item.getItemId();
        boolean own = keyInfo.getAlias().startsWith(OpenSMIME.KEY_PREFIX_OWN);
        if (id == R.id.delete) {
            keyAdapter.deleteCertificate(keyInfo);
        } else if (id == R.id.export) {
            keyAdapter.exportCertificate(keyInfo);
        }
        return true;
    }
}
