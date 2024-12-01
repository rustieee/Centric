package com.finals.centric;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;

import java.util.Locale;

public class TimePickerWheelDialog extends Dialog {
    private OnTimeSelectedListener listener;
    private NumberPicker hourPicker;

    public TimePickerWheelDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_time_picker_wheel);

        // Set dialog window attributes
        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        }

        hourPicker = findViewById(R.id.hourPicker);
        Button btnConfirm = findViewById(R.id.btnConfirm);

        // Set up the hour picker
        String[] hours = new String[18]; // 7:00 to 24:00
        for (int i = 0; i < 18; i++) {
            int hour = i + 7;
            hours[i] = String.format(Locale.getDefault(), "%02d:00", hour);
        }
        hourPicker.setMinValue(0);
        hourPicker.setMaxValue(hours.length - 1);
        hourPicker.setDisplayedValues(hours);

        btnConfirm.setOnClickListener(v -> {
            if (listener != null) {
                String selectedTime = hours[hourPicker.getValue()];
                listener.onTimeSelected(selectedTime);
            }
            dismiss();
        });

    }

    public void setOnTimeSelectedListener(OnTimeSelectedListener listener) {
        this.listener = listener;
    }

    public interface OnTimeSelectedListener {
        void onTimeSelected(String time);
    }
}
