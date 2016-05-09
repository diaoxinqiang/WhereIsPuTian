package com.tangentlu.whereisputian;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class HospitalLvItemAdapter extends BaseAdapter {

    private List<HospitalItem> objects = new ArrayList<HospitalItem>();

    private Context context;
    private LayoutInflater layoutInflater;

    public HospitalLvItemAdapter(Context context, List<HospitalItem> objects) {
        this.objects = objects;
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public HospitalItem getItem(int position) {
        return objects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.hospital_lv_item, null);
            convertView.setTag(new ViewHolder(convertView));
        }
        initializeViews((HospitalItem) getItem(position), (ViewHolder) convertView.getTag());
        return convertView;
    }

    private void initializeViews(HospitalItem object, ViewHolder holder) {
        holder.hospitalName.setText(object.queryHospitalName);
        holder.hospitalAddress.setText(object.address);
    }

    protected class ViewHolder {
        private TextView hospitalName;
        private TextView hospitalAddress;

        public ViewHolder(View view) {
            hospitalName = (TextView) view.findViewById(R.id.hospitalName);
            hospitalAddress = (TextView) view.findViewById(R.id.hospitalAddress);
        }
    }
}
