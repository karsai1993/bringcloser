package karsai.laszlo.bringcloser.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;

import karsai.laszlo.bringcloser.R;

/**
 * Created by Laci on 06/06/2018.
 */

public class DialogUtils {

    public static void onDialogRequest(
            Context context,
            String title,
            View dialogView,
            DialogInterface.OnClickListener onClickListener,
            int animationStyle
    ) {
        final AlertDialog.Builder builder =
                new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setView(dialogView);
        builder.setPositiveButton(R.string.dialog_settings_positive_btn, onClickListener);
        builder.setNegativeButton(
                R.string.dialog_settings_negative_btn,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.getWindow().getAttributes().windowAnimations = animationStyle;
        dialog.show();
        if (dialogView instanceof EditText) {
            EditText dialogText = (EditText) dialogView;
            dialogText.setHintTextColor(context.getResources().getColor(R.color.colorAccent));
            dialogText.setSelection(dialogText.getText().toString().length());
        }
    }

    public static void onDialogRequestForSorting(
            Context context,
            String title,
            String [] options,
            final DialogInterface.OnClickListener onClickListener) {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle(title);
        alertDialog.setSingleChoiceItems(options, -1, onClickListener);
        AlertDialog alert = alertDialog.create();
        alert.show();
    }

    public static void onDialogRequestForImage(
            Context context,
            View dialogView,
            int animationStyle) {
        final AlertDialog.Builder builder =
                new AlertDialog.Builder(context);
        //builder.setTitle(title);
        builder.setView(dialogView);
        //builder.setPositiveButton(R.string.dialog_settings_positive_btn, onClickListener);
        builder.setNegativeButton(
                R.string.dialog_settings_back_btn,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.getWindow().getAttributes().windowAnimations = animationStyle;
        dialog.show();
    }
}
