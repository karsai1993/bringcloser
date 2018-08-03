package karsai.laszlo.bringcloser.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

import karsai.laszlo.bringcloser.ApplicationHelper;
import karsai.laszlo.bringcloser.R;
import timber.log.Timber;

/**
 * Fragment to handle date picker related information
 */
public class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        private Calendar mActualCalendar;
        private TextView textView;
        private Activity mActivity;

        public DatePickerFragment() {}

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Bundle bundle = getArguments();
            mActivity = getActivity();
            if (bundle != null) {
                int textViewId = bundle.getInt(ApplicationHelper.EXTRA_ID);
                textView = mActivity.findViewById(textViewId);
            } else {
                Timber.wtf("date picker getArguments returned null");
            }
            mActualCalendar = Calendar.getInstance();
            int year;
            int month;
            int day;
            String prevSetValue;
            if (textView == null) {
                year = mActualCalendar.get(Calendar.YEAR);
                month = mActualCalendar.get(Calendar.MONTH);
                day = mActualCalendar.get(Calendar.DAY_OF_MONTH);
                prevSetValue = year + "-" + month + "-" + day;
            } else {
                prevSetValue = textView.getText().toString();
            }
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
            DatePickerDialog dialog;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                dialog = new DatePickerDialog(mActivity,
                        AlertDialog.THEME_HOLO_LIGHT,this,year,month,day);
            } else {
                dialog = new DatePickerDialog(mActivity,
                        R.style.AppTheme_DialogTheme,this,year,month,day);
            }
            return dialog;
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