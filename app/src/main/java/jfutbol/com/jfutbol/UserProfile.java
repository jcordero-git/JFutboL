package jfutbol.com.jfutbol;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import java.util.Arrays;
import java.util.List;

import jfutbol.com.jfutbol.model.PlayerSkills;
import jfutbol.com.jfutbol.model.Team;
import jfutbol.com.jfutbol.model.User;
import jfutbol.com.jfutbol.singleton.singleton_token;


public class UserProfile extends Activity {

    Bundle params;
    private String userJson;
    User user;
    PlayerSkills[] mySkills;
    Team[] myTeams;
    RelativeLayout root;
    String url_host_connection;
    String url_host_connection_secure;
    singleton_token token = singleton_token.getInstance();
    AQuery aq;
    TextView lbSkills;
    Gson gson=new GsonBuilder()
            .setDateFormat("yyyy-MM-dd")
            .create();
    boolean memCache = false;
    boolean fileCache = false;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_me);
        url_host_connection=getResources().getString(R.string.url_host_connection).toString();
        url_host_connection_secure=getResources().getString(R.string.url_host_connection).toString()+"/api";

        params=getIntent().getExtras();
        //root = (FrameLayout) findViewById(R.id.coordinatorLayout);

        if (!params.isEmpty()) {
            userJson  = params.getString("user");
            user=gson.fromJson(userJson,User.class);
        }

        root = (RelativeLayout) findViewById(R.id.idRL);
        Toolbar bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.settings_toolbar, root, false);
        root.addView(bar, 0); // insert at top
        bar.setTitle(user.getFirstName() + " " + user.getLastName());
        bar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                setResult(RESULT_OK, returnIntent);
                finish();
            }
        });

        aq = new AQuery(UserProfile.this, null);
        aq.id(R.id.imgMe).image(url_host_connection + "/images/profile/" + user.getUserId() + ".png", memCache, fileCache);
        lbSkills = (TextView) findViewById(R.id.lbSkills);

        lbSkills.setText("Skills");

        GetUpdateUserInfoTask userInfoTask = new GetUpdateUserInfoTask();
        userInfoTask.execute(new String[]{user.getUserId().toString()});

        GetMyTeamsTask teamTask = new GetMyTeamsTask();
        teamTask.execute();
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private class GetUpdateUserInfoTask extends AsyncTask<String, Void, User> {

        protected User doInBackground(String... params) {
            final HttpClient httpClient= new DefaultHttpClient();
            final HttpGet httpGetLogin= new HttpGet(url_host_connection_secure+"/user/"+params[0]);
            httpGetLogin.addHeader("x-access-token",token.getUser_token());
            try {
                try {
                    HttpResponse httpResponseLogin = httpClient.execute(httpGetLogin);
                    String jsonResultLogin = inputStreamToString(httpResponseLogin.getEntity().getContent()).toString();
                    JSONObject object = new JSONObject(jsonResultLogin);
                    Gson gsonLogin = new GsonBuilder()
                            .setDateFormat("yyyy-MM-dd")
                            .create();
                    user=gsonLogin.fromJson(object.toString(),User.class);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }catch (RuntimeException e)
            {
                e.printStackTrace();
            }
            return user;
        }//close doInBackground

        @Override
        protected void onPostExecute(final User user) {
            aq.id(R.id.lbFirstName).text(user.getFirstName()+" "+user.getLastName());
            aq.id(R.id.lbAge).text(user.getAge()+" Years old");
            aq.id(R.id.lbEmail).text(user.getEmail());
            aq.id(R.id.lbPhone).text(user.getPhone());


            List<PlayerSkills> skilllist = Arrays.asList(user.getSkills());
            ListView list = (ListView) findViewById(R.id.listViewSkills);
            skillListViewAdapter listAdapter = new skillListViewAdapter(UserProfile.this, skilllist );

            list.setAdapter(listAdapter);
            list.setDivider(null);
            list.setItemsCanFocus(false);
            list.setLongClickable(false);
            listAdapter.filter(1);
            setListViewHeightBasedOnItems(list);
        }//close onPostExecute
    }

    private class GetMySkillsTask extends AsyncTask<PlayerSkills, View, PlayerSkills[]> {
        private final ProgressDialog dialog = new ProgressDialog(UserProfile.this);
        protected void onPreExecute() {
            this.dialog.setMessage("Updating Skills...");
            this.dialog.setCancelable(false);
            this.dialog.show();
        }
        protected PlayerSkills[] doInBackground(PlayerSkills... params) {
            final HttpClient httpClient = new DefaultHttpClient();
            JSONObject responseJSON = null;
            final HttpGet httpGetMySkills= new HttpGet(url_host_connection_secure+"/players/"+user.getUserId()+"/skills");
            httpGetMySkills.addHeader("x-access-token",token.getUser_token());
            try {
                try {
                    HttpResponse httpResponseMySkills = httpClient.execute(httpGetMySkills);
                    String jsonResultMySkills = inputStreamToString(httpResponseMySkills.getEntity().getContent()).toString();
                    Gson gsonMySkills = new Gson();
                    mySkills= gsonMySkills.fromJson(jsonResultMySkills, PlayerSkills[].class);

                    user.setSkills(mySkills);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }catch (RuntimeException e)
            {
                e.printStackTrace();
            }
            return mySkills;
        }
        @Override
        protected void onPostExecute(PlayerSkills[] skills) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }
            List<PlayerSkills> skilllist = Arrays.asList(skills);
            ListView list = (ListView) findViewById(R.id.listViewSkills);
            skillListViewAdapter listAdapter = new skillListViewAdapter(UserProfile.this, skilllist );
            list.setAdapter(listAdapter);
            list.setDivider(null);
            list.setItemsCanFocus(false);
            list.setLongClickable(false);
            listAdapter.filter(1);
            setListViewHeightBasedOnItems(list);
        }
    }

    private class GetMyTeamsTask extends AsyncTask<Team, View, Team[]> {
        private final ProgressDialog dialog = new ProgressDialog(UserProfile.this);
        protected void onPreExecute() {
            this.dialog.setMessage("Updating Teams...");
            this.dialog.setCancelable(false);
            this.dialog.show();
        }
        protected Team[] doInBackground(Team... params) {
            final HttpClient httpClient = new DefaultHttpClient();
            JSONObject responseJSON = null;
            final HttpGet httpGetMyTeams= new HttpGet(url_host_connection_secure+"/players/"+user.getUserId()+"/teams");
            httpGetMyTeams.addHeader("x-access-token",token.getUser_token());
            try {
                try {
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
            return myTeams;
        }
        @Override
        protected void onPostExecute(Team[] team) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }
            List<Team> teamlist = Arrays.asList(team);
            ListView list = (ListView) findViewById(R.id.listViewTeams);
            teamListViewAdapter listAdapter = new teamListViewAdapter(UserProfile.this, teamlist );
            list.setAdapter(listAdapter);
            setListViewHeightBasedOnItems(list);
            list.setDivider(null);
            list.setItemsCanFocus(false);
            list.setLongClickable(false);
        }
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

    public void refreshUserInfo() {
        aq.id(R.id.imgMe).image(url_host_connection + "/images/profile/" + user.getUserId() + ".png", memCache, fileCache);
        GetMyTeamsTask teamTask = new GetMyTeamsTask();
        teamTask.execute();
        GetUpdateUserInfoTask userInfoTask = new GetUpdateUserInfoTask();
        userInfoTask.execute(new String[]{user.getUserId().toString()});
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

    }//onActivityResult

}
