package com.example.UTM5_info;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

/**
 * Сообщение с одной кнопкой "ОК"
 * пример:
 * Dialog.showMessage(ActivityClass.this, String title, String message)
 */
public class Dialog {

    public static void showMessage(Context context, String title, String message){
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        alertDialog.show();
    }
}
