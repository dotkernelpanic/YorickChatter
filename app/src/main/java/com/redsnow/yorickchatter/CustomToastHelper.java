package com.redsnow.yorickchatter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class CustomToastHelper {

    private Context     context;
    private Drawable    iconWarning;
    private Drawable    iconError;
    private Drawable    iconInformation;

    public CustomToastHelper(Context context) {
        this.context = context;
    }

    private void initDrawables() {
        iconWarning     = context.getDrawable(R.drawable.ic_warning);
        iconError       = context.getDrawable(R.drawable.ic_error);
        iconInformation = context.getDrawable(R.drawable.ic_information);
    }

    public void initWarningToast(String message) {
        initDrawables();
        View v = View.inflate(context, R.layout.toast_layout, null);
        ((ImageView) v.findViewById(R.id.toastIcon)).setBackground(iconWarning);
        ((TextView) v.findViewById(R.id.toastText)).setText(message);
        Toast toast = new Toast(context);
        toast.setView(v);
        toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();
    }

    public void initErrorToast(String message) {
        initDrawables();
        View v = View.inflate(context, R.layout.toast_layout, null);
        ((ImageView) v.findViewById(R.id.toastIcon)).setBackground(iconError);
        ((TextView) v.findViewById(R.id.toastText)).setText(message);
        Toast toast = new Toast(context);
        toast.setView(v);
        toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();
    }

    public void initInformationToast(String message) {
        initDrawables();
        View v = View.inflate(context, R.layout.toast_layout, null);
        ((ImageView) v.findViewById(R.id.toastIcon)).setBackground(iconInformation);
        ((TextView) v.findViewById(R.id.toastText)).setText(message);
        Toast toast = new Toast(context);
        toast.setView(v);
        toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();
    }

}
