package jfutbol.com.jfutbol;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
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
import java.io.OutputStreamWriter;

import jfutbol.com.jfutbol.model.Team;
import jfutbol.com.jfutbol.model.User;
import jfutbol.com.jfutbol.singleton.Utils;
import jfutbol.com.jfutbol.singleton.singleton_token;


public class Login extends Activity {

    EditText txtEmail,txtPassword;
    Button btnLogin;
    TextView linkUserRegister;

    User user;
    private String userJson;
    Team []myTeams;
    String url_host_connection;
    String url_host_connection_secure;
    singleton_token token = singleton_token.getInstance();
    String processingStatus="";
    Boolean errorOnSplash = false;

    String apkVersion="";
    String newApkVersion="";
    String urlApkUpdate="";
    Bundle params;

    View coordinatorLayout;

    Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd")
            .create();

    View.OnClickListener mOnClickListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        url_host_connection=getResources().getString(R.string.url_host_connection).toString();
        url_host_connection_secure=getResources().getString(R.string.url_host_connection).toString()+"/api";

        apkVersion="0.0.1";
        urlApkUpdate= url_host_connection+"/apks/JFutboL.apk";

        coordinatorLayout = findViewById(R.id.RLLogin);

        if(getIntent().hasExtra("errorOnSplash")) {
            params = getIntent().getExtras();
            errorOnSplash = params.getBoolean("errorOnSplash");
            String message = params.getString("errorMessage");
            if(errorOnSplash)
               Utils.ShowMessage(coordinatorLayout, message, 3);
        }

        user = new User();

        txtEmail = (EditText) findViewById(R.id.txtEmail);
        txtPassword = (EditText) findViewById(R.id.txtPassword);
        linkUserRegister = (TextView) findViewById(R.id.linkUserRegister);
        btnLogin=(Button) findViewById(R.id.btLogin);

        txtEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                verifyEmptyCredentials();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        txtPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                verifyEmptyCredentials();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        txtPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    DoLogin();
                }
                return false;
            }
        });


        linkUserRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent userRegister = new Intent(Login.this, User_Register.class);
                //Bundle params=new Bundle();
                //params.putString("userName",result);
                //mainMenu.putExtras(params);
                startActivityForResult(userRegister, 1);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });


        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              DoLogin();
            }
        });


       // verifyApkVersion();
      //  verifySessionActive();
    }

    public void DoLogin(){
        try {
            // try {
            validateUserTask task = new validateUserTask();
            task.execute(new String[]{txtEmail.getText().toString().trim(), txtPassword.getText().toString().trim()});

            Utils.HideKeyBoard(getApplicationContext(), Login.this);

                     /*
                     HttpResponse httpResponse = httpClient.execute(httpPost);
                     String jsonResult = inputStreamToString(httpResponse.getEntity().getContent()).toString();
                     JSONObject object = new JSONObject(jsonResult);
                     String name = object.getString("username");
                     showDialog(name);
                     */
                /* } catch (JSONException e)
                     {
                         e.printStackTrace();
                     }
                     */
        } catch (Exception e) {
            e.printStackTrace();
        }
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

            fin.close();
        }
        catch (Exception ex)
        {
           Log.e("Ficheros", "Error al leer fichero desde memoria interna");
        }
    }

    private boolean SaveNewSessionActive(String email, String password)  {
        try
        {
            OutputStreamWriter fout=
                    new OutputStreamWriter(
                            openFileOutput("sessionActive.json", Context.MODE_PRIVATE));

            JSONObject jsonobj= new JSONObject();
            jsonobj.put("email", email);
            jsonobj.put("password", password);
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

    private class validateUserTask extends AsyncTask<String, Void, JSONObject> {
        String response = null;
        private final ProgressDialog dialog = new ProgressDialog(Login.this);
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
                            Utils.ShowMessage(coordinatorLayout, message, 3);
                            //showToastDialog(message,3);
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
                Utils.ShowMessage(coordinatorLayout, message, 3);
                //showToastDialog(message,3);
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
                if (user.getUserId() > 0) {
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
                        nextIntent = new Intent(Login.this, Code_Activation.class);
                        nextIntent.putExtras(params);
                        startActivityForResult(nextIntent, 2);
                    } else {
                        //message = getResources().getString(R.string.welcome) + ": " + user.getFirstName() + " " + user.getLastName();
                        //showToastDialog(message,1);
                        nextIntent = new Intent(Login.this, MainMenu.class);
                        nextIntent.putExtras(params);
                        startActivity(nextIntent);
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        SaveNewSessionActive(user.getEmail(), user.getPassword());
                        finish();
                    }
                } else {
                    try {
                        String message = getResources().getString(R.string.user_not_registered);
                        Utils.ShowMessage(coordinatorLayout, message, 3);
                        //showToastDialog(message,3);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            else{
                String message = getResources().getString(R.string.not_server_connection);
                Utils.ShowMessage(coordinatorLayout, message, 3);
                showToastDialog(message, 3);
                Log.e("Login", "not server connection");
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

    public void showDialog(final String txt, final String result) throws Exception {
        AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
        builder.setMessage(txt);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!result.trim().isEmpty()) {
                    Intent mainMenu = new Intent(Login.this, MainMenu.class);
                    Bundle params = new Bundle();
                    Gson gson = new GsonBuilder()
                            .setDateFormat("yyyy-MM-dd")
                            .create();
                    String json = gson.toJson(user);
                    params.putString("user", json.toString());
                    mainMenu.putExtras(params);
                    startActivity(mainMenu);
                }
                dialog.dismiss();
            }
        });

        /*
        builder.setPositiveButton("Llamar", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);// (Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + name));
                startActivity(callIntent);
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("Avbryt", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
            }
        });
        */

        builder.show();
    }

    public void verifyApkVersion () {
        try
        {
            BufferedReader fin =
                    new BufferedReader(
                            new InputStreamReader(
                                    openFileInput("apkVersion.json")));

            String strJson = fin.readLine();
            Gson gson = new GsonBuilder()
                    .create();
            JSONObject jsonobj= new JSONObject(strJson);
            apkVersion=jsonobj.getString("apkVersion");


            getApkVersion task = new getApkVersion();
            task.execute(new String[]{newApkVersion});

            fin.close();
        }
        catch (Exception ex)
        {
            Log.e("Ficheros", "Error al leer fichero desde memoria interna");
            try
            {
                OutputStreamWriter fout=
                        new OutputStreamWriter(
                                openFileOutput("apkVersion.json", Context.MODE_PRIVATE));

                JSONObject jsonobj= new JSONObject();
                jsonobj.put("apkVersion", apkVersion);
                fout.write(jsonobj.toString());
                fout.close();
            }
            catch (Exception ex2)
            {
                Log.e("Ficheros", "Error al escribir fichero a memoria interna");
            }
        }


    }

    private class getApkVersion extends AsyncTask<String, Void, String> {

        String response = null;

        private final ProgressDialog dialog = new ProgressDialog(Login.this);
        JSONObject object_feed;

        // can use UI thread here
        protected void onPreExecute() {
            this.dialog.setMessage("Verifying apk Version...");
            this.dialog.setCancelable(false);
            this.dialog.show();
        }

        protected String doInBackground(String... params) {

            String strReturn="";
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet httpPost = new HttpGet(url_host_connection+"/getApkVersion/");

            HttpResponse httpResponse = null;
            try {
                httpResponse = httpClient.execute(httpPost);
                String jsonResult = inputStreamToString(httpResponse.getEntity().getContent()).toString();
                JSONObject object = new JSONObject(jsonResult);
                newApkVersion = object.getString("apkVersion").toString();
                strReturn=newApkVersion;

            } catch (IOException e) {
                e.printStackTrace();
                strReturn = apkVersion;
            } catch (JSONException e) {
                e.printStackTrace();
                strReturn = apkVersion;
            }
            return strReturn;
        }

        @Override
        protected void onPostExecute(String param) {

            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }

            if((!apkVersion.equals(newApkVersion)) && (!newApkVersion.isEmpty()))
            {
                try {
                    showDialog("There is a new app version ("+newApkVersion+"), do you want to update now?","");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void showDialogApkVersion(final String txt, final String message) throws Exception {
        AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
        builder.setMessage(txt);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                try
                {
                    OutputStreamWriter fout=
                            new OutputStreamWriter(
                                    openFileOutput("apkVersion.json", Context.MODE_PRIVATE));

                    JSONObject jsonobj= new JSONObject();
                    jsonobj.put("", "");
                    fout.write(jsonobj.toString());
                    fout.close();
                }
                catch (Exception ex2)
                {
                    Log.e("Ficheros", "Error al escribir fichero a memoria interna");
                }

                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(urlApkUpdate));
                startActivity(i);

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

    public void verifyEmptyCredentials() {
        if((!txtEmail.getText().toString().trim().isEmpty()) && (!txtPassword.getText().toString().trim().isEmpty()))
        {
            btnLogin.setEnabled(true);
        }
        else{
            btnLogin.setEnabled(false);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if(resultCode == RESULT_OK){
                params = data.getExtras();
                userJson  = params.getString("user");
                user = gson.fromJson(userJson,User.class);
                validateUserTask task = new validateUserTask();
                task.execute(new String[]{user.getEmail().trim(), user.getPassword().trim()});
            }
            if (resultCode == 2) {

            }
        }
        if (requestCode == 2) {
            if(resultCode == RESULT_OK){
                String message = "Your Account had been activated successfully!";
                Utils.ShowMessage(coordinatorLayout, message, 3);
                validateUserTask task = new validateUserTask();
                task.execute(new String[]{user.getEmail().trim(), user.getPassword().trim()});
            }
        }
    }//onActivityResult

}
