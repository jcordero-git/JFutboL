package jfutbol.com.jfutbol;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
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
import java.util.Arrays;
import java.util.List;

import jfutbol.com.jfutbol.model.User;
import jfutbol.com.jfutbol.model.mSoccerCenters;
import jfutbol.com.jfutbol.singleton.singleton_token;


public class MySoccerCenters extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";

    // TODO: Rename and change types of parameters
    private String userJson;
    public static soccerCenterListViewAdapter listAdapter;
    String url_host_connection;
    String url_host_connection_secure;
    singleton_token token = singleton_token.getInstance();
    User user;
    mSoccerCenters[] mySoccerCenters;
    View root;
    //boolean loadTeamsComplete=false;
    boolean saveNewTeamComplete=false;

    public MySoccerCenters() {
        // Required empty public constructor
    }

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_my_soccer_centers, container, false);
        GetMySoccerCentersTask task = new GetMySoccerCentersTask();
        task.execute();
        return root;
    }

    private class GetMySoccerCentersTask extends AsyncTask<mSoccerCenters, View, mSoccerCenters[]> {
        private final ProgressDialog  dialog = new ProgressDialog(getActivity());
        protected void onPreExecute() {
            this.dialog.setMessage("Updating My Soccer Centers...");
            this.dialog.setCancelable(false);
            this.dialog.show();
        }
        protected mSoccerCenters[] doInBackground(mSoccerCenters... params) {
            final HttpClient httpClient = new DefaultHttpClient();
            JSONObject responseJSON = null;
            final HttpGet httpGetMySoccerCenters= new HttpGet(url_host_connection_secure+"/soccercenter/"+user.getId());
            httpGetMySoccerCenters.addHeader("x-access-token",token.getUser_token());
            try {
                try {
                    HttpResponse httpResponseMySoccerCenters = httpClient.execute(httpGetMySoccerCenters);
                    String jsonResultMySoccerCenters = inputStreamToString(httpResponseMySoccerCenters.getEntity().getContent()).toString();
                    Gson gsonMyTeams = new Gson();
                    mySoccerCenters= gsonMyTeams.fromJson(jsonResultMySoccerCenters, mSoccerCenters[].class);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }catch (RuntimeException e)
            {
                e.printStackTrace();
            }
            return mySoccerCenters;
        }
        @Override
        protected void onPostExecute(mSoccerCenters[] soccerCenters) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }
            List<mSoccerCenters> soccerCenterlist = Arrays.asList(soccerCenters);
            ListView list = (ListView) root.findViewById(R.id.listView);
            listAdapter = new soccerCenterListViewAdapter(getActivity(), soccerCenterlist );
            list.setAdapter(listAdapter);
            list.setItemsCanFocus(false);
            list.setLongClickable(true);
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    mSoccerCenters soccerCenterSelected=new mSoccerCenters();
                    String jsonSoccerCenterSelected="";
                    soccerCenterSelected=(mSoccerCenters)parent.getItemAtPosition(position);
                    Gson gson = new GsonBuilder()
                            .setDateFormat("yyyy-MM-dd")
                            .create();
                    jsonSoccerCenterSelected=gson.toJson(soccerCenterSelected);


                    Intent soccer_center_Info = new Intent(getActivity(), Soccer_Center_Info.class);
                    Bundle params=new Bundle();
                    params.putString("soccercenter", jsonSoccerCenterSelected );
                    soccer_center_Info.putExtras(params);
                    //getActivity().startActivity(team_Info);
                    startActivityForResult(soccer_center_Info, 1);

                }
            });


            list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
                @Override
                public boolean onItemLongClick(AdapterView<?> av, View v, int pos, long id)
                {
                    try {
                        mSoccerCenters soccerCenterSelected=new mSoccerCenters();
                        String jsonSoccerCenterSelected="";
                        soccerCenterSelected=(mSoccerCenters)av.getItemAtPosition(pos);
                        Gson gson = new GsonBuilder()
                                .setDateFormat("yyyy-MM-dd")
                                .create();
                        jsonSoccerCenterSelected=gson.toJson(soccerCenterSelected);

                        final CharSequence[] items = {"Edit", "Add a Soccer Field" , "Delete"};
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle("Options");
                        final mSoccerCenters finalSelected = soccerCenterSelected;
                        final String finalJsonSoccerCenterSelected = jsonSoccerCenterSelected;
                        builder.setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int item) {
                                if(item==0)
                                {
                                    /*
                                    Intent team_Info = new Intent(getActivity().getApplication().getApplicationContext(), Team_Info.class);
                                    Bundle params=new Bundle();
                                    params.putString("team", finalJsonSoccerCenterSelected );
                                    team_Info.putExtras(params);
                                    getActivity().startActivity(team_Info);
                                    */
                                }
                                if(item==1)
                                {
                                    /*
                                    Intent search_match = new Intent(getActivity(), SearchMatch.class);
                                    Bundle params=new Bundle();
                                    params.putString("team", finalJsonTeamSelected );
                                    search_match.putExtras(params);
                                    getActivity().startActivity(search_match);
                                    */
                                }
                                if(item==2)
                                {
                                    try {
                                        confirmDeleteSoccerCenter("Do you want to delete this Soccer Center: " + finalSelected.getName() + "?", finalSelected.getId() + "");

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
                    Intent add_New_Soccer_Center = new Intent(getActivity(), Add_New_Soccer_Center.class);
                    Bundle params = new Bundle();
                    params.putString("user", userJson.toString());
                    add_New_Soccer_Center.putExtras(params);
                    startActivityForResult(add_New_Soccer_Center, 1);
                }
            });

           // loadTeamsComplete=true;

        }//close onPostExecute
    }

    public void confirmDeleteSoccerCenter(final String message, final String soccerCenterId) throws Exception {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(message);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
               /*
                DeleteTeam task = new DeleteTeam();
                task.execute(new String[]{soccerCenterId});
                */
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

        private final ProgressDialog dialog = new ProgressDialog(getActivity());
        JSONObject object_feed;
        // can use UI thread here
        protected void onPreExecute() {
            this.dialog.setMessage("Deleting Soccer Center");
            this.dialog.setCancelable(false);
            this.dialog.show();
        }

        protected Boolean doInBackground(String... params) {
            // TODO Auto-generated method stub
            //User user = new User();
            final HttpClient httpClient= new DefaultHttpClient();
            final HttpGet httpDeteleSoccerCenter= new HttpGet(url_host_connection_secure+"/soccercenter/delete/"+params[0]);
            httpDeteleSoccerCenter.addHeader("x-access-token", token.getUser_token());

            try {
                try {
                    HttpResponse httpResponseLogin = httpClient.execute(httpDeteleSoccerCenter);
                    String jsonResultDeleteSoccerCenter = inputStreamToString(httpResponseLogin.getEntity().getContent()).toString();
                    JSONObject object = new JSONObject(jsonResultDeleteSoccerCenter);

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
                GetMySoccerCentersTask task = new GetMySoccerCentersTask();
                task.execute();
            }
        }//close onPostExecute
    }

    private class AddNewSoccerCenterTask extends AsyncTask<mSoccerCenters, Void, mSoccerCenters> {
        private final ProgressDialog dialog = new ProgressDialog(getActivity());

       /* protected void onPreExecute() {
            this.dialog.setMessage("Saving Data...");
            this.dialog.setCancelable(false);
            this.dialog.show();
        }*/
        protected mSoccerCenters doInBackground(mSoccerCenters... params) {
            final HttpClient httpClient = new DefaultHttpClient();
            JSONObject responseJSON = null;
            final HttpPost httpPost= new HttpPost(url_host_connection_secure+"/soccercenter");
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
        protected void onPostExecute(mSoccerCenters soccerCenter) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }
            saveNewTeamComplete=true;
            GetMySoccerCentersTask task2 = new GetMySoccerCentersTask();
            task2.execute();
            try {
                Toast toast = Toast.makeText(getActivity().getApplication().getApplicationContext(), "Needs to refresh the list" , Toast.LENGTH_LONG);
                toast.show();
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if(resultCode == -1){
                //String teamReturned=data.getStringExtra("team");
                //team=gson.fromJson(teamReturned, Team.class);
                GetMySoccerCentersTask task = new GetMySoccerCentersTask();
                task.execute();
                getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            }
            if (resultCode == 2) {
                //Write your code if there's no result
            }
        }
    }//onActivityResult
    /*
    public interface AsyncListener{
        void postTaskMethod();
    }
    */



}
