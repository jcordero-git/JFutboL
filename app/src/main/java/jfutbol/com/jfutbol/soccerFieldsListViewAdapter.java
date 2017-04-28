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

import jfutbol.com.jfutbol.model.mSoccerFields;

public class soccerFieldsListViewAdapter extends BaseAdapter {
    private final Context mContext;
    private List <mSoccerFields> mDataset= null;
    private ArrayList<mSoccerFields> arraylist;
    ViewHolder viewHolder;
    mSoccerFields values;
    String url_host_connection;
    ImageView status;

    public soccerFieldsListViewAdapter(Context context, List<mSoccerFields> dataset) {
        mContext = context;
        mDataset = dataset;

        this.arraylist = new ArrayList<mSoccerFields>();
        this.arraylist.addAll(mDataset);
        url_host_connection= context.getResources().getString(R.string.url_host_connection).toString();
    }

    @Override
    public int getCount() {
        return mDataset.size();
    }

    @Override
    public mSoccerFields getItem(int position) {
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
            convertView = LayoutInflater.from(mContext).inflate(R.layout.soccerfield_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.mImgSoccerField = (ImageView) convertView.findViewById(R.id.imgSoccerField);
            viewHolder.mlbSoccerCenterdName = (TextView) convertView.findViewById(R.id.lbSoccerCenterdName);
            viewHolder.mlbSoccerFieldName = (TextView) convertView.findViewById(R.id.lbSoccerFieldName);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        status= (ImageView) convertView.findViewById(R.id.status);

        values = mDataset.get(position);
        Integer soccerFieldId = values.getId();
        //String soccerFieldName = values.getName();


        AQuery aq;
        aq = new AQuery(null, convertView);

        aq.id(viewHolder.mImgSoccerField).image(url_host_connection+"/images/soccerfield/"+soccerFieldId+".png", true, true);
        viewHolder.mImgSoccerField.setAdjustViewBounds(false);
        viewHolder.mlbSoccerCenterdName.setText(values.getSoccerCenterName());
        viewHolder.mlbSoccerFieldName.setText(values.getName());

        return convertView;
    }

    private static class ViewHolder {
        public ImageView mImgSoccerField;
        public TextView mlbSoccerCenterdName;
        public TextView mlbSoccerFieldName;
    }


}
