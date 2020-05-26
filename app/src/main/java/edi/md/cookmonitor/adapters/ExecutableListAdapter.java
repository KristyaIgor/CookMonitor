package edi.md.cookmonitor.adapters;
import android.content.Context;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import edi.md.cookmonitor.NetworkUtils.ServiceResultAndBody.OrdersLinesList;
import edi.md.cookmonitor.R;

/**
 * Created by Igor on 18.05.2020
 */

public class ExecutableListAdapter extends ArrayAdapter<OrdersLinesList> {
    private int resourceLayout;
    private Context mContext;
    private MediaPlayer ring;

    public ExecutableListAdapter(Context context, int resource, List<OrdersLinesList> items) {
        super(context, resource, items);
        this.resourceLayout = resource;
        this.mContext = context;
//        ring = MediaPlayer.create(mContext,R.raw.alert);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(mContext);
            v = vi.inflate(resourceLayout, null);
        }

        OrdersLinesList item = getItem(position);

        if (item != null) {
            TextView name = (TextView) v.findViewById(R.id.text_assortment_name_fin);
            TextView prepRate = (TextView) v.findViewById(R.id.time_remain);
            TextView number = (TextView) v.findViewById(R.id.txt_no_order_fin);
            TextView count = (TextView) v.findViewById(R.id.txt_count_fin);
            TextView comment = (TextView) v.findViewById(R.id.txt_comment_executable_line);
            TextView TextTime = (TextView) v.findViewById(R.id.txt_time);

            if (name != null) {
                name.setText(item.getAssortimentName());
            }
            if (prepRate != null) {
                int time_remain = 0;
                int mPreparationRate = item.getPreparationRate();
                int mMinutesLeft = item.getMinutesLeft();

                if (mPreparationRate - mMinutesLeft < 0) {
                    time_remain = Math.abs(mPreparationRate - mMinutesLeft);
                } else {
                    time_remain = mPreparationRate - mMinutesLeft;
                }
                if (time_remain < 60 && mPreparationRate - mMinutesLeft > 0) {
                    prepRate.setText(time_remain + " минут");
                } else if (time_remain < 60 && mPreparationRate - mMinutesLeft < 0) {
                    prepRate.setText("-" + time_remain + " минут");
                } else if (time_remain > 60 && time_remain < 120) {
                    prepRate.setText("-1 час и " + (time_remain - 60) + " мин.");
                } else if (time_remain > 120 && time_remain < 180) {
                    prepRate.setText("-2 часа и " + (time_remain - 120) + " мин.");
                }
                else if(time_remain > 180 && time_remain < 240){
                    prepRate.setText("-3 часа и " + (time_remain - 240) + " мин.");
                }
                else if(time_remain > 240 && time_remain < 300){
                    prepRate.setText("-4 часа и " + (time_remain - 300) + " мин.");
                }
                else if(mPreparationRate - mMinutesLeft == 0){
                    prepRate.setText("0 минут");
                }
                else{
                    prepRate.setText(time_remain + " мин.");
                }
                if(mPreparationRate - mMinutesLeft < 0){
//                    ring.start();
                    TextTime.setBackgroundColor(mContext.getResources().getColor(R.color.aftertimeColor));
                    prepRate.setBackgroundColor(mContext.getResources().getColor(R.color.aftertimeColor));
                }
            }
            if (number != null) {
                number.setText(String.valueOf(item.getNumber()));
            }
            if (count != null) {
                count.setText(String.format("%.2f", item.getCount()));
            }
            if (comment != null) {
                comment.setText(item.getComment());
            }
        }

        return v;
    }
}
