package jfutbol.com.jfutbol;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
//import android.widget.SearchView;
import android.support.v7.widget.SearchView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jfutbol.com.jfutbol.model.PlayerSkills;
import jfutbol.com.jfutbol.model.Team;
import jfutbol.com.jfutbol.model.User;
import jfutbol.com.jfutbol.model.mCantons;
import jfutbol.com.jfutbol.model.mProvinces;
import jfutbol.com.jfutbol.singleton.singleton_token;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;


public class SearchPlayer extends Activity implements SearchView.OnQueryTextListener {

    String url_host_connection;
    String url_host_connection_secure;
    singleton_token token = singleton_token.getInstance();
    User[] players;
    User playerForFilter;
    playerListViewAdapter listAdapter;
    Bundle params;
    String idPositionToAdd;
    String positionToAdd;
    CharSequence[] skillsChar;
    String teamJson;
    Team team;
    ArrayList<String> mSelectedItems;


    //variales to filter
    GetPlayersTask task;
    EditText txtFields[];
    boolean[] checkedItems;
    mProvinces[] provinces;
    String provinceNameSelected;
    String cantonNameSelected;
    Boolean updateCantonSelected;
    mCantons[] cantons;
    mCantons canton=new mCantons();
    Boolean doRegionFilter=false;

    //variales to filter


    //variables to add extra player to match
    Boolean addingExtraPlayerToMatch=false;
    int matchId=0;

    private SearchView mSearchView;

    //ShowCase
    final String FILTER_SHOWCASE = "searchPlayer_filter_ShowCase";

    //ShowCase


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_player);

        params=getIntent().getExtras();
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd")
                .create();

        teamJson  = params.getString("team");
        team=gson.fromJson(teamJson, Team.class);
        idPositionToAdd  = params.getString("idPositionToAdd");
        positionToAdd  = params.getString("positionToAdd");
        skillsChar  = params.getCharSequenceArray("skillsChar");
        addingExtraPlayerToMatch = params.getBoolean("addingExtraPlayerToMatch");
        if (addingExtraPlayerToMatch)
            matchId = params.getInt("matchId");

        RelativeLayout root = (RelativeLayout) findViewById(R.id.idRL);
        Toolbar bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.settings_toolbar, root, false);
        root.addView(bar, 0); // insert at top
        bar.setTitle(positionToAdd.toUpperCase());
        bar.inflateMenu(R.menu.menu_search_player);

        MenuItem searchItem = bar.getMenu().findItem(R.id.action_search);
        mSearchView = (SearchView) searchItem.getActionView();
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        if (searchManager != null) {
            SearchableInfo info = searchManager.getSearchableInfo(getComponentName());
            mSearchView.setSearchableInfo(info);
        }
        mSearchView.setOnQueryTextListener(this);

        bar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        bar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (menuItem.getTitle().equals(getResources().getString(R.string.filter).toString())) {
                    showPopupPlayersFilter();
                    //showPopupSkills();
                }
                return false;
            }
        });


        url_host_connection=getResources().getString(R.string.url_host_connection).toString();
        url_host_connection_secure=getResources().getString(R.string.url_host_connection).toString()+"/api";

        playerForFilter=new User();

        provinceNameSelected=team.getProvinceName();
        cantonNameSelected=team.getCantonName();
        canton.setProvinceId(team.getProvinceId());
        canton.setCantonId(team.getCantonId());

        task = new GetPlayersTask();
        task.execute(new String[]{team.getProvinceId()+"",team.getCantonId()+""});

        checkedItems = new boolean[skillsChar.length];
        mSelectedItems = new ArrayList();
        for(int i=0;i<skillsChar.length;i++) {
            if(skillsChar[i].toString().contains(positionToAdd))
            {
                checkedItems[i]=true;
                mSelectedItems.add(skillsChar[i].toString());
            }
        }
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        showShowCase(bar.findViewById(R.id.filter), "You can use this button to filter by region or player skills", FILTER_SHOWCASE);

    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private class GetPlayersTask extends AsyncTask<String, Void, User[]> {
        private final ProgressDialog dialog = new ProgressDialog(SearchPlayer.this);
        protected void onPreExecute() {
            this.dialog.setMessage("Updating Players...");
            this.dialog.setCancelable(false);
            this.dialog.show();
        }
        protected User[] doInBackground(String... params) {
            final HttpClient httpClient = new DefaultHttpClient();
            JSONObject responseJSON = null;
            final HttpGet httpGetMyTeams;

            if(addingExtraPlayerToMatch)
                httpGetMyTeams= new HttpGet(url_host_connection_secure+"/players/match/"+matchId+"/"+params[0]+"/"+params[1]);
            else
                httpGetMyTeams= new HttpGet(url_host_connection_secure+"/players/team/"+team.getTeamId()+"/"+params[0]+"/"+params[1]);

            //final HttpGet httpGetMyTeams= new HttpGet(url_host_connection_secure+"/players/"+params[0]+"/"+params[1]);
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
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }

            List <User> playerlist = Arrays.asList(players);
            ListView list = (ListView) findViewById(R.id.listView);

            listAdapter = new playerListViewAdapter(SearchPlayer.this, playerlist, 0);
            list.setAdapter(listAdapter);

            list.setItemsCanFocus(false);
            list.setLongClickable(true);


            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    User userSelected=new User();
                    userSelected=(User) parent.getItemAtPosition(position);
                    PlayerSkills playerSkills[]=new PlayerSkills[1];
                    playerSkills[0]=new PlayerSkills(idPositionToAdd, positionToAdd, 1);
                    userSelected.setSkills(playerSkills);
                    AddPlayersTask task = new AddPlayersTask();
                    task.execute(userSelected);
                }
            });

            listAdapter.filter("", mSelectedItems);

        }//close onPostExecute
    }

    private class AddPlayersTask extends AsyncTask<User, Void, JSONObject> {
        private final ProgressDialog dialog = new ProgressDialog(SearchPlayer.this);
        protected void onPreExecute() {
            this.dialog.setMessage("Adding Player...");
            this.dialog.setCancelable(false);
            this.dialog.show();
        }
        protected JSONObject doInBackground(User... params) {

            JSONObject responseJSON = null;
            final HttpClient httpClient = new DefaultHttpClient();
            final HttpPost httpPost;
            if(addingExtraPlayerToMatch)
                httpPost= new HttpPost(url_host_connection_secure+"/matchPlayer/"+ matchId+"/"+team.getTeamId());
            else
                httpPost= new HttpPost(url_host_connection_secure+"/team/"+team.getTeamId());

            httpPost.addHeader("x-access-token",token.getUser_token());
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

    public boolean onQueryTextChange(String newText) {
        listAdapter.filter(newText, mSelectedItems);
        return false;
    }

    public boolean onQueryTextSubmit(String query) {
        listAdapter.filter(query, mSelectedItems);
        return false;
    }

    public void showPopupPlayersFilter() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SearchPlayer.this);
        // Get the layout inflater
        LayoutInflater inflater = getLayoutInflater();
        builder.setTitle("Filter");
        builder.setCancelable(false);
        builder.setIcon(R.drawable.filter_b);
        View v=inflater.inflate(R.layout.layout_player_filter, null);

        Button btnPlayerSkills = (Button) v.findViewById(R.id.btnPlayerSkills);
        txtFields=new EditText[2];
        txtFields[0] = (EditText) v.findViewById(R.id.txtProvince);
        txtFields[1] = (EditText) v.findViewById(R.id.txtCanton);

        txtFields[0].setText(provinceNameSelected);
        txtFields[1].setText(cantonNameSelected);

        GetProvincesTask provincesTask = new GetProvincesTask();
        provincesTask.execute();

        updateCantonSelected=false;
        GetCantonsTask cantonsTask = new GetCantonsTask();
        cantonsTask.execute(canton.getProvinceId());

        btnPlayerSkills.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
                showPopupSkills();
            }
        });

        builder.setView(v)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                if(doRegionFilter) {
                                    task = new GetPlayersTask();
                                    task.execute(new String[]{canton.getProvinceId() + "", canton.getCantonId() + ""});
                                }
                                listAdapter.filter("", mSelectedItems);
                                doRegionFilter=false;
                            }
                        }
                );
        builder.create();
        builder.show();
    }

    public void showPopupSkills() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SearchPlayer.this);
        builder.setTitle("Skills")
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

                        //listAdapter.filter("", mSelectedItems);
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
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

    //mehtods to filter
    private class GetProvincesTask extends AsyncTask<String, Void, mProvinces[]> {

        protected void onPreExecute() {
            txtFields[0].setEnabled(false);
        }

        protected mProvinces[] doInBackground(String... params) {
            final HttpClient httpClient = new DefaultHttpClient();
            JSONObject responseJSON = null;
            final HttpGet httpGetProvinces= new HttpGet(url_host_connection+"/provinces");
            try {
                try {
                    HttpResponse httpResponseProvinces = httpClient.execute(httpGetProvinces);
                    String jsonResultProvinces = inputStreamToString(httpResponseProvinces.getEntity().getContent()).toString();
                    Gson gsonProvinces = new GsonBuilder()
                            .setDateFormat("yyyy-MM-dd")
                            .create();
                    provinces= gsonProvinces.fromJson(jsonResultProvinces, mProvinces[].class);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }catch (RuntimeException e)
            {
                e.printStackTrace();
            }
            return provinces;
        }
        @Override
        protected void onPostExecute(final mProvinces[] provinces) {

            final List<mProvinces> provinceList = Arrays.asList(provinces);
            final CharSequence[] ProvincesChar = new CharSequence[provinceList.size()];

            txtFields[0].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    for (int i = 0; i < provinceList.size(); i++) {
                        ProvincesChar[i] = provinces[i].getName();
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(SearchPlayer.this);
                    builder.setTitle("Provinces");

                    builder.setItems(ProvincesChar,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    int proviceIdSelected = 0;
                                    for (int i = 0; i < provinces.length; i++) {
                                        if (ProvincesChar[which].toString().equals(provinces[i].getName())) {
                                            proviceIdSelected = provinces[i].getProvinceId();
                                            provinceNameSelected=provinces[i].getName();
                                            break;
                                        }
                                    }
                                    doRegionFilter=true;
                                    txtFields[0].setText(ProvincesChar[which].toString());
                                    dialog.dismiss();
                                    updateCantonSelected=true;
                                    GetCantonsTask cantonsTask = new GetCantonsTask();
                                    cantonsTask.execute(proviceIdSelected);
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            });
            txtFields[0].setEnabled(true);
        }//close onPostExecute
    }

    private class GetCantonsTask extends AsyncTask<Integer, Void, mCantons[]> {

        protected void onPreExecute() {
            txtFields[1].setEnabled(false);
        }
        protected mCantons[] doInBackground(Integer... params) {
            final HttpClient httpClient = new DefaultHttpClient();
            JSONObject responseJSON = null;
            final HttpGet httpGetCantons= new HttpGet(url_host_connection+"/cantons/"+params[0]);
            try {
                try {
                    HttpResponse httpResponseCantons = httpClient.execute(httpGetCantons);
                    String jsonResultCantons = inputStreamToString(httpResponseCantons.getEntity().getContent()).toString();
                    Gson gsonCantons = new GsonBuilder()
                            .setDateFormat("yyyy-MM-dd")
                            .create();
                    cantons= gsonCantons.fromJson(jsonResultCantons, mCantons[].class);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }catch (RuntimeException e)
            {
                e.printStackTrace();
            }
            return cantons;
        }
        @Override
        protected void onPostExecute(final mCantons[] cantons) {

            final List<mCantons> cantonList = Arrays.asList(cantons);
            final CharSequence[] CantonsChar = new CharSequence[cantonList.size()];

            for(int i=0;i<cantonList.size();i++)
            {
                CantonsChar[i]=cantons[i].getName();
            }
            if(updateCantonSelected) {
                canton = cantons[0];
                cantonNameSelected = canton.getName();
            }
            txtFields[1].setText(cantonNameSelected);

            txtFields[1].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SearchPlayer.this);
                    builder.setTitle("Cantons");

                    builder.setItems(CantonsChar,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    CantonsChar[which].toString();
                                    txtFields[1].setText(CantonsChar[which].toString());
                                    for (int i = 0; i < cantons.length; i++) {
                                        if (CantonsChar[which].toString().equals(cantons[i].getName())) {
                                            canton = cantons[i];
                                            cantonNameSelected=cantons[i].getName();
                                            break;
                                        }
                                    }
                                    doRegionFilter=true;
                                    dialog.dismiss();
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            });
            txtFields[1].setEnabled(true);
        }//close onPostExecute
    }


}
