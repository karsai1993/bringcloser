package karsai.laszlo.bringcloser.fragment;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

import karsai.laszlo.bringcloser.ApplicationHelper;
import karsai.laszlo.bringcloser.R;

public class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        public DatePickerFragment(){}

        private Calendar mActualCalendar;
        private int textViewId;
        private Activity mActivity;
        private TextView textView;

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Bundle bundle = getArguments();
            mActivity = getActivity();
            textViewId = bundle.getInt(ApplicationHelper.EXTRA_ID);
            textView = mActivity.findViewById(textViewId);
            String prevSetValue = textView.getText().toString();
            int year;
            int month;
            int day;
            mActualCalendar = Calendar.getInstance();
            if (prevSetValue.equals(getString(R.string.selected_date_default))) {
                year = mActualCalendar.get(Calendar.YEAR);
                month = mActualCalendar.get(Calendar.MONTH);
                day = mActualCalendar.get(Calendar.DAY_OF_MONTH);
            } else {
                String [] parts = prevSetValue.split("-");
                year = Integer.parseInt(parts[0]);
                month = Integer.parseInt(parts[1]) - 1;
                day = Integer.parseInt(parts[2]);
            }
            return new DatePickerDialog(mActivity,
                    android.app.AlertDialog.THEME_HOLO_LIGHT,this,year,month,day);
        }

        @Override
        public void onDateSet(DatePicker datePicker, int year, int month, int day) {
            Calendar newCalendar = Calendar.getInstance();
            newCalendar.set(year, month, day);
            if (newCalendar.getTime().before(mActualCalendar.getTime())) {
                Toast.makeText(
                        getContext(),
                        getResources().getString(R.string.wrong_date_selected_message_after),
                        Toast.LENGTH_LONG
                ).show();
            } else {
                String increasedMonth = String.valueOf(month + 1);
                String date = new StringBuilder()
                        .append(year)
                        .append("-")
                        .append(increasedMonth.length() == 1 ? "0" + increasedMonth : increasedMonth)
                        .append("-")
                        .append(day < 10 ? "0" + day : day)
                        .toString();
                textView.setText(date);
            }
        }
}