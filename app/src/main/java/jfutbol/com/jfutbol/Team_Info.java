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

import jfutbol.com.jfutbol.model.PlayerSkills;
import jfutbol.com.jfutbol.model.Team;
import jfutbol.com.jfutbol.model.User;
import jfutbol.com.jfutbol.singleton.singleton_token;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;


public class Team_Info extends Activity {

    Bundle params;
    String teamJson;
    Team team;
    AQuery aq;
    boolean memCache = false;
    boolean fileCache = false;

    String url_host_connection;
    String url_host_connection_secure;
    singleton_token token = singleton_token.getInstance();
    User[] players;
    PlayerSkills[] skills;
    Gson gson;



    //Button btnPlayers;
    FloatingActionButton fab;

    ListView list;
    playerListViewAdapter listAdapter;

    //ShowCase
    final String ADD_SHOWCASE = "teamInfo_add_ShowCase";
    final String CAPTAIN_SHOWCASE = "teamInfo_caption_ShowCase";

    //ShowCase

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       setContentView(R.layout.activity_team_info);

        url_host_connection=getResources().getString(R.string.url_host_connection).toString();
        url_host_connection_secure=getResources().getString(R.string.url_host_connection).toString()+"/api";

        params=getIntent().getExtras();
        gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd")
                .create();

        teamJson  = params.getString("team");
        team=gson.fromJson(teamJson, Team.class);

        FrameLayout root = (FrameLayout) findViewById(R.id.idRL);
        Toolbar bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.settings_toolbar, root, false);
        root.addView(bar, 0); // insert at top
        bar.setTitle(team.getName());
        bar.inflateMenu(R.menu.menu_team_info);
        bar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                setResult(RESULT_OK,returnIntent);
                finish();
            }
        });
        //Spinner spinner = (Spinner) findViewById(R.id.spinner);
        TextView lbTeamName= (TextView) findViewById(R.id.lbTeamName);
        TextView lbProvince= (TextView) findViewById(R.id.lbProvince);
        TextView lbCanton= (TextView) findViewById(R.id.lbCanton);

        //btnPlayers = (Button) findViewById(R.id.fab);

        list = (ListView) findViewById(R.id.listView);

        aq = new AQuery(Team_Info.this, root);
        aq.id(R.id.imgTeam).image(url_host_connection+"/images/team/"+team.getId()+".png", memCache, fileCache);
        lbTeamName.setText(team.getName());
        lbProvince.setText(team.getProvinceName());
        lbCanton.setText(team.getCantonName());


        GetTeamPlayersTask getTeamPlayersTask = new GetTeamPlayersTask();
        getTeamPlayersTask.execute();


        GetSkillsTask skillsTask = new GetSkillsTask();
        skillsTask.execute();



        /*
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.lineups, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);


        btnPlayers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent search_player = new Intent(Team_Info.this, SearchPlayer.class);
                final Bundle params=new Bundle();
                final CharSequence[] items = {"Portero","Defensa","Central","Delantero"};
                //final CharSequence[] items = skillsList.toArray(new CharSequence[skillsList.size()]);
                AlertDialog.Builder builder = new AlertDialog.Builder(Team_Info.this);
                builder.setTitle("Position to Add");
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        String positionToSearch=items[item].toString();
                        params.putString("team", teamJson );
                        params.putString("positionToSearch", positionToSearch);
                        search_player.putExtras(params);
                        //startActivity(search_player);
                        startActivityForResult(search_player,1);
                    }
                });
                builder.setPositiveButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
  */

    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private class GetTeamPlayersTask extends AsyncTask<User, Void, User[]> {
        private final ProgressDialog dialog = new ProgressDialog(Team_Info.this);
        protected void onPreExecute() {
            this.dialog.setMessage("Getting Players...");
            this.dialog.setCancelable(false);
            this.dialog.show();
        }
        protected User[] doInBackground(User... params) {
            final HttpClient httpClient = new DefaultHttpClient();
            JSONObject responseJSON = null;
            final HttpGet httpGetMyTeams= new HttpGet(url_host_connection_secure+"/team/"+team.getId()+"/players");
            httpGetMyTeams.addHeader("x-access-token",token.getUser_token());
            try {
                try {
                    HttpResponse httpResponseMyTeams = httpClient.execute(httpGetMyTeams);
                    String jsonResultPlayers = inputStreamToString(httpResponseMyTeams.getEntity().getContent()).toString();
                    Gson gsonMyTeams = new GsonBuilder()
                            .setDateFormat("yyyy-MM-dd")
                            .create();
                    players= gsonMyTeams.fromJson(jsonResultPlayers, User[].class);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }catch (RuntimeException e)
            {
                e.printStackTrace();
            }
            return players;
        }
        @Override
        protected void onPostExecute(User[] player) {

            List<User> playerlist = Arrays.asList(players);
            listAdapter = new playerListViewAdapter(Team_Info.this, playerlist, team.getCaptainId() );
            list.setAdapter(listAdapter);
            list.setItemsCanFocus(false);
            list.setLongClickable(true);
            listAdapter.notifyDataSetChanged();


            list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
                @Override
                public boolean onItemLongClick(AdapterView<?> av, View v, int pos, long id)
                {
                    try {
                        User userSelected=new User();
                        String jsonUserSelected="";
                        userSelected=(User)av.getItemAtPosition(pos);
                        Gson gson = new GsonBuilder()
                                .setDateFormat("yyyy-MM-dd")
                                .create();
                        jsonUserSelected=gson.toJson(userSelected);

                        final CharSequence[] items = {"Make Captain", "Remove to "+userSelected.getFirstName().toUpperCase()+" from "+team.getName()+" team"};
                        AlertDialog.Builder builder = new AlertDialog.Builder(Team_Info.this);
                        builder.setTitle("Options");
                        final User finalSelected = userSelected;
                        final String finalJsonTeamSelected = jsonUserSelected;
                        builder.setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int item) {
                                if(item==0)
                                {
                                    try {
                                        String teamId=team.getId()+"";
                                        confirmCaptainPlayer("Do you want to make this player: " + finalSelected.getFirstName().toUpperCase() + " as captain?", teamId , finalSelected.getId() + "");

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                if(item==1)
                                {
                                    try {
                                        String teamId=team.getId()+"";
                                        confirmDeletePlayer("Do you want to delete this player: " + finalSelected.getFirstName().toUpperCase() + "?", teamId , finalSelected.getId() + "");

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

            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }

            showShowCase(fab, "Use this button to add player in your team: "+team.getName().toUpperCase(), ADD_SHOWCASE, false);


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

    public void confirmDeletePlayer(final String message, final String teamId, final String playerId) throws Exception {
        AlertDialog.Builder builder = new AlertDialog.Builder(Team_Info.this);
        builder.setMessage(message);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                /*
                    new DeleteTeam (new AsyncListener(){
                        public void postTaskMethod(){
                            //do stuff here
                            GetMyTeamsTask task = new GetMyTeamsTask();
                            task.execute();
                        }

                    }).execute(new String[]{teamId});
                */

                DeleteTeamPlayer task = new DeleteTeamPlayer();
                task.execute(new String[]{teamId, playerId});


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
        // private AsyncListener listener;

        /*
        public DeleteTeam(AsyncListener listener){
            this.listener=listener;
        }
        */


        private final ProgressDialog dialog = new ProgressDialog(Team_Info.this);
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
            final HttpGet httpDetelePlayer= new HttpGet(url_host_connection_secure+"/team/Delete/"+params[0]+"/"+params[1]);
            httpDetelePlayer.addHeader("x-access-token",token.getUser_token());
            try {
                try {
                    HttpResponse httpResponseLogin = httpClient.execute(httpDetelePlayer);
                    String jsonResultDeletePlayer = inputStreamToString(httpResponseLogin.getEntity().getContent()).toString();
                    JSONObject object = new JSONObject(jsonResultDeletePlayer);

                    Gson gsonLogin = new GsonBuilder()
                            .setDateFormat("yyyy-MM-dd")
                            .create();
                    // user=gsonLogin.fromJson(object.toString(),User.class);

                    /*
                    final HttpGet httpGetMyTeams= new HttpGet(url_host_connection+"/team/"+user.getUserId()+"");
                    try {
                        try {
                            processingStatus="My Teams";
                            HttpResponse httpResponseMyTeams = httpClient.execute(httpGetMyTeams);
                            String jsonResultMyTeams = inputStreamToString(httpResponseMyTeams.getEntity().getContent()).toString();
                            Gson gsonMyTeams = new Gson();
                            myTeams= gsonMyTeams.fromJson(jsonResultMyTeams, Team[].class);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }catch (RuntimeException e)
                    {
                        e.printStackTrace();
                    }
                    */

                    /*
                    user.setUserId(object.getInt("userId"));
                    user.setEmail(object.getString("email"));
                    user.setPassword(object.getString("password"));
                    user.setFirstName(object.getString("firstName"));
                    user.setLastName(object.getString("lastName"));
                    */

                    //   showDialog(name);

                } catch (Exception e) {
                    // user.setUserId(0);
                    e.printStackTrace();
                }
            }catch (RuntimeException e)
            {
                // user.setUserId(0);
                e.printStackTrace();
            }




            /*
            ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
            postParameters.add(new BasicNameValuePair("username", params[0] ));
            postParameters.add(new BasicNameValuePair("password", params[1] ));
            String res = null;
            try {
                response = CustomHttpClient.executeHttpPost("http://akinads.0fees.net/check.php", postParameters);
                res=response.toString();
                res= res.replaceAll("\\s+","");
            }
            catch (Exception e) {
                e.printStackTrace();
                //txt_Error.setText(e.toString());
            }
            */

            return true;
        }//close doInBackground
        @Override
        protected void onPostExecute(Boolean status) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
                // listener.postTaskMethod();
                GetTeamPlayersTask task = new GetTeamPlayersTask();
                task.execute();
            }
        }//close onPostExecute
    }

    public void confirmCaptainPlayer(final String message, final String teamId, final String playerId) throws Exception {
        AlertDialog.Builder builder = new AlertDialog.Builder(Team_Info.this);
        builder.setMessage(message);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                /*
                    new DeleteTeam (new AsyncListener(){
                        public void postTaskMethod(){
                            //do stuff here
                            GetMyTeamsTask task = new GetMyTeamsTask();
                            task.execute();
                        }

                    }).execute(new String[]{teamId});
                */

                CaptainTeamPlayer task = new CaptainTeamPlayer();
                task.execute(new String[]{teamId, playerId});


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

    private class CaptainTeamPlayer extends AsyncTask<String, Void, Boolean> {
        String response = null;
        // private AsyncListener listener;

        /*
        public DeleteTeam(AsyncListener listener){
            this.listener=listener;
        }
        */


        private final ProgressDialog dialog = new ProgressDialog(Team_Info.this);
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
            final HttpGet httpDetelePlayer= new HttpGet(url_host_connection_secure+"/team/captain/"+params[0]+"/"+params[1]);
            httpDetelePlayer.addHeader("x-access-token",token.getUser_token());
            try {
                try {
                    HttpResponse httpResponseLogin = httpClient.execute(httpDetelePlayer);
                    String jsonResultDeletePlayer = inputStreamToString(httpResponseLogin.getEntity().getContent()).toString();
                    JSONObject object = new JSONObject(jsonResultDeletePlayer);

                    Gson gsonLogin = new GsonBuilder()
                            .setDateFormat("yyyy-MM-dd")
                            .create();

                    team.setCaptainId( Integer.parseInt(params[1]));

                    // user=gsonLogin.fromJson(object.toString(),User.class);

                    /*
                    final HttpGet httpGetMyTeams= new HttpGet(url_host_connection+"/team/"+user.getUserId()+"");
                    try {
                        try {
                            processingStatus="My Teams";
                            HttpResponse httpResponseMyTeams = httpClient.execute(httpGetMyTeams);
                            String jsonResultMyTeams = inputStreamToString(httpResponseMyTeams.getEntity().getContent()).toString();
                            Gson gsonMyTeams = new Gson();
                            myTeams= gsonMyTeams.fromJson(jsonResultMyTeams, Team[].class);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }catch (RuntimeException e)
                    {
                        e.printStackTrace();
                    }
                    */

                    /*
                    user.setUserId(object.getInt("userId"));
                    user.setEmail(object.getString("email"));
                    user.setPassword(object.getString("password"));
                    user.setFirstName(object.getString("firstName"));
                    user.setLastName(object.getString("lastName"));
                    */

                    //   showDialog(name);

                } catch (Exception e) {
                    // user.setUserId(0);
                    e.printStackTrace();
                }
            }catch (RuntimeException e)
            {
                // user.setUserId(0);
                e.printStackTrace();
            }




            /*
            ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
            postParameters.add(new BasicNameValuePair("username", params[0] ));
            postParameters.add(new BasicNameValuePair("password", params[1] ));
            String res = null;
            try {
                response = CustomHttpClient.executeHttpPost("http://akinads.0fees.net/check.php", postParameters);
                res=response.toString();
                res= res.replaceAll("\\s+","");
            }
            catch (Exception e) {
                e.printStackTrace();
                //txt_Error.setText(e.toString());
            }
            */

            return true;
        }//close doInBackground
        @Override
        protected void onPostExecute(Boolean status) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
                // listener.postTaskMethod();
                GetTeamPlayersTask task = new GetTeamPlayersTask();
                task.execute();
            }
        }//close onPostExecute
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if(resultCode == RESULT_OK){
                //String teamReturned=data.getStringExtra("team");
                //team=gson.fromJson(teamReturned, Team.class);
                GetTeamPlayersTask task = new GetTeamPlayersTask();
                task.execute();

                showShowCase(list, "Use the long click under player to make him as team captain or delete him of the team: " + team.getName().toUpperCase(), CAPTAIN_SHOWCASE, true);


            }
            if (resultCode == RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }//onActivityResult

    private class GetSkillsTask extends AsyncTask<String, Void, PlayerSkills[]> {
        private final ProgressDialog dialog = new ProgressDialog(Team_Info.this);
        protected void onPreExecute() {
            this.dialog.setMessage("Getting Skills...");
            this.dialog.setCancelable(false);
            this.dialog.show();
        }
        protected PlayerSkills[] doInBackground(String... params) {
            final HttpClient httpClient = new DefaultHttpClient();
            JSONObject responseJSON = null;
            final HttpGet httpGetSkills= new HttpGet(url_host_connection+"/skills");
            try {
                try {
                    HttpResponse httpResponseMyTeams = httpClient.execute(httpGetSkills);
                    String jsonResultPlayers = inputStreamToString(httpResponseMyTeams.getEntity().getContent()).toString();
                    Gson gsonMyTeams = new GsonBuilder()
                            .setDateFormat("yyyy-MM-dd")
                            .create();
                    skills= gsonMyTeams.fromJson(jsonResultPlayers, PlayerSkills[].class);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }catch (RuntimeException e)
            {
                e.printStackTrace();
            }
            return skills;
        }
        @Override
        protected void onPostExecute(final PlayerSkills[] skills) {

            ListView list = (ListView) findViewById(R.id.listView);
            final List<PlayerSkills> skillsList = Arrays.asList(skills);

            //playerListViewAdapter listAdapter = new playerListViewAdapter(Team_Info.this, playerlist );
            //list.setAdapter(listAdapter);

            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Intent search_player = new Intent(Team_Info.this, SearchPlayer.class);
                    final Bundle params=new Bundle();

                    //final CharSequence[] items = {"Portero","Defensa","Central","Delantero"};
                    //final CharSequence[] items = skillsList.toArray(new CharSequence[skillsList.size()]);
                    final CharSequence[] skillsChar = new CharSequence[skillsList.size()];
                    for(int i=0;i<skillsList.size();i++)
                        {
                            skillsChar[i]=skills[i].getSkillName();
                        }
                    AlertDialog.Builder builder = new AlertDialog.Builder(Team_Info.this);
                    builder.setTitle("Position to add");
                    builder.setItems(skillsChar, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int item) {

                            String idPositionToSearch=skills[item].getId();
                            String positionToSearch=skillsChar[item].toString();
                            params.putString("team", teamJson );
                            params.putString("idPositionToAdd", idPositionToSearch);
                            params.putString("positionToAdd", positionToSearch);
                            params.putCharSequenceArray("skillsChar", skillsChar);
                            search_player.putExtras(params);
                            //startActivity(search_player);
                            startActivityForResult(search_player,1);
                        }
                    });
                    builder.setPositiveButton("CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            });

            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }




        }//close onPostExecute
    }

    public void showShowCase( View target, String message, final String SHOWCASE_ID, Boolean closeOnTouch ){
        //MaterialShowcaseView.resetSingleUse(this, SHOWCASE_ID);
        new MaterialShowcaseView.Builder(this)
                .setTarget(target)
                .setDismissText("GOT IT")
                .setContentText(message)
                .setDelay(500) // optional but starting animations immediately in onCreate can make them choppy
                .singleUse(SHOWCASE_ID) // provide a unique ID used to ensure it is only shown once
                .setMaskColour(R.color.background_material_light)
                .setDismissOnTouch(closeOnTouch)
                .setUseAutoRadius(true)
                .show();
    }


}
