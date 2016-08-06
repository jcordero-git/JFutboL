package jfutbol.com.jfutbol;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

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

import jfutbol.com.jfutbol.model.PlayerSkills;
import jfutbol.com.jfutbol.model.User;
import jfutbol.com.jfutbol.model.mSoccerFields;
import jfutbol.com.jfutbol.singleton.singleton_token;

//import android.widget.SearchView;


public class Players extends Fragment {

    static String url_host_connection;
    static String url_host_connection_secure;
    static singleton_token token = singleton_token.getInstance();
    static User[] players;
    static User playerForFilter;
    private static GetPlayersTask task;
    static playerListViewAdapter listAdapter;
    Bundle params;
    static View root;
    private static String userJson;
    static SwipeRefreshLayout swipeContainer;
    static User user;
    static ArrayList<String> mSelectedItems;
    static Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd")
            .create();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        url_host_connection=getResources().getString(R.string.url_host_connection).toString();
        url_host_connection_secure=getResources().getString(R.string.url_host_connection).toString()+"/api";

        if (getArguments() != null) {
            userJson  = getArguments().getString("user");
            user=gson.fromJson(userJson,User.class);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_search_soccer_field, container, false);
        //task = new GetSoccerFieldsTask();
       // task.execute(new String[]{user.getProvinceId()+"",user.getCantonId()+""});

        swipeContainer = (SwipeRefreshLayout) root.findViewById(R.id.swipe_container);
        swipeContainer.setProgressViewOffset(false, 0,(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics()));
        swipeContainer.setRefreshing(true);

        task = new GetPlayersTask();
        task.execute(new String[]{user.getProvinceId() + "", user.getCantonId() + ""});


        return root;
    }

    public static void refreshPlayers(int provinceId, int cantonId) {
        swipeContainer.setRefreshing(true);
        task = new GetPlayersTask();
        task.execute(new String[]{provinceId+"",cantonId+""});
    }

    private static class GetPlayersTask extends AsyncTask<String, Void, User[]> {
        protected void onPreExecute() {

        }
        protected User[] doInBackground(String... params) {
            final HttpClient httpClient = new DefaultHttpClient();
            JSONObject responseJSON = null;
            final HttpGet httpGetMyTeams= new HttpGet(url_host_connection_secure+"/players/all/0/"+params[0]+"/"+params[1]);
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

            List <User> playerlist = Arrays.asList(players);
            ListView list = (ListView) root.findViewById(R.id.listView);

            listAdapter = new playerListViewAdapter(root.getContext(), playerlist, 0);
            list.setAdapter(listAdapter);

            list.setItemsCanFocus(false);
            list.setLongClickable(true);


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

                    Intent user_profile = new Intent(root.getContext(), UserProfile.class);
                    Bundle params = new Bundle();
                    params.putString("user", jsonUserSelected);
                    user_profile.putExtras(params);
                    root.getContext().startActivity(user_profile);
                }
            });

            listAdapter.filter("", mSelectedItems);

            swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    GetPlayersTask task = new GetPlayersTask();
                    task.execute(new String[]{user.getProvinceId() + "", user.getCantonId() + ""});
                }
            });

            swipeContainer.setRefreshing(false);

        }//close onPostExecute
    }

    public static StringBuilder inputStreamToString(InputStream imput) {
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
