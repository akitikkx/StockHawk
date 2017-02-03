package com.udacity.stockhawk.widget;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class CollectionRemoteViewsService extends RemoteViewsService {

    public static final String LOG_TAG = CollectionRemoteViewsService.class.getSimpleName();
    private DecimalFormat dollarFormat;
    private DecimalFormat dollarFormatWithPlus;
    private DecimalFormat percentageFormat;
    private int changeBackground;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {


            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }

                final long identityToken = Binder.clearCallingIdentity();
                data = getContentResolver().query(Contract.Quote.URI, Contract.Quote.QUOTE_COLUMNS, null, null, Contract.Quote.COLUMN_SYMBOL);
                Binder.restoreCallingIdentity(identityToken);

            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }

                dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
                dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
                dollarFormatWithPlus.setPositivePrefix("+$");
                percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());


                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.widget_quote_collection_item);

                String symbol = data.getString(Contract.Quote.POSITION_SYMBOL);
                float price = data.getFloat(Contract.Quote.POSITION_PRICE);
                float rawAbsoluteChange = data.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);

                if (rawAbsoluteChange > 0) {
                    changeBackground = R.drawable.percent_change_pill_green;
                } else {
                    changeBackground = R.drawable.percent_change_pill_red;
                }

                views.setTextViewText(R.id.symbol, symbol);
                views.setTextViewText(R.id.price, dollarFormat.format(price));
                views.setTextViewText(R.id.change, dollarFormatWithPlus.format(rawAbsoluteChange));
                views.setInt(R.id.change, "setBackgroundResource", changeBackground);

                final Intent fillInIntent = new Intent();
                Uri collectionItem = Contract.Quote.makeUriForStock(symbol);
                fillInIntent.setData(collectionItem);
                views.setOnClickFillInIntent(R.id.widget_quote_collection_item, fillInIntent);

                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return null;
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                return data.moveToPosition(position) ? data.getLong(Contract.Quote.POSITION_ID) : position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
