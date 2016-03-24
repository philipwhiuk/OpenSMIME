package com.whiuk.philip.opensmime.ui.activity.items;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import java.util.LinkedHashMap;

import com.whiuk.philip.opensmime.App;
import com.whiuk.philip.opensmime.R;

/**
 * Represents personal and CA information
 */
public class PersonalAndCAInformationItem extends AbstractCertificateInfoItem {
    private LinkedHashMap<String, String[]> data;

    public void buildComplex(LinkedHashMap<String, String[]> data) {
        for(String[] arr : data.values()) {
            if(arr.length < 2) {
                throw new IllegalArgumentException("Each value of the hash map must have a minimal size of two.");
            }
        }
        this.data = data;
    }

    @Override
    public View getView(ViewGroup parent) {
        Context context = App.getContext();
        LayoutInflater layoutInflater = (LayoutInflater) App.getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View convertView = layoutInflater.inflate(R.layout.personal_information, parent, false);
        TableLayout table = (TableLayout) convertView.findViewById(R.id.table01);
        for(String key : data.keySet()) {
            String[] values = data.get(key);
            for (int i = 1; i < values.length; ++i) {
                LinearLayout row = (LinearLayout) layoutInflater.inflate(R.layout.table_row, table, false);
                TextView name = (TextView) row.findViewById(R.id.column_left);
                TextView entry = (TextView) row.findViewById(R.id.column_right);
                name.setText(values[0]);
                entry.setText(values[i]);
                table.addView(row);
            }
        }
        return convertView;
    }
}
