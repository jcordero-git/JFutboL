package jfutbol.com.jfutbol;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.util.Common;
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
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
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
import jfutbol.com.jfutbol.singleton.singleton_token;


public class MyAccount extends Activity {

    private static int RESULT_LOAD_IMAGE = 5;

    AQuery aq;
    String url_host_connection;
    String url_host_connection_secure;
    singleton_token token = singleton_token.getInstance();
    User user;
    String userJson;
    PlayerSkills[] skills;
    mProvinces[] provinces;
    mCantons[] cantons;
    mCantons canton;
    ArrayList<String> mSelectedItems;
    boolean[] checkedItems;

    EditText []txtFields;
    EditText txtEmail;
    EditText txtPassword;
    EditText txtConfirmPassword;
    EditText txtFirstName;
    EditText txtLastName;
    EditText txtCelPhone;
    EditText txtBirthDay;
    //String date="";
    Calendar date = Calendar.getInstance();
    SimpleDateFormat dateAppFormat = new SimpleDateFormat("E dd/MMMM/yyyy");
    SimpleDateFormat dateSQLFormat = new SimpleDateFormat("yyyy-MM-dd");

    boolean updatePassword=false;
    boolean updatePlayerSkill=false;

    ImageView btnImage;
    Button btnPlayerSkills;

    View coordinatorLayout;

    Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd")
            .create();

    Bitmap bm;
    static Uri fileUri;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_account);
        coordinatorLayout = findViewById(R.id.coordinatorLayout);
        RelativeLayout root = (RelativeLayout) findViewById(R.id.idRL);
        Toolbar bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.settings_toolbar, root, false);
        root.addView(bar, 0); // insert at top
        bar.setTitle("My Account");
        bar.inflateMenu(R.menu.menu_my_account);

        url_host_connection=getResources().getString(R.string.url_host_connection).toString();
        url_host_connection_secure=getResources().getString(R.string.url_host_connection).toString()+"/api";
        Bundle args = getIntent().getExtras();
        userJson = args.getString("user");

        user=gson.fromJson(userJson,User.class);
        boolean memCache = false;
        boolean fileCache = false;

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

        aq = new AQuery(this);

        txtEmail= (EditText) findViewById(R.id.txtEmail);
        txtEmail.setText(user.getEmail());
        txtPassword= (EditText) findViewById(R.id.txtPassword);
        txtConfirmPassword= (EditText) findViewById(R.id.txtConfirmPassword);
        txtFirstName= (EditText) findViewById(R.id.txtFirstName);
        txtFirstName.setText(user.getFirstName());
        txtLastName= (EditText) findViewById(R.id.txtLastName);
        txtLastName.setText(user.getLastName());
        txtCelPhone= (EditText) findViewById(R.id.txtCelPhone);
        txtCelPhone.setText(user.getPhone());
        txtBirthDay= (EditText) findViewById(R.id.txtBirthday);
        txtBirthDay.setText(user.getBirthdayApp());
        date.setTime(user.getBirthday());
        txtFields[7].setText(user.getProvinceName());
        txtFields[8].setText(user.getCantonName());
        btnImage= (ImageView) findViewById(R.id.btnImage);

        aq.id(R.id.btnImage).image(url_host_connection+"/images/profile/"+user.getId()+".png", memCache, fileCache);

        btnPlayerSkills = (Button) findViewById(R.id.btnPlayerSkills);

        if(user.getUserType()==2)
            btnPlayerSkills.setVisibility(View.INVISIBLE);


        btnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent();
                intent.putExtra("crop","true");
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "PICK AN OPTION"), RESULT_LOAD_IMAGE);

                //picPhoto();
            }
        });

        txtBirthDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
                showDatePickerDialog(v);
            }
        });

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

                if(menuItem.getTitle().equals(getResources().getString(R.string.save).toString()))
                {
                    CloseKeyBoard();
                    try {
                        String encodedImage="";
                        if(bm!=null) {
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                            byte[] imageBytes = baos.toByteArray();
                            encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
                        }

                        if(verifyRequiredFields()==true) {
                            user.setEmail(txtEmail.getText().toString().trim());
                            user.setFirstName(txtFirstName.getText().toString().trim());
                            user.setLastName(txtLastName.getText().toString().trim());
                            user.setPhone(txtCelPhone.getText().toString().trim());
                            user.setBirthday(dateSQLFormat.format(date.getTime()));
                            user.setEncodedImage(encodedImage);
                            if ((txtPassword.getText().toString().trim().compareTo("") != 0) && (txtConfirmPassword.getText().toString().trim().compareTo("") != 0)) {
                                if (txtPassword.getText().toString().trim().equals(txtConfirmPassword.getText().toString().trim())) {
                                    updatePassword = true;
                                    user.setPassword(txtPassword.getText().toString().trim());
                                    EditUserTask task = new EditUserTask();
                                    task.execute(user);
                                } else {
                                    String message = getResources().getString(R.string.password_does_not_match);
                                    //showToastDialog(message,2);
                                    Utils.ShowMessage(coordinatorLayout, message,2);
                                    txtFields[1].getBackground().setColorFilter(getResources().getColor(R.color.rejected), PorterDuff.Mode.SRC_ATOP);
                                    txtFields[2].getBackground().setColorFilter(getResources().getColor(R.color.rejected), PorterDuff.Mode.SRC_ATOP);
                                }
                            } else {
                                updatePassword = false;
                                EditUserTask task = new EditUserTask();
                                task.execute(user);
                            }
                        }
                        else{
                            String message = getResources().getString(R.string.required_fields);
                            Utils.ShowMessage(coordinatorLayout, message, 2);
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

        GetCantonsTask cantonsTask = new GetCantonsTask();
        cantonsTask.execute(user.getProvinceId());

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    public void CloseKeyBoard() {
        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private boolean verifyRequiredFields() {
        boolean allFilled= true;
        for(int i=0;i<txtFields.length;i++)
        {
            if(txtFields[i].getText().toString().trim().equals("") && i!=1 && i!=2) {
                txtFields[i].getBackground().setColorFilter(getResources().getColor(R.color.rejected), PorterDuff.Mode.SRC_ATOP);
                allFilled=false;
            }
            else {
                txtFields[i].getBackground().setColorFilter(0, PorterDuff.Mode.SRC_ATOP);
            }
        }
        return allFilled;
    }

    private class EditUserTask extends AsyncTask<User, Void, JSONObject> {
        private final ProgressDialog dialog = new ProgressDialog(MyAccount.this);
        protected void onPreExecute() {
            this.dialog.setMessage("Saving your changes...");
            this.dialog.setCancelable(false);
            this.dialog.show();
        }
        protected JSONObject doInBackground(User... params) {
            final HttpClient httpClient = new DefaultHttpClient();
            JSONObject responseJSON = null;
            final HttpPost httpPost = new HttpPost(url_host_connection_secure+"/user/"+updatePassword+"/"+updatePlayerSkill);
            httpPost.addHeader("x-access-token",token.getUser_token());
            try {
                try {
                    Gson gson = new GsonBuilder()
                            .setDateFormat("yyyy-MM-dd")
                            .create();
                    String json = gson.toJson(user);
                    JSONObject jsonobj= new JSONObject(json);
                    StringEntity se = new StringEntity(jsonobj.toString());
                    se.setContentType("application/json;charset=UTF-8");
                    se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,"application/json;charset=UTF-8"));
                    httpPost.setEntity(se);
                    HttpResponse httpResponse = httpClient.execute(httpPost);
                    String jsonResult = inputStreamToString(httpResponse.getEntity().getContent()).toString();
                    responseJSON = new JSONObject(jsonResult);

                } catch (Exception e) {
                    //user.setUserId(0);
                    e.printStackTrace();
                }
            }catch (RuntimeException e)
            {
                e.printStackTrace();
            }
            return responseJSON;
        }
        @Override
        protected void onPostExecute(JSONObject responseJSON) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }
            try {
                if(responseJSON.getString("code").equals("2000")||(responseJSON.getInt("code")==2001))
                {
                    SaveNewSessionActive(user.getEmail(), user.getPassword());
                    Intent returnIntent = new Intent();
                    userJson = gson.toJson(user);
                    returnIntent.putExtra("user", userJson);
                    setResult(RESULT_OK, returnIntent);
                    finish();
                    //showToastDialog("The user was updated successfully",1);
                }
                //showDialog(responseJSON.getString("message").toString(), responseJSON);
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

    public void showDialog(final String txt, final JSONObject responseJSON) throws Exception {
        AlertDialog.Builder builder = new AlertDialog.Builder(MyAccount.this);
        builder.setMessage(txt);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    if ((responseJSON.getInt("code")==2000)||(responseJSON.getInt("code")==2001)) {
                        Intent returnIntent = new Intent();
                        setResult(RESULT_OK, returnIntent);
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

    public void showDatePickerDialog(View v) {
        CloseKeyBoard();
        DialogFragment newFragment = new DatePickerFragment() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
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

    private class GetSkillsTask extends AsyncTask<String, Void, PlayerSkills[]> {
        private final ProgressDialog dialog = new ProgressDialog(MyAccount.this);
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

            checkedItems = new boolean[skillsChar.length];
            mSelectedItems = new ArrayList();
                btnPlayerSkills.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CloseKeyBoard();
                        for (int i = 0; i < skillsList.size(); i++) {
                            skillsChar[i] = skills[i].getSkillName();
                            if (user.getSkills()[i].getIntValue() == 1) {
                                checkedItems[i] = true;
                                mSelectedItems.add(user.getSkills()[i].getSkillName());
                            }
                        }
                        AlertDialog.Builder builder = new AlertDialog.Builder(MyAccount.this);
                        builder.setTitle("Editing Skills");
                        builder.setMultiChoiceItems(skillsChar, checkedItems,
                                new DialogInterface.OnMultiChoiceClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which,
                                                        boolean isChecked) {
                                        if (isChecked) {
                                            mSelectedItems.add(skillsChar[which].toString());
                                            skills[which].setValue(1);
                                            user.getSkills()[which].setValue(1);
                                        } else if (mSelectedItems.contains(skillsChar[which].toString())) {
                                            mSelectedItems.remove(skillsChar[which].toString());
                                            skills[which].setValue(0);
                                            user.getSkills()[which].setValue(0);
                                        }
                                        updatePlayerSkill=true;
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
                    CloseKeyBoard();
                    for (int i = 0; i < provinceList.size(); i++) {
                        ProvincesChar[i] = provinces[i].getName();
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(MyAccount.this);
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
                                    user.setProvinceId(proviceIdSelected);
                                    txtFields[7].setText(ProvincesChar[which].toString());
                                    txtFields[8].setText("");
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

            txtFields[8].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CloseKeyBoard();
                    AlertDialog.Builder builder = new AlertDialog.Builder(MyAccount.this);
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
                                            user.setCantonId(  canton.getId());
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                bm = Utils.ResizeImage(bitmap, 200, 200);
                ImageView imageView = (ImageView) findViewById(R.id.btnImage);
                imageView.setImageBitmap(bm);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



}
