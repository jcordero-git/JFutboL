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

import jfutbol.com.jfutbol.model.mSoccerFieldAvailableHours;

public class soccerFieldHourListViewAdapter extends BaseAdapter {
    private final Context mContext;
    private List <mSoccerFieldAvailableHours> mDataset= null;
    private List <mSoccerFieldAvailableHours> mDatasetReserved= null;

    private ArrayList<mSoccerFieldAvailableHours> arraylist;
    TextView txtPlayerCompleteName;
    ViewHolder viewHolder;
    mSoccerFieldAvailableHours values;
    String url_host_connection;
    ImageView status;

    public soccerFieldHourListViewAdapter(Context context, List<mSoccerFieldAvailableHours> dataset, List<mSoccerFieldAvailableHours> datasetReserved) {
        mContext = context;
        mDataset = dataset;
        mDatasetReserved = datasetReserved;

        this.arraylist = new ArrayList<mSoccerFieldAvailableHours>();
        this.arraylist.addAll(mDataset);
        url_host_connection= context.getResources().getString(R.string.url_host_connection).toString();
    }

    @Override
    public int getCount() {
        return mDataset.size();
    }

    @Override
    public mSoccerFieldAvailableHours getItem(int position) {
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
            convertView = LayoutInflater.from(mContext).inflate(R.layout.soccer_field_hour_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.mSoccerFieldHourImg = (ImageView) convertView.findViewById(R.id.imgSoccerFieldImg);
            viewHolder.mTxtTimeRange = (TextView) convertView.findViewById(R.id.lbTimeRange);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        //status= (ImageView) convertView.findViewById(R.id.status);



        //txtPlayerCompleteName= (TextView) convertView.findViewById(R.id.txtPlayerCompleteNameID);

        values = mDataset.get(position);

        AQuery aq;
        aq = new AQuery(null, convertView);

       // viewHolder.mSoccerFieldHourImg.setBackgroundColor(convertView.getResources().getColor(R.color.approved));
        aq.id(viewHolder.mSoccerFieldHourImg).image(R.drawable.approved);


        for(int i=0; i< mDatasetReserved.size();i++) {
            if(values.getStartTime().equals(mDatasetReserved.get(i).getStartTime())) {
                if(mDatasetReserved.get(i).isReserved()==1||mDatasetReserved.get(i).isReserved()==2||mDatasetReserved.get(i).isReserved()==3) {
                  //  viewHolder.mSoccerFieldHourImg.setBackgroundColor(convertView.getResources().getColor(R.color.pending));
                    aq.id(viewHolder.mSoccerFieldHourImg).image(R.drawable.pending);
                }
               if(mDatasetReserved.get(i).isReserved()==4||mDatasetReserved.get(i).isReserved()==5) {
                  // viewHolder.mSoccerFieldHourImg.setBackgroundColor(convertView.getResources().getColor(R.color.rejected));
                   aq.id(viewHolder.mSoccerFieldHourImg).image(R.drawable.rejected);

               }
            }
        }


        /*
        if (values.getRequestStatus() == 0) {
           status.setBackgroundColor(convertView.getResources().getColor(R.color.rejected));
           aq.id(status).image(R.drawable.rejected);
        }
            if (values.getRequestStatus() == 1) {
                status.setBackgroundColor(convertView.getResources().getColor(R.color.pending));
                aq.id(status).image(R.drawable.pending);
            }
            if (values.getRequestStatus() == 2) {
                status.setBackgroundColor(convertView.getResources().getColor(R.color.approved));
                aq.id(status).image(R.drawable.approved);
            }
        */


      //  aq.id(viewHolder.mSoccerFieldHourImg).image(url_host_connection+"/images/profile/"+playerId+".png", true, true);
        viewHolder.mTxtTimeRange.setText(values.getStartTimeApp()+" - "+values.getEndTimeApp());
        return convertView;
    }

    private static class ViewHolder {
        public ImageView mSoccerFieldHourImg;
        public TextView mTxtTimeRange;
    }

}
