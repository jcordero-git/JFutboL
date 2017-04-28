package jfutbol.com.jfutbol;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;

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
import jfutbol.com.jfutbol.singleton.Utils;
import jfutbol.com.jfutbol.singleton.singleton_token;


public class me extends Fragment {

    static String userJson;
    static User user;
    static PlayerSkills[] mySkills;
    static Team[] myTeams;
    static View root;
    static String url_host_connection;
    static String url_host_connection_secure;
    static singleton_token token = singleton_token.getInstance();
    static AQuery aq;
    static Gson gson;
    static boolean memCache = false;
    static boolean fileCache = false;

    View coordinatorLayout;

    public me() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        url_host_connection=getResources().getString(R.string.url_host_connection).toString();
        url_host_connection_secure=getResources().getString(R.string.url_host_connection).toString()+"/api";
        if (getArguments() != null) {
            gson = new GsonBuilder()
                    .setDateFormat("yyyy-MM-dd")
                    .create();
            userJson  = getArguments().getString("user");
            user=gson.fromJson(userJson,User.class);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_me, container, false);
        aq = new AQuery(getActivity(), root);

        aq.id(R.id.imgMe).image(url_host_connection+"/images/profile/"+user.getId()+".png", memCache, fileCache);

        coordinatorLayout = root.findViewById(R.id.coordinatorLayout);
        RelativeLayout RLPlayer = (RelativeLayout) root.findViewById(R.id.idRLPlayers);

        if(user.getUserType()==2)
            RLPlayer.setVisibility(View.INVISIBLE);

        GetUpdateUserInfoTask userInfoTask = new GetUpdateUserInfoTask();
        userInfoTask.execute(new String[]{user.getId().toString()});

        GetMyTeamsTask teamTask = new GetMyTeamsTask();
        teamTask.execute();

        GetMySkillsTask SkillTask = new GetMySkillsTask();
        SkillTask.execute();

        /*
        List <PlayerSkills> playerSkill = Arrays.asList(user.getSkills());
        ListView skillList = (ListView) root.findViewById(R.id.listViewSkills);
        //skillListViewAdapter listAdapter = new skillListViewAdapter(getActivity(), user.getSkills() );
        skillListViewAdapter listAdapter = new skillListViewAdapter(getActivity(), playerSkill );
        skillList.setAdapter(listAdapter);
        listAdapter.filter(1);
        setListViewHeightBasedOnItems(skillList);
        skillList.setDivider(null);
        skillList.setItemsCanFocus(false);
        skillList.setLongClickable(true);
        */


        return root;
    }

    private static class GetUpdateUserInfoTask extends AsyncTask<String, Void, User> {

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
            ListView list = (ListView) root.findViewById(R.id.listViewSkills);
            skillListViewAdapter listAdapter = new skillListViewAdapter(root.getContext(), skilllist );
            list.setAdapter(listAdapter);
            list.setDivider(null);
            list.setItemsCanFocus(false);
            list.setLongClickable(true);
            listAdapter.filter(1);
            setListViewHeightBasedOnItems(list);

            list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
                @Override
                public boolean onItemLongClick(AdapterView<?> av, View v, int pos, long id)
                {
                    try {
                        PlayerSkills skillSelected=new PlayerSkills("0","0",0);
                        String jsonSkillSelected="";
                        skillSelected=(PlayerSkills)av.getItemAtPosition(pos);
                        Gson gson = new GsonBuilder()
                                .setDateFormat("yyyy-MM-dd")
                                .create();
                        jsonSkillSelected=gson.toJson(skillSelected);

                        final CharSequence[] items = {"Delete"};
                        AlertDialog.Builder builder = new AlertDialog.Builder(root.getContext());
                        builder.setTitle("Options");
                        final PlayerSkills finalSelected = skillSelected;
                        final String finalJsonSkillSelected = jsonSkillSelected;
                        builder.setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int item) {
                                if(item==0)
                                {
                                    try {
                                        confirmDeleteSkill("Do you want to remove this skill: "+ finalSelected.getSkillName()+"?" ,user.getId()+"", finalSelected.getId());

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

        }//close onPostExecute
    }

    private static class GetMySkillsTask extends AsyncTask<PlayerSkills, View, PlayerSkills[]> {
        private final ProgressDialog dialog = new ProgressDialog(root.getContext());
        protected void onPreExecute() {
            this.dialog.setMessage("Updating Skills...");
            this.dialog.setCancelable(false);
            this.dialog.show();
        }
        protected PlayerSkills[] doInBackground(PlayerSkills... params) {
            final HttpClient httpClient = new DefaultHttpClient();
            JSONObject responseJSON = null;
            final HttpGet httpGetMySkills= new HttpGet(url_host_connection_secure+"/players/"+user.getId()+"/skills");
            httpGetMySkills.addHeader("x-access-token",token.getUser_token());
            try {
                try {
                    HttpResponse httpResponseMySkills = httpClient.execute(httpGetMySkills);
                    String jsonResultMySkills = inputStreamToString(httpResponseMySkills.getEntity().getContent()).toString();
                    //Gson gsonMySkills = new Gson();
                    mySkills= gson.fromJson(jsonResultMySkills, PlayerSkills[].class);

                    user.setSkills(mySkills);
                    userJson = gson.toJson(user);

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
            ListView list = (ListView) root.findViewById(R.id.listViewSkills);
            skillListViewAdapter listAdapter = new skillListViewAdapter(root.getContext(), skilllist );
            list.setAdapter(listAdapter);
            list.setDivider(null);
            list.setItemsCanFocus(false);
            list.setLongClickable(true);
            listAdapter.filter(1);
            setListViewHeightBasedOnItems(list);

            list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
                @Override
                public boolean onItemLongClick(AdapterView<?> av, View v, int pos, long id)
                {
                    try {
                        PlayerSkills skillSelected=new PlayerSkills("0","0",0);
                        String jsonSkillSelected="";
                        skillSelected=(PlayerSkills)av.getItemAtPosition(pos);
                        Gson gson = new GsonBuilder()
                                .setDateFormat("yyyy-MM-dd")
                                .create();
                        jsonSkillSelected=gson.toJson(skillSelected);

                        final CharSequence[] items = {"Delete"};
                        AlertDialog.Builder builder = new AlertDialog.Builder(root.getContext());
                        builder.setTitle("Options");
                        final PlayerSkills finalSelected = skillSelected;
                        final String finalJsonSkillSelected = jsonSkillSelected;
                        builder.setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int item) {
                                if(item==0)
                                {
                                    try {
                                        confirmDeleteSkill("Do you want to remove this skill: "+ finalSelected.getSkillName()+"?" ,user.getId()+"", finalSelected.getId());

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
    }

    private static class GetMyTeamsTask extends AsyncTask<Team, View, Team[]> {
        private final ProgressDialog dialog = new ProgressDialog(root.getContext());
        protected void onPreExecute() {
            this.dialog.setMessage("Updating Teams...");
            this.dialog.setCancelable(false);
            this.dialog.show();
        }
        protected Team[] doInBackground(Team... params) {
            final HttpClient httpClient = new DefaultHttpClient();
            JSONObject responseJSON = null;
            final HttpGet httpGetMyTeams= new HttpGet(url_host_connection_secure+"/players/"+user.getId()+"/teams");
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
            ListView list = (ListView) root.findViewById(R.id.listViewTeams);
            teamListViewAdapter listAdapter = new teamListViewAdapter(root.getContext(), teamlist );
            list.setAdapter(listAdapter);
            setListViewHeightBasedOnItems(list);
            list.setDivider(null);
            list.setItemsCanFocus(false);
            list.setLongClickable(true);
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    Team teamSelected=new Team(0,0,"");
                    String jsonTeamSelected="";
                    teamSelected=(Team)parent.getItemAtPosition(position);
                    Gson gson = new GsonBuilder()
                            .setDateFormat("yyyy-MM-dd")
                            .create();
                    jsonTeamSelected=gson.toJson(teamSelected);

                    Intent team_Info = new Intent(root.getContext(), Team_Info.class);
                    Bundle params=new Bundle();
                    params.putString("team", jsonTeamSelected );
                    team_Info.putExtras(params);
                    root.getContext().startActivity(team_Info);
                }
            });


            list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
                @Override
                public boolean onItemLongClick(AdapterView<?> av, View v, int pos, long id)
                {
                    try {
                        Team teamSelected=new Team(0,0,"");
                        String jsonTeamSelected="";
                        teamSelected=(Team)av.getItemAtPosition(pos);
                        Gson gson = new GsonBuilder()
                                .setDateFormat("yyyy-MM-dd")
                                .create();
                        jsonTeamSelected=gson.toJson(teamSelected);

                        final CharSequence[] items = {"Leave"};
                        AlertDialog.Builder builder = new AlertDialog.Builder(root.getContext());
                        builder.setTitle("Options");
                        final Team finalSelected = teamSelected;
                        final String finalJsonTeamSelected = jsonTeamSelected;
                        builder.setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int item) {
                                if(item==0)
                                {
                                    try {
                                        confirmDeleteTeam("Do you want to leave this team: "+ finalSelected.getName()+"?" , finalSelected.getId()+"");

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
    }

    public static void confirmDeleteSkill(final String message, final String playerId, final String skillId) throws Exception {
        AlertDialog.Builder builder = new AlertDialog.Builder(root.getContext());
        builder.setMessage(message);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DeletePlayerSkill task = new DeletePlayerSkill();
                task.execute(new String[]{playerId, skillId});
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

    private static class DeletePlayerSkill extends AsyncTask<String, Void, Boolean> {
        String response = null;
        private final ProgressDialog dialog = new ProgressDialog(root.getContext());
        JSONObject object_feed;
        // can use UI thread here
        protected void onPreExecute() {
            this.dialog.setMessage("Removing Skill");
            this.dialog.setCancelable(false);
            this.dialog.show();
        }

        protected Boolean doInBackground(String... params) {
            // TODO Auto-generated method stub
            //User user = new User();
            final HttpClient httpClient= new DefaultHttpClient();
            final HttpGet httpRemoveSkill= new HttpGet(url_host_connection_secure+"/players_skills_delete/"+params[0]+"/"+params[1]);
            httpRemoveSkill.addHeader("x-access-token",token.getUser_token());
            try {
                try {
                    HttpResponse httpResponseLogin = httpClient.execute(httpRemoveSkill);
                    String jsonResultRemoveSkill = inputStreamToString(httpResponseLogin.getEntity().getContent()).toString();
                    JSONObject object = new JSONObject(jsonResultRemoveSkill);

                    Gson gsonLogin = new GsonBuilder()
                            .setDateFormat("yyyy-MM-dd")
                            .create();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }catch (RuntimeException e)
            {
                e.printStackTrace();
            }
            return true;
        }//close doInBackground

        @Override
        protected void onPostExecute(Boolean status) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
                GetMySkillsTask task = new GetMySkillsTask();
                task.execute();
            }
        }//close onPostExecute
    }

    public static void confirmDeleteTeam(final String message, final String teamId) throws Exception {
        AlertDialog.Builder builder = new AlertDialog.Builder(root.getContext());
        builder.setMessage(message);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                DeleteTeam task = new DeleteTeam();
                task.execute(new String[]{teamId});

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

    private static class DeleteTeam extends AsyncTask<String, Void, Boolean> {
        String response = null;
        private final ProgressDialog dialog = new ProgressDialog(root.getContext());
        JSONObject object_feed;
        // can use UI thread here
        protected void onPreExecute() {
            this.dialog.setMessage("Deleting Team");
            this.dialog.setCancelable(false);
            this.dialog.show();
        }

        protected Boolean doInBackground(String... params) {
            // TODO Auto-generated method stub
            //User user = new User();
            final HttpClient httpClient= new DefaultHttpClient();
            final HttpGet httpDeteleTeam= new HttpGet(url_host_connection_secure+"/team/Delete/"+params[0]);
            httpDeteleTeam.addHeader("x-access-token",token.getUser_token());
            try {
                try {
                    HttpResponse httpResponseLogin = httpClient.execute(httpDeteleTeam);
                    String jsonResultDeleteTeam = inputStreamToString(httpResponseLogin.getEntity().getContent()).toString();
                    JSONObject object = new JSONObject(jsonResultDeleteTeam);

                    Gson gsonLogin = new GsonBuilder()
                            .setDateFormat("yyyy-MM-dd")
                            .create();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }catch (RuntimeException e)
            {
                e.printStackTrace();
            }
            return true;
        }//close doInBackground

        @Override
        protected void onPostExecute(Boolean status) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
                GetMyTeamsTask task = new GetMyTeamsTask();
                task.execute();
            }
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

    public static void refreshUserInfo() {
        aq.id(R.id.imgMe).image(url_host_connection + "/images/profile/" + user.getId() + ".png", memCache, fileCache);
        GetMyTeamsTask teamTask = new GetMyTeamsTask();
        teamTask.execute();
        GetUpdateUserInfoTask userInfoTask = new GetUpdateUserInfoTask();
        userInfoTask.execute(new String[]{user.getId().toString()});
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

    }//onActivityResult

}
