package jfutbol.com.jfutbol;

/**
 * Created by JOSEPH on 07/04/2015.
 */
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import jfutbol.com.jfutbol.model.PlayerSkills;

public class skillListViewAdapter extends BaseAdapter {
    private final Context mContext;
    //private final PlayerSkills[] mDataset;
    private List<PlayerSkills> mDataset= null;
    private ArrayList<PlayerSkills> arraylist;
    RelativeLayout RL;

    String url_host_connection;

    public skillListViewAdapter(Context context, List<PlayerSkills> dataset) {
        mContext = context;
        mDataset = dataset;
        this.arraylist = new ArrayList<PlayerSkills>();
        this.arraylist.addAll(mDataset);
    }

    @Override
    public int getCount() {
        return mDataset.size();
    }

    @Override
    public PlayerSkills getItem(int position) { return mDataset.get(position); }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.skill_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.mTextView = (TextView) convertView.findViewById(R.id.lbSkill);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        RL= (RelativeLayout) convertView.findViewById(R.id.relativeLayout);

        PlayerSkills values = mDataset.get(position);
        String skillName = values.getSkillName();
        viewHolder.mTextView.setText(skillName);
        return convertView;
    }
    private static class ViewHolder {
        public TextView mTextView;
    }

    public void filter(Integer statusSkill) {
        ArrayList <PlayerSkills> tempArrayList= new ArrayList<PlayerSkills>(mDataset);
        tempArrayList.clear();

        for (PlayerSkills wp : arraylist) {
            if (wp.getIntValue()== statusSkill) {
                tempArrayList.add(wp);
            }
        }
        mDataset=tempArrayList;
        notifyDataSetChanged();
    }


}
