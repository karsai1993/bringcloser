package karsai.laszlo.bringcloser.fragment;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;

import karsai.laszlo.bringcloser.ApplicationHelper;
import karsai.laszlo.bringcloser.R;

public class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        public TimePickerFragment(){}

        private Calendar mActualCalendar;
        private int textViewId;
        private Activity mActivity;

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Bundle bundle = getArguments();
            mActivity = getActivity();
            textViewId = bundle.getInt(ApplicationHelper.EXTRA_ID);
            mActualCalendar = Calendar.getInstance();
            int hour = mActualCalendar.get(Calendar.HOUR_OF_DAY);
            int minute = mActualCalendar.get(Calendar.MINUTE);
            return new TimePickerDialog(mActivity,
                    android.app.AlertDialog.THEME_HOLO_LIGHT,this,hour,minute,true);
        }

        @Override
        public void onTimeSet(TimePicker timePicker, int hour, int minute) {
            TextView textView = mActivity.findViewById(textViewId);
            String date = new StringBuilder()
                    .append(hour < 10 ? "0" + hour : hour)
                    .append(":")
                    .append(minute < 10 ? "0" + minute : minute)
                    .toString();
            textView.setText(date);
        }
}