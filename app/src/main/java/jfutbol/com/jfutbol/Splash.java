package jfutbol.com.jfutbol;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import jfutbol.com.jfutbol.model.Team;
import jfutbol.com.jfutbol.model.User;
import jfutbol.com.jfutbol.singleton.Utils;
import jfutbol.com.jfutbol.singleton.singleton_token;


public class Splash  extends Activity {

    User user;
    Team[]myTeams;
    String url_host_connection;
    String url_host_connection_secure;
    singleton_token token = singleton_token.getInstance();

    Bundle params, paramsError;
    Boolean isOpenedFromNotification = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        params=getIntent().getExtras();
        paramsError = new Bundle();

        url_host_connection=getResources().getString(R.string.url_host_connection).toString();
        url_host_connection_secure=getResources().getString(R.string.url_host_connection).toString()+"/api";

        if(params!=null)
            isOpenedFromNotification  = params.getBoolean("isOpenedFromNotification");

        verifySessionActive();

    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    public void verifySessionActive () {

        String email="";
        String password="";
        try
        {
            BufferedReader fin =
                    new BufferedReader(
                            new InputStreamReader(
                                    openFileInput("sessionActive.json")));

            String strJson = fin.readLine();
            Gson gson = new GsonBuilder()
                    .create();
            JSONObject jsonobj= new JSONObject(strJson);
            email=jsonobj.getString("email");
            password=jsonobj.getString("password");

            if(!email.isEmpty() && !password.isEmpty()) {
                validateUserTask task = new validateUserTask();
                task.execute(new String[]{email.trim(), password.trim()});
            }
            else{
                goToLogin();
                Log.e("Login", "Invalid Credentials");
            }

            fin.close();
        }
        catch (Exception ex)
        {
            Log.e("Login", "Error while reading the file from memory");
            goToLogin();
        }
    }

    public void goToLogin()
    {
        Intent login = new Intent(Splash.this, Login.class);
        login.putExtras(paramsError);
        startActivity(login);
        finish();
    }

    private class validateUserTask extends AsyncTask<String, Void, JSONObject> {
        String response = null;
        private final ProgressDialog dialog = new ProgressDialog(Splash.this);
        JSONObject jsonObject;
        // can use UI thread here
        protected void onPreExecute() {
            this.dialog.setMessage(getResources().getString(R.string.processing));
            this.dialog.setCancelable(false);
            this.dialog.show();
        }

        protected JSONObject doInBackground(String... params) {
            final HttpClient httpClient= new DefaultHttpClient();
            final HttpGet httpGetLogin= new HttpGet(url_host_connection+"/user/"+params[0]+"/"+params[1]+"");
            try {
                try {
                    HttpResponse httpResponseLogin = httpClient.execute(httpGetLogin);
                    String jsonResultLogin = inputStreamToString(httpResponseLogin.getEntity().getContent()).toString();
                    JSONObject object = new JSONObject(jsonResultLogin);
                    Gson gsonLogin = new GsonBuilder()
                            .setDateFormat("yyyy-MM-dd")
                            .create();
                    jsonObject= object;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }catch (RuntimeException e) {
                e.printStackTrace();
            }
            return jsonObject;
        }//close doInBackground

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }
            if(jsonObject!=null) {
                try {
                    if (jsonObject.getInt("code") == 2000) {
                        getUserInfo task = new getUserInfo();
                        task.execute(new String[]{jsonObject.getString("message")});
                    } else {
                        try {
                            String message = getResources().getString(R.string.user_not_registered);
                            //showToastDialog(message,3);
                            paramsError.putBoolean("errorOnSplash", true);
                            paramsError.putString("errorMessage", message);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else{
                String message = getResources().getString(R.string.not_server_connection);
                //showToastDialog(message,3);
                paramsError.putBoolean("errorOnSplash", true);
                paramsError.putString("errorMessage", message);
                //ShowErrorMessage(message);
                goToLogin();
                Log.e("Login", "not server connection");
            }
        }//close onPostExecute
    }

    private class getUserInfo extends AsyncTask<String, Void, User> {
        protected User doInBackground(String... params) {
            final HttpClient httpClient= new DefaultHttpClient();
            final HttpGet httpGetLogin= new HttpGet(url_host_connection_secure+"/check/");
            httpGetLogin.addHeader("x-access-token",params[0]);
            try {
                try {
                    HttpResponse httpResponseLogin = httpClient.execute(httpGetLogin);
                    String jsonResultLogin = inputStreamToString(httpResponseLogin.getEntity().getContent()).toString();
                    JSONObject object = new JSONObject(jsonResultLogin);
                    Gson gsonLogin = new GsonBuilder()
                            .setDateFormat("yyyy-MM-dd")
                            .create();
                    user=gsonLogin.fromJson(object.toString(),User.class);
                    token.setUser_token(params[0]);
                    //user.setToken(params[0]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }catch (RuntimeException e) {
                e.printStackTrace();
            }
            return user;
        }//close doInBackground

        @Override
        protected void onPostExecute(User user) {
            if(user!=null) {
                if (user.getId() > 0) {
                    String message;
                    Intent nextIntent = null;
                    Bundle params = new Bundle();
                    Gson gson = new GsonBuilder()
                            .setDateFormat("yyyy-MM-dd")
                            .create();
                    String jsonUser = gson.toJson(user);
                    String jsonMyTeams = gson.toJson(myTeams);
                    params.putString("user", jsonUser.toString());
                    params.putString("myTeams", jsonMyTeams.toString());

                    if (user.getActivationCode().compareTo("") != 0) {
                        //message = getResources().getString(R.string.activateAccount);
                        //showToastDialog(message,2);
                        nextIntent = new Intent(Splash.this, Code_Activation.class);
                        nextIntent.putExtras(params);
                        startActivityForResult(nextIntent, 2);
                    } else {
                        //message = getResources().getString(R.string.welcome) + ": " + user.getFirstName() + " " + user.getLastName();
                        //showToastDialog(message,1);
                        nextIntent = new Intent(Splash.this, MainMenu.class);
                        params.putBoolean("isOpenedFromNotification", isOpenedFromNotification);
                        nextIntent.putExtras(params);
                        startActivity(nextIntent);
                        finish();
                    }
                } else {
                    try {
                        String message = getResources().getString(R.string.user_not_registered);
                        //showToastDialog(message, 3);
                        paramsError.putBoolean("errorOnSplash", true);
                        paramsError.putString("errorMessage", message);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            else{
                String message = getResources().getString(R.string.not_server_connection);
                paramsError.putBoolean("errorOnSplash", true);
                paramsError.putString("errorMessage", message);
                //showToastDialog(message,3);
                Log.e("Login", "not server connection");
            }
        }//close onPostExecute
    }

    public void showToastDialog(String message, int type){

        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast_layout,(ViewGroup) findViewById(R.id.custom_toast_layout));
        TextView text = (TextView) layout.findViewById(R.id.lbMessage);
        ImageView toastImage= (ImageView) layout.findViewById(R.id.toastImage);
        text.setText(message);
        if(type==1)
            toastImage.setImageResource(R.drawable.toast_success);
        if(type==2)
            toastImage.setImageResource(R.drawable.toast_warning);
        if(type==3)
            toastImage.setImageResource(R.drawable.toast_error);
        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.BOTTOM, 0, 0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
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

        if (requestCode == 2) {
            if(resultCode == RESULT_OK){
                validateUserTask task = new validateUserTask();
                task.execute(new String[]{user.getEmail().trim(), user.getPassword().trim()});
            }
        }
    }//onActivityResult


}
