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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
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
import jfutbol.com.jfutbol.model.mCantons;
import jfutbol.com.jfutbol.model.mSoccerCenters;
import jfutbol.com.jfutbol.model.User;
import jfutbol.com.jfutbol.model.mProvinces;
import jfutbol.com.jfutbol.singleton.Utils;
import jfutbol.com.jfutbol.singleton.singleton_token;


public class Add_New_Soccer_Center extends Activity {

    private static int RESULT_LOAD_IMAGE = 5;

    String url_host_connection;
    String url_host_connection_secure;
    singleton_token token = singleton_token.getInstance();
    CircleImageView btnImage;
    Bitmap bm;
    Bundle params;
    User user;
    private String userJson;
    mProvinces[] provinces;
    mCantons[] cantons;
    mCantons canton;
    EditText []txtFields;
    EditText txtEmail;
    mSoccerCenters newSoccerCenter = new mSoccerCenters();
    int openHourSelected;
    int openMinutesSelected;
    int closeHourSelected;
    int closeMinutesSelected;
    Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd")
            .create();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_soccer_center);

        RelativeLayout root = (RelativeLayout) findViewById(R.id.idRL);
        Toolbar bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.settings_toolbar, root, false);
        root.addView(bar, 0); // insert at top
        bar.setTitle(R.string.title_activity_add_new_soccer_center);

        bar.inflateMenu(R.menu.menu_add_new_soccer_center);
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

                    String encodedImage="";
                    if(bm!=null) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        byte[] imageBytes = baos.toByteArray();
                        encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
                    }

                    if (verifyRequiredFields() == true) {
                        newSoccerCenter.setEncodedImage(encodedImage);
                        newSoccerCenter.setOwnerId(user.getUserId());
                        newSoccerCenter.setName(txtFields[0].getText().toString());
                        newSoccerCenter.setOpenTime(openHourSelected + ":" + openMinutesSelected);
                        newSoccerCenter.setCloseTime(closeHourSelected + ":" + closeMinutesSelected);
                        newSoccerCenter.setAddress(txtFields[5].getText().toString().trim());
                        newSoccerCenter.setPhone(txtFields[6].getText().toString().trim());
                        newSoccerCenter.setEmail(txtEmail.getText().toString().trim());
                        AddNewSoccerCenterTask task = new AddNewSoccerCenterTask();
                        task.execute(newSoccerCenter);
                    } else {
                        String message = getResources().getString(R.string.required_fields);
                        showToastDialog(message, 2);
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

        txtFields= new EditText[7];
        txtFields[0]=(EditText) findViewById(R.id.txtNewSoccerCenter);
        txtFields[1]=(EditText) findViewById(R.id.txtProvince);
        txtFields[2]=(EditText) findViewById(R.id.txtState);
        txtFields[3]=(EditText) findViewById(R.id.txtOpenTime);
        txtFields[4]=(EditText) findViewById(R.id.txtCloseTime);
        txtFields[5]=(EditText) findViewById(R.id.txtAdress);
        txtFields[6]=(EditText) findViewById(R.id.txtCelPhone);
        txtEmail=(EditText) findViewById(R.id.txtEmail);

        ConvertToUppercase();

        txtFields[1].setText(user.getProvinceName());
        newSoccerCenter.setProvinceId(user.getProvinceId());
        txtFields[2].setText(user.getCantonName());
        newSoccerCenter.setCantonId(user.getCantonId());


        txtFields[3].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOpenTimePickerDialog(v);
            }
        });

        txtFields[4].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCloseTimePickerDialog(v);
            }
        });

        btnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("crop", "true");
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(intent, "PICK AN OPTION"), RESULT_LOAD_IMAGE);
            }
        });

        GetProvincesTask provincesTask = new GetProvincesTask();
        provincesTask.execute();

        GetCantonsTask cantonTask = new GetCantonsTask();
        cantonTask.execute(user.getProvinceId());
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void ConvertToUppercase() {
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

    public void showOpenTimePickerDialog(View v) {
        DialogFragment newFragment = new TimePickerFragment() {
            @Override
            public void onTimeSet(TimePicker view, int hour, int minutes) {
                String strHour="";
                String strMinutes="";
                String strTi="";
                openHourSelected=hour;
                openMinutesSelected=minutes;
                if(hour>12) {strHour = (hour-12)+"";strTi="PM";} else {strHour = ""+hour;strTi="AM";}
                if(minutes<10) {strMinutes = "0"+minutes;} else {strMinutes = ""+minutes;}

                txtFields[3].setText(strHour + ":" + strMinutes + " " + strTi);
            }
        };

        Bundle args_time = new Bundle();
        args_time.putInt("hour", openHourSelected);
        args_time.putInt("minutes", openMinutesSelected);
        newFragment.setArguments(args_time);
        newFragment.show(getFragmentManager(), "TimePicker");
    }

    public void showCloseTimePickerDialog(View v) {
        DialogFragment newFragment = new TimePickerFragment() {
            @Override
            public void onTimeSet(TimePicker view, int hour, int minutes) {
                String strHour="";
                String strMinutes="";
                String strTi="";
                closeHourSelected=hour;
                closeMinutesSelected=minutes;
                if(hour>12) {strHour = (hour-12)+"";strTi="PM";} else {strHour = ""+hour;strTi="AM";}
                if(minutes<10) {strMinutes = "0"+minutes;} else {strMinutes = ""+minutes;}

                txtFields[4].setText(strHour + ":" + strMinutes + " " + strTi);
            }
        };

        Bundle args_time = new Bundle();
        args_time.putInt("hour", closeHourSelected);
        args_time.putInt("minutes", closeMinutesSelected);
        newFragment.setArguments(args_time);
        newFragment.show(getFragmentManager(), "TimePicker");
    }

    private class AddNewSoccerCenterTask extends AsyncTask<mSoccerCenters, Void, JSONObject> {
        private final ProgressDialog dialog = new ProgressDialog(Add_New_Soccer_Center.this);
        protected void onPreExecute() {
             this.dialog.setMessage("Saving Data...");
             this.dialog.setCancelable(false);
             this.dialog.show();
         }
        protected JSONObject doInBackground(mSoccerCenters... params) {
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
            return responseJSON;
        }
        @Override
        protected void onPostExecute(JSONObject responseJSON) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }
            try {
                showToastDialog("New Soccer Center saved successfully",1);
                confirmAddSoccerField("Do you want to add soccer fields now?",responseJSON.getInt("insertId"));
                //Intent returnIntent = new Intent();
                //returnIntent.putExtra("team", teamJson);
               // setResult(RESULT_OK,returnIntent);
                //finish();
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

                    for (int i = 0; i < provinceList.size(); i++) {
                        ProvincesChar[i] = provinces[i].getName();
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(Add_New_Soccer_Center.this);
                    builder.setTitle("Provinces");

                    builder.setItems(ProvincesChar,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    int proviceIdSelected = 0;
                                    for (int i = 0; i < provinces.length; i++) {
                                        if (ProvincesChar[which].toString().equals(provinces[i].getName())) {
                                            proviceIdSelected = provinces[i].getProvinceId();
                                            break;
                                        }
                                    }
                                    newSoccerCenter.setProvinceId(proviceIdSelected);
                                    txtFields[1].setText(ProvincesChar[which].toString());
                                    txtFields[2].setText("");
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
                    cantons = gsonCantons.fromJson(jsonResultCantons, mCantons[].class);
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

            txtFields[2].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(Add_New_Soccer_Center.this);
                    builder.setTitle("Cantons");

                    builder.setItems(CantonsChar,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    CantonsChar[which].toString();
                                    txtFields[2].setText(CantonsChar[which].toString());
                                    for (int i = 0; i < cantons.length; i++) {
                                        if (CantonsChar[which].toString().equals(cantons[i].getName())) {
                                            canton = cantons[i];
                                            newSoccerCenter.setCantonId(canton.getCantonId());
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

    public void confirmAddSoccerField(final String message, final int soccerCenterId) throws Exception {
        AlertDialog.Builder builder = new AlertDialog.Builder(Add_New_Soccer_Center.this);
        builder.setMessage(message);
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                mSoccerCenters tempSoccerCenter = newSoccerCenter;
                tempSoccerCenter.setSoccerCenterId(soccerCenterId);
                String soccerCenterJson = gson.toJson(tempSoccerCenter);

                Intent add_New_Soccer_Field = new Intent(getApplicationContext(), Add_New_Soccer_Field.class);
                Bundle params = new Bundle();
                params.putString("user", userJson.toString());
                params.putString("soccercenter", soccerCenterJson.toString());
                add_New_Soccer_Field.putExtras(params);
                startActivityForResult(add_New_Soccer_Field, 1);


                dialog.dismiss();
            }
        });
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Intent returnIntent = new Intent();
                //returnIntent.putExtra("team", teamJson);
                setResult(RESULT_OK, returnIntent);
                finish();
            }
        });
        builder.show();
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

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if(resultCode == -1){
                Intent returnIntent = new Intent();
                //returnIntent.putExtra("team", teamJson);
                 setResult(RESULT_OK,returnIntent);
                finish();
            }
            if (resultCode == 2) {
                //Write your code if there's no result
            }
        }
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
    }//onActivityResult

}
