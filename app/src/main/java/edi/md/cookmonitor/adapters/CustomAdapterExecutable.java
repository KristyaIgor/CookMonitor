package edi.md.cookmonitor.adapters;
import android.content.Context;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.List;
import java.util.Map;

import edi.md.cookmonitor.MainActivity;
import edi.md.cookmonitor.R;

/**
 * Created by Igor on 20.09.2019
 */

public class CustomAdapterExecutable extends BaseAdapter {

    private Context context;
    private List<? extends Map<String, ?>> mData;
    //private Animation animBlink;
    private MediaPlayer ring;

    public CustomAdapterExecutable(Context context, List<? extends Map<String, ?>> data) {
        this.context = context;
        mData = data;
        // load the animation
       // animBlink = AnimationUtils.loadAnimation(context, R.anim.blink);
        ring = MediaPlayer.create(context,R.raw.alert);
    }

    // View lookup cache
    private static class ViewHolder {
        TextView AssortmentName;
        TextView AssortmentTime;
        TextView OrderNumber;
        TextView AssortmentCount;
        TextView TextTime;
        ConstraintLayout constraintLayout;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder = null;

        final View result;

        if(view == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(context);

            view = inflater.inflate(R.layout.item_execut, viewGroup, false);

            viewHolder.AssortmentName = (TextView) view.findViewById(R.id.text_assortment_name_fin);
            viewHolder.AssortmentTime = (TextView) view.findViewById(R.id.time_remain);
            viewHolder.AssortmentCount = (TextView) view.findViewById(R.id.txt_count_fin);
            viewHolder.OrderNumber = (TextView) view.findViewById(R.id.txt_no_order_fin);
            viewHolder.TextTime = (TextView) view.findViewById(R.id.txt_time);

        }


        if (viewHolder != null) {
            viewHolder.AssortmentName.setText((String) mData.get(position).get("Name"));
            viewHolder.AssortmentTime.setText((String) mData.get(position).get("MinutesLeft"));
            viewHolder.OrderNumber.setText(String.valueOf(mData.get(position).get("Number")));
            viewHolder.AssortmentCount.setText((String) mData.get(position).get("Count"));

            int PrepRate = (Integer) mData.get(position).get("PrepRate");
            int Minutes = (Integer) mData.get(position).get("Minutes");

            if (PrepRate - Minutes < 0) {
                ring.start();
                viewHolder.TextTime.setBackgroundColor(context.getResources().getColor(R.color.aftertimeColor));
                viewHolder.AssortmentTime.setBackgroundColor(context.getResources().getColor(R.color.aftertimeColor));
                //viewHolder.constraintLayout.startAnimation(animBlink);
            }
        }
        return view;
    }
    public void clearData() {
        // clear the data
        mData.clear();
    }
}
