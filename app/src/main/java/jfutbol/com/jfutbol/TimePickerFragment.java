package jfutbol.com.jfutbol;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.TimePicker;

/**
 * Created by JOSEPH on 31/05/2015.
 */
public class TimePickerFragment extends DialogFragment  implements TimePickerDialog.OnTimeSetListener {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        int hour = 0;
        int minutes = 0;
        boolean is24Hour = false;
        if (getArguments() != null ) {
            hour  = getArguments().getInt("hour");
            minutes  = getArguments().getInt("minutes");
        }
        return new TimePickerDialog(getActivity(), this, hour, minutes, is24Hour); //DateFormat.is24HourFormat(getActivity())
    }

    @Override
    public void onTimeSet(TimePicker view, int hour, int minute) {
    }

}
