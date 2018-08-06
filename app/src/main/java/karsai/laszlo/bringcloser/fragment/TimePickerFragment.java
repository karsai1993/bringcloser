package karsai.laszlo.bringcloser.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;

import karsai.laszlo.bringcloser.utils.ApplicationUtils;
import karsai.laszlo.bringcloser.R;
import timber.log.Timber;

/**
 * Fragment to handle time picker related information
 */
public class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        private int textViewId;
        private Activity mActivity;

        public TimePickerFragment() {}

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            mActivity = getActivity();
            Bundle bundle = getArguments();
            if (bundle !=  null) {
                textViewId = bundle.getInt(ApplicationUtils.EXTRA_ID);
            } else {
                Timber.wtf("time picker getArguments null");
            }
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            TimePickerDialog dialog;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                dialog = new TimePickerDialog(mActivity,
                        AlertDialog.THEME_HOLO_LIGHT,this,hour,minute,true);
            } else {
                dialog = new TimePickerDialog(mActivity,
                        R.style.AppTheme_DialogTheme,this,hour,minute,true);
            }
            return dialog;
        }

        @Override
        public void onTimeSet(TimePicker timePicker, int hour, int minute) {
            if (textViewId == 0) {
                Timber.wtf("time picker textview id 0");
                return;
            }
            TextView textView = mActivity.findViewById(textViewId);
            String date = new StringBuilder()
                    .append(hour < 10 ? "0" + hour : hour)
                    .append(":")
                    .append(minute < 10 ? "0" + minute : minute)
                    .toString();
            textView.setText(date);
        }
}