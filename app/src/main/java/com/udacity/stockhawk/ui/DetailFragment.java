package com.udacity.stockhawk.ui;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.udacity.stockhawk.R.id.change;
import static com.udacity.stockhawk.R.id.symbol;

/**
 * Created by Ahmed on 2017/02/04.
 */

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = DetailFragment.class.getSimpleName();
    @BindView(R.id.line_chart)
    public LineChart mLineChart;
    @BindView(symbol)
    TextView mSymbol;
    @BindView(R.id.price)
    TextView mPrice;
    @BindView(change)
    TextView mChange;
    public static final String DETAIL_URI = "URI";
    private static final int DETAIL_LOADER = 0;
    private Uri mUri;
    private DecimalFormat dollarFormat;
    private DecimalFormat dollarFormatWithPlus;
    private DecimalFormat percentageFormat;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Bundle args = getArguments();
        if (args != null) {
            mUri = args.getParcelable(DetailFragment.DETAIL_URI);
        }

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        ButterKnife.bind(this, rootView);

        dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus.setPositivePrefix("+$");
        percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
        percentageFormat.setMaximumFractionDigits(2);
        percentageFormat.setMinimumFractionDigits(2);
        percentageFormat.setPositivePrefix("+");

        return rootView;

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (null != mUri) {
            return new CursorLoader(getActivity(), mUri, Contract.Quote.QUOTE_COLUMNS, null, null, null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            List<Float> stockDates = new ArrayList<>();
            List<Float> stockValues = new ArrayList<>();
            List<Entry> stockEntries = new ArrayList<>();

            String symbol = data.getString(Contract.Quote.POSITION_SYMBOL);

            // set the data for the current stock details area
            setCurrentStockDetails(symbol, data);

            String stockHistory = data.getString(Contract.Quote.POSITION_HISTORY);

            // add the individual stock items to an array to further split them
            // currently each stock and date row is separated by a new line \n
            String[] historyItems = stockHistory.split("\\n");

            for (String stockItem: historyItems) {
                // string array for holding the stock and date row
                // left value being the date and right value being the stock value
                String[] stockAndValue = stockItem.split(":");
                // the date
                stockDates.add(Float.valueOf(stockAndValue[0]));
                // the stock value
                stockValues.add(Float.valueOf(stockAndValue[1]));
            }

            Collections.reverse(stockDates);
            Collections.reverse(stockValues);
            Float date = stockDates.get(0);

            // create a entry pair of date and stock value to be used for the x and y
            // respectively
            for (int i = 0; i < stockDates.size(); i++) {
                stockEntries.add(new Entry(stockDates.get(i) - date, stockValues.get(i)));
            }

            Description lineChartDescription = new Description();
            lineChartDescription.setText(symbol);

            generateChart(stockEntries, lineChartDescription);
        }

    }

    private void setCurrentStockDetails(String symbol, Cursor data)
    {
        float rawAbsoluteChange = data.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
        float percentageChange = data.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);
        String percentage = percentageFormat.format(percentageChange / 100);

        getActivity().setTitle(symbol);
        mSymbol.setText(symbol);
        mPrice.setText(dollarFormat.format(data.getFloat(Contract.Quote.POSITION_PRICE)));
        if (rawAbsoluteChange > 0) {
            mChange.setBackgroundResource(R.drawable.percent_change_pill_green);
        } else {
            mChange.setBackgroundResource(R.drawable.percent_change_pill_red);
        }
        if (PrefUtils.getDisplayMode(getActivity())
                .equals(getActivity().getString(R.string.pref_display_mode_absolute_key))) {
            mChange.setText(dollarFormatWithPlus.format(rawAbsoluteChange));
        } else {
            mChange.setText(percentage);
        }
    }

    private void generateChart(List<Entry> stockEntries, Description description)
    {
        mLineChart.setDescription(description);

        // configuring the line data set
        LineDataSet lineDataSet = new LineDataSet(stockEntries, "");
        lineDataSet.setColor(R.color.chart_line_color);
        lineDataSet.setLineWidth(1f);
        lineDataSet.setDrawValues(false);
        lineDataSet.setHighLightColor(R.color.chart_line_color);
        lineDataSet.setDrawHighlightIndicators(false);
        lineDataSet.setDrawFilled(true);
        lineDataSet.setFillColor(R.color.chart_line_color);

        LineData lineData = new LineData(lineDataSet);
        mLineChart.setData(lineData);

        // configuring the y axis
        YAxis yAxisRight = mLineChart.getAxisRight();
        yAxisRight.setEnabled(false);

        YAxis yAxis = mLineChart.getAxisLeft();
        yAxis.setTextColor(R.color.chart_line_color);
        yAxis.setDrawGridLines(false);
        yAxis.setAxisLineWidth(1.5f);
        yAxis.setTextSize(12f);
        yAxis.setDrawZeroLine(true);
        yAxis.setAxisLineColor(R.color.chart_line_color);

        // configuring the x axis
        XAxis xAxisConfig = mLineChart.getXAxis();
        xAxisConfig.setTextColor(R.color.chart_line_color);
        xAxisConfig.setDrawGridLines(false);
        xAxisConfig.setAxisLineColor(R.color.chart_line_color);
        xAxisConfig.setTextSize(13f);
        xAxisConfig.setAxisLineWidth(2f);
        xAxisConfig.setPosition(XAxis.XAxisPosition.BOTTOM);

        Legend legend = mLineChart.getLegend();
        legend.setEnabled(false);

        mLineChart.animateX(1500, Easing.EasingOption.Linear);
        mLineChart.setExtraOffsets(10, 0, 0, 10);
    }


    @Override
    public void onLoaderReset(Loader loader) {

    }
}
