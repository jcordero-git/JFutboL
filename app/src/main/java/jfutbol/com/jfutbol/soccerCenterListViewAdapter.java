package jfutbol.com.jfutbol;

/**
 * Created by JOSEPH on 07/04/2015.
 */

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;

import java.util.ArrayList;
import java.util.List;

import jfutbol.com.jfutbol.model.mSoccerCenters;

public class soccerCenterListViewAdapter extends BaseAdapter {
    private final Context mContext;
    private List <mSoccerCenters> mDataset= null;
    private ArrayList<mSoccerCenters> arraylist;
    ViewHolder viewHolder;
    String url_host_connection;
    mSoccerCenters values;
    //RelativeLayout RL;

    public soccerCenterListViewAdapter(Context context, List<mSoccerCenters> dataset) {
        mContext = context;
        mDataset = dataset;

        this.arraylist = new ArrayList<mSoccerCenters>();
        this.arraylist.addAll(mDataset);
        url_host_connection= context.getResources().getString(R.string.url_host_connection).toString();
    }

    @Override
    public int getCount() {
        return mDataset.size();
    }

    @Override
    public mSoccerCenters getItem(int position) {
        return mDataset.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.soccercenter_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.mImgSoccerCenter = (ImageView) convertView.findViewById(R.id.imgSoccerCenter);
            viewHolder.mLbSoccerCenterName = (TextView) convertView.findViewById(R.id.lbSoccerCenterName);
            viewHolder.mLbPhone = (TextView) convertView.findViewById(R.id.lbPhone);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        values = mDataset.get(position);
        Integer soccerCenterId = values.getSoccerCenterId();

        AQuery aq;
        aq = new AQuery(null, convertView);

        aq.id(viewHolder.mImgSoccerCenter).image(url_host_connection+"/images/soccercenter/"+soccerCenterId+".png", true, true);

        viewHolder.mLbSoccerCenterName.setText(values.getName());
        viewHolder.mLbPhone.setText(values.getPhone());
        return convertView;
    }

    private static class ViewHolder {
        public ImageView mImgSoccerCenter;
        public TextView mLbSoccerCenterName;
        public TextView mLbPhone;
    }
}
