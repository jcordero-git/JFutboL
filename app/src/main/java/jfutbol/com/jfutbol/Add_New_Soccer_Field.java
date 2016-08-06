package jfutbol.com.jfutbol;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
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
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loopj.android.http.Base64;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
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
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;
import jfutbol.com.jfutbol.model.User;
import jfutbol.com.jfutbol.model.mSoccerCenters;
import jfutbol.com.jfutbol.model.mSoccerFields;
import jfutbol.com.jfutbol.singleton.Utils;
import jfutbol.com.jfutbol.singleton.singleton_token;


public class Add_New_Soccer_Field extends Activity {

    private static int RESULT_LOAD_IMAGE = 5;

    String url_host_connection;
    String url_host_connection_secure;
    singleton_token token = singleton_token.getInstance();
    CircleImageView btnImage;
    Bitmap bm;
    Bundle params;
    User user;
    private String userJson;
    private String soccerCenterJson;
    EditText []txtFields;
    mSoccerCenters soccerCenter;
    mSoccerFields newSoccerField = new mSoccerFields();
    Date openTime=new Date();
    Date closeTime=new Date();
    Boolean saveAndNew=false;

    View coordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_soccer_field);
        coordinatorLayout = findViewById(R.id.coordinatorLayout);
        RelativeLayout root = (RelativeLayout) findViewById(R.id.idRL);
        Toolbar bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.settings_toolbar, root, false);
        root.addView(bar, 0); // insert at top
        bar.setTitle(R.string.title_activity_add_new_soccer_field);

        bar.inflateMenu(R.menu.menu_add_new_soccer_field);
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

                CloseKeyBoard();
                String encodedImage="";
                if(bm!=null) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
                    byte[] imageBytes = baos.toByteArray();
                    encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
                }

                if(menuItem.getTitle().equals(getResources().getString(R.string.send).toString()))
                {
                    if(verifyRequiredFields()==true && encodedImage.compareTo("")!=0) {
                        newSoccerField.setEncodedImage(encodedImage);
                        newSoccerField.setSoccerCenterId(soccerCenter.getSoccerCenterId());
                        newSoccerField.setName(txtFields[0].getText().toString());
                        newSoccerField.setStatus(1);
                        saveAndNew=false;
                        AddNewSoccerFieldTask task = new AddNewSoccerFieldTask();
                        task.execute(newSoccerField);
                    }
                    else{
                        String message;
                        if(encodedImage.equals("")) {
                            message = getResources().getString(R.string.required_image);
                            btnImage.setBorderColor(getResources().getColor(R.color.red));
                        }
                        else
                            message = getResources().getString(R.string.required_fields);
                        //showToastDialog(message,2);
                        Utils.ShowMessage(coordinatorLayout, message, 2);
                    }
                }
                if(menuItem.getTitle().equals(getResources().getString(R.string.saveAndNew).toString()))
                {
                    if(verifyRequiredFields()==true && encodedImage.compareTo("")!=0)  {
                        newSoccerField.setEncodedImage(encodedImage);
                        newSoccerField.setSoccerCenterId(soccerCenter.getSoccerCenterId());
                        newSoccerField.setName(txtFields[0].getText().toString());
                        newSoccerField.setStatus(1);
                        saveAndNew=true;
                        AddNewSoccerFieldTask task = new AddNewSoccerFieldTask();
                        task.execute(newSoccerField);
                    }
                    else{
                        String message;
                        if(encodedImage.equals("")) {
                            message = getResources().getString(R.string.required_image);
                            btnImage.setBorderColor(getResources().getColor(R.color.red));
                        }
                        else
                            message = getResources().getString(R.string.required_fields);
                        //showToastDialog(message,2);
                        Utils.ShowMessage(coordinatorLayout, message, 2);
                    }
                }
                return false;
            }
        });

        params=getIntent().getExtras();
        url_host_connection=getResources().getString(R.string.url_host_connection).toString();
        url_host_connection_secure=getResources().getString(R.string.url_host_connection).toString()+"/api";

        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd")
                .create();

        userJson  = params.getString("user");
        user=gson.fromJson(userJson, User.class);

        soccerCenterJson  = params.getString("soccercenter");
        soccerCenter=gson.fromJson(soccerCenterJson, mSoccerCenters.class);

        btnImage = (CircleImageView) findViewById(R.id.btnImage);

        txtFields= new EditText[3];
        txtFields[0]=(EditText) findViewById(R.id.txtNewSoccerField);
        txtFields[1]=(EditText) findViewById(R.id.txtOpenTime);
        txtFields[2]=(EditText) findViewById(R.id.txtCloseTime);

        ConvertToUppercase();

        setDefaultTimes();

        btnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupImageChooser();
            }
        });

        txtFields[1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOpenTimePickerDialog(v);
            }
        });

        txtFields[2].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCloseTimePickerDialog(v);
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void setDefaultTimes(){

        newSoccerField.setOpenTime(soccerCenter.getOpenTime());
        newSoccerField.setCloseTime(soccerCenter.getCloseTime());
        // Time=(soccerCenter.getOpenTime().split(":"));
       // hour=Integer.parseInt(Time[0]);
       // minutes=Integer.parseInt(Time[1]);
        txtFields[1].setText(newSoccerField.getOpenTimeApp());
       // newSoccerField.setOpenTime(soccerCenter.getOpenTime());
        //Time=(soccerCenter.getCloseTime().split(":"));
        //hour=Integer.parseInt(Time[0]);
       // minutes=Integer.parseInt(Time[1]);
        txtFields[2].setText(newSoccerField.getCloseTimeApp());
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

    private String convertTime12(int hour, int minutes) {
        String strHour="";
        String strMinutes="";
        String strTi="";
        if(hour>12) {strHour = (hour-12)+"";strTi="PM";} else {strHour = ""+hour;strTi="AM";}
        if(minutes<10) {strMinutes = "0"+minutes;} else {strMinutes = ""+minutes;}
        return (strHour + ":" + strMinutes + " " + strTi);
    }

    public void showOpenTimePickerDialog(View v) {
        CloseKeyBoard();
        DialogFragment newFragment = new TimePickerFragment() {
            @Override
            public void onTimeSet(TimePicker view, int hour, int minutes) {
                //String strHour="";
                //String strMinutes="";
               // String strTi="";
                    //openHourSelected=hour;
                    //openMinutesSelected=minutes;
                openTime.setHours(hour);
                openTime.setMinutes(minutes);
                //openTime.set(0,0,0,hour,minutes);
               // if(hour>12) {strHour = (hour-12)+"";strTi="PM";} else {strHour = ""+hour;strTi="AM";}
               // if(minutes<10) {strMinutes = "0"+minutes;} else {strMinutes = ""+minutes;}
                newSoccerField.setOpenTimeSQL(openTime);
                txtFields[1].setText(newSoccerField.getOpenTimeApp());
                //txtFields[1].setText(convertTime12(hour, minutes));
            }
        };

        Bundle args_time = new Bundle();
        args_time.putInt("hour", openTime.getHours());
        args_time.putInt("minutes", openTime.getMinutes());
        newFragment.setArguments(args_time);
        newFragment.show(getFragmentManager(), "TimePicker");
    }

    public void showCloseTimePickerDialog(View v) {
        CloseKeyBoard();
        DialogFragment newFragment = new TimePickerFragment() {
            @Override
            public void onTimeSet(TimePicker view, int hour, int minutes) {
               // String strHour="";
               // String strMinutes="";
               // String strTi="";
                   //closeHourSelected=hour;
                   //closeMinutesSelected=minutes;
               // if(hour>12) {strHour = (hour-12)+"";strTi="PM";} else {strHour = ""+hour;strTi="AM";}
               // if(minutes<10) {strMinutes = "0"+minutes;} else {strMinutes = ""+minutes;}
                closeTime.setHours(hour);
                closeTime.setMinutes(minutes);
                //closeTime.set(0,0,0,hour,minutes);
                newSoccerField.setCloseTimeSQL(closeTime);
                txtFields[2].setText(newSoccerField.getCloseTimeApp());
                //txtFields[2].setText(convertTime12(hour,minutes));
            }
        };

        Bundle args_time = new Bundle();
        args_time.putInt("hour", closeTime.getHours());
        args_time.putInt("minutes", closeTime.getMinutes());
        newFragment.setArguments(args_time);
        newFragment.show(getFragmentManager(), "TimePicker");
    }

    private class AddNewSoccerFieldTask extends AsyncTask<mSoccerFields, Void, mSoccerFields> {
        private final ProgressDialog dialog = new ProgressDialog(Add_New_Soccer_Field.this);
        protected void onPreExecute() {
             this.dialog.setMessage("Saving Data...");
             this.dialog.setCancelable(false);
             this.dialog.show();
         }
        protected mSoccerFields doInBackground(mSoccerFields... params) {
            final HttpClient httpClient = new DefaultHttpClient();
            JSONObject responseJSON = null;
            final HttpPost httpPost= new HttpPost(url_host_connection_secure+"/soccercenter/soccerfield");
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
        protected void onPostExecute(mSoccerFields soccerfield) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }

            //saveNewTeamComplete=true;

            //GetMyTeamsTask task2 = new GetMyTeamsTask();
            //task2.execute();
            try {
                //showToastDialog("New Soccer Field saved successfully",1);
               // Intent mainMenu = new Intent(Add_New_Team.this, MainMenu.class);
               // params.putString("user", userJson.toString() );
               // params.putInt("viewPosition", 1 );

               // mainMenu.putExtras(params);
                //startActivity(mainMenu);

                if(saveAndNew==true){
                    txtFields[0].setText("");
                    setDefaultTimes();
                    bm=null;
                    btnImage.setImageDrawable(getResources().getDrawable(R.drawable.gallery));
                    String message = getResources().getString(R.string.field_added_success);
                    Utils.ShowMessage(coordinatorLayout, message, 1);
                    txtFields[0].requestFocus();
                }
                else {
                    Intent returnIntent = new Intent();
                    //returnIntent.putExtra("team", teamJson);
                    setResult(RESULT_OK, returnIntent);
                    finish();
                }
            } catch (Exception e) {
                e.printStackTrace();
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

    public void showPopupImageChooser() {
        CloseKeyBoard();
        final AlertDialog.Builder builder = new AlertDialog.Builder(Add_New_Soccer_Field.this);
        // Get the layout inflater
        LayoutInflater inflater = getLayoutInflater();
        builder.setTitle("Choose an field number");
        builder.setCancelable(true);
        builder.setIcon(R.drawable.gallery);
        View v=inflater.inflate(R.layout.layout_choose_image, null);

        final CircleImageView [] imgImages=new CircleImageView[9];
        CircleImageView imgGallery = (CircleImageView) v.findViewById(R.id.btnImage);
        imgImages[0] = (CircleImageView) v.findViewById(R.id.btnImage1);
        imgImages[1] = (CircleImageView) v.findViewById(R.id.btnImage2);
        imgImages[2] = (CircleImageView) v.findViewById(R.id.btnImage3);
        imgImages[3] = (CircleImageView) v.findViewById(R.id.btnImage4);
        imgImages[4] = (CircleImageView) v.findViewById(R.id.btnImage5);
        imgImages[5] = (CircleImageView) v.findViewById(R.id.btnImage6);
        imgImages[6] = (CircleImageView) v.findViewById(R.id.btnImage7);
        imgImages[7] = (CircleImageView) v.findViewById(R.id.btnImage8);
        imgImages[8] = (CircleImageView) v.findViewById(R.id.btnImage9);

        builder.setView(v);
        builder.create();
        final AlertDialog alertDialog=builder.show();


        for(int i=0;i<imgImages.length;i++) {
            final int k=i;
            imgImages[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    imgImages[k].buildDrawingCache();
                    Bitmap bt=imgImages[k].getDrawingCache();
                    Bitmap bitmap = ((BitmapDrawable)imgImages[k].getDrawable()).getBitmap();
                    Bitmap bmap = Bitmap.createBitmap(((BitmapDrawable) imgImages[k].getDrawable()).getBitmap());
                    bm = bmap;
                    btnImage.setImageBitmap(bmap);
                    alertDialog.dismiss();
                }
            });
        }

        imgGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("crop", "true");
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), RESULT_LOAD_IMAGE);
                alertDialog.dismiss();
            }
        });
/*
        txtFields[4].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEndTimePickerDialog(v);
            }
        });
        */


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
                //bm = bitmap;
                bm = Utils.ResizeImage(bitmap, 200, 200);
                // ImageView imageView = (ImageView) findViewById(R.id.btnImage);
                btnImage.setImageBitmap(bm);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
