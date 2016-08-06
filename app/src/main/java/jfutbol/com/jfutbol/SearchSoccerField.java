package jfutbol.com.jfutbol;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
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
import jfutbol.com.jfutbol.model.mSoccerFields;
import jfutbol.com.jfutbol.singleton.singleton_token;

//import android.widget.SearchView;


public class SearchSoccerField extends Fragment {

    static String url_host_connection;
    static String url_host_connection_secure;
    static singleton_token token = singleton_token.getInstance();
    static mSoccerFields[] soccerFields;
    mSoccerFields soccerFieldForFilter;
    private static GetSoccerFieldsTask task;
    soccerFieldsListViewAdapter listAdapter;
    Bundle params;
    static View root;
    private static String userJson;
    static SwipeRefreshLayout swipeContainer;
    static User user;
    static Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd")
            .create();

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
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

        task = new GetSoccerFieldsTask();
        task.execute(new String[]{user.getProvinceId()+"",user.getCantonId()+""});

        return root;
    }

    public static void refreshSoccerFields(int provinceId, int cantonId) {
        swipeContainer.setRefreshing(true);
        task = new GetSoccerFieldsTask();
        task.execute(new String[]{provinceId+"",cantonId+""});
    }

    private static class GetSoccerFieldsTask extends AsyncTask<String, View, mSoccerFields[]> {

        protected mSoccerFields[] doInBackground(String... params) {
            final HttpClient httpClient = new DefaultHttpClient();
            JSONObject responseJSON = null;
            final HttpGet httpGet= new HttpGet(url_host_connection_secure+"/soccerfields/"+params[0]+"/"+params[1]);
            httpGet.addHeader("x-access-token",token.getUser_token());
            try {
                try {
                    HttpResponse httpResponse = httpClient.execute(httpGet);
                    String jsonResult = inputStreamToString(httpResponse.getEntity().getContent()).toString();
                    Gson gsonMyTeams = new Gson();
                    soccerFields= gsonMyTeams.fromJson(jsonResult, mSoccerFields[].class);
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
        protected void onPostExecute(mSoccerFields[] soccerField) {
            List<mSoccerFields> soccerFieldlist = Arrays.asList(soccerField);
            ListView list = (ListView) root.findViewById(R.id.listView);
            soccerFieldsListViewAdapter listAdapter = new soccerFieldsListViewAdapter(root.getContext(), soccerFieldlist );
            list.setAdapter(listAdapter);
            list.setItemsCanFocus(false);
            list.setLongClickable(true);
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    mSoccerFields selected=new mSoccerFields();
                    String jsonSelected="";
                    selected=(mSoccerFields)parent.getItemAtPosition(position);
                    jsonSelected=gson.toJson(selected);

                    Activity context = (Activity)view.getContext();
                   // Activity context = mContext;
                    //Application context = mContext;

                    Intent match_Info = new Intent(context, Soccer_Field_Info_Reserve.class);
                    Bundle params=new Bundle();
                    params.putString("user", userJson);
                    params.putString("soccerfield", jsonSelected);
                    match_Info.putExtras(params);
                    //context.startActivity(match_Info);
                    context.startActivityForResult(match_Info, 2);
                }
            });

            swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    task = new GetSoccerFieldsTask();
                    task.execute(new String[]{user.getProvinceId()+"",user.getCantonId()+""});
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if(resultCode == -1){

            }
        }
    }//onActivityResult

}
