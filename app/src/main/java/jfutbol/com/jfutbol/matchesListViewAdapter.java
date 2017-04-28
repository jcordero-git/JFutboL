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

import jfutbol.com.jfutbol.model.mMatches;

public class matchesListViewAdapter extends BaseAdapter {
    private final Context mContext;
    private List <mMatches> mDataset= null;
    private ArrayList<mMatches> arraylist;
    ViewHolder viewHolder;
    String url_host_connection;
    mMatches values;
    //RelativeLayout RL;

    public matchesListViewAdapter(Context context, List<mMatches> dataset) {
        mContext = context;
        mDataset = dataset;

        this.arraylist = new ArrayList<mMatches>();
        this.arraylist.addAll(mDataset);
        url_host_connection= context.getResources().getString(R.string.url_host_connection).toString();
    }

    @Override
    public int getCount() {
        return mDataset.size();
    }

    @Override
    public mMatches getItem(int position) {
        return mDataset.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {
//        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.matches_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.mImgTeam1 = (ImageView) convertView.findViewById(R.id.imgTeam1);
            viewHolder.mImgTeam2 = (ImageView) convertView.findViewById(R.id.imgTeam2);
            viewHolder.mLbTeam1Name = (TextView) convertView.findViewById(R.id.lbTeam1Name);
            viewHolder.mLbGoalsTeam1 = (TextView) convertView.findViewById(R.id.lbGoalsTeam1);
            viewHolder.mLbTeam2Name = (TextView) convertView.findViewById(R.id.lbTeam2Name);
            viewHolder.mLbGoalsTeam2 = (TextView) convertView.findViewById(R.id.lbGoalsTeam2);
            viewHolder.mLbMatchDate = (TextView) convertView.findViewById(R.id.lbMatchDate);
            viewHolder.mSoccerCenterName = (TextView) convertView.findViewById(R.id.lbSoccerCenterName);
            viewHolder.mstatus = (ImageView) convertView.findViewById(R.id.status);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        AQuery aq;
        aq = new AQuery(null, convertView);

        values = mDataset.get(position);

        aq.id(viewHolder.mImgTeam1).image(url_host_connection+"/images/team/"+values.getTeam1Id()+".png", true, true);
        aq.id(viewHolder.mImgTeam2).image(url_host_connection+"/images/team/"+values.getTeam2Id()+".png", true, true);
        viewHolder.mLbTeam1Name.setText(values.getTeam1Name());
        viewHolder.mLbGoalsTeam1.setText(values.getGoalsTeam1()+"");
        viewHolder.mLbTeam2Name.setText(values.getTeam2Name());
        viewHolder.mLbGoalsTeam2.setText(values.getGoalsTeam2()+"");
        viewHolder.mLbMatchDate.setText(values.getDateApp().toString()+" "+values.getStartTimeApp()+" - "+values.getEndTimeApp());
        viewHolder.mSoccerCenterName.setText(values.getSoccerCenterName()+" - "+values.getSoccerFieldName());


        if(values.getIsReserved()==-1)
        {
            //viewHolder.mLbMatchDate.setBackgroundColor(convertView.getResources().getColor(R.color.rejected));
           // viewHolder.mstatus.setBackgroundColor(convertView.getResources().getColor(R.color.rejected));
            aq.id(viewHolder.mstatus).image(R.drawable.rejected);
        }
        if(values.getIsReserved()==0)
        {
           // viewHolder.mstatus.setBackgroundColor(convertView.getResources().getColor(R.color.rejected));
            aq.id(viewHolder.mstatus).image(R.drawable.rejected);
        }
        if(values.getIsReserved()==1)
        {
           // viewHolder.mstatus.setBackgroundColor(convertView.getResources().getColor(R.color.pending));
            aq.id(viewHolder.mstatus).image(R.drawable.pending);
        }
        if(values.getIsReserved()==2)
        {
           // viewHolder.mstatus.setBackgroundColor(convertView.getResources().getColor(R.color.pending));
            aq.id(viewHolder.mstatus).image(R.drawable.pending);
        }
        if(values.getIsReserved()==3)
        {
          //  viewHolder.mstatus.setBackgroundColor(convertView.getResources().getColor(R.color.pending));
            aq.id(viewHolder.mstatus).image(R.drawable.pending);
        }
        if(values.getIsReserved()==4)
        {
          //  viewHolder.mstatus.setBackgroundColor(convertView.getResources().getColor(R.color.approved));
            aq.id(viewHolder.mstatus).image(R.drawable.approved);
        }
        if(values.getIsReserved()==5)
        {
          //  viewHolder.mstatus.setBackgroundColor(convertView.getResources().getColor(R.color.rejected));
            aq.id(viewHolder.mstatus).image(R.drawable.rejected);
        }


        return convertView;
    }

    private static class ViewHolder {
        public ImageView mImgTeam1;
        public ImageView mImgTeam2;
        public TextView mLbTeam1Name;
        public TextView mLbTeam2Name;
        public TextView mLbGoalsTeam1;
        public TextView mLbGoalsTeam2;
        public TextView mLbMatchDate;
        public TextView mSoccerCenterName;
        public ImageView mstatus;
    }
}
