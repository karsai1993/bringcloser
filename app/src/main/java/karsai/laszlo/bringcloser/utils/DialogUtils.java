package karsai.laszlo.bringcloser.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Switch;

import karsai.laszlo.bringcloser.R;

/**
 * Created by Laci on 06/06/2018.
 * Util to handle dialog alert requests
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
        Window window = dialog.getWindow();
        if (window != null) {
            window.getAttributes().windowAnimations = animationStyle;
        }
        dialog.show();
        if (dialogView instanceof EditText) {
            EditText dialogText = (EditText) dialogView;
            dialogText.setHintTextColor(context.getResources().getColor(R.color.colorAccent));
            dialogText.setSelection(dialogText.getText().toString().length());
        }
    }

    public static void onDialogRequestForMemorySave(
            Context context,
            String title,
            String [] options,
            final DialogInterface.OnClickListener onClickListener,
            int animationStyle) {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle(title);
        alertDialog.setSingleChoiceItems(options, -1, onClickListener);
        AlertDialog alert = alertDialog.create();
        Window window = alert.getWindow();
        if (window != null) {
            window.getAttributes().windowAnimations = animationStyle;
        }
        alert.show();
    }

    public static void onDialogRequestForThoughtSending(
            final Context context,
            String title,
            View dialogView,
            DialogInterface.OnClickListener onClickListener,
            int animationStyle,
            final Switch switchView
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
        Window window = dialog.getWindow();
        if (window != null) {
            window.getAttributes().windowAnimations = animationStyle;
        }
        dialog.show();
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                switchView.setChecked(false);
            }
        });
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
        Window window = dialog.getWindow();
        if (window != null) {
            window.getAttributes().windowAnimations = animationStyle;
        }
        dialog.show();
    }
}
