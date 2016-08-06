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

import jfutbol.com.jfutbol.model.mNotifications;
import jfutbol.com.jfutbol.model.PlayerSkills;

public class notificationListViewAdapter extends BaseAdapter {
    private final Context mContext;
    private List <mNotifications> mDataset= null;
    private ArrayList<mNotifications> arraylist;
    TextView txtPlayerCompleteName;
    private PlayerSkills skills[];
    ViewHolder viewHolder;
    mNotifications values;
    AQuery aq;
    String url_host_connection;
    //RelativeLayout RL;

    public notificationListViewAdapter(Context context, List<mNotifications> dataset) {
        mContext = context;
        mDataset = dataset;

        this.arraylist = new ArrayList<mNotifications>();
        this.arraylist.addAll(mDataset);
        url_host_connection= context.getResources().getString(R.string.url_host_connection).toString();


    }

    @Override
    public int getCount() {
        return mDataset.size();
    }

    @Override
    public mNotifications getItem(int position) {
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
            convertView = LayoutInflater.from(mContext).inflate(R.layout.notification_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.mImgNotification = (ImageView) convertView.findViewById(R.id.imgNotification);
            viewHolder.mTxtNotification = (TextView) convertView.findViewById(R.id.txtNotification);
            viewHolder.mTxtDate = (TextView) convertView.findViewById(R.id.txtDate);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        //RL= (RelativeLayout) convertView.findViewById(R.id.relativeLayout);
        //txtPlayerCompleteName= (TextView) convertView.findViewById(R.id.txtPlayerCompleteNameID);

        values = mDataset.get(position);
        String notification = values.getShortNotification();
        String date = values.getDateApp();

        //txtPlayerName.setHint(values.getUserId()+"");
        viewHolder.mTxtNotification.setText(notification);
        viewHolder.mTxtDate.setText(date);

        aq = new AQuery(null, convertView);

        if(values.getType()==1 || values.getType()==2)
            aq.id(viewHolder.mImgNotification).image(url_host_connection+"/images/team/"+values.getImg()+".png", true, true);

        if(values.getType()==5  || values.getType()==6)
            aq.id(viewHolder.mImgNotification).image(url_host_connection+"/images/soccerfield/"+values.getImg()+".png", true, true);



        return convertView;
    }

    private static class ViewHolder {
        public ImageView mImgNotification;
        public TextView mTxtNotification;
        public TextView mTxtDate;
    }
}
