package jfutbol.com.jfutbol;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import jfutbol.com.jfutbol.model.mSoccerFields;
import jfutbol.com.jfutbol.model.User;
import jfutbol.com.jfutbol.model.mSoccerFieldAvailableHours;
import jfutbol.com.jfutbol.singleton.Utils;
import jfutbol.com.jfutbol.singleton.singleton_token;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;


public class Soccer_Field_Info_Reserve extends Activity {

    Bundle params;

    String userJson;
    User user;
    String soccerFieldJson;
    mSoccerFields soccerField;

    View coordinatorLayout;

    String url_host_connection;
    String url_host_connection_secure;
    singleton_token token = singleton_token.getInstance();
    mSoccerFieldAvailableHours[] soccerFieldHours;
    mSoccerFieldAvailableHours[] soccerFieldHoursTemp;
    Gson gson = new GsonBuilder()
    .setDateFormat("yyyy-MM-dd")
    .create();

    GetSoccerFieldHoursTask getSoccerFieldHoursTask;
    EditText txtFields[];

    // variables to filter soccer fields
    //DateFormat dateSQLFormat = new SimpleDateFormat("yyyy-MM-dd");
   // DateFormat dateAppFormat = new SimpleDateFormat("dd/MM/yyy");
   // Date date = new Date();
    Calendar date = Calendar.getInstance();

    SimpleDateFormat dateAppFormat = new SimpleDateFormat("E dd/MMMM/yyyy");
    SimpleDateFormat dateSQLFormat = new SimpleDateFormat("yyyy-MM-dd");
    Date startHour=new Date();
    Date endHour=new Date();
    SimpleDateFormat timeAppFormat = new SimpleDateFormat("hh:mm a");
   // String date="", dateFormat="";
    int startHourSelected;
    int startMinutesSelected;
    int endHourSelected;
    int endMinutesSelected;
    String []openT;
    String []closeT;
    Toolbar bar;

    AQuery aq;
    boolean memCache = true;
    boolean fileCache = true;
    // variables to filter soccer fields

    //ShowCase
    final String FILTER_SHOWCASE = "soccerfieldreserve_filterShowCase";
    //ShowCase

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       setContentView(R.layout.activity_soccer_field_info_reserve);

        url_host_connection=getResources().getString(R.string.url_host_connection).toString();
        url_host_connection_secure=getResources().getString(R.string.url_host_connection).toString()+"/api";

        params=getIntent().getExtras();

        userJson = params.getString("user");
        user = gson.fromJson(userJson, User.class);

        soccerFieldJson  = params.getString("soccerfield");
        soccerField=gson.fromJson(soccerFieldJson, mSoccerFields.class);

       // Date today = DateUtils.truncate(new Date(), Calendar.DAY_OF_MONTH);
       // date="2015-10-04";
       // dateFormat="04/10/2015";

        FrameLayout root = (FrameLayout) findViewById(R.id.idRL);
        bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.settings_toolbar, root, false);
        root.addView(bar, 0); // insert at top
        bar.setTitle(dateAppFormat.format(date.getTime()));
        bar.inflateMenu(R.menu.menu_search_soccer_field_datetime);
        bar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                finish();
            }
        });
        bar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (menuItem.getTitle().equals(getResources().getString(R.string.filter).toString())) {
                    showPopupSoccerFieldFilter();
                }
                return false;
            }
        });
        showShowCase(bar.findViewById(R.id.filter), "You can use this button to change the match date and filter by hours", FILTER_SHOWCASE);

        openT=soccerField.getOpenTime().split(":");
        startHourSelected=Integer.parseInt(openT[0]);
        closeT=soccerField.getCloseTime().split(":");
        endHourSelected=Integer.parseInt(closeT[0]);

        coordinatorLayout = findViewById(R.id.coordinatorLayout);

        TextView lbSoccerCenterName = (TextView) findViewById(R.id.lbSoccerCenterName);
        TextView lbSoccerFieldName = (TextView) findViewById(R.id.lbSoccerFieldName);
        TextView lbTimeRange = (TextView) findViewById(R.id.lbTimeRange);

        aq = new AQuery(Soccer_Field_Info_Reserve.this, root);
        aq.id(R.id.imgSoccerField).image(url_host_connection+"/images/soccerfield/"+soccerField.getSoccerFieldId()+".png", memCache, fileCache);
        lbSoccerCenterName.setText(soccerField.getSoccerCenterName());
        lbSoccerFieldName.setText(soccerField.getName());
        lbTimeRange.setText("OPEN FROM: "+soccerField.getOpenTimeApp()+" TO "+soccerField.getCloseTimeApp());

        getSoccerFieldHoursTask = new GetSoccerFieldHoursTask();
        getSoccerFieldHoursTask.execute(dateSQLFormat.format(date.getTime()));
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private class GetSoccerFieldHoursTask extends AsyncTask<String, Void, mSoccerFieldAvailableHours[]> {
        private final ProgressDialog dialog = new ProgressDialog(Soccer_Field_Info_Reserve.this);
        protected void onPreExecute() {
            this.dialog.setMessage("Getting Socccer Field Hours...");
            this.dialog.setCancelable(false);
            this.dialog.show();
        }
        protected mSoccerFieldAvailableHours[] doInBackground(String... params) {
            final HttpClient httpClient = new DefaultHttpClient();
            JSONObject responseJSON = null;
            final HttpGet httpGet= new HttpGet(url_host_connection_secure+"/soccerfield/"+soccerField.getSoccerFieldId()+"/reservedHours/"+params[0]);
            httpGet.addHeader("x-access-token",token.getUser_token());
            try {
                try {
                    HttpResponse httpResponse = httpClient.execute(httpGet);
                    String jsonResult = inputStreamToString(httpResponse.getEntity().getContent()).toString();

                    soccerFieldHours= gson.fromJson(jsonResult, mSoccerFieldAvailableHours[].class);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }catch (RuntimeException e)
            {
                e.printStackTrace();
            }
            return soccerFieldHours;
        }
        @Override
        protected void onPostExecute(mSoccerFieldAvailableHours[] soccerFieldHours) {

            ListView list = (ListView) findViewById(R.id.listView);


            int timeNumer = endHourSelected-startHourSelected;
            int timeH=startHourSelected;
            String timeHString="";

            soccerFieldHoursTemp=new mSoccerFieldAvailableHours[timeNumer];

            for(int i=0; i< soccerFieldHoursTemp.length;i++) {
                if(timeH<10)
                    timeHString="0"+timeH;
                else
                    timeHString=timeH+"";
                soccerFieldHoursTemp[i]=new mSoccerFieldAvailableHours();
                soccerFieldHoursTemp[i].setStartTime(timeHString+":"+ openT[1]+":"+openT[2]);
                timeH++;
                if(timeH<10)
                    timeHString="0"+timeH;
                else
                    timeHString=timeH+"";
                soccerFieldHoursTemp[i].setEndTime(timeHString + ":" + openT[1]+":"+openT[2]);

                for(int i1=0;i1<soccerFieldHours.length;i1++)
                {
                    if(soccerFieldHours[i1].getStartTime().equals(soccerFieldHoursTemp[i].getStartTime()))
                        soccerFieldHoursTemp[i].setReserved(soccerFieldHours[i1].isReserved());
                }

            }
            List<mSoccerFieldAvailableHours> soccerFieldHourlist = Arrays.asList(soccerFieldHours);
            List<mSoccerFieldAvailableHours> soccerFieldHourTemplist = Arrays.asList(soccerFieldHoursTemp);
            soccerFieldHourListViewAdapter listAdapter = new soccerFieldHourListViewAdapter(Soccer_Field_Info_Reserve.this, soccerFieldHourTemplist, soccerFieldHourlist );
            list.setAdapter(listAdapter);

            list.setItemsCanFocus(false);
            list.setLongClickable(true);


            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    try {

                        mSoccerFieldAvailableHours mSoccerFieldAvailableHoursSelected = new mSoccerFieldAvailableHours();
                        String jsonSelected = "";
                        mSoccerFieldAvailableHoursSelected = (mSoccerFieldAvailableHours) parent.getItemAtPosition(position);
                        if(mSoccerFieldAvailableHoursSelected.isReserved()!=4 && mSoccerFieldAvailableHoursSelected.isReserved()!=5) {
                            Gson gson = new GsonBuilder()
                                    .setDateFormat("yyyy-MM-dd")
                                    .create();
                            mSoccerFieldAvailableHoursSelected.setSoccerFieldId(soccerField.getSoccerFieldId());
                            mSoccerFieldAvailableHoursSelected.setSoccerFieldName(soccerField.getName());
                            mSoccerFieldAvailableHoursSelected.setSoccerCenterdId(soccerField.getSoccerCenterId());
                            mSoccerFieldAvailableHoursSelected.setSoccerCenterName(soccerField.getSoccerCenterName());
                            mSoccerFieldAvailableHoursSelected.setDate(dateSQLFormat.format(date.getTime()));

                            jsonSelected = gson.toJson(mSoccerFieldAvailableHoursSelected);

                            Intent search_match = new Intent(Soccer_Field_Info_Reserve.this, SearchMatch.class);
                            Bundle params = new Bundle();
                            params.putString("user", userJson);
                            params.putString("soccerFieldAvailableHours", jsonSelected);
                            search_match.putExtras(params);
                            startActivityForResult(search_match, 1);
                        }
                        else {
                            //showToastDialog("This hour is not available", 3);
                            String message="The hour picked is not available";
                            Utils.ShowMessage(coordinatorLayout, message, 3);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }
        }//close onPostExecute
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

    public void showPopupSoccerFieldFilter() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Soccer_Field_Info_Reserve.this);
        LayoutInflater inflater = getLayoutInflater();
        builder.setTitle("Filter");
        builder.setCancelable(false);
        builder.setIcon(R.drawable.filter_b);
        View v=inflater.inflate(R.layout.layout_soccer_field_filter_datetime, null);
        txtFields = new EditText[3];

        txtFields[0] = (EditText) v.findViewById(R.id.txtDate);
        txtFields[1] = (EditText) v.findViewById(R.id.txtStartTime);
        txtFields[2] = (EditText) v.findViewById(R.id.txtEndTime);

        txtFields[0].setText(dateAppFormat.format(date.getTime()));
        startHour.setHours(startHourSelected);
        startHour.setMinutes(startMinutesSelected);

        txtFields[1].setText(timeAppFormat.format(startHour));
        endHour.setHours(endHourSelected);
        endHour.setMinutes(endMinutesSelected);

        txtFields[2].setText(timeAppFormat.format(endHour));

        txtFields[0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
                showDatePickerDialog(v);
            }
        });

        txtFields[1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showStartTimePickerDialog(v);
            }
        });

        txtFields[2].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEndTimePickerDialog(v);
            }
        });

        builder.setView(v)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                getSoccerFieldHoursTask = new GetSoccerFieldHoursTask();
                                getSoccerFieldHoursTask.execute(dateSQLFormat.format(date.getTime()));
                            }
                        }
                );
        builder.create();
        builder.show();
    }

    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                //month=month+1;
                /*
                String strMonth="";
                if(month<10) {
                    strMonth = "0" + month;
                }
                else {
                    strMonth = ""+month;
                }*/
                //date="" + year + "-" + strMonth  + "-" + day;
                //dateFormat=day + "/" + strMonth + "/" + year;
                date.set(Calendar.DAY_OF_MONTH, day);
                date.set(Calendar.MONTH, month);
                date.set(Calendar.YEAR, year);
                txtFields[0].setText(dateAppFormat.format(date.getTime()));
                bar.setTitle(dateAppFormat.format(date.getTime()));
            }
        };
        newFragment.show(getFragmentManager(), "datePicker");
    }

    public void showStartTimePickerDialog(View v) {
        DialogFragment newFragment = new TimePickerFragment() {
            @Override
            public void onTimeSet(TimePicker view, int hour, int minutes) {
                startHourSelected=hour;
                startMinutesSelected=minutes;
                startHour.setHours(hour);
                startHour.setMinutes(minutes);
                txtFields[1].setText(timeAppFormat.format(startHour));
            }
        };

        Bundle args_time = new Bundle();
        args_time.putInt("hour", startHourSelected);
        args_time.putInt("minutes", startMinutesSelected);
        newFragment.setArguments(args_time);
        newFragment.show(getFragmentManager(), "TimePicker");
    }

    public void showEndTimePickerDialog(View v) {
        DialogFragment newFragment = new TimePickerFragment() {
            @Override
            public void onTimeSet(TimePicker view, int hour, int minutes) {
                endHourSelected=hour;
                endMinutesSelected=minutes;
                endHour.setHours(hour);
                endHour.setMinutes(minutes);
                txtFields[2].setText(timeAppFormat.format(endHour));
            }
        };

        Bundle args_time = new Bundle();
        args_time.putInt("hour", endHourSelected);
        args_time.putInt("minutes", endMinutesSelected);
        newFragment.setArguments(args_time);
        newFragment.show(getFragmentManager(), "TimePicker");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if(resultCode == RESULT_OK){
                Intent returnIntent = new Intent();
                setResult(RESULT_OK,returnIntent);
                finish();
            }
        }
    }//onActivityResult

    public void showToastDialog(String message, int type){
        LayoutInflater inflater = Soccer_Field_Info_Reserve.this.getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast_layout, (ViewGroup) Soccer_Field_Info_Reserve.this.findViewById(R.id.custom_toast_layout));
        TextView text = (TextView) layout.findViewById(R.id.lbMessage);
        ImageView toastImage= (ImageView) layout.findViewById(R.id.toastImage);
        text.setText(message);
        if(type==1)
            toastImage.setImageResource(R.drawable.toast_success);
        if(type==2)
            toastImage.setImageResource(R.drawable.toast_warning);
        if(type==3)
            toastImage.setImageResource(R.drawable.toast_error);
        Toast toast = new Toast(Soccer_Field_Info_Reserve.this.getApplication().getApplicationContext());
        toast.setGravity(Gravity.BOTTOM, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }

    public void showShowCase( View target, String message, final String SHOWCASE_ID ){
        //MaterialShowcaseView.resetSingleUse(this, SHOWCASE_ID);
        new MaterialShowcaseView.Builder(this)
                .setTarget(target)
                .setDismissText("GOT IT")
                .setContentText(message)
                .setDelay(500) // optional but starting animations immediately in onCreate can make them choppy
                .singleUse(SHOWCASE_ID) // provide a unique ID used to ensure it is only shown once
                .setMaskColour(R.color.background_material_light)
                .setUseAutoRadius(true)
                .show();
    }
}
