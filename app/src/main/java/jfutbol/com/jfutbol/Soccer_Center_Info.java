package jfutbol.com.jfutbol;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.melnykov.fab.FloatingActionButton;
import com.melnykov.fab.ScrollDirectionListener;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import jfutbol.com.jfutbol.model.mSoccerCenters;
import jfutbol.com.jfutbol.model.mSoccerFields;
import jfutbol.com.jfutbol.singleton.Utils;
import jfutbol.com.jfutbol.singleton.singleton_token;


public class Soccer_Center_Info extends Activity {

    Bundle params;
    String soccerCenterJson;
    mSoccerCenters soccerCenter;
    TextView lbSoccerCenterName;
    TextView lbPhone;
    AQuery aq;
    boolean memCache = false;
    boolean fileCache = false;

    String url_host_connection;
    String url_host_connection_secure;
    singleton_token token = singleton_token.getInstance();
    mSoccerFields[] soccerFields;
    Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd")
            .create();

    View coordinatorLayout;

    //Button btnPlayers;
    FloatingActionButton fab;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       setContentView(R.layout.activity_soccer_center_info);

        url_host_connection=getResources().getString(R.string.url_host_connection).toString();
        url_host_connection_secure=getResources().getString(R.string.url_host_connection).toString()+"/api";

        coordinatorLayout = findViewById(R.id.coordinatorLayout);
        lbSoccerCenterName = (TextView) findViewById(R.id.lbSoccerCenterName);
        lbPhone = (TextView) findViewById(R.id.lbPhone);

        params=getIntent().getExtras();
        soccerCenterJson  = params.getString("soccercenter");
        soccerCenter=gson.fromJson(soccerCenterJson, mSoccerCenters.class);

        FrameLayout root = (FrameLayout) findViewById(R.id.idRL);
        Toolbar bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.settings_toolbar, root, false);
        root.addView(bar, 0); // insert at top
        bar.setTitle(soccerCenter.getName());
        bar.inflateMenu(R.menu.menu_soccer_center_info);
        bar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                setResult(RESULT_OK, returnIntent);
                finish();
            }
        });

        aq = new AQuery(Soccer_Center_Info.this, root);
        aq.id(R.id.imgSoccerCenter).image(url_host_connection + "/images/soccercenter/" + soccerCenter.getId() + ".png", memCache, fileCache);
        lbSoccerCenterName.setText(soccerCenter.getName());
        lbPhone.setText(soccerCenter.getPhone());

        GetSoccerFieldsTask getTeamPlayersTask = new GetSoccerFieldsTask();
        getTeamPlayersTask.execute();
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private class GetSoccerFieldsTask extends AsyncTask<mSoccerFields, Void, mSoccerFields[]> {
        private final ProgressDialog dialog = new ProgressDialog(Soccer_Center_Info.this);
        protected void onPreExecute() {
            this.dialog.setMessage("Getting Fields...");
            this.dialog.setCancelable(false);
            this.dialog.show();
        }
        protected mSoccerFields[] doInBackground(mSoccerFields... params) {
            final HttpClient httpClient = new DefaultHttpClient();
            JSONObject responseJSON = null;
            final HttpGet httpGet= new HttpGet(url_host_connection_secure+"/soccercenter/"+soccerCenter.getId()+"/soccerfields");
            httpGet.addHeader("x-access-token",token.getUser_token());
            try {
                try {
                    HttpResponse httpResponse = httpClient.execute(httpGet);
                    String jsonResult = inputStreamToString(httpResponse.getEntity().getContent()).toString();
                    soccerFields= gson.fromJson(jsonResult, mSoccerFields[].class);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }catch (RuntimeException e)
            {
                e.printStackTrace();
            }
            return soccerFields;
        }
        @Override
        protected void onPostExecute(mSoccerFields[] soccerFields) {

            ListView list = (ListView) findViewById(R.id.listView);
            List<mSoccerFields> soccerFieldlist = Arrays.asList(soccerFields);
            soccerFieldsListViewAdapter listAdapter = new soccerFieldsListViewAdapter(Soccer_Center_Info.this, soccerFieldlist);
            list.setAdapter(listAdapter);

            list.setItemsCanFocus(false);
            list.setLongClickable(true);


            list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
                @Override
                public boolean onItemLongClick(AdapterView<?> av, View v, int pos, long id)
                {
                    try {
                        mSoccerFields soccerFieldSelected=new mSoccerFields();
                        String jsonSoccerFieldSelected="";
                        soccerFieldSelected=(mSoccerFields)av.getItemAtPosition(pos);
                        Gson gson = new GsonBuilder()
                                .setDateFormat("yyyy-MM-dd")
                                .create();
                        jsonSoccerFieldSelected=gson.toJson(soccerFieldSelected);

                        final CharSequence[] items = {"Delete"};
                        AlertDialog.Builder builder = new AlertDialog.Builder(Soccer_Center_Info.this);
                        builder.setTitle("Options");
                        final mSoccerFields finalSelected = soccerFieldSelected;
                        final String finalJsonTeamSelected = jsonSoccerFieldSelected;
                        builder.setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int item) {
                                if(item==1)
                                {
                                    try {
                                        String soccerCenterId=soccerCenter.getId()+"";
                                        confirmDeleteSoccerField("Do you want to delete this Soccer Field: " + finalSelected.getName() + "?", soccerCenterId, finalSelected.getId() + "");

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });
                        AlertDialog alert = builder.create();
                        alert.show();
                        // confirmDeleteTeam("Do you want to delete this team: "+ teamToDelete.getName()+"?" ,teamToDelete.getTeamId()+"");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return true;
                }
            });


            fab = (FloatingActionButton) findViewById(R.id.fab);
            fab.attachToListView(list, new ScrollDirectionListener() {
                @Override
                public void onScrollDown() {
                    Log.d("ListViewFragment", "onScrollDown()");
                }

                @Override
                public void onScrollUp() {
                    Log.d("ListViewFragment", "onScrollUp()");
                }
            }, new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                    Log.d("ListViewFragment", "onScrollStateChanged()");
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    Log.d("ListViewFragment", "onScroll()");
                }
            });

            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent add_New_Soccer_Field = new Intent(Soccer_Center_Info.this, Add_New_Soccer_Field.class);
                    Bundle params = new Bundle();
                    params.putString("soccercenter", soccerCenterJson.toString());
                    add_New_Soccer_Field.putExtras(params);
                    startActivityForResult(add_New_Soccer_Field, 1);
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

    public void confirmDeleteSoccerField(final String message, final String soccerCenterId, final String soccerFieldId) throws Exception {
        AlertDialog.Builder builder = new AlertDialog.Builder(Soccer_Center_Info.this);
        builder.setMessage(message);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                DeleteTeamPlayer task = new DeleteTeamPlayer();
                task.execute(new String[]{soccerCenterId, soccerFieldId});

                dialog.dismiss();
            }
        });
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private class DeleteTeamPlayer extends AsyncTask<String, Void, Boolean> {
        String response = null;
        private final ProgressDialog dialog = new ProgressDialog(Soccer_Center_Info.this);
        JSONObject object_feed;
        // can use UI thread here
        protected void onPreExecute() {
            this.dialog.setMessage("Deleting Player");
            this.dialog.setCancelable(false);
            this.dialog.show();
        }

        protected Boolean doInBackground(String... params) {
            // TODO Auto-generated method stub
            //User user = new User();
            final HttpClient httpClient= new DefaultHttpClient();
            final HttpGet httpDetelePlayer= new HttpGet(url_host_connection_secure+"/soccerCenter/Delete/"+params[0]+"/"+params[1]);
            httpDetelePlayer.addHeader("x-access-token",token.getUser_token());
            try {
                try {
                    HttpResponse httpResponseLogin = httpClient.execute(httpDetelePlayer);
                    String jsonResultDeletePlayer = inputStreamToString(httpResponseLogin.getEntity().getContent()).toString();
                    JSONObject object = new JSONObject(jsonResultDeletePlayer);

                    Gson gsonLogin = new GsonBuilder()
                            .setDateFormat("yyyy-MM-dd")
                            .create();

                } catch (Exception e) {
                    // user.setUserId(0);
                    e.printStackTrace();
                }
            }catch (RuntimeException e)
            {
                // user.setUserId(0);
                e.printStackTrace();
            }

            return true;
        }//close doInBackground
        @Override
        protected void onPostExecute(Boolean status) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
                // listener.postTaskMethod();
                GetSoccerFieldsTask task = new GetSoccerFieldsTask();
                task.execute();
            }
        }//close onPostExecute
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if(resultCode == RESULT_OK){
                //String teamReturned=data.getStringExtra("team");
                //team=gson.fromJson(teamReturned, Team.class);
                String message = getResources().getString(R.string.field_added_success);
                Utils.ShowMessage(coordinatorLayout, message, 1);
                GetSoccerFieldsTask task = new GetSoccerFieldsTask();
                task.execute();
            }
            if (resultCode == RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }//onActivityResult

}
