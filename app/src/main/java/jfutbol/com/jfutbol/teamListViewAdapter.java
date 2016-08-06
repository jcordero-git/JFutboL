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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.androidquery.AQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import jfutbol.com.jfutbol.model.Team;

public class teamListViewAdapter extends BaseAdapter {
    private final Context mContext;
    private List<Team> mDataset=null;
    private ArrayList<Team> arraylist;
    TextView txtTeamName;
    RelativeLayout RL;

    String url_host_connection;

    public teamListViewAdapter(Context context, List<Team> dataset) {
        mContext = context;
        mDataset = dataset;

        this.arraylist = new ArrayList<Team>();
        this.arraylist.addAll(mDataset);
    }

    @Override
    public int getCount() {
        return mDataset.size();
    }

    @Override
    public Team getItem(int position) {
        return mDataset.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.team_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.mImage = (ImageView) convertView.findViewById(R.id.imgImage);
            viewHolder.mTeamName = (TextView) convertView.findViewById(R.id.lbTeamName);
            viewHolder.mCompleteCaptainName = (TextView) convertView.findViewById(R.id.lbCaptainName);
            viewHolder.mProvince = (TextView) convertView.findViewById(R.id.lbProvince);
            viewHolder.mCanton = (TextView) convertView.findViewById(R.id.lbCanton);
            viewHolder.mCountplayers = (TextView) convertView.findViewById(R.id.lbCountplayers);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        RL= (RelativeLayout) convertView.findViewById(R.id.relativeLayout);
        url_host_connection=convertView.getResources().getString(R.string.url_host_connection).toString();
        AQuery aq;
        aq = new AQuery(null, convertView);
        Team values = mDataset.get(position);
        aq.id(viewHolder.mImage).image(url_host_connection+"/images/team/"+values.getTeamId()+".png", true, true);
        viewHolder.mTeamName.setText(values.getName());
        viewHolder.mTeamName.setHint(values.getTeamId()+"");
        viewHolder.mProvince.setText(values.getProvinceName());
        viewHolder.mCanton.setText(values.getCantonName());
        viewHolder.mCompleteCaptainName.setText(values.getCompleteCaptainName());
        viewHolder.mCountplayers.setText(values.getCountPlayers()+"");
        return convertView;
    }

    private static class ViewHolder {
        public ImageView mImage;
        public TextView mTeamName;
        public TextView mProvince;
        public TextView mCanton;
        public TextView mCompleteCaptainName;
        public TextView mCountplayers;
    }

    public void filter(String charText, ArrayList skills) {
        charText = charText.toLowerCase(Locale.getDefault());
        ArrayList <Team> tempArrayList= new ArrayList<Team>(mDataset);
        tempArrayList.clear();

        if (charText.length() != 0) {
            for (Team wp : arraylist) {
                    if (wp.getName().toLowerCase(Locale.getDefault()).contains(charText)) {
                        tempArrayList.add(wp);
                    }

            }
        }
        else{
            tempArrayList.addAll(arraylist);
        }


        /*
        if (charText.length() == 0) {
            if(skills.size()==0)
            {
                tempArrayList.addAll(arraylist);
            }
            else {
                for (Team wp : arraylist) {
                    if (wp.verifySkillName(skills)) {
                        tempArrayList.add(wp);
                    }
                }
            }
        }
        else
        {
            for (Team wp : arraylist)
            {
                if(skills.size()==0)
                {
                    if (wp.getFirstName().toLowerCase(Locale.getDefault()).contains(charText) ||
                            wp.getLastName().toLowerCase(Locale.getDefault()).contains(charText)) {
                        tempArrayList.add(wp);
                    }
                }
                else {
                    if (wp.verifySkillName(skills)) {
                        if (wp.getFirstName().toLowerCase(Locale.getDefault()).contains(charText) ||
                                wp.getLastName().toLowerCase(Locale.getDefault()).contains(charText)) {
                            tempArrayList.add(wp);
                        }
                    }
                }
            }
        }
        */
        mDataset=tempArrayList;
        notifyDataSetChanged();
    }

}
