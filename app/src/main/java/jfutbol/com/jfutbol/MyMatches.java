package jfutbol.com.jfutbol;

import android.app.Activity;
import android.app.Fragment;
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
import java.util.Arrays;
import java.util.List;

import jfutbol.com.jfutbol.model.User;
import jfutbol.com.jfutbol.model.mMatches;
import jfutbol.com.jfutbol.singleton.singleton_token;

public class MyMatches extends Fragment {

    private static String userJson;
    static  User user;
    private static GetMatchesTask matchesTask;
    static mMatches[] matches;
    static View root;
    static String url_host_connection;
    static String url_host_connection_secure;
    static singleton_token token = singleton_token.getInstance();

    static SwipeRefreshLayout swipeContainer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        url_host_connection=getResources().getString(R.string.url_host_connection).toString();
        url_host_connection_secure=getResources().getString(R.string.url_host_connection).toString()+"/api";


        if (getArguments() != null) {
            Gson gson = new GsonBuilder()
                    .setDateFormat("yyyy-MM-dd")
                    .create();
            userJson  = getArguments().getString("user");
            user=gson.fromJson(userJson,User.class);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_my_matches, container, false);
        swipeContainer = (SwipeRefreshLayout) root.findViewById(R.id.swipe_container);
        swipeContainer.setProgressViewOffset(false, 0,(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics()));
        swipeContainer.setRefreshing(true);
        matchesTask = new GetMatchesTask();
        matchesTask.execute();
        return root;
    }

    public static void refreshMatches() {
        swipeContainer.setRefreshing(true);
        matchesTask = new GetMatchesTask();
        matchesTask.execute();
    }

    private static class GetMatchesTask extends AsyncTask<User, Void, mMatches[]> {
        protected void onPreExecute() {

        }
        protected mMatches[] doInBackground(User... params) {
            final HttpClient httpClient = new DefaultHttpClient();
            JSONObject responseJSON = null;
            final HttpGet httpGetMyTeams= new HttpGet(url_host_connection_secure+"/matches/"+user.getId());
            httpGetMyTeams.addHeader("x-access-token",token.getUser_token());
            try {
                try {
                    HttpResponse httpResponseMyTeams = httpClient.execute(httpGetMyTeams);
                    String jsonResultNotifications = inputStreamToString(httpResponseMyTeams.getEntity().getContent()).toString();
                    Gson gsonMyTeams = new GsonBuilder()
                            .setDateFormat("yyyy-MM-dd")
                            .create();
                    matches= gsonMyTeams.fromJson(jsonResultNotifications, mMatches[].class);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }catch (RuntimeException e)
            {
                e.printStackTrace();
            }
            return matches;
        }
        @Override
        protected void onPostExecute(mMatches[] matches) {

            final ListView list = (ListView) root.findViewById(R.id.listViewMatches);

            List<mMatches> matchlist = Arrays.asList(matches);
            matchesListViewAdapter listAdapter = new matchesListViewAdapter(root.getContext(), matchlist );
            list.setAdapter(listAdapter);

            list.setItemsCanFocus(false);
            list.setLongClickable(true);

            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    try {
                        mMatches matchSelected = new mMatches();
                        String jsonMatchSelected = "";
                        matchSelected = (mMatches) parent.getItemAtPosition(position);
                        Gson gson = new GsonBuilder()
                                .setDateFormat("dd-mm-yyyy HH:MM ")
                                .create();
                        jsonMatchSelected = gson.toJson(matchSelected);

                        Activity context = (Activity)view.getContext();
                        Intent match_Info = new Intent(context, Match_Info.class);
                        Bundle params=new Bundle();
                        params.putString("match", jsonMatchSelected );
                        params.putString("user", userJson);
                        match_Info.putExtras(params);
                        context.startActivity(match_Info);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
/*
            list.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                    if(scrollState<0) {
                        GetNotificationsPlayerTask task = new GetNotificationsPlayerTask();
                        task.execute();
                    }
                }
                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    int topRowVerticalPosition = (list == null || list.getChildCount() == 0) ? 0 : list.getChildAt(0).getTop();
                    swipeContainer.setEnabled(firstVisibleItem == 0 && topRowVerticalPosition >= 0);

                }
            });
            */

            swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    GetMatchesTask task = new GetMatchesTask();
                    task.execute();
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
