package jfutbol.com.jfutbol;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
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

import jfutbol.com.jfutbol.model.mNotifications;
import jfutbol.com.jfutbol.model.User;
import jfutbol.com.jfutbol.singleton.singleton_token;

public class Notifications extends Fragment {

    private String userJson;
    User user;
    mNotifications[] notifications;
    View root;
    String url_host_connection;
    String url_host_connection_secure;
    singleton_token token = singleton_token.getInstance();
    String requestNotification;
    SwipeRefreshLayout swipeContainer;

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
        root = inflater.inflate(R.layout.fragment_notifications, container, false);
        swipeContainer = (SwipeRefreshLayout) root.findViewById(R.id.swipe_container);
        swipeContainer.setProgressViewOffset(false, 0,(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics()));
        swipeContainer.setRefreshing(true);
        GetNotificationsPlayerTask task = new GetNotificationsPlayerTask();
        task.execute();
        return root;
    }

    private class GetNotificationsPlayerTask extends AsyncTask<User, Void, mNotifications[]> {
        protected void onPreExecute() {
            //swipeContainer.setRefreshing(true);
        }
        protected mNotifications[] doInBackground(User... params) {
            final HttpClient httpClient = new DefaultHttpClient();
            JSONObject responseJSON = null;
            final HttpGet httpGetMyTeams= new HttpGet(url_host_connection_secure+"/playersNotifications/"+user.getId());
            httpGetMyTeams.addHeader("x-access-token",token.getUser_token());
            try {
                try {
                    HttpResponse httpResponseMyTeams = httpClient.execute(httpGetMyTeams);
                    String jsonResultNotifications = inputStreamToString(httpResponseMyTeams.getEntity().getContent()).toString();
                    Gson gsonMyTeams = new GsonBuilder()
                            .setDateFormat("yyyy-MM-dd'T'HH:mm")
                            .create();
                    notifications= gsonMyTeams.fromJson(jsonResultNotifications, mNotifications[].class);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }catch (RuntimeException e)
            {
                e.printStackTrace();
            }
            return notifications;
        }
        @Override
        protected void onPostExecute(mNotifications[] notifications) {

            final ListView list = (ListView) root.findViewById(R.id.listViewNotifications);

            List<mNotifications> notificationlist = Arrays.asList(notifications);
            notificationListViewAdapter listAdapter = new notificationListViewAdapter(getActivity(), notificationlist );
            list.setAdapter(listAdapter);

            list.setItemsCanFocus(false);
            list.setLongClickable(true);

            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    try {
                        mNotifications notificationSelected = new mNotifications();
                        String jsonNotificationSelected = "";
                        notificationSelected = (mNotifications) parent.getItemAtPosition(position);
                        Gson gson = new GsonBuilder()
                                .setDateFormat("dd-mm-yyyy h:mm a")
                                .create();
                        jsonNotificationSelected = gson.toJson(notificationSelected);

                        confirmNotification(notificationSelected);
                        /*
                        UpdateNotificationStatusTask task = new UpdateNotificationStatusTask();
                        task.execute(notificationSelected);
                        */

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
                    GetNotificationsPlayerTask task = new GetNotificationsPlayerTask();
                    task.execute();
                }
            });
            swipeContainer.setRefreshing(false);
        }//close onPostExecute
    }

    public void confirmNotification(final mNotifications notificationSelected) throws Exception {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        String message=notificationSelected.getNotification();
        int type=notificationSelected.getType();
        final String keyId=notificationSelected.getKeyId()+"";
        final String rejected="0";
        final String pending="1";
        final String approved="2";

        //swipeContainer.setRefreshing(true);

        builder.setMessage(message);
        if(type==1) {
            builder.setNeutralButton("STILL PENDING", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    //swipeContainer.setRefreshing(false);
                }
            });
            builder.setNegativeButton("REJECT", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    UpdateTeamPlayerRequestStatusTask task1 = new UpdateTeamPlayerRequestStatusTask();
                    task1.execute(new String[]{keyId, rejected});
                    UpdateNotificationStatusTask task2 = new UpdateNotificationStatusTask();
                    task2.execute(notificationSelected);
                    dialog.dismiss();
                }
            });
            builder.setPositiveButton("APPROVE", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    UpdateTeamPlayerRequestStatusTask task1 = new UpdateTeamPlayerRequestStatusTask();
                    task1.execute(new String[]{keyId, approved});
                    UpdateNotificationStatusTask task2 = new UpdateNotificationStatusTask();
                    task2.execute(notificationSelected);
                    dialog.dismiss();
                }
            });
        }
        if(type==2) {
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    UpdateNotificationStatusTask task = new UpdateNotificationStatusTask();
                    task.execute(notificationSelected);
                    dialog.dismiss();
                }
            });
        }
        if(type==3) {
            builder.setNeutralButton("STILL PENDING", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    //swipeContainer.setRefreshing(false);
                }
            });
            builder.setNegativeButton("REJECT", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    UpdateMatchPlayerRequestStatusTask task1 = new UpdateMatchPlayerRequestStatusTask();
                    task1.execute(new String[]{keyId, rejected});
                    UpdateNotificationStatusTask task2 = new UpdateNotificationStatusTask();
                    task2.execute(notificationSelected);
                    dialog.dismiss();
                }
            });
            builder.setPositiveButton("APPROVE", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    UpdateMatchPlayerRequestStatusTask task1 = new UpdateMatchPlayerRequestStatusTask();
                    task1.execute(new String[]{keyId, approved});
                    UpdateNotificationStatusTask task2 = new UpdateNotificationStatusTask();
                    task2.execute(notificationSelected);
                    dialog.dismiss();
                }
            });
        }
        if(type==4) {
            builder.setNeutralButton("STILL PENDING", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    //swipeContainer.setRefreshing(false);
                }
            });
            builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    UpdateFieldDisponibilityRequestStatusTask task1 = new UpdateFieldDisponibilityRequestStatusTask();
                    task1.execute(new String[]{keyId, rejected, user.getId().toString()});
                    UpdateNotificationStatusTask task2 = new UpdateNotificationStatusTask();
                    task2.execute(notificationSelected);

                    dialog.dismiss();
                }
            });
            builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    UpdateFieldDisponibilityRequestStatusTask task1 = new UpdateFieldDisponibilityRequestStatusTask();
                    task1.execute(new String[]{keyId, approved, user.getId().toString()});
                    UpdateNotificationStatusTask task2 = new UpdateNotificationStatusTask();
                    task2.execute(notificationSelected);
                    dialog.dismiss();
                }
            });
        }

        if(type==5) {
            builder.setNeutralButton("STILL PENDING", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    //swipeContainer.setRefreshing(false);
                }
            });
            builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    UpdateTeamdDisponibilityRequestStatusTask task1 = new UpdateTeamdDisponibilityRequestStatusTask();
                    task1.execute(new String[]{keyId, rejected, user.getId().toString()});
                    UpdateNotificationStatusTask task2 = new UpdateNotificationStatusTask();
                    task2.execute(notificationSelected);
                    dialog.dismiss();
                }
            });
            builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    UpdateTeamdDisponibilityRequestStatusTask task1 = new UpdateTeamdDisponibilityRequestStatusTask();
                    task1.execute(new String[]{keyId, approved, user.getId().toString()});
                    UpdateNotificationStatusTask task2 = new UpdateNotificationStatusTask();
                    task2.execute(notificationSelected);
                    dialog.dismiss();
                }
            });
        }

        builder.show();
    }

    private class UpdateTeamPlayerRequestStatusTask extends AsyncTask<String, Void, JSONObject> {
        protected void onPreExecute() {
            swipeContainer.setRefreshing(true);
        }
        protected JSONObject doInBackground(String... params) {
            final HttpClient httpClient = new DefaultHttpClient();
            JSONObject responseJSON = null;
            final HttpGet httpGet= new HttpGet(url_host_connection_secure+"/team/playerRequest/"+params[0]+"/"+params[1]);
            httpGet.addHeader("x-access-token",token.getUser_token());
            try {
                try {
                    HttpResponse httpResponse = httpClient.execute(httpGet);
                    String jsonResult = inputStreamToString(httpResponse.getEntity().getContent()).toString();
                    Gson gson = new GsonBuilder()
                            .setDateFormat("yyyy-MM-dd")
                            .create();
                    responseJSON= new JSONObject(jsonResult);

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


        }//close onPostExecute
    }

    private class UpdateMatchPlayerRequestStatusTask extends AsyncTask<String, Void, JSONObject> {
        protected void onPreExecute() {
            swipeContainer.setRefreshing(true);
        }
        protected JSONObject doInBackground(String... params) {
            final HttpClient httpClient = new DefaultHttpClient();
            JSONObject responseJSON = null;
            final HttpGet httpGet= new HttpGet(url_host_connection_secure+"/match/playerRequest/"+params[0]+"/"+params[1]);
            httpGet.addHeader("x-access-token",token.getUser_token());
            try {
                try {
                    HttpResponse httpResponse = httpClient.execute(httpGet);
                    String jsonResult = inputStreamToString(httpResponse.getEntity().getContent()).toString();
                    Gson gson = new GsonBuilder()
                            .setDateFormat("yyyy-MM-dd")
                            .create();
                    responseJSON= new JSONObject(jsonResult);

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


        }//close onPostExecute
    }

    private class UpdateNotificationStatusTask extends AsyncTask<mNotifications, Void, JSONObject> {
       protected void onPreExecute() {
           swipeContainer.setRefreshing(true);
        }
        protected JSONObject doInBackground(mNotifications... params) {
            final HttpClient httpClient = new DefaultHttpClient();
            JSONObject responseJSON = null;
            final HttpGet httpGetMyTeams= new HttpGet(url_host_connection_secure+"/playersNotificationStatus/"+params[0].getId());
            httpGetMyTeams.addHeader("x-access-token",token.getUser_token());
            try {
                try {
                    HttpResponse httpResponseMyTeams = httpClient.execute(httpGetMyTeams);
                    String jsonResultNotifications = inputStreamToString(httpResponseMyTeams.getEntity().getContent()).toString();
                    Gson gson = new GsonBuilder()
                            .setDateFormat("yyyy-MM-dd")
                            .create();
                    responseJSON= new JSONObject(jsonResultNotifications);

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

            GetNotificationsPlayerTask task = new GetNotificationsPlayerTask();
            task.execute();

        }//close onPostExecute
    }

    private class UpdateFieldDisponibilityRequestStatusTask extends AsyncTask<String, Void, JSONObject> {
        protected void onPreExecute() {
            swipeContainer.setRefreshing(true);
        }
        protected JSONObject doInBackground(String... params) {
            final HttpClient httpClient = new DefaultHttpClient();
            JSONObject responseJSON = null;
            final HttpGet httpGet= new HttpGet(url_host_connection_secure+"/disponibilityField/ownerRequest/"+params[0]+"/"+params[1]+"/"+params[2]);
            httpGet.addHeader("x-access-token",token.getUser_token());
            try {
                try {
                    HttpResponse httpResponse = httpClient.execute(httpGet);
                    String jsonResult = inputStreamToString(httpResponse.getEntity().getContent()).toString();
                    Gson gson = new GsonBuilder()
                            .setDateFormat("yyyy-MM-dd")
                            .create();
                    responseJSON= new JSONObject(jsonResult);

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


        }//close onPostExecute
    }

    private class UpdateTeamdDisponibilityRequestStatusTask extends AsyncTask<String, Void, JSONObject> {
        protected void onPreExecute() {
            swipeContainer.setRefreshing(true);
        }
        protected JSONObject doInBackground(String... params) {
            final HttpClient httpClient = new DefaultHttpClient();
            JSONObject responseJSON = null;
            final HttpGet httpGet= new HttpGet(url_host_connection_secure+"/disponibilityField/teamOwnerRequest/"+params[0]+"/"+params[1]+"/"+params[2]);
            httpGet.addHeader("x-access-token",token.getUser_token());
            try {
                try {
                    HttpResponse httpResponse = httpClient.execute(httpGet);
                    String jsonResult = inputStreamToString(httpResponse.getEntity().getContent()).toString();
                    Gson gson = new GsonBuilder()
                            .setDateFormat("yyyy-MM-dd")
                            .create();
                    responseJSON= new JSONObject(jsonResult);

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

}
