package jfutbol.com.jfutbol;

import android.app.Activity;
import android.app.AlertDialog;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;


import jfutbol.com.jfutbol.model.Team;
import jfutbol.com.jfutbol.model.User;
import jfutbol.com.jfutbol.model.mMatches;
import jfutbol.com.jfutbol.model.mSoccerFieldAvailableHours;
import jfutbol.com.jfutbol.singleton.singleton_token;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;


public class SearchMatch extends Activity {

    String url_host_connection;
    String url_host_connection_secure;
    singleton_token token = singleton_token.getInstance();

    AQuery aq;
    boolean memCache = true;
    boolean fileCache = true;

    TextView lbTeamName1;
    //TextView lbCaptainName1;
    TextView lbTeamName2;
    //TextView lbCaptainName2;
    TextView lbSoccerCenterName;
    TextView lbSoccerFieldName;

    TextView lbMatchDate;
    TextView lbMatchTime;
    RelativeLayout RLTeam1;
    RelativeLayout RLTeam2;
    RelativeLayout RLPlace;
    ImageView imgTeam1;
    ImageView imgTeam2;
    ImageView imgSoccerField;

    //int yearSelected;
    //int monthSelected;
    //int daySelected;
    //int hourSelected;
    //int minutesSelected;

    Bundle params;
    String userJson;
    User user;
    String team1Json;
    Team team1;
    String soccerFieldAvailableHoursJson;
    mSoccerFieldAvailableHours soccerFieldAvailableHours;

    SimpleDateFormat dateAppFormat = new SimpleDateFormat("E dd/MMMM/yyyy");
    SimpleDateFormat dateSQLFormat = new SimpleDateFormat("yyyy-MM-dd");

    Gson gson;

    String team2Json;
    Team team2;

    mMatches matches;

    //ShowCase
    final String ADD_TEAM1_SHOWCASE = "searchMatch_addTeam1ShowCase";
    final String ADD_TEAM2_SHOWCASE = "searchMatch_addTeam2ShowCase";
    final String CHANGE_SOCCER_FIELD_SHOWCASE = "searchMatch_changeSoccerFieldShowCase";
    //ShowCase

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_match);

        url_host_connection=getResources().getString(R.string.url_host_connection).toString();
        url_host_connection_secure=getResources().getString(R.string.url_host_connection).toString()+"/api";
        matches=new mMatches();

        gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd")
                .create();


        aq = new AQuery(SearchMatch.this, null);
        params=getIntent().getExtras();

        RelativeLayout root = (RelativeLayout) findViewById(R.id.idRL);
        Toolbar bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.settings_toolbar, root, false);
        root.addView(bar, 0); // insert at top
        bar.setTitle("CREATING MATCH");
        bar.inflateMenu(R.menu.menu_user__register);
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
                if (menuItem.getTitle().equals(getResources().getString(R.string.send).toString())) {
                    RegisterMatchTask task = new RegisterMatchTask();
                    task.execute(matches);
                }
                return true;
            }
        });

        lbTeamName1= (TextView) findViewById(R.id.lbTeamName1);
        //lbCaptainName1= (TextView) findViewById(R.id.lbCaptainName1);
        lbTeamName2= (TextView) findViewById(R.id.lbTeamName2);
        //lbCaptainName2= (TextView) findViewById(R.id.lbCaptainName2);


        lbSoccerCenterName = (TextView) findViewById(R.id.lbSoccerCenterName);
        lbSoccerFieldName = (TextView) findViewById(R.id.lbSoccerFieldName);
        lbMatchDate = (TextView) findViewById(R.id.lbMatchDate);
        lbMatchTime = (TextView) findViewById(R.id.lbMatchTime);

        imgTeam1 = (ImageView) findViewById(R.id.imgTeam1);
        imgTeam2 = (ImageView) findViewById(R.id.imgTeam2);
        imgSoccerField = (ImageView) findViewById(R.id.imgPlace);


        if(params.getString("user")!=null) {
            userJson = params.getString("user");
            user = gson.fromJson(userJson, User.class);
        }
        if(params.getString("team")!=null) {
            team1Json = params.getString("team");
            team1 = gson.fromJson(team1Json, Team.class);
            lbTeamName1.setText(team1.getName());
            matches.setTeam1Id(team1.getId());
            matches.setTeam1Name(team1.getName());
        }
        if(params.getString("soccerFieldAvailableHours")!=null) {
            soccerFieldAvailableHoursJson = params.getString("soccerFieldAvailableHours");
            soccerFieldAvailableHours = gson.fromJson(soccerFieldAvailableHoursJson, mSoccerFieldAvailableHours.class);
            aq.id(R.id.imgPlace).image(url_host_connection+"/images/soccerfield/"+soccerFieldAvailableHours.getSoccerFieldId()+".png", memCache, fileCache);
            lbSoccerCenterName.setText(soccerFieldAvailableHours.getSoccerCenterName());
            lbSoccerFieldName.setText(soccerFieldAvailableHours.getSoccerFieldName());
            //lbMatchDate.setText(dateAppFormat.format(soccerFieldAvailableHours.getDate().getTime()));
            lbMatchDate.setText(soccerFieldAvailableHours.getDateApp());
            lbMatchTime.setText(soccerFieldAvailableHours.getStartTimeApp()+" - "+soccerFieldAvailableHours.getEndTimeApp());

            matches.setDate(soccerFieldAvailableHours.getDate());
            matches.setStartTime(soccerFieldAvailableHours.getStartTime());
            matches.setEndTime(soccerFieldAvailableHours.getEndTime());
            matches.setSoccerFieldId(soccerFieldAvailableHours.getSoccerFieldId());

        }

        RLTeam1 = (RelativeLayout) findViewById(R.id.RLTeam1);
        RLTeam2 = (RelativeLayout) findViewById(R.id.RLTeam2);
        RLPlace = (RelativeLayout) findViewById(R.id.RLPlace);

        RLTeam1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent search_team = new Intent(SearchMatch.this, jfutbol.com.jfutbol.SearchTeam.class);
                final Bundle params=new Bundle();
                params.putString("user", userJson );
                params.putBoolean("myTeams", true);
                search_team.putExtras(params);
                startActivityForResult(search_team,1);
            }
        });

        RLTeam2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    final Intent search_team = new Intent(SearchMatch.this, jfutbol.com.jfutbol.SearchTeam.class);
                    final Bundle params = new Bundle();
                    params.putString("user", userJson);
                    params.putBoolean("myTeams", false);
                    search_team.putExtras(params);
                    startActivityForResult(search_team, 2);
            }
        });

        RLPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                setResult(RESULT_OK,returnIntent);
                finish();
            }
        });
        showShowCase(imgTeam1, "You must pick your team", ADD_TEAM1_SHOWCASE );
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private class RegisterMatchTask extends AsyncTask<mMatches, Void, JSONObject> {

        private final ProgressDialog dialog = new ProgressDialog(SearchMatch.this);
        JSONObject object_feed;
        // can use UI thread here
        protected void onPreExecute() {
            this.dialog.setMessage("Loading...");
            this.dialog.setCancelable(false);
            this.dialog.show();
        }

        protected JSONObject doInBackground(mMatches... params) {
            // TODO Auto-generated method stub
            final HttpClient httpClient= new DefaultHttpClient();
            JSONObject responseJSON = null;
            final HttpPost httpPost= new HttpPost(url_host_connection_secure+"/matches/");
            httpPost.addHeader("x-access-token",token.getUser_token());
            try {
                try {
                    Gson gson = new GsonBuilder()
                            .setDateFormat("yyyy-MM-dd HH:mm")
                            .create();
                    String json = gson.toJson(matches);
                    JSONObject jsonobj= new JSONObject(json);

                    StringEntity se = new StringEntity(jsonobj.toString());
                    se.setContentType("application/json;charset=UTF-8");
                    se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,"application/json;charset=UTF-8"));
                    httpPost.setEntity(se);
                    HttpResponse httpResponse = httpClient.execute(httpPost);
                    String jsonResult = inputStreamToString(httpResponse.getEntity().getContent()).toString();
                    responseJSON = new JSONObject(jsonResult);

                } catch (Exception e) {
                    //user.setUserId(0);
                    e.printStackTrace();
                }
            }catch (RuntimeException e)
            {
                //user.setUserId(0);
                e.printStackTrace();
            }
            return responseJSON;
        }//close doInBackground

        @Override
        protected void onPostExecute(JSONObject responseJSON) {

            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }

            try {
                if (responseJSON.getInt("code")==2000) {
                    //showToastDialog("The match was set successfully", 1);
                    //showToastDialog("The match is waiting for soccer field owner confirmation",2);
                    Intent returnIntent = new Intent();
                    setResult(RESULT_OK,returnIntent);
                    finish();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            //  txt_Error.setText("Sorry!! Incorrect Username or Password");

        }//close onPostExecute
    }

    public void showDialog(final String txt) throws Exception {
        AlertDialog.Builder builder = new AlertDialog.Builder(SearchMatch.this);
        builder.setMessage(txt);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
                dialog.dismiss();
            }
        });
        builder.show();
    }

    public void showToastDialog(String message, int type){
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast_layout,(ViewGroup) findViewById(R.id.custom_toast_layout));
        TextView text = (TextView) layout.findViewById(R.id.lbMessage);
        ImageView toastImage= (ImageView) layout.findViewById(R.id.toastImage);
        text.setText(message);
        if(type==1)
            toastImage.setImageResource(R.drawable.toast_success);
        if(type==2)
            toastImage.setImageResource(R.drawable.toast_warning);
        if(type==3)
            toastImage.setImageResource(R.drawable.toast_error);
        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.BOTTOM, 0, 0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
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

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if(resultCode == RESULT_OK){
                team1Json=data.getStringExtra("team");
                team1=gson.fromJson(team1Json, Team.class);
                aq.id(R.id.imgTeam1).image(url_host_connection+"/images/team/"+team1.getId()+".png", memCache, fileCache);
                lbTeamName1.setText(team1.getName());
                //lbCaptainName1.setText("Captain Name");
                matches.setTeam1Id(team1.getId());
                matches.setTeam1Name(team1.getName());
                showShowCase(imgTeam2, "You must pick an opposing team", ADD_TEAM2_SHOWCASE );
            }
            if (resultCode == RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }

        if (requestCode == 2) {
            if(resultCode == RESULT_OK){
                team2Json=data.getStringExtra("team");
                team2=gson.fromJson(team2Json, Team.class);
                aq.id(R.id.imgTeam2).image(url_host_connection+"/images/team/"+team2.getId()+".png", memCache, fileCache);
                lbTeamName2.setText(team2.getName());
                //lbCaptainName2.setText("Captain Name");
                matches.setTeam2Id(team2.getId());
                matches.setTeam2Name(team2.getName());
                showShowCase(imgSoccerField, "Also you can change the soccer field clicking this icon", CHANGE_SOCCER_FIELD_SHOWCASE);
            }
            if (resultCode == RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }//onActivityResult

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
