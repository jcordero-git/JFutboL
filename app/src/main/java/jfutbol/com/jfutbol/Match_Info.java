package jfutbol.com.jfutbol;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListAdapter;
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
import jfutbol.com.jfutbol.model.mMatches;
import jfutbol.com.jfutbol.model.User;
import jfutbol.com.jfutbol.singleton.Utils;
import jfutbol.com.jfutbol.singleton.singleton_token;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;


public class Match_Info extends Activity {

    Bundle params;
    String matchJson;
    mMatches match;
    User user;
    String userJson;
    Team myTeam;
    String teamJson;

    String url_host_connection;
    String url_host_connection_secure;
    singleton_token token = singleton_token.getInstance();

    TextView lbMatchDate;
    TextView lbSoccerCenterName;
    ImageView imgTeam1;
    TextView lbTeam1Name;
    TextView lbTeam1Goals;
    ImageView imgTeam2;
    TextView lbTeam2Name;
    TextView lbTeam2Goals;
    ImageView imgStatus;

    User[] playersTeam1;
    User[] playersTeam2;
    PlayerSkills[] skills;
    Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd")
            .create();

    AQuery aq;
    FrameLayout root;
    View coordinatorLayout;
    SwipeRefreshLayout swipeContainer;

    //Button btnPlayers;
    FloatingActionButton fab;

    //ShowCase
    final String ADD_SHOWCASE = "matchInfo_add_ShowCase";

    //ShowCase

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_info);

        url_host_connection=getResources().getString(R.string.url_host_connection).toString();
        url_host_connection_secure=getResources().getString(R.string.url_host_connection).toString()+"/api";

        params=getIntent().getExtras();


        matchJson  = params.getString("match");
        match = gson.fromJson(matchJson, mMatches.class);
        userJson = params.getString("user");
        user = gson.fromJson(userJson, User.class);


        root = (FrameLayout) findViewById(R.id.idRL);
        Toolbar bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.settings_toolbar, root, false);
        root.addView(bar, 0); // insert at top
        bar.setTitle("MATCH");
        bar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                setResult(RESULT_OK, returnIntent);
                finish();
            }
        });

        coordinatorLayout = findViewById(R.id.coordinatorLayout);

        lbMatchDate = (TextView) findViewById(R.id.lbMatchDate);
        lbSoccerCenterName = (TextView) findViewById(R.id.lbSoccerCenterName);
        imgTeam1 = (ImageView) findViewById(R.id.imgTeam1);
        lbTeam1Name = (TextView) findViewById(R.id.lbTeam1Name);
        imgTeam2 = (ImageView) findViewById(R.id.imgTeam2);
        lbTeam2Name = (TextView) findViewById(R.id.lbTeam2Name);
        lbTeam1Goals = (TextView) findViewById(R.id.lbGoalsTeam1);
        lbTeam2Goals = (TextView) findViewById(R.id.lbGoalsTeam2);
        imgStatus = (ImageView) findViewById(R.id.status);

        lbMatchDate.setText(match.getDateApp().toString()+" "+match.getStartTimeApp()+" - "+match.getEndTimeApp());
        lbSoccerCenterName.setText(match.getSoccerCenterName() + " - " + match.getSoccerFieldName());

        fab = (FloatingActionButton) findViewById(R.id.fab);

        if(match.getMyTeamId()==0)
        {
            fab.setVisibility(View.INVISIBLE);
        }

        aq = new AQuery(Match_Info.this, root);

        aq.id(imgTeam1).image(url_host_connection + "/images/team/" + match.getTeam1ID() + ".png", true, true);
        aq.id(imgTeam2).image(url_host_connection + "/images/team/" + match.getTeam2ID() + ".png", true, true);

        lbTeam1Name.setText(match.getTeam1Name().toString());
        lbTeam2Name.setText(match.getTeam2Name().toString());
        lbTeam1Goals.setText(match.getGoalsTeam1() + "");
        lbTeam2Goals.setText(match.getGoalsTeam2() + "");

        swipeContainer = (SwipeRefreshLayout) root.findViewById(R.id.swipe_container);
        swipeContainer.setProgressViewOffset(false, 0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics()));
        swipeContainer.setRefreshing(true);

        RefreshData();
    }

    public void RefreshData()
    {
        GetTeam1PlayersTask getTeam1PlayersTask = new GetTeam1PlayersTask();
        getTeam1PlayersTask.execute();
        GetTeam2PlayersTask getTeam2PlayersTask = new GetTeam2PlayersTask();
        getTeam2PlayersTask.execute();
        VerifyStatus();
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    public void VerifyStatus() {
        if(match.getIsReserved()==-1)
            aq.id(imgStatus).image(R.drawable.rejected);
        if(match.getIsReserved()==0)
            aq.id(imgStatus).image(R.drawable.rejected);
        if(match.getIsReserved()==1)
            aq.id(imgStatus).image(R.drawable.pending);
        if(match.getIsReserved()==2)
            aq.id(imgStatus).image(R.drawable.pending);
        if(match.getIsReserved()==3)
            aq.id(imgStatus).image(R.drawable.pending);
        if(match.getIsReserved()==4)
            aq.id(imgStatus).image(R.drawable.approved);
        if(match.getIsReserved()==5)
            aq.id(imgStatus).image(R.drawable.rejected);

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                RefreshData();
            }
        });
        //swipeContainer.setRefreshing(false);

    }

    private class GetTeam1PlayersTask extends AsyncTask<String, Void, User[]> {
        private final ProgressDialog dialog = new ProgressDialog(Match_Info.this);
        protected void onPreExecute() {
            /*
            this.dialog.setMessage("Updating Players...");
            this.dialog.setCancelable(false);
            this.dialog.show();
            */
           // swipeContainer.setProgressViewOffset(false, 0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics()));
            //swipeContainer.setRefreshing(true);
        }
        protected User[] doInBackground(String... params) {
            final HttpClient httpClient = new DefaultHttpClient();
            JSONObject responseJSON = null;
            final HttpGet httpGetMyTeams= new HttpGet(url_host_connection_secure+"/match/"+match.getMatchID()+"/"+match.getTeam1ID()+"/players");
            httpGetMyTeams.addHeader("x-access-token",token.getUser_token());
            try {
                try {
                    HttpResponse httpResponseMyTeams = httpClient.execute(httpGetMyTeams);
                    String jsonResultPlayers = inputStreamToString(httpResponseMyTeams.getEntity().getContent()).toString();
                    Gson gsonMyTeams = new GsonBuilder()
                            .setDateFormat("yyyy-MM-dd")
                            .create();
                    playersTeam1= gsonMyTeams.fromJson(jsonResultPlayers, User[].class);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }catch (RuntimeException e)
            {
                e.printStackTrace();
            }
            return playersTeam1;
        }
        @Override
        protected void onPostExecute(User[] player) {

            ListView list = (ListView) findViewById(R.id.listView);
            List<User> playerlist = Arrays.asList(playersTeam1);
            matchPlayerListViewAdapter listAdapter = new matchPlayerListViewAdapter(Match_Info.this, playerlist, match.getTeam1captainId());
            list.setAdapter(listAdapter);
            list.setDivider(null);
            list.setItemsCanFocus(false);
            list.setLongClickable(true);
            setListViewHeightBasedOnItems(list);

            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    User userSelected=new User();
                    String jsonUserSelected="";
                    userSelected=(User)parent.getItemAtPosition(position);
                    Gson gson = new GsonBuilder()
                            .setDateFormat("yyyy-MM-dd")
                            .create();
                    jsonUserSelected=gson.toJson(userSelected);

                    Intent user_profile = new Intent(Match_Info.this, UserProfile.class);
                    Bundle params=new Bundle();
                    params.putString("user", jsonUserSelected );
                    user_profile.putExtras(params);
                    startActivity(user_profile);
                }
            });

            if(match.getMyTeamId()== match.getTeam1ID()) {
                list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> av, View v, int pos, long id) {
                        try {
                            User userSelected = new User();
                            String jsonUserSelected = "";
                            userSelected = (User) av.getItemAtPosition(pos);
                            Gson gson = new GsonBuilder()
                                    .setDateFormat("yyyy-MM-dd")
                                    .create();
                            jsonUserSelected = gson.toJson(userSelected);

                            final CharSequence[] items = {"Remove to " + userSelected.getFirstName().toUpperCase() + " from the match"};
                            AlertDialog.Builder builder = new AlertDialog.Builder(Match_Info.this);
                            builder.setTitle("Options");
                            final User finalSelected = userSelected;
                            final String finalJsonUserSelected = jsonUserSelected;
                            builder.setItems(items, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int item) {
                                    if (item == 0) {
                                        try {
                                            if(finalSelected.getUserId()!=match.getTeam1OwnerId()) {
                                                String matchId = match.getMatchID() + "";
                                                confirmDeletePlayer("Do you want to delete this player: " + finalSelected.getFirstName().toUpperCase() + "?", matchId, finalSelected.getUserId() + "");
                                            }
                                            else{
                                                String message="You can not remove the team owner";
                                                Utils.ShowMessage(coordinatorLayout, message, 3);
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            });
                            AlertDialog alert = builder.create();
                            alert.show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return true;
                    }
                });
            }

            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }
           // swipeContainer.setRefreshing(false);
        }//close onPostExecute
    }

    private class GetTeam2PlayersTask extends AsyncTask<String, Void, User[]> {
        private final ProgressDialog dialog = new ProgressDialog(Match_Info.this);
        protected void onPreExecute() {
            /*
            this.dialog.setMessage("Updating Players...");
            this.dialog.setCancelable(false);
            this.dialog.show();
            */
           // swipeContainer.setProgressViewOffset(false, 0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics()));
            //swipeContainer.setRefreshing(true);
        }
        protected User[] doInBackground(String... params) {
            final HttpClient httpClient = new DefaultHttpClient();
            JSONObject responseJSON = null;
            final HttpGet httpGetMyTeams= new HttpGet(url_host_connection_secure+"/match/"+match.getMatchID()+"/"+match.getTeam2ID()+"/players");
            httpGetMyTeams.addHeader("x-access-token",token.getUser_token());
            try {
                try {
                    HttpResponse httpResponseMyTeams = httpClient.execute(httpGetMyTeams);
                    String jsonResultPlayers = inputStreamToString(httpResponseMyTeams.getEntity().getContent()).toString();
                    Gson gsonMyTeams = new GsonBuilder()
                            .setDateFormat("yyyy-MM-dd")
                            .create();
                    playersTeam2= gsonMyTeams.fromJson(jsonResultPlayers, User[].class);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }catch (RuntimeException e)
            {
                e.printStackTrace();
            }
            return playersTeam2;
        }
        @Override
        protected void onPostExecute(User[] player) {

            ListView list = (ListView) findViewById(R.id.listView2);
            List<User> playerlist = Arrays.asList(playersTeam2);
            matchPlayerListViewAdapter listAdapter = new matchPlayerListViewAdapter(Match_Info.this, playerlist, match.getTeam2captainId());
            list.setAdapter(listAdapter);
            list.setDivider(null);
            list.setItemsCanFocus(false);
            list.setLongClickable(true);
            setListViewHeightBasedOnItems(list);

            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    User userSelected = new User();
                    String jsonUserSelected = "";
                    userSelected = (User) parent.getItemAtPosition(position);
                    Gson gson = new GsonBuilder()
                            .setDateFormat("yyyy-MM-dd")
                            .create();
                    jsonUserSelected = gson.toJson(userSelected);

                    Intent user_profile = new Intent(Match_Info.this, UserProfile.class);
                    Bundle params = new Bundle();
                    params.putString("user", jsonUserSelected);
                    user_profile.putExtras(params);
                    startActivity(user_profile);
                }
            });
            if(match.getMyTeamId()== match.getTeam2ID()) {
                list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> av, View v, int pos, long id) {
                        try {
                            User userSelected = new User();
                            String jsonUserSelected = "";
                            userSelected = (User) av.getItemAtPosition(pos);
                            Gson gson = new GsonBuilder()
                                    .setDateFormat("yyyy-MM-dd")
                                    .create();
                            jsonUserSelected = gson.toJson(userSelected);

                            final CharSequence[] items = {"Remove to " + userSelected.getFirstName().toUpperCase() + " from the match"};
                            AlertDialog.Builder builder = new AlertDialog.Builder(Match_Info.this);
                            builder.setTitle("Options");
                            final User finalSelected = userSelected;
                            final String finalJsonUserSelected = jsonUserSelected;
                            builder.setItems(items, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int item) {
                                    if (item == 0) {
                                        try {
                                            if(finalSelected.getUserId()!=match.getTeam2OwnerId()) {
                                                String matchId = match.getMatchID() + "";
                                                confirmDeletePlayer("Do you want to delete this player: " + finalSelected.getFirstName().toUpperCase() + "?", matchId, finalSelected.getUserId() + "");
                                            }
                                            else{
                                                String message="You can not remove the team owner";
                                                Utils.ShowMessage(coordinatorLayout, message, 3);
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            });
                            AlertDialog alert = builder.create();
                            alert.show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return true;
                    }
                });
            }

            if(match.getMyTeamId()!=0) {

                myTeam=new Team();
                myTeam.setTeamId(match.getMyTeamId());
                myTeam.setName(match.getMyTeamName());
                myTeam.setProvinceId(match.getMyTeamProvinceId());
                myTeam.setProvinceName(match.getMyTeamProvinceName());
                myTeam.setCantonName(match.getMyTeamCantonName());
                myTeam.setCantonId(match.getMyTeamCantonId());

                teamJson = gson.toJson(myTeam);


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


                showShowCase(fab, "Use this button to add extra players in your team: " +myTeam.getName().toUpperCase(), ADD_SHOWCASE, false);


                final List<PlayerSkills> skillsList = Arrays.asList(user.getSkills());

                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final Intent search_player = new Intent(Match_Info.this, SearchPlayer.class);
                        final Bundle params = new Bundle();

                        //final CharSequence[] items = {"Portero","Defensa","Central","Delantero"};
                        //final CharSequence[] items = skillsList.toArray(new CharSequence[skillsList.size()]);
                        final CharSequence[] skillsChar = new CharSequence[skillsList.size()];
                        for (int i = 0; i < skillsList.size(); i++) {
                            skillsChar[i] = user.getSkills()[i].getSkillName();
                        }
                        AlertDialog.Builder builder = new AlertDialog.Builder(Match_Info.this);
                        builder.setTitle("Position to add on: "+myTeam.getName().toUpperCase());
                        builder.setItems(skillsChar, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int item) {

                                String idPositionToSearch = user.getSkills()[item].getSkillId();
                                String positionToSearch = skillsChar[item].toString();
                                params.putString("team", teamJson);
                                params.putString("idPositionToAdd", idPositionToSearch);
                                params.putString("positionToAdd", positionToSearch);
                                params.putCharSequenceArray("skillsChar", skillsChar);
                                params.putBoolean("addingExtraPlayerToMatch", true);
                                params.putInt("matchId", match.getMatchID());
                                search_player.putExtras(params);
                                //startActivity(search_player);
                                startActivityForResult(search_player, 1);
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
            }
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }
            swipeContainer.setRefreshing(false);
        }//close onPostExecute
    }

    public static boolean setListViewHeightBasedOnItems(ListView listView) {

        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter != null) {
            int numberOfItems = listAdapter.getCount();
            // Get total height of all items.
            int totalItemsHeight = 0;
            for (int itemPos = 0; itemPos < numberOfItems; itemPos++) {
                View item = listAdapter.getView(itemPos, null, listView);
                item.measure(0, 0);
                totalItemsHeight += item.getMeasuredHeight();
            }
            // Get total height of all item dividers.
            int totalDividersHeight = listView.getDividerHeight() *
                    (numberOfItems - 1);
            // Set list height.
            ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = totalItemsHeight + totalDividersHeight;
            listView.setLayoutParams(params);
            listView.requestLayout();
            return true;
        } else {
            return false;
        }

    }

    public void confirmDeletePlayer(final String message, final String matchId, final String playerId) throws Exception {
        AlertDialog.Builder builder = new AlertDialog.Builder(Match_Info.this);
        builder.setMessage(message);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DeleteTeamPlayer task = new DeleteTeamPlayer();
                task.execute(new String[]{matchId, playerId});
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
        private final ProgressDialog dialog = new ProgressDialog(Match_Info.this);
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
            final HttpGet httpDetelePlayer= new HttpGet(url_host_connection_secure+"/matchPlayer_DeletePlayer/"+params[0]+"/"+params[1]);
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
                GetTeam1PlayersTask getTeam1PlayersTask = new GetTeam1PlayersTask();
                getTeam1PlayersTask.execute();

                GetTeam2PlayersTask getTeam2PlayersTask = new GetTeam2PlayersTask();
                getTeam2PlayersTask.execute();

                VerifyStatus();
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

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if(resultCode == RESULT_OK){
                //String teamReturned=data.getStringExtra("team");
                //team=gson.fromJson(teamReturned, Team.class);
                GetTeam1PlayersTask getTeam1PlayersTask = new GetTeam1PlayersTask();
                getTeam1PlayersTask.execute();

                GetTeam2PlayersTask getTeam2PlayersTask = new GetTeam2PlayersTask();
                getTeam2PlayersTask.execute();

                VerifyStatus();
            }
            if (resultCode == RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }//onActivityResult

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
