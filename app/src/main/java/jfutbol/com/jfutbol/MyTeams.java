package jfutbol.com.jfutbol;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.melnykov.fab.FloatingActionButton;
import com.melnykov.fab.ScrollDirectionListener;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jfutbol.com.jfutbol.model.Team;
import jfutbol.com.jfutbol.model.User;
import jfutbol.com.jfutbol.singleton.Utils;
import jfutbol.com.jfutbol.singleton.singleton_token;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;


public class MyTeams extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";

    // TODO: Rename and change types of parameters
    private String myTeamsJson;
    private String userJson;
    public static teamListViewAdapter listAdapter;
    String url_host_connection;
    String url_host_connection_secure;
    singleton_token token = singleton_token.getInstance();
    //List <Team> myTeams;
    ArrayList<String> mSelectedItems;
    Team[] myTeams;
    User user;
    View root;
    View coordinatorLayout;

    boolean loadTeamsComplete=false;
    boolean saveNewTeamComplete=false;
    Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd")
            .create();

//ShowCase
    final String ADD_SHOWCASE = "myTeams_add_ShowCase";

//ShowCase

    public MyTeams() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        url_host_connection=getResources().getString(R.string.url_host_connection).toString();
        url_host_connection_secure=getResources().getString(R.string.url_host_connection).toString()+"/api";

        if (getArguments() != null) {
            userJson  = getArguments().getString("user");
            user=gson.fromJson(userJson,User.class);
            myTeamsJson = getArguments().getString("myTeams");
            myTeams= gson.fromJson(myTeamsJson, Team[].class);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
      //  return inflater.inflate(R.layout.fragment_my_teams, container, false);
        root = inflater.inflate(R.layout.fragment_my_teams, container, false);
        //ListView list = (ListView) root.findViewById(R.id.listView);
        coordinatorLayout = root.findViewById(R.id.coordinatorLayout);
        GetMyTeamsTask task = new GetMyTeamsTask();
        task.execute();

      /*  final EditText txtSearchTeam= (EditText) root.findViewById(R.id.txtSearchTeam);
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
        */

        return root;
    }

    private void showNewTeamPopUp() {
        saveNewTeamComplete=false;
        final AlertDialog.Builder helpBuilder = new AlertDialog.Builder(getActivity());
        helpBuilder.setTitle("New Team");
        LayoutInflater inflater = (LayoutInflater) getActivity().getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final View newTeamLayout = getActivity().getLayoutInflater().inflate(R.layout.new_team, null);
        final EditText txtNewTeam= (EditText) newTeamLayout.findViewById(R.id.txtNewTeam);

        helpBuilder.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Team newTeam=new Team(0,user.getUserId(),txtNewTeam.getText().toString().trim());
                        AddNewTeamTask task = new AddNewTeamTask();
                        task.execute(newTeam);
                       // while (saveNewTeamComplete!=true){}

                       // myTeams.add(new Team(2,19,""));
                    }
                });
        helpBuilder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                     dialog.cancel();
                    }
                });

        // Remember, create doesn't show the dialog
        helpBuilder.setView(newTeamLayout);
        AlertDialog helpDialog = helpBuilder.create();
        helpDialog.show();


    }

    private int showOptionsPopup(){

        int selectedOption=0;
        final AlertDialog.Builder helpBuilder = new AlertDialog.Builder(getActivity());
        helpBuilder.setTitle("Options");
        LayoutInflater inflater = (LayoutInflater) getActivity().getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View optionsLayout = getActivity().getLayoutInflater().inflate(R.layout.slide_header, null);
       // final Button btnEdit= (Button) optionsLayout.findViewById(R.id.btnEdit);
       // final Button btnDelete= (Button) optionsLayout.findViewById(R.id.btnDelete);

        helpBuilder.setView(optionsLayout);
        AlertDialog helpDialog = helpBuilder.create();
        helpDialog.show();

        return selectedOption;
    }

    private class GetMyTeamsTask extends AsyncTask<Team, View, Team[]> {
        private final ProgressDialog  dialog = new ProgressDialog(getActivity());
        protected void onPreExecute() {
            this.dialog.setMessage("Updating My Teams...");
            this.dialog.setCancelable(false);
            this.dialog.show();
        }
        protected Team[] doInBackground(Team... params) {
            final HttpClient httpClient = new DefaultHttpClient();
            JSONObject responseJSON = null;
            final HttpGet httpGetMyTeams= new HttpGet(url_host_connection_secure+"/team/"+user.getUserId());
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
            ListView list = (ListView) root.findViewById(R.id.listView);
            listAdapter = new teamListViewAdapter(getActivity(), teamlist );
            list.setAdapter(listAdapter);
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

                    Intent team_Info = new Intent(getActivity(), Team_Info.class);
                    Bundle params=new Bundle();
                    params.putString("team", jsonTeamSelected );
                    team_Info.putExtras(params);
                    //getActivity().startActivity(team_Info);
                    startActivityForResult(team_Info, 2);
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

                        final CharSequence[] items = {"Edit", "Delete"};
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle("Options");
                        final Team finalSelected = teamSelected;
                        final String finalJsonTeamSelected = jsonTeamSelected;
                        builder.setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int item) {
                                if(item==0)
                                {
                                    Intent team_Info = new Intent(getActivity().getApplication().getApplicationContext(), Team_Info.class);
                                    Bundle params=new Bundle();
                                    params.putString("team", finalJsonTeamSelected);
                                    team_Info.putExtras(params);
                                    //getActivity().startActivity(team_Info);
                                    startActivityForResult(team_Info, 2);
                                }
                                if(item==1)
                                {
                                    try {
                                        confirmDeleteTeam("Do you want to delete this team: "+ finalSelected.getName()+"?" , finalSelected.getTeamId()+"");

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

            FloatingActionButton fab = (FloatingActionButton) root.findViewById(R.id.fab);
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
                    Intent add_New_Team = new Intent(getActivity(), Add_New_Team.class);
                    Bundle params=new Bundle();
                    params.putString("user", userJson.toString() );
                    add_New_Team.putExtras(params);
                    startActivityForResult(add_New_Team, 1);
                    /*
                    Fragment fragment = new Home();
                    Bundle args_notifications = new Bundle();
                    args_notifications.putString("user", userJson);
                    fragment.setArguments(args_notifications);
                    if (fragment != null) {
                        FragmentManager fragmentManager = getFragmentManager();
                        fragmentManager.beginTransaction()
                                .replace(R.id.frame_container, fragment)
                                .addToBackStack(null)
                                .commit();
                        //mDrawerLayout.closeDrawer(mDrawerList);
                    } else {
                        // error in creating fragment
                        Log.e("MainActivity", "Error in creating fragment");
                    } */
                }
            });

            showShowCase(fab, "Use this button to add your teams", ADD_SHOWCASE );


            loadTeamsComplete=true;

            /*
            try {
                Toast toast = Toast.makeText(getActivity().getApplication().getApplicationContext(), "Needs to refresh the list" , Toast.LENGTH_LONG);
                toast.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
            */

        }//close onPostExecute
    }

    public void confirmDeleteTeam(final String message, final String teamId) throws Exception {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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

    private class DeleteTeam extends AsyncTask<String, Void, Boolean> {
        String response = null;
       // private AsyncListener listener;

        /*
        public DeleteTeam(AsyncListener listener){
            this.listener=listener;
        }
        */


        private final ProgressDialog dialog = new ProgressDialog(getActivity());
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
                GetMyTeamsTask task = new GetMyTeamsTask();
                task.execute();
            }
        }//close onPostExecute
    }

    private class AddNewTeamTask extends AsyncTask<Team, Void, Team> {
        private final ProgressDialog dialog = new ProgressDialog(getActivity());

       /* protected void onPreExecute() {
            this.dialog.setMessage("Saving Data...");
            this.dialog.setCancelable(false);
            this.dialog.show();
        }*/
        protected Team doInBackground(Team... params) {
            final HttpClient httpClient = new DefaultHttpClient();
            JSONObject responseJSON = null;
            final HttpPost httpPost= new HttpPost(url_host_connection_secure+"/team/create");
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
            return null;
        }
        @Override
        protected void onPostExecute(Team team) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }
            saveNewTeamComplete=true;
            GetMyTeamsTask task2 = new GetMyTeamsTask();
            task2.execute();
            try {
                showToastDialog("Needs to refresh the list",2);
            } catch (Exception e) {
                e.printStackTrace();
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

    public void showToastDialog(String message, int type){
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast_layout,(ViewGroup) getActivity().findViewById(R.id.custom_toast_layout));
        TextView text = (TextView) layout.findViewById(R.id.lbMessage);
        ImageView toastImage= (ImageView) layout.findViewById(R.id.toastImage);
        text.setText(message);
        if(type==1)
            toastImage.setImageResource(R.drawable.toast_success);
        if(type==2)
            toastImage.setImageResource(R.drawable.toast_warning);
        if(type==3)
            toastImage.setImageResource(R.drawable.toast_error);
        Toast toast = new Toast(getActivity().getApplication().getApplicationContext());
        toast.setGravity(Gravity.BOTTOM, 0, 0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if(resultCode == -1){
                //String teamReturned=data.getStringExtra("team");
                //team=gson.fromJson(teamReturned, Team.class);
                GetMyTeamsTask task = new GetMyTeamsTask();
                task.execute();
                getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                String message = root.getResources().getString(R.string.team_added_success);
                Utils.ShowMessage(coordinatorLayout, message, 1);
            }
        }
        if (requestCode == 2) {
            if(resultCode == -1){
                //String teamReturned=data.getStringExtra("team");
                //team=gson.fromJson(teamReturned, Team.class);
                GetMyTeamsTask task = new GetMyTeamsTask();
                task.execute();
                getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            }
        }
    }//onActivityResult
    /*
    public interface AsyncListener{
        void postTaskMethod();
    }
    */

    public void showShowCase( View target, String message, final String SHOWCASE_ID ){
        //MaterialShowcaseView.resetSingleUse(getActivity(), SHOWCASE_ID);
        new MaterialShowcaseView.Builder(getActivity())
                .setTarget(target)
                .setDismissText("GOT IT")
                .setContentText(message)
                .setDelay(500) // optional but starting animations immediately in onCreate can make them choppy
                .singleUse(SHOWCASE_ID) // provide a unique ID used to ensure it is only shown once
                .setMaskColour(R.color.background_material_light)
                .setUseAutoRadius(true)
                .show();
    }



}
