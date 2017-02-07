package com.udacity.stockhawk;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateValueFormatter implements IAxisValueFormatter {

    private float stockDate;

    public DateValueFormatter (Float stockDate) {
        this.stockDate = stockDate;
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {

        String formattedDate = "";

        try {
                Date date = new Date();

                SimpleDateFormat format = new SimpleDateFormat("dd-MM-yy", Locale.getDefault());

                date.setTime((long)(stockDate + value));

                formattedDate = format.format(date);

        } catch (IllegalArgumentException e) {

        }

        return formattedDate;
    }
}
