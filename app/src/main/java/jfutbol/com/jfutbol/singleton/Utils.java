package jfutbol.com.jfutbol.singleton;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import jfutbol.com.jfutbol.R;

/**
 * Created by JOSEPH on 08/11/2015.
 */
public class Utils {

    private static Utils utils = new Utils( );

    private Utils(){ }

    public static Utils getInstance( ) {
        return utils;
    }

    public static void ShowMessage(View view ,String message, int type) {
        Snackbar snackbar = Snackbar
                .make(view, message, Snackbar.LENGTH_LONG)
                /*.setAction("GO TO LOGIN", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                }
                )*/
                ;

        // Changing message text color
        //snackbar.setActionTextColor(Color.RED);

        // Changing action button text color
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        if(type==1)
            textView.setTextColor(view.getResources().getColor(R.color.green));
        if(type==2)
            textView.setTextColor(view.getResources().getColor(R.color.orange));
        if(type==3)
            textView.setTextColor(view.getResources().getColor(R.color.red));

        textView.setGravity(View.TEXT_ALIGNMENT_CENTER);

        snackbar.show();
    }

    public static Bitmap ResizeImage(Bitmap mBitmap, float newWidth, float newHeigth){
        int width = mBitmap.getWidth();
        int height = mBitmap.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeigth) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        return Bitmap.createBitmap(mBitmap, 0, 0, width, height, matrix, false);
    }

    public static void HideKeyBoard(Context context, Activity activity) {
        InputMethodManager inputManager = (InputMethodManager)
                context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }
}
