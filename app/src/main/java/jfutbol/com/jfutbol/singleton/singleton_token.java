package jfutbol.com.jfutbol.singleton;

import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.TextView;

import jfutbol.com.jfutbol.R;

/**
 * Created by JOSEPH on 28/09/2015.
 */
public class singleton_token {

    private static singleton_token singleton = new singleton_token( );

    /* A private Constructor prevents any other
     * class from instantiating.
     */

    private static String user_token;

    private singleton_token(){ }

    /* Static 'instance' method */
    public static singleton_token getInstance( ) {
        return singleton;
    }
    /* Other methods protected by singleton-ness */
    public static void demoMethod( ) {
        System.out.println("demoMethod for singleton");
    }

    public static void setUser_token(String token) {
        user_token=token;
    }

    public static String getUser_token()
    {
        return user_token;
    }
}
