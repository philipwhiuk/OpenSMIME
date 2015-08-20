package de.fau.cs.mad.smile.android.encryption;


import android.content.Context;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Typeface;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

import de.fau.cs.mad.smile.android.encryption.R;

public class ExpandableListAdapter extends BaseExpandableListAdapter{

    private Context context;
    private List<String> listDataHeader; // header titles
    // child data in format of header title, child title
    private HashMap<String, List<Pair<Integer, String[]>>> listDataChild;

    public ExpandableListAdapter(Context context, List<String> listDataHeader,
                                 HashMap<String, List<Pair<Integer, String[]>>> listChildData) {
        this.context = context;
        this.listDataHeader = listDataHeader;
        this.listDataChild = listChildData;
    }

    @Override
    public int getGroupCount() {
        return this.listDataHeader.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this.listDataChild.get(this.listDataHeader.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.listDataHeader.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return this.listDataChild.get(this.listDataHeader.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.activity_help_list_group, parent, false);
        }

        TextView lblListHeader = (TextView) convertView
                .findViewById(R.id.listHeader);
        lblListHeader.setTypeface(null, Typeface.BOLD);
        lblListHeader.setText(headerTitle);

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final Pair<Integer, String[]> item = (Pair<Integer, String[]>) getChild(groupPosition, childPosition);
        if(item.first.equals((Integer.valueOf(0)))) {
            LayoutInflater infalInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.activity_help_list_item, parent, false);

            TextView listChild = (TextView) convertView.findViewById(R.id.listItem);
            listChild.setText(item.second[0]);
        } else if(item.first.equals((Integer.valueOf(1))))  {
            LayoutInflater infalInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.validity_circles, parent, false);
            String[] data = item.second;
            if(data.length == 6) {
                ImageView validCircle0 = (ImageView) convertView.findViewById(R.id.valid_circle_0);
                ImageView validCircle1 = (ImageView) convertView.findViewById(R.id.valid_circle_1);
                ImageView validCircle2 = (ImageView) convertView.findViewById(R.id.valid_circle_2);
                ImageView validCircle3 = (ImageView) convertView.findViewById(R.id.valid_circle_3);
                ImageView validCircle4 = (ImageView) convertView.findViewById(R.id.valid_circle_4);
                TextView text = (TextView) convertView.findViewById(R.id.valid_text);
                validCircle0.getBackground().setColorFilter(getColorFilter(data[0]));
                validCircle1.getBackground().setColorFilter(getColorFilter(data[1]));
                validCircle2.getBackground().setColorFilter(getColorFilter(data[2]));
                validCircle3.getBackground().setColorFilter(getColorFilter(data[3]));
                validCircle4.getBackground().setColorFilter(getColorFilter(data[4]));
                text.setText(data[5]);
            }
        } else {
            Log.d(SMileCrypto.LOG_TAG, "This case shouldn't occur. Check if you forgot to add a new case.");
        }
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    private ColorFilter getColorFilter(String color) {
        int iColor = Color.parseColor(color);

        int red = (iColor & 0xFF0000) / 0xFFFF;
        int green = (iColor & 0xFF00) / 0xFF;
        int blue = iColor & 0xFF;

        float[] matrix = { 0, 0, 0, 0, red
                , 0, 0, 0, 0, green
                , 0, 0, 0, 0, blue
                , 0, 0, 0, 1, 0 };

        return new ColorMatrixColorFilter(matrix);
    }
}