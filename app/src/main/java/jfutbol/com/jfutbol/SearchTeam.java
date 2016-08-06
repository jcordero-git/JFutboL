package jfutbol.com.jfutbol;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import jfutbol.com.jfutbol.model.Team;
import jfutbol.com.jfutbol.model.User;
import jfutbol.com.jfutbol.singleton.singleton_token;


public class SearchTeam extends Activity {

    String url_host_connection;
    String url_host_connection_secure;
    singleton_token token = singleton_token.getInstance();
    Team[] teams;
    User playerForFilter;
    teamListViewAdapter listAdapter;
    Bundle params;
    String positionToAdd;
    //CharSequence[] skillsChar;
    String userJson;
    User user;

    Boolean myTeams=false;

    String teamJson;
    //Team team;
    Gson gson;
    ArrayList<String> mSelectedItems;
    boolean[] checkedItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_team);

        params=getIntent().getExtras();
        gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd")
                .create();

        if(params.getString("user")!=null) {
            userJson = params.getString("user");
            user = gson.fromJson(userJson, User.class);
        }

        myTeams = params.getBoolean("myTeams");

        /*
        if(params.getString("team")!=null) {
            teamJson = params.getString("team");
            team = gson.fromJson(teamJson, Team.class);
        }
        */
        //positionToAdd  = params.getString("positionToAdd");
        //skillsChar  = params.getCharSequenceArray("skillsChar");

        RelativeLayout root = (RelativeLayout) findViewById(R.id.idRL);
        Toolbar bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.settings_toolbar, root, false);
        root.addView(bar, 0); // insert at top
        bar.setTitle("Searching Team");
        bar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        url_host_connection=getResources().getString(R.string.url_host_connection).toString();
        url_host_connection_secure=getResources().getString(R.string.url_host_connection).toString()+"/api";

        final EditText txtSearchTeam= (EditText) findViewById(R.id.txtSearchTeam);
        //final Button btnSkills= (Button) findViewById(R.id.btnSkills);


        playerForFilter=new User();

        GetTeamsTask task = new GetTeamsTask();
        task.execute();

        /*
        checkedItems = new boolean[skillsChar.length];
        mSelectedItems = new ArrayList();
        for(int i=0;i<skillsChar.length;i++) {
            if(skillsChar[i].toString().contains(positionToAdd))
            {
                checkedItems[i]=true;
                mSelectedItems.add(skillsChar[i].toString());
            }
        }*/


        /*
        btnSkills.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SearchTeam.this);
                builder.setTitle("Filter by Skills")
                        .setMultiChoiceItems(skillsChar,checkedItems,
                                new DialogInterface.OnMultiChoiceClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which,
                                                        boolean isChecked) {
                                        if (isChecked) {
                                            mSelectedItems.add(skillsChar[which].toString());
                                        } else if (mSelectedItems.contains(skillsChar[which].toString())) {
                                            mSelectedItems.remove(skillsChar[which].toString());
                                        }
                                    }
                                })
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                String text = txtSearchPlayer.getText().toString().toLowerCase(Locale.getDefault());
                                listAdapter.filter(text, mSelectedItems);
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
        */


        txtSearchTeam.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) { }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {  }

            @Override
            public void afterTextChanged(Editable arg0) {
                String text = txtSearchTeam.getText().toString().toLowerCase(Locale.getDefault());
                listAdapter.filter(text, mSelectedItems);
            }
        });

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private class GetTeamsTask extends AsyncTask<User, Void, Team[]> {
        private final ProgressDialog dialog = new ProgressDialog(SearchTeam.this);
        protected void onPreExecute() {
            this.dialog.setMessage("Updating Teams...");
            this.dialog.setCancelable(false);
            this.dialog.show();
        }
        protected Team[] doInBackground(User... params) {
            final HttpClient httpClient = new DefaultHttpClient();
            JSONObject responseJSON = null;
            String request="";

            final HttpGet httpGetMyTeams;

            if(myTeams==true && user!=null)
                request=url_host_connection_secure+"/team/"+user.getUserId();

            if(myTeams==false && user!=null)
                request=url_host_connection_secure+"/team/noMyTeams/"+user.getUserId();

            httpGetMyTeams= new HttpGet(request);

            httpGetMyTeams.addHeader("x-access-token",token.getUser_token());
            try {
                try {
                    HttpResponse httpResponseMyTeams = httpClient.execute(httpGetMyTeams);
                    String jsonResultTeams = inputStreamToString(httpResponseMyTeams.getEntity().getContent()).toString();
                    Gson gsonMyTeams = new GsonBuilder()
                            .setDateFormat("yyyy-MM-dd")
                            .create();
                    teams= gsonMyTeams.fromJson(jsonResultTeams, Team[].class);


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }catch (RuntimeException e)
            {
                e.printStackTrace();
            }
            return teams;
        }
        @Override
        protected void onPostExecute(Team[] team) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }

            List <Team> teamlist = Arrays.asList(teams);
            ListView list = (ListView) findViewById(R.id.listView);

            listAdapter = new teamListViewAdapter(SearchTeam.this, teamlist );
            list.setAdapter(listAdapter);

            list.setItemsCanFocus(false);
            list.setLongClickable(true);


            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    Team teamSelected=new Team(0,0,"");
                    teamSelected=(Team) parent.getItemAtPosition(position);
                    teamJson=gson.toJson(teamSelected);
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("team", teamJson);
                    setResult(RESULT_OK,returnIntent);
                    finish();
                    /*
                    User userSelected=new User();
                    userSelected=(User) parent.getItemAtPosition(position);
                    PlayerSkills playerSkills[]=new PlayerSkills[1];
                    playerSkills[0]=new PlayerSkills(positionToAdd, 1);
                   // playerSkills[0].setSkill();
                    userSelected.setSkills(playerSkills);
                    AddPlayersTask task = new AddPlayersTask();
                    task.execute(userSelected);
                    */
                }
            });

            listAdapter.filter("", mSelectedItems);

        }//close onPostExecute
    }

    /*

    private class AddPlayersTask extends AsyncTask<User, Void, JSONObject> {
        private final ProgressDialog dialog = new ProgressDialog(SearchTeam.this);
        protected void onPreExecute() {
            this.dialog.setMessage("Adding Player...");
            this.dialog.setCancelable(false);
            this.dialog.show();
        }
        protected JSONObject doInBackground(User... params) {

            JSONObject responseJSON = null;
            final HttpClient httpClient = new DefaultHttpClient();
            final HttpPost httpPost= new HttpPost(url_host_connection+"/team/"+team.getTeamId());
            try {
                try {
                    Gson gson = new GsonBuilder()
                            .setDateFormat("yyyy-MM-dd")
                            .create();
                    String json = gson.toJson(params[0]);
                    JSONObject jsonobj= new JSONObject(json);


                    StringEntity se = new StringEntity(jsonobj.toString());
                    se.setContentType("application/json;charset=UTF-8");
                    se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,"application/json;charset=UTF-8"));
                    httpPost.setEntity(se);
                    HttpResponse httpResponse = httpClient.execute(httpPost);
                    String jsonResult = inputStreamToString(httpResponse.getEntity().getContent()).toString();
                    responseJSON = new JSONObject(jsonResult);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }catch (RuntimeException e)
            {
                e.printStackTrace();
            }
            return responseJSON;
        }
        @Override
        protected void onPostExecute(JSONObject result) {
            int affectedRows = 0;
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }
            try {
                affectedRows=result.getInt("affectedRows");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if(affectedRows!=0) {

                Intent returnIntent = new Intent();
                //returnIntent.putExtra("team", teamJson);
                setResult(RESULT_OK,returnIntent);
                finish();

                /*
                Intent team_Info = new Intent(SearchPlayer.this, Team_Info.class);
                Bundle params = new Bundle();
                params.putString("team", teamJson);
                team_Info.putExtras(params);
                startActivity(team_Info);
                finish();
                */

       //     }
      //  }//close onPostExecute
   // }



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
