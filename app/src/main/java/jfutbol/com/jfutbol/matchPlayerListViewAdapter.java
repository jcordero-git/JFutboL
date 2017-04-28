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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import jfutbol.com.jfutbol.model.PlayerSkills;
import jfutbol.com.jfutbol.model.User;

public class matchPlayerListViewAdapter extends BaseAdapter {
    private final Context mContext;
    private List <User> mDataset= null;
    private ArrayList<User> arraylist;
    TextView txtPlayerCompleteName;
    private PlayerSkills skills[];
    ViewHolder viewHolder;
    User values;
    String url_host_connection;
    ImageView status;
    ImageView extraPlayer;
    Integer mCaptainId;

    public matchPlayerListViewAdapter(Context context, List<User> dataset, Integer captainId) {
        mContext = context;
        mDataset = dataset;
        mCaptainId=captainId;

        this.arraylist = new ArrayList<User>();
        this.arraylist.addAll(mDataset);
        url_host_connection= context.getResources().getString(R.string.url_host_connection).toString();
    }

    @Override
    public int getCount() {
        return mDataset.size();
    }

    @Override
    public User getItem(int position) {
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
            convertView = LayoutInflater.from(mContext).inflate(R.layout.match_player_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.mPlayerPhoto = (ImageView) convertView.findViewById(R.id.playerPhotoID);
            viewHolder.mTxtCompleteName = (TextView) convertView.findViewById(R.id.txtPlayerCompleteNameID);
            viewHolder.mAge = (TextView) convertView.findViewById(R.id.txtAge);
            viewHolder.mPosition = (TextView) convertView.findViewById(R.id.txtPosition);
            viewHolder.mCaptain= (ImageView) convertView.findViewById(R.id.captain);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        status= (ImageView) convertView.findViewById(R.id.status);
        extraPlayer= (ImageView) convertView.findViewById(R.id.extraPlayer);

        values = mDataset.get(position);
        Integer playerId = values.getId();
        String playerName = values.getFirstName();
        String playerLastName = values.getLastName();
        String age = values.getAge();
        String userPosition = values.getPosition();

        AQuery aq;
        aq = new AQuery(null, convertView);

        if(values.getExtraPlayer()==1)
            aq.id(extraPlayer).image(R.drawable.extra);


        if(values.getRequestStatus()!=null) {
            if (values.getRequestStatus() == 0) {
              //  status.setBackgroundColor(convertView.getResources().getColor(R.color.rejected));
                aq.id(status).image(R.drawable.rejected);
            }
            if (values.getRequestStatus() == 1) {
             //   status.setBackgroundColor(convertView.getResources().getColor(R.color.pending));
                aq.id(status).image(R.drawable.pending);
            }
            if (values.getRequestStatus() == 2) {
              //  status.setBackgroundColor(convertView.getResources().getColor(R.color.approved));
                aq.id(status).image(R.drawable.approved);
            }
        }

        if(mCaptainId!=0) {
            if (mCaptainId.equals(values.getId())) {
               // viewHolder.mCaptain.setBackgroundColor(convertView.getResources().getColor(R.color.captain));
                aq.id(viewHolder.mCaptain).image(R.drawable.captain);
            }
            else{
                viewHolder.mCaptain.setBackgroundColor(0);
                aq.id(viewHolder.mCaptain).image(0);
            }
        }
        aq.id(viewHolder.mPlayerPhoto).image(url_host_connection+"/images/profile/"+playerId+".png", true, true);
        viewHolder.mPlayerPhoto.setAdjustViewBounds(false);
        viewHolder.mTxtCompleteName.setText(playerName);
        viewHolder.mAge.setText(age+" years old");
        viewHolder.mPosition.setText(userPosition);
        return convertView;
    }

    private static class ViewHolder {
        public ImageView mPlayerPhoto;
        public TextView mTxtCompleteName;
        public TextView mAge;
        public TextView mPosition;
        public ImageView mCaptain;
    }

    public void filter(String charText, ArrayList skills) {
        charText = charText.toLowerCase(Locale.getDefault());
        ArrayList <User> tempArrayList= new ArrayList<User>(mDataset);
        tempArrayList.clear();
        if (charText.length() == 0) {
           if(skills.size()==0)
           {
               tempArrayList.addAll(arraylist);
           }
            else {
               for (User wp : arraylist) {
                   if (wp.verifySkillName(skills)) {
                       tempArrayList.add(wp);
                   }
               }
           }
        }
        else
        {
            for (User wp : arraylist)
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
        mDataset=tempArrayList;
        notifyDataSetChanged();
    }

    public StringBuilder inputStreamToString(InputStream imput) {
        String rLine="";
        StringBuilder answer=new StringBuilder();
        BufferedReader rd=new BufferedReader(new InputStreamReader(imput));
        try {
            while ((rLine=rd.readLine())!=null)
            {
                answer.append(rLine);
            }
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
        return answer;
    }



}
