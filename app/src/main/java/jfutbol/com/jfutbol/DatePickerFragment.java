package jfutbol.com.jfutbol;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.widget.DatePicker;

import java.util.Calendar;

public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

@Override
public Dialog onCreateDialog(Bundle savedInstanceState) {

    int year = 0;
    int month = 0;
    int day=0;

    if (getArguments() != null ) {
        year  = getArguments().getInt("year");
        month  = getArguments().getInt("month");
        day  = getArguments().getInt("day");
        if(year==0 || month==0 || day==0)
        {
            final Calendar c = Calendar.getInstance();
            year = c.get(Calendar.YEAR);
            month = c.get(Calendar.MONTH);
            day = c.get(Calendar.DAY_OF_MONTH);
        }
    }
    else {
        final Calendar c = Calendar.getInstance();
        year = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH);
        day = c.get(Calendar.DAY_OF_MONTH);
    }
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

public void onDateSet(DatePicker view, int year, int month, int day) {
        // Do something with the date chosen by the user
        }
 }

