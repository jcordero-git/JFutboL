package jfutbol.com.jfutbol;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loopj.android.http.Base64;

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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import jfutbol.com.jfutbol.model.Team;
import jfutbol.com.jfutbol.model.User;
import jfutbol.com.jfutbol.model.mCantons;
import jfutbol.com.jfutbol.model.mProvinces;
import jfutbol.com.jfutbol.singleton.Utils;
import jfutbol.com.jfutbol.singleton.singleton_token;


public class Add_New_Team extends Activity {

    private static int RESULT_LOAD_IMAGE = 5;

    String url_host_connection;
    String url_host_connection_secure;

    CircleImageView btnImage;
    Bitmap bm;

    singleton_token token = singleton_token.getInstance();
    Bundle params;
    Team[] myTeams;
    User user;
    private String myTeamsJson;
    private String userJson;
    mProvinces[] provinces;
    mCantons[] states;
    mCantons state;
    EditText []txtFields;
    View coordinatorLayout;
    Team newTeam = new Team();
    Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd")
            .create();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add__new__team);
        coordinatorLayout = findViewById(R.id.coordinatorLayout);
        RelativeLayout root = (RelativeLayout) findViewById(R.id.idRL);
        Toolbar bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.settings_toolbar, root, false);
        root.addView(bar, 0); // insert at top
        bar.setTitle(R.string.title_activity_add_new_team);
        bar.inflateMenu(R.menu.menu_add__new__team);
        bar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                finish();
            }
        });
        bar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                if (menuItem.getTitle().equals(getResources().getString(R.string.send).toString())) {

                    String encodedImage = "";
                    if (bm != null) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        byte[] imageBytes = baos.toByteArray();
                        encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
                    }

                    if (verifyRequiredFields() == true) {
                        // EditText txtNewTeam = (EditText) findViewById(R.id.txtNewTeam);
                        // Team newTeam = new Team(0, user.getId(), txtNewTeam.getText().toString().trim());
                        newTeam.setOwnerId(user.getId());
                        newTeam.setName(txtFields[0].getText().toString());
                        newTeam.setEncodedImage(encodedImage);
                        AddNewTeamTask task = new AddNewTeamTask();
                        task.execute(newTeam);
                    } else {
                        String message = getResources().getString(R.string.required_fields);
                        Utils.ShowMessage(coordinatorLayout, message, 2);
                        //showToastDialog(message, 2);
                    }
                }
                return false;
            }
        });

        params=getIntent().getExtras();
        url_host_connection=getResources().getString(R.string.url_host_connection).toString();
        url_host_connection_secure=getResources().getString(R.string.url_host_connection).toString()+"/api";



        userJson  = params.getString("user");
        user=gson.fromJson(userJson, User.class);

        btnImage = (CircleImageView) findViewById(R.id.btnImage);


        txtFields= new EditText[3];
        txtFields[0]=(EditText) findViewById(R.id.txtNewTeam);
        txtFields[1]=(EditText) findViewById(R.id.txtProvince);
        txtFields[2]=(EditText) findViewById(R.id.txtState);

        ConvertToUppercase();

        txtFields[1].setText(user.getProvinceName());
        newTeam.setProvinceId(user.getProvinceId());
        txtFields[2].setText(user.getCantonName());
        newTeam.setCantonId(user.getCantonId());

        btnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("crop", "true");
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(intent, "Select Picture"), RESULT_LOAD_IMAGE);
            }
        });

        GetProvincesTask provincesTask = new GetProvincesTask();
        provincesTask.execute();

        GetStatesTask statesTask = new GetStatesTask();
        statesTask.execute(user.getProvinceId());

    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void ConvertToUppercase()
    {
            txtFields[0].setFilters(new InputFilter[]{new InputFilter.AllCaps()});
    }

    private boolean verifyRequiredFields() {
        boolean allFilled= true;
        for(int i=0;i<txtFields.length;i++)
        {
            if(txtFields[i].getText().toString().trim().equals("")) {
                txtFields[i].getBackground().setColorFilter(getResources().getColor(R.color.rejected), PorterDuff.Mode.SRC_ATOP);
                allFilled=false;
            }
            else {
                txtFields[i].getBackground().setColorFilter(0, PorterDuff.Mode.SRC_ATOP);
            }
        }
        return allFilled;
    }

    private class AddNewTeamTask extends AsyncTask<Team, Void, Team> {
        private final ProgressDialog dialog = new ProgressDialog(Add_New_Team.this);
        protected void onPreExecute() {
             this.dialog.setMessage("Saving Data...");
             this.dialog.setCancelable(false);
             this.dialog.show();
         }
        protected Team doInBackground(Team... params) {
            final HttpClient httpClient = new DefaultHttpClient();
            JSONObject responseJSON = null;
            final HttpPost httpPost= new HttpPost(url_host_connection_secure+"/team");
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
                    //String jsonResult = inputStreamToString(httpResponse.getEntity().getContent()).toString();
                    //responseJSON = new JSONObject(jsonResult);

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

            //saveNewTeamComplete=true;

            //GetMyTeamsTask task2 = new GetMyTeamsTask();
            //task2.execute();
            try {
                //showToastDialog( "New Team saved successfully",1);

               // Intent mainMenu = new Intent(Add_New_Team.this, MainMenu.class);
               // params.putString("user", userJson.toString() );
               // params.putInt("viewPosition", 1 );

               // mainMenu.putExtras(params);
                //startActivity(mainMenu);

                Intent returnIntent = new Intent();
                //returnIntent.putExtra("team", teamJson);
                setResult(RESULT_OK,returnIntent);
                finish();


            } catch (Exception e) {
                e.printStackTrace();
            }


        }//close onPostExecute
    }

    private class GetProvincesTask extends AsyncTask<String, Void, mProvinces[]> {

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

            txtFields[1].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CloseKeyBoard();
                    for (int i = 0; i < provinceList.size(); i++) {
                        ProvincesChar[i] = provinces[i].getName();
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(Add_New_Team.this);
                    builder.setTitle("Provinces");

                    builder.setItems(ProvincesChar,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    int proviceIdSelected = 0;
                                    for (int i = 0; i < provinces.length; i++) {
                                        if (ProvincesChar[which].toString().equals(provinces[i].getName())) {
                                            proviceIdSelected = provinces[i].getId();
                                            break;
                                        }
                                    }
                                    newTeam.setProvinceId(proviceIdSelected);
                                    txtFields[1].setText(ProvincesChar[which].toString());
                                    txtFields[2].setText("");
                                    dialog.dismiss();
                                    GetStatesTask statesTask = new GetStatesTask();
                                    statesTask.execute(proviceIdSelected);
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            });
        }//close onPostExecute
    }

    private class GetStatesTask extends AsyncTask<Integer, Void, mCantons[]> {

        protected mCantons[] doInBackground(Integer... params) {
            final HttpClient httpClient = new DefaultHttpClient();
            JSONObject responseJSON = null;
            final HttpGet httpGetStates= new HttpGet(url_host_connection+"/cantons/"+params[0]);
            try {
                try {
                    HttpResponse httpResponseStates = httpClient.execute(httpGetStates);
                    String jsonResultStates = inputStreamToString(httpResponseStates.getEntity().getContent()).toString();
                    Gson gsonStates = new GsonBuilder()
                            .setDateFormat("yyyy-MM-dd")
                            .create();
                    states= gsonStates.fromJson(jsonResultStates, mCantons[].class);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }catch (RuntimeException e)
            {
                e.printStackTrace();
            }
            return states;
        }
        @Override
        protected void onPostExecute(final mCantons[] states) {

            final List<mCantons> stateList = Arrays.asList(states);
            final CharSequence[] StatesChar = new CharSequence[stateList.size()];

            for(int i=0;i<stateList.size();i++)
            {
                StatesChar[i]=states[i].getName();
            }

            txtFields[2].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CloseKeyBoard();
                    AlertDialog.Builder builder = new AlertDialog.Builder(Add_New_Team.this);
                    builder.setTitle("States");

                    builder.setItems(StatesChar,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    StatesChar[which].toString();
                                    txtFields[2].setText(StatesChar[which].toString());
                                    for (int i = 0; i < states.length; i++) {
                                        if (StatesChar[which].toString().equals(states[i].getName())) {
                                            state = states[i];
                                            newTeam.setCantonId(state.getId());
                                            break;
                                        }
                                    }
                                    dialog.dismiss();
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            });
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

    public void CloseKeyBoard() {
        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                bm = Utils.ResizeImage(bitmap, 200, 200);
                // ImageView imageView = (ImageView) findViewById(R.id.btnImage);
                btnImage.setImageBitmap(bm);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
