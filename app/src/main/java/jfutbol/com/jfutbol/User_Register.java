package jfutbol.com.jfutbol;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loopj.android.http.*;
import de.hdodenhof.circleimageview.CircleImageView;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;


import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import jfutbol.com.jfutbol.model.PlayerSkills;
import jfutbol.com.jfutbol.model.User;
import jfutbol.com.jfutbol.model.mCantons;
import jfutbol.com.jfutbol.model.mProvinces;
import jfutbol.com.jfutbol.singleton.Utils;

public class User_Register extends Activity {

    private static int RESULT_LOAD_IMAGE = 5;

    String url_host_connection;
    CircleImageView btnImage;
    Bitmap bm;

    JSONObject responseUserRegister;

    Button btnPlayerSkills;
    EditText []txtFields;
    EditText txtEmail;
    EditText txtPassword;
    EditText txtConfirmPassword;
    EditText txtFirstName;
    EditText txtLastName;
    EditText txtPhone;
    EditText txtBirthDay;

    Switch switchPlayer;
    Switch switchOwner;

    Calendar date = Calendar.getInstance();

    SimpleDateFormat dateAppFormat = new SimpleDateFormat("E dd/MMMM/yyyy");
    SimpleDateFormat dateSQLFormat = new SimpleDateFormat("yyyy-MM-dd");

    //String date="";
    User user;
    String userJson;
    PlayerSkills[] skills;
    mProvinces[] provinces;
    mCantons[] cantons;
    mCantons canton;
    View coordinatorLayout;
    String profileImagePath;

    ArrayList<String> mSelectedItems;
    boolean[] checkedItems;
    Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd")
            .create();
    int year;
    int month;
    int day;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user__register);


        url_host_connection=getResources().getString(R.string.url_host_connection).toString();


        coordinatorLayout = findViewById(R.id.coordinatorLayout);

        btnImage = (CircleImageView) findViewById(R.id.btnImage);


        txtFields= new EditText[9];
        txtFields[0]=(EditText) findViewById(R.id.txtEmail);
        txtFields[1]=(EditText) findViewById(R.id.txtPassword);
        txtFields[2]=(EditText) findViewById(R.id.txtConfirmPassword);
        txtFields[3]=(EditText) findViewById(R.id.txtFirstName);
        txtFields[4]=(EditText) findViewById(R.id.txtLastName);
        txtFields[5]=(EditText) findViewById(R.id.txtCelPhone);
        txtFields[6]=(EditText) findViewById(R.id.txtBirthday);
        txtFields[7]=(EditText) findViewById(R.id.txtProvince);
        txtFields[8]=(EditText) findViewById(R.id.txtState);

        switchPlayer = (Switch) findViewById(R.id.switch_player);
        switchOwner = (Switch) findViewById(R.id.switch_owner);

        txtEmail= (EditText) findViewById(R.id.txtEmail);
        txtPassword= (EditText) findViewById(R.id.txtPassword);
        txtConfirmPassword= (EditText) findViewById(R.id.txtConfirmPassword);
        txtFirstName= (EditText) findViewById(R.id.txtFirstName);
        txtLastName= (EditText) findViewById(R.id.txtLastName);
        txtPhone= (EditText) findViewById(R.id.txtCelPhone);
        txtBirthDay= (EditText) findViewById(R.id.txtBirthday);

        user=new User();

        btnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent i = new Intent(
                //        Intent.ACTION_GET_CONTENT, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                //i.putExtra("crop","true");
                //startActivityForResult(i, RESULT_LOAD_IMAGE);

                Intent intent = new Intent();
                intent.putExtra("crop","true");
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(intent, "Select Picture"), RESULT_LOAD_IMAGE);
            }
        });

        btnPlayerSkills = (Button) findViewById(R.id.btnPlayerSkills);

        switchPlayer.setChecked(true);

        switchPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(switchPlayer.isChecked()) {
                    btnPlayerSkills.setVisibility(View.VISIBLE);
                    switchOwner.setChecked(false);
                }
                else {
                    switchOwner.setChecked(true);
                    btnPlayerSkills.setVisibility(View.INVISIBLE);
                }
            }
        });

        switchOwner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(switchOwner.isChecked()) {
                    switchPlayer.setChecked(false);
                    btnPlayerSkills.setVisibility(View.INVISIBLE);
                }
                else {
                    switchPlayer.setChecked(true);
                    btnPlayerSkills.setVisibility(View.VISIBLE);
                }
            }
        });

        /*
        btnPlayerSkills.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSkillPopUp();
            }
        });
        */

        txtBirthDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
                showDatePickerDialog(v);
            }
        });

        RelativeLayout root = (RelativeLayout) findViewById(R.id.idRL);
        Toolbar bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.settings_toolbar, root, false);
        root.addView(bar, 0); // insert at top
        bar.setTitle("SIGNING UP");
        bar.inflateMenu(R.menu.menu_user__register);
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

                if(menuItem.getTitle().equals(getResources().getString(R.string.send).toString()))
                {
                    Utils.HideKeyBoard(getApplicationContext(), User_Register.this);

                    try {
                        String encodedImage="";
                        if(bm!=null) {
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                            byte[] imageBytes = baos.toByteArray();
                            encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
                        }

                        String email = txtEmail.getText().toString().trim();
                        String password = txtPassword.getText().toString().trim();
                        String confirmPassword = txtConfirmPassword.getText().toString().trim();
                        String firstName = txtFirstName.getText().toString().trim();
                        String lastName = txtLastName.getText().toString().trim();
                        String phone = txtPhone.getText().toString().trim();
                        String birthday = dateSQLFormat.format(date.getTime());

                        //verifyRequiredFields();

                        if(verifyRequiredFields()) {
                            if(txtPassword.getText().toString().trim().equals(txtConfirmPassword.getText().toString().trim())) {
                                user.setEmail(email);
                                user.setPassword(password);
                                user.setFirstName(firstName);
                                user.setLastName(lastName);
                                user.setPhone(phone);
                                user.setBirthday(birthday);
                                if(switchPlayer.isChecked()) {
                                    user.setSkills(skills);
                                    user.setUserType(1);
                                }else{
                                    PlayerSkills[] dummySkills = new PlayerSkills[0];
                                    user.setSkills(dummySkills);
                                    user.setUserType(2);
                                }
                                user.setProvinceId(canton.getProvinceId());
                                user.setCantonId(canton.getId());
                                user.setEncodedImage(encodedImage);
                                RegisterUserTask task = new RegisterUserTask();
                                task.execute(user);
                            }
                            else{
                                String message = getResources().getString(R.string.password_does_not_match);
                                //showToastDialog(message,2);
                                Utils.ShowMessage(coordinatorLayout,message,2);
                                txtFields[1].getBackground().setColorFilter(getResources().getColor(R.color.rejected), PorterDuff.Mode.SRC_ATOP);
                                txtFields[2].getBackground().setColorFilter(getResources().getColor(R.color.rejected), PorterDuff.Mode.SRC_ATOP);
                            }
                        }
                        else{
                            String message = getResources().getString(R.string.required_fields);
                            Utils.ShowMessage(coordinatorLayout,message,2);
                            //showToastDialog(message,2);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }
        });
        GetSkillsTask skillsTask = new GetSkillsTask();
        skillsTask.execute();

        GetProvincesTask provincesTask = new GetProvincesTask();
        provincesTask.execute();

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private boolean verifyRequiredFields()  {
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

    private class RegisterUserTask extends AsyncTask<User, Void, JSONObject> {

        private final ProgressDialog dialog = new ProgressDialog(User_Register.this);
        JSONObject object_feed;
        // can use UI thread here
        protected void onPreExecute() {
            this.dialog.setMessage("Loading...");
            this.dialog.setCancelable(false);
            this.dialog.show();
        }

        protected JSONObject doInBackground(User... params) {
            // TODO Auto-generated method stub
            final HttpClient httpClient= new DefaultHttpClient();
            JSONObject responseJSON = null;
            final HttpPost httpPost= new HttpPost(url_host_connection+"/user/");
            try {
                try {
                    /*
                    JSONObject jsonobj = new JSONObject();
                    jsonobj.put("email", user.getEmail());
                    jsonobj.put("password", user.getPassword());
                    jsonobj.put("name", user.getCompleteName());
                    jsonobj.put("phone", user.getPhone());
                    jsonobj.put("birthday", user.getBirthday());
                    */


                    String json = gson.toJson(user);
                    JSONObject jsonobj= new JSONObject(json);

                  //  String json = JsonWriter.objectToJson(user);
                   // JSONObject jsonobj = new JSONObject(json);

                    /*
                    JSONObject jsonobjSkill = new JSONObject();
                    for (int i=0;i<user.getSkills().length;i++){
                        jsonobjSkill.put("skillName",user.getSkillName(i));
                        jsonobjSkill.put("skillValue",user.getSkillValue(i));
                    }
                    jsonobj.put("skills",jsonobjSkill);
                    //user.getSkills();
                    */


                    StringEntity se = new StringEntity(jsonobj.toString());
                    se.setContentType("application/json;charset=UTF-8");
                    se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,"application/json;charset=UTF-8"));
                    httpPost.setEntity(se);
                    HttpResponse httpResponse = httpClient.execute(httpPost);
                    String jsonResult = inputStreamToString(httpResponse.getEntity().getContent()).toString();
                    responseJSON = new JSONObject(jsonResult);

                   /* if(object.getInt("code")==2000)
                    {
                        responseJSONObject.put(object.getString("Code"),object.get("message"));
                        //user.setUserId(object.getInt("userId"));
                    }
                    else{
                        responseJSONObject.put(object.getString("Code"),object.get("message"));
                    }
                    */

                } catch (Exception e) {
                    //user.setUserId(0);
                    e.printStackTrace();
                }
            }catch (RuntimeException e)
            {
                //user.setUserId(0);
                e.printStackTrace();
            }
            return responseJSON;
        }//close doInBackground

        @Override
        protected void onPostExecute(JSONObject responseJSON) {

            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }
            /*
            responseUserRegister=responseJSON;
            String insertedUserId="";
            try {
                insertedUserId = responseUserRegister.getString("userId");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            */

            try {
               // showDialog(responseJSON.getString("message").toString(), responseJSON);
                if (responseJSON.getInt("code")==2000) {
                   userJson= gson.toJson(user, User.class);
                   //showToastDialog(responseJSON.getString("message").toString(),1);
                   //showToastDialog("An email was sent with the activation code to your email.",1);
                   Intent returnIntent = new Intent();
                   returnIntent.putExtra("user", userJson);
                   setResult(RESULT_OK,returnIntent);
                   finish();
                }
                else
                {
                    //showToastDialog(responseJSON.getString("message").toString(), 3);
                    String message = responseJSON.getString("message").toString();
                    Utils.ShowMessage(coordinatorLayout, message, 3);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

/*
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imageBytes = baos.toByteArray();
            String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
            new uploadImageByJson().execute(encodedImage, insertedUserId );
*/

            //  txt_Error.setText("Sorry!! Incorrect Username or Password");

        }//close onPostExecute
    }

    public Boolean uploadPhoto(String encodedImage, String nameImage) throws ClientProtocolException, IOException, JSONException
    {
        String HTTP_EVENT=url_host_connection+"/uploadbyjson";

        HttpClient httpclient = new DefaultHttpClient();
        //url y tipo de contenido
        HttpPost httppost = new HttpPost(HTTP_EVENT);
        httppost.addHeader("Content-Type", "application/json");
        //forma el JSON y tipo de contenido
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("encodedImage", encodedImage );
        jsonObject.put("nameImage", nameImage );
        //
        StringEntity stringEntity = new StringEntity( jsonObject.toString());
        stringEntity.setContentType("application/json;charset=UTF-8");
        stringEntity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,"application/json;charset=UTF-8"));
        httppost.setEntity(stringEntity);
        //ejecuta
        HttpResponse response = httpclient.execute(httppost);
        //obtiene la respuesta y transorma a objeto JSON
        String jsonResult = inputStreamToString(response.getEntity().getContent()).toString();
        JSONObject object = new JSONObject(jsonResult);

        if( object.getString("code").equals("2000"))
        {
            return true;
        }
        return false;
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

    public void showDialog(final String txt, final JSONObject responseJSON) throws Exception {
        AlertDialog.Builder builder = new AlertDialog.Builder(User_Register.this);
        builder.setMessage(txt);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    if (responseJSON.getInt("code") == 2000) {
                        finish();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                dialog.dismiss();
            }
        });
        builder.show();
    }

    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {

                /*month=month+1;
                String strMonth="";
                if(month<10) {
                    strMonth = "0" + month;
                }
                else {
                    strMonth = ""+month;
                }
                */

                date.set(Calendar.DAY_OF_MONTH, day);
                date.set(Calendar.MONTH, month);
                date.set(Calendar.YEAR, year);
                txtBirthDay.setText(dateAppFormat.format(date.getTime()));
            }
        };
        Bundle args_time = new Bundle();
        args_time.putInt("day", date.get(Calendar.DAY_OF_MONTH));
        args_time.putInt("month", date.get(Calendar.MONTH));
        args_time.putInt("year", date.get(Calendar.YEAR));
        newFragment.setArguments(args_time);
        newFragment.show(getFragmentManager(), "datePicker");
    }



/*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            ImageView imageView = (ImageView) findViewById(R.id.btnImage);
            imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));
        }
    }
    */

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

    /*

    private void showSkillPopUp() {
        final AlertDialog.Builder helpBuilder = new AlertDialog.Builder(this);
        helpBuilder.setTitle("Player Skills");
        LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final View checkboxLayout = inflater.inflate(R.layout.player_skill, null);
        final CheckBox GK= (CheckBox) checkboxLayout.findViewById(R.id.cbGK);
        final CheckBox SW_LIB= (CheckBox) checkboxLayout.findViewById(R.id.cbSW_LIB);
        final CheckBox CB= (CheckBox) checkboxLayout.findViewById(R.id.cbCB);
        final CheckBox RB= (CheckBox) checkboxLayout.findViewById(R.id.cbRB);
        final CheckBox LB= (CheckBox) checkboxLayout.findViewById(R.id.cbLB);
        final CheckBox RWB= (CheckBox) checkboxLayout.findViewById(R.id.cbRWB);
        final CheckBox LWB= (CheckBox) checkboxLayout.findViewById(R.id.cbLWB);
        final CheckBox DMF= (CheckBox) checkboxLayout.findViewById(R.id.cbDMF);
        final CheckBox CMF= (CheckBox) checkboxLayout.findViewById(R.id.cbCMF);
        final CheckBox RMF= (CheckBox) checkboxLayout.findViewById(R.id.cbRMF);
        final CheckBox LMF= (CheckBox) checkboxLayout.findViewById(R.id.cbLMF);
        final CheckBox AMF= (CheckBox) checkboxLayout.findViewById(R.id.cbAMF);
        final CheckBox SS= (CheckBox) checkboxLayout.findViewById(R.id.cbSS);
        final CheckBox RWF_RW= (CheckBox) checkboxLayout.findViewById(R.id.cbRWF_RW);
        final CheckBox LWF_LW= (CheckBox) checkboxLayout.findViewById(R.id.cbLWF_LW);
        final CheckBox CF= (CheckBox) checkboxLayout.findViewById(R.id.cbCF);
        final CheckBox ST= (CheckBox) checkboxLayout.findViewById(R.id.cbST);
        final CheckBox FCF= (CheckBox) checkboxLayout.findViewById(R.id.cbFCF);
        GK.setChecked(user.getSkills()[0].getValue());
        SW_LIB.setChecked(user.getSkills()[1].getValue());
        CB.setChecked(user.getSkills()[2].getValue());
        RB.setChecked(user.getSkills()[3].getValue());
        LB.setChecked(user.getSkills()[4].getValue());
        RWB.setChecked(user.getSkills()[5].getValue());
        LWB.setChecked(user.getSkills()[6].getValue());
        DMF.setChecked(user.getSkills()[7].getValue());
        CMF.setChecked(user.getSkills()[8].getValue());
        RMF.setChecked(user.getSkills()[9].getValue());
        LMF.setChecked(user.getSkills()[10].getValue());
        AMF.setChecked(user.getSkills()[11].getValue());
        SS.setChecked(user.getSkills()[12].getValue());
        RWF_RW.setChecked(user.getSkills()[13].getValue());
        LWF_LW.setChecked(user.getSkills()[14].getValue());
        CF.setChecked(user.getSkills()[15].getValue());
        ST.setChecked(user.getSkills()[16].getValue());
        FCF.setChecked(user.getSkills()[17].getValue());
                helpBuilder.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        PlayerSkills []skills= new PlayerSkills[18];
                        for(int i=0;i<18;skills[i++] = new PlayerSkills("","",0)){}
                        skills[0].setSkill(GK.getHint().toString(), GK.getText().toString(), GK.isChecked());
                        skills[1].setSkill(SW_LIB.getHint().toString(), SW_LIB.getText().toString(), SW_LIB.isChecked());
                        skills[2].setSkill(CB.getHint().toString(), CB.getText().toString(), CB.isChecked());
                        skills[3].setSkill(RB.getHint().toString(), RB.getText().toString(), RB.isChecked());
                        skills[4].setSkill(LB.getHint().toString(), LB.getText().toString(), LB.isChecked());
                        skills[5].setSkill(RWB.getHint().toString(), RWB.getText().toString(), RWB.isChecked());
                        skills[6].setSkill(LWB.getHint().toString(), LWB.getText().toString(), LWB.isChecked());
                        skills[7].setSkill(DMF.getHint().toString(), DMF.getText().toString(), DMF.isChecked());
                        skills[8].setSkill(CMF.getHint().toString(), CMF.getText().toString(), CMF.isChecked());
                        skills[9].setSkill(RMF.getHint().toString(), RMF.getText().toString(), RMF.isChecked());
                        skills[10].setSkill(LMF.getHint().toString(), LMF.getText().toString(), LMF.isChecked());
                        skills[11].setSkill(AMF.getHint().toString(), AMF.getText().toString(), AMF.isChecked());
                        skills[12].setSkill(SS.getHint().toString(), SS.getText().toString(), SS.isChecked());
                        skills[13].setSkill(RWF_RW.getHint().toString(), RWF_RW.getText().toString(), RWF_RW.isChecked());
                        skills[14].setSkill(LWF_LW.getHint().toString(), LWF_LW.getText().toString(), LWF_LW.isChecked());
                        skills[15].setSkill(CF.getHint().toString(), CF.getText().toString(), CF.isChecked());
                        skills[16].setSkill(ST.getHint().toString(), ST.getText().toString(), ST.isChecked());
                        skills[17].setSkill(FCF.getHint().toString(), FCF.getText().toString(), FCF.isChecked());

                        user.setSkills(skills);
                    }
                });

        // Remember, create doesn't show the dialog
        helpBuilder.setView(checkboxLayout);
        AlertDialog helpDialog = helpBuilder.create();
        helpDialog.show();


    }
    */

    private class GetSkillsTask extends AsyncTask<String, Void, PlayerSkills[]> {
        private final ProgressDialog dialog = new ProgressDialog(User_Register.this);
        protected void onPreExecute() {
            this.dialog.setMessage("Getting Skills...");
            this.dialog.setCancelable(false);
            this.dialog.show();
        }
        protected PlayerSkills[] doInBackground(String... params) {
            final HttpClient httpClient = new DefaultHttpClient();
            JSONObject responseJSON = null;
            final HttpGet httpGetSkills= new HttpGet(url_host_connection+"/skills");
            try {
                try {
                    HttpResponse httpResponseMyTeams = httpClient.execute(httpGetSkills);
                    String jsonResultPlayers = inputStreamToString(httpResponseMyTeams.getEntity().getContent()).toString();
                    Gson gsonMyTeams = new GsonBuilder()
                            .setDateFormat("yyyy-MM-dd")
                            .create();
                    skills= gsonMyTeams.fromJson(jsonResultPlayers, PlayerSkills[].class);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }catch (RuntimeException e)
            {
                e.printStackTrace();
            }
            return skills;
        }
        @Override
        protected void onPostExecute(final PlayerSkills[] skills) {

            final List<PlayerSkills> skillsList = Arrays.asList(skills);
            final CharSequence[] skillsChar = new CharSequence[skillsList.size()];
            //playerListViewAdapter listAdapter = new playerListViewAdapter(Team_Info.this, playerlist );
            //list.setAdapter(listAdapter);


            btnPlayerSkills.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    for(int i=0;i<skillsList.size();i++)
                    {
                        skillsChar[i]=skills[i].getSkillName();
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(User_Register.this);
                    builder.setTitle("Skills");
                    builder.setMultiChoiceItems(skillsChar,checkedItems,
                            new DialogInterface.OnMultiChoiceClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which,
                                                    boolean isChecked) {
                                    if (isChecked) {
                                        mSelectedItems.add(skillsChar[which].toString());
                                        skills[which].setValue(1);
                                    } else if (mSelectedItems.contains(skillsChar[which].toString())) {
                                        mSelectedItems.remove(skillsChar[which].toString());
                                       skills[which].setValue(0);
                                    }
                                }
                            })
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            });

            checkedItems = new boolean[skillsChar.length];
            mSelectedItems = new ArrayList();

            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
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

            txtFields[7].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    for (int i = 0; i < provinceList.size(); i++) {
                        ProvincesChar[i] = provinces[i].getName();
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(User_Register.this);
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
                                    txtFields[7].setText(ProvincesChar[which].toString());
                                    dialog.dismiss();
                                    GetCantonsTask cantonsTask = new GetCantonsTask();
                                    cantonsTask.execute(proviceIdSelected);
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            });
        }//close onPostExecute
    }

    private class GetCantonsTask extends AsyncTask<Integer, Void, mCantons[]> {

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
            canton=cantons[0];
            txtFields[8].setText(cantons[0].getName().toString());

            txtFields[8].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(User_Register.this);
                    builder.setTitle("Cantons");

                    builder.setItems(CantonsChar,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    CantonsChar[which].toString();
                                    txtFields[8].setText(CantonsChar[which].toString());
                                    for (int i = 0; i < cantons.length; i++) {
                                        if (CantonsChar[which].toString().equals(cantons[i].getName())) {
                                            canton = cantons[i];
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


}
