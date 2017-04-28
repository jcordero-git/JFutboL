package jfutbol.com.jfutbol;

import jfutbol.com.jfutbol.model.NavDrawerItem;
import jfutbol.com.jfutbol.adapter.NavDrawerListAdapter;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.app.AlertDialog;

import android.widget.EditText;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
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
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import jfutbol.com.jfutbol.model.PlayerSkills;
import jfutbol.com.jfutbol.model.Team;
import jfutbol.com.jfutbol.model.User;
import jfutbol.com.jfutbol.model.mCantons;
import jfutbol.com.jfutbol.model.mProvinces;
import jfutbol.com.jfutbol.singleton.Utils;
import jfutbol.com.jfutbol.singleton.singleton_token;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;


public class MainMenu extends Activity  implements SearchView.OnQueryTextListener{

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    // nav drawer title
    private CharSequence mDrawerTitle;

    // used to store app title
    private CharSequence mTitle;

    // slide menu items
    private String[] navMenuTitles;
    private TypedArray navMenuIcons;

    private Toolbar bar;
    private ArrayList<NavDrawerItem> navDrawerItems;
    private NavDrawerListAdapter adapter;

    String url_host_connection;
    String url_host_connection_secure;
    singleton_token token = singleton_token.getInstance();

    Bundle params;
    EditText userName;
    String userJson;
    User user;
    String myTeamsJson;
    Team[] myTeams;

    int viewPosition=0;

    String urlPublicImages;

    String apkVersion;
    String newApkVersion;
    String urlApkUpdate;

    private SearchView mSearchView;
    AQuery aq;

    View coordinatorLayout;

// variables to filter soccer fields
    EditText txtFields[];
    mProvinces[] provinces;
    String provinceNameSelected;
    String cantonNameSelected;
    Boolean updateCantonSelected;
    mCantons[] cantons;
    mCantons canton=new mCantons();
// variables to filter soccer fields

//variables to filter players
    Boolean doRegionFilter=false;
    PlayerSkills[] skills;
    boolean[] checkedItems;
    CharSequence[] skillsChar;
    ArrayList<String> mSelectedItems;
//variables to filter players

    Gson gson;


//ShowCase
    final String MENU_SHOWCASE = "mainMenu_menu_ShowCase";
    final String FILTER_MY_MATCHES_SHOWCASE = "mainMenu_myMathes_filter_ShowCase";
    final String FILTER_PLAYERS_SHOWCASE = "mainMenu_player_filter_ShowCase";
    final String NOTIFICATION_SHOWCASE = "mainMenu_notification_ShowCase";
//ShowCase



// GCM Google Messaging
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = "MainMenu";
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    Boolean isOpenedFromNotification = false;
// GCM Google Messaging

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        url_host_connection=getResources().getString(R.string.url_host_connection).toString();
        url_host_connection_secure=getResources().getString(R.string.url_host_connection).toString()+"/api";
        setContentView(R.layout.activity_main_menu);

        apkVersion="0.0.1";
        urlApkUpdate= url_host_connection+"/apks/JFutboL.apk";

        params=getIntent().getExtras();
        urlPublicImages= url_host_connection+"/images/";


        coordinatorLayout = findViewById(R.id.coordinatorLayout);
        //userName=(EditText)findViewById(R.id.txtUserName);

        gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd")
                .create();

        //viewPosition  = params.getInt("viewPosition");

        isOpenedFromNotification  = params.getBoolean("isOpenedFromNotification");

        userJson  = params.getString("user");
        user=gson.fromJson(userJson, User.class);

        String message = getResources().getString(R.string.welcome) + ": " + user.getFirstName() + " " + user.getLastName();
        Utils.ShowMessage(coordinatorLayout, message, 1);

        myTeamsJson  = params.getString("myTeams");
        myTeams= gson.fromJson(myTeamsJson, Team[].class);

        mTitle = mDrawerTitle = getTitle();

        if(user.getUserType()==1) {
            // load slide menu items PLAYER
            navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items_player);
            // nav drawer icons from resources PLAYER
            navMenuIcons = getResources().obtainTypedArray(R.array.nav_drawer_icons_player);
        }
        if(user.getUserType()==2) {
            // load slide menu items OWNER
            navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items_owner_field);
            // nav drawer icons from resources OWNER
            navMenuIcons = getResources().obtainTypedArray(R.array.nav_drawer_icons_owner_field);
        }

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.list_slidermenu);

        navDrawerItems = new ArrayList<NavDrawerItem>();

        for(int i=0;i<navMenuTitles.length;i++)
            navDrawerItems.add(new NavDrawerItem(navMenuTitles[i],navMenuIcons.getResourceId(i,-1)));



        /*
        // SocialMatch
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[0], navMenuIcons.getResourceId(0, -1)));
        // Notifications
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[1], navMenuIcons.getResourceId(1, -1)));
        // My Teams
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[2], navMenuIcons.getResourceId(2, -1)));
        // Find Players
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[3], navMenuIcons.getResourceId(3, -1)));
        // Matches with counter
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[4], navMenuIcons.getResourceId(4, -1)));
        // My Account
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[5], navMenuIcons.getResourceId(5, -1)));
        // Settings
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[6], navMenuIcons.getResourceId(6, -1)));
        */

        mDrawerList.setOnItemClickListener(new SlideMenuClickListener());
        // setting the nav drawer list adapter
        adapter = new NavDrawerListAdapter(getApplicationContext(), navDrawerItems);
        mDrawerList.setAdapter(adapter);


        // enabling action bar app icon and behaving it as toggle button
       // getSupportActionBar().setDisplayHomeAsUpEnabled(true);
       // getSupportActionBar().setHomeButtonEnabled(true);

        FrameLayout root = (FrameLayout) findViewById(R.id.frame_container);

        /*
        LayoutInflater inflater = getLayoutInflater();
        ViewGroup header = (ViewGroup)inflater.inflate(R.layout.slide_header, mDrawerList, false);
        mDrawerList.addHeaderView(header, null, false);

        TextView lbUserName = (TextView) header.findViewById(R.id.lbFirstName);
        aq = new AQuery(null,root);
        aq.id(header.findViewById(R.id.imgMe)).image(url_host_connection+"/images/profile/"+user.getUserId()+".png", true, true);
        lbUserName.setText(user.getFirstName());
        */

        bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.settings_toolbar, root , false);
        root.addView(bar, 0); // insert at top

        bar.setNavigationIcon(R.drawable.main_menu);

        bar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer(mDrawerList);
            }
        });

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.main_menu, //nav menu toggle icon
                R.string.app_name, // nav drawer open - description for accessibility
                R.string.app_name // nav drawer close - description for accessibility
        ){
            public void onDrawerClosed(View view) {
               // getSupportActionBar().setTitle(mTitle);
                // calling onPrepareOptionsMenu() to show action bar icons
                bar.setTitle(mTitle);
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
               // getSupportActionBar().setTitle(mDrawerTitle);
                // calling onPrepareOptionsMenu() to hide action bar icons
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        if (savedInstanceState == null) {
            // on first time display view for first nav item
            if(isOpenedFromNotification)
                viewPosition=1;
            displayView(viewPosition);
        }
        ActivateNotificationThread();
        showShowCase(bar.getChildAt(1), "Use this menu to navigate under all app options", MENU_SHOWCASE);

        suscribePushNotification(user.getId(), true);


//method to use on player filter
        //loadSkills();
    }


    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    public void ActivateNotificationThread() {
        new Thread (){
            public void run()
            { int i=0;
                while(1!=0)
                {
                    try {
                        i++;
                        GetNotificationsPlayerTask task = new GetNotificationsPlayerTask();
                        task.execute();
                        sleep(10000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(MainMenu.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }.start();
    }

    /**
     * Slide menu item click listener
     * */
    private class SlideMenuClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            // display view for selected nav drawer item
            displayView(position);
        }
    }


/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_menu, menu);
        return true;
    }
*/


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // toggle nav drawer on selecting action bar app icon/title
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return false;
        // Handle action bar actions click
        /*
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent mainMenu = new Intent(MainMenu.this, SettingsActivity.class);
                startActivity(mainMenu);
                return true;
            case R.id.action_logout:
                DestroySessionActive();
                Intent login = new Intent(MainMenu.this, Login.class);
                startActivity(login);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
        */
    }



    /*

    private boolean DestroySessionActive()  {
        try
        {
            OutputStreamWriter fout=
                    new OutputStreamWriter(
                            openFileOutput("sessionActive.json", Context.MODE_PRIVATE));

            JSONObject jsonobj= new JSONObject();
            jsonobj.put("email", "");
            jsonobj.put("password", "");
            fout.write(jsonobj.toString());
            fout.close();
            return true;
        }
        catch (Exception ex2)
        {
            Log.e("Ficheros", "Error al escribir fichero a memoria interna");
            return false;
        }
    }

    */
    /***
     * Called when invalidateOptionsMenu() is triggered
     */
    /*
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // if nav drawer is opened, hide the action items
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
        menu.findItem(R.id.action_logout).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }
    */

    /**
     * Diplaying fragment view for selected nav drawer list item
     * */

    private void displayView(int position) {
        // update the main content by replacing fragments
        Fragment fragment = null;
        String menuToDisplay="";
        bar.getMenu().clear();
        for (int i = 0; i < navDrawerItems.size(); i++){
            if (navDrawerItems.get(position).getTitle().equals(navMenuTitles[i].toString())) {
                menuToDisplay = navMenuTitles[i].toString();
                break;
            }
    }
        switch (menuToDisplay) {
            case "JFUTBOL":
                fragment = new Home();
                Bundle args_home = new Bundle();
                args_home.putString("user", userJson);
                fragment.setArguments(args_home);
                bar.inflateMenu(R.menu.menu_home_menu);

                bar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        if(menuItem.getTitle().equals(getResources().getString(R.string.refresh).toString())) {
                            Home.refreshMatches();
                        }
                        return false;
                    }
                });
                break;
            case "NOTIFICATIONS":
                fragment = new Notifications();
                Bundle args_notifications = new Bundle();
                args_notifications.putString("user", userJson);
                fragment.setArguments(args_notifications);
                bar.inflateMenu(R.menu.menu_main_menu);
                showShowCase(mDrawerList.getAdapter().getView(0,null,null), "Here you can see all notifications about Teams and Soccer Matches", NOTIFICATION_SHOWCASE);

                break;
            case "SOCCER CENTERS":

                fragment = new MySoccerCenters();
                Bundle args_my_soccer_centers = new Bundle();
                args_my_soccer_centers.putString("user", userJson);
                fragment.setArguments(args_my_soccer_centers);
                bar.inflateMenu(R.menu.menu_my_soccer_centers);
                MenuItem searchSoccerCenterItem = bar.getMenu().findItem(R.id.action_search);
                mSearchView = (SearchView) searchSoccerCenterItem.getActionView();
                SearchManager searchManagerMySoccerCenters = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
                if (searchManagerMySoccerCenters != null) {
                    SearchableInfo info = searchManagerMySoccerCenters.getSearchableInfo(getComponentName());
                    mSearchView.setSearchableInfo(info);
                }
                mSearchView.setOnQueryTextListener(this);

                break;
            case "MY TEAMS":
                fragment = new MyTeams();
                Bundle args_my_teams = new Bundle();
                args_my_teams.putString("user", userJson);
                args_my_teams.putString("myTeams",myTeamsJson);
                fragment.setArguments(args_my_teams);
                bar.inflateMenu(R.menu.menu_search_myteams);
                MenuItem searchItem = bar.getMenu().findItem(R.id.action_search);
                mSearchView = (SearchView) searchItem.getActionView();
                SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
                if (searchManager != null) {
                    SearchableInfo info = searchManager.getSearchableInfo(getComponentName());
                    mSearchView.setSearchableInfo(info);
                }
                mSearchView.setOnQueryTextListener(this);
                break;
            case "PLAYERS":
                loadSkills();
                provinceNameSelected=user.getProvinceName();
                cantonNameSelected=user.getCantonName();
                canton.setProvinceId(user.getProvinceId());
                canton.setId(user.getCantonId());
                fragment = new Players();
                Bundle args_players = new Bundle();
                args_players.putString("user", userJson);
                fragment.setArguments(args_players);
                bar.inflateMenu(R.menu.menu_search_player);

                bar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        if(menuItem.getTitle().equals(getResources().getString(R.string.refresh).toString())) {
                            //SearchSoccerField.refreshSoccerFields(canton.getProvinceId(), canton.getCantonId(), date, startHourSelected+":"+startMinutesSelected , endHourSelected+":"+endMinutesSelected );
                            Players.refreshPlayers(canton.getProvinceId(), canton.getId());
                            Players.listAdapter.filter("", mSelectedItems);
                        }
                        if(menuItem.getTitle().equals(getResources().getString(R.string.filter).toString())) {
                            showPopupPlayersFilter();
                        }
                        return false;
                    }
                });
                showShowCase(bar.findViewById(R.id.filter), "You can use this button to filter by province, cantons and skills", FILTER_PLAYERS_SHOWCASE );

                break;
            case "SOCCER FIELDS":
                provinceNameSelected=user.getProvinceName();
                cantonNameSelected=user.getCantonName();
                canton.setProvinceId(user.getProvinceId());
                canton.setId(user.getCantonId());
                fragment = new SearchSoccerField();
                Bundle args_soccer_field = new Bundle();
                args_soccer_field.putString("user", userJson);
                fragment.setArguments(args_soccer_field);
                bar.inflateMenu(R.menu.menu_search_soccer_field);

                bar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        if(menuItem.getTitle().equals(getResources().getString(R.string.refresh).toString())) {
                            //SearchSoccerField.refreshSoccerFields(canton.getProvinceId(), canton.getCantonId(), date, startHourSelected+":"+startMinutesSelected , endHourSelected+":"+endMinutesSelected );
                            SearchSoccerField.refreshSoccerFields(canton.getProvinceId(), canton.getId() );

                        }
                        if(menuItem.getTitle().equals(getResources().getString(R.string.filter).toString())) {
                            showPopupSoccerFieldFilter();
                        }
                        return false;
                    }
                });
                showShowCase(bar.findViewById(R.id.filter), "You can use this button to filter by province and cantons", FILTER_MY_MATCHES_SHOWCASE );
                break;
            case "MY MATCHES":
                fragment = new MyMatches();
                Bundle args_my_matches = new Bundle();
                args_my_matches.putString("user", userJson);
                fragment.setArguments(args_my_matches);
                bar.inflateMenu(R.menu.menu_my_matches);
                bar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        if(menuItem.getTitle().equals(getResources().getString(R.string.refresh).toString())) {
                            MyMatches.refreshMatches();
                        }
                        return false;
                    }
                });
                break;
            case "MY ACCOUNT":
                fragment = new me();
                Bundle args_my_account = new Bundle();
                args_my_account.putString("user", userJson);
                fragment.setArguments(args_my_account);
                bar.inflateMenu(R.menu.menu_me);

                bar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        if (menuItem.getTitle().equals(getResources().getString(R.string.edit).toString()))
                        {
                            GoToEditUser();
                        }
                        if(menuItem.getTitle().equals(getResources().getString(R.string.logout).toString())) {
                            logout();
                        }
                        return false;
                    }
                });
                break;
            case "SETTINGS":
                Intent settings = new Intent(MainMenu.this, SettingsActivity.class);
                startActivity(settings);
                break;
            default:

                break;
        }


       /*
        if(navDrawerItems.get(position).getTitle()==navMenuTitles[0].toString())
        {

        }
        if(navDrawerItems.get(position).getTitle()==navMenuTitles[1].toString())
        {

        }
        if(navDrawerItems.get(position).getTitle()==navMenuTitles[2].toString())
        {

        }
        if(navDrawerItems.get(position).getTitle()==navMenuTitles[3].toString())
        {

        }
        if(navDrawerItems.get(position).getTitle()==navMenuTitles[4].toString())
        {

        }
        if(navDrawerItems.get(position).getTitle()==navMenuTitles[5].toString())
        {

        }
        if(navDrawerItems.get(position).getTitle()==navMenuTitles[6].toString())
        {

        }
        */

        if (fragment != null) {
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.frame_container, fragment)
                    .addToBackStack(null)
                    .commit();

            mDrawerList.setItemChecked(position, true);
            mDrawerList.setSelection(position);
            setTitle(navMenuTitles[position]);
            //mDrawerLayout.closeDrawer(mDrawerList);
        } else {
            // error in creating fragment
            Log.e("MainActivity", "Error in creating fragment");
        }
        mDrawerLayout.closeDrawer(mDrawerList);


    }

    /*
    private void displayView(int position) {
        // update the main content by replacing fragments
        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = new Home();
                Bundle args_home = new Bundle();
                args_home.putString("user", userJson);
                fragment.setArguments(args_home);
                break;
            case 1:
                fragment = new Notifications();
                Bundle args_notifications = new Bundle();
                args_notifications.putString("user", userJson);
                fragment.setArguments(args_notifications);
                break;
            case 2:
                fragment = new MyTeams();
                Bundle args_my_teams = new Bundle();
                args_my_teams.putString("user", userJson);
                args_my_teams.putString("myTeams",myTeamsJson);
                fragment.setArguments(args_my_teams);
                break;
            case 3:

                break;
            case 4:

                break;
            case 5:
                fragment = new me();
                Bundle args_my_account = new Bundle();
                args_my_account.putString("user", userJson);
                fragment.setArguments(args_my_account);
                break;
            case 6:
                Intent mainMenu = new Intent(MainMenu.this, SettingsActivity.class);
                startActivity(mainMenu);
                break;

            default:
                break;
        }


        if (fragment != null) {
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.frame_container, fragment)
                    .addToBackStack(null)
                    .commit();

                mDrawerList.setItemChecked(position, true);
                mDrawerList.setSelection(position);
                setTitle(navMenuTitles[position]);
            //mDrawerLayout.closeDrawer(mDrawerList);
        } else {
            // error in creating fragment
            Log.e("MainActivity", "Error in creating fragment");
        }
        mDrawerLayout.closeDrawer(mDrawerList);

        GetNotificationsPlayerTask task = new GetNotificationsPlayerTask();
        task.execute();
    }
    */


    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        bar.setTitle(mTitle);
       // getSupportActionBar().setTitle(mTitle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    private class GetNotificationsPlayerTask extends AsyncTask<User, Void, Integer> {
               protected void onPreExecute() {
        }
        protected Integer doInBackground(User... params) {
            final HttpClient httpClient = new DefaultHttpClient();
            Integer notificationCounter=0;
            final HttpGet httpGetMyTeams= new HttpGet(url_host_connection_secure+"/playersNotificationsCount/"+user.getId());
            httpGetMyTeams.addHeader("x-access-token",token.getUser_token());
            try {
                try {
                    HttpResponse httpResponseMyTeams = httpClient.execute(httpGetMyTeams);
                    String jsonResultNotifications = inputStreamToString(httpResponseMyTeams.getEntity().getContent()).toString();
                    JSONObject object = new JSONObject(jsonResultNotifications);
                    notificationCounter= object.getInt("newNotifications");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }catch (RuntimeException e)
            {
                e.printStackTrace();
            }
            return notificationCounter;
        }
        @Override
        protected void onPostExecute(Integer newNotifications) {

            navDrawerItems.set(1,(new NavDrawerItem(navMenuTitles[1], navMenuIcons.getResourceId(1, -1), true, newNotifications+"")));

            adapter = new NavDrawerListAdapter(getApplicationContext(), navDrawerItems);
            mDrawerList.setAdapter(adapter);

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
        ArrayList<String> mSelectedItems = null;
        MyTeams.listAdapter.filter(newText, mSelectedItems);
        return false;
    }

    public boolean onQueryTextSubmit(String query) {
        ArrayList<String> mSelectedItems = null;
        MyTeams.listAdapter.filter(query, mSelectedItems);
        return false;
    }

    public void GoToEditUser() {
        Intent myAccount = new Intent(this, MyAccount.class);
        Bundle params=new Bundle();
        params.putString("user", me.userJson.toString() );
        myAccount.putExtras(params);
        startActivityForResult(myAccount, 1);
    }

    public void logout() {
        DestroySessionActive();
        suscribePushNotification(user.getId(), false);
        Intent login = new Intent(this, Login.class);
        startActivity(login);
        this.finish();
    }

    private boolean DestroySessionActive()  {
        try
        {
            OutputStreamWriter fout = new OutputStreamWriter(this.openFileOutput("sessionActive.json", Context.MODE_PRIVATE));
            JSONObject jsonobj= new JSONObject();
            jsonobj.put("email", "");
            jsonobj.put("password", "");
            fout.write(jsonobj.toString());
            fout.close();
            return true;
        }
        catch (Exception ex2)
        {
            Log.e("Ficheros", "Error al escribir fichero a memoria interna");
            return false;
        }
    }

//***************GCM Google*************************
@Override
    protected void onResume() {
    super.onResume();
    LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
            new IntentFilter(QuickstartPreferences.REGISTRATION_COMPLETE));
    }

    private void suscribePushNotification(int userId, Boolean subscribe) {
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(context);
                boolean sentToken = sharedPreferences
                        .getBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false);
                if (sentToken) {
                    //mInformationTextView.setText(getString(R.string.gcm_send_message));
                } else {
                    //mInformationTextView.setText(getString(R.string.token_error_message));
                }
            }
        };
        //mInformationTextView = (TextView) findViewById(R.id.informationTextView);

        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            intent.putExtra("userId", userId);
            intent.putExtra("subscribe", subscribe);
            startService(intent);
        }
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

//***************GCM Google*************************


//***********************Methods to filter players

    public void showPopupPlayersFilter() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainMenu.this);
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
                                    Players.user.setProvinceId(canton.getProvinceId());
                                    Players.user.setCantonId(canton.getId());
                                    Players.refreshPlayers(canton.getProvinceId(), canton.getId());
                                }
                                Players.listAdapter.filter("", mSelectedItems);
                                doRegionFilter=false;
                            }
                        }
                );
        builder.create();
        builder.show();
    }

    public void showPopupSkills() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainMenu.this);
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
                                Players.mSelectedItems=mSelectedItems;
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

    public void loadSkills(){
        final List<PlayerSkills> skillsList = Arrays.asList(user.getSkills());
        skillsChar = new CharSequence[skillsList.size()];
        for(int i=0;i<skillsList.size();i++)
        {
            skillsChar[i]=user.getSkills()[i].getSkillName();
        }
        checkedItems = new boolean[skillsChar.length];
        mSelectedItems = new ArrayList();
        for(int i=0;i<skillsChar.length;i++) {
            checkedItems[i]=true;
            mSelectedItems.add(skillsChar[i].toString());
        }
        Players.mSelectedItems=mSelectedItems;
    }

//***********************Methods to filter soccer field

    public void showPopupSoccerFieldFilter() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainMenu.this);
        // Get the layout inflater
        LayoutInflater inflater = getLayoutInflater();
        builder.setTitle("Filter");
        builder.setCancelable(false);
        builder.setIcon(R.drawable.filter_b);
        View v=inflater.inflate(R.layout.layout_soccer_field_filter, null);

        txtFields=new EditText[5];
        txtFields[0] = (EditText) v.findViewById(R.id.txtProvince);
        txtFields[1] = (EditText) v.findViewById(R.id.txtState);

        txtFields[0].setText(provinceNameSelected);
        txtFields[1].setText(cantonNameSelected);

        GetProvincesTask provincesTask = new GetProvincesTask();
        provincesTask.execute();

        updateCantonSelected=false;
        GetCantonsTask cantonsTask = new GetCantonsTask();
        cantonsTask.execute(canton.getProvinceId());

        builder.setView(v)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                SearchSoccerField.user.setProvinceId(canton.getProvinceId());
                                SearchSoccerField.user.setCantonId(canton.getId());
                                SearchSoccerField.refreshSoccerFields(canton.getProvinceId(), canton.getId() );
                            }
                        }
                );
        builder.create();
        builder.show();
    }

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
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainMenu.this);
                    builder.setTitle("Provinces");

                    builder.setItems(ProvincesChar,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    int proviceIdSelected = 0;
                                    for (int i = 0; i < provinces.length; i++) {
                                        if (ProvincesChar[which].toString().equals(provinces[i].getName())) {
                                            proviceIdSelected = provinces[i].getId();
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
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainMenu.this);
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

    public void showShowCase(View target, String message, final String SHOWCASE_ID ){
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if(resultCode == -1){
               me.refreshUserInfo();
               userJson=data.getStringExtra("user");
               user=gson.fromJson(userJson, User.class);
               String message=getResources().getString(R.string.user_updated_success);
               Utils.ShowMessage(coordinatorLayout, message, 1);
            }
            if (resultCode == 2) {
                //Write your code if there's no result
            }
        }
        if (requestCode == 2) {
            if(resultCode == -1){
                displayView(5);
                String message = "The match was set successfully.\nThe match is waiting for soccer field owner confirmation.";
                Utils.ShowMessage(coordinatorLayout, message, 1);
            }
        }
    }//onActivityResult

}
