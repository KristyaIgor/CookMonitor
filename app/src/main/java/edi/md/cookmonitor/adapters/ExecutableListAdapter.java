package edi.md.cookmonitor.adapters;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Random;

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
            TextView textTime = (TextView) v.findViewById(R.id.textViewTimeBackground);

            if (name != null) {
                name.setText(item.getAssortimentName());
            }
            if (prepRate != null) {
                int time_remain = 0;
                int mPreparationRate = item.getPreparationRate();
                int mMinutesLeft = item.getMinutesLeft();

                if(mPreparationRate == 0){
                    prepRate.setText("--");

                    Drawable background = textTime.getBackground();

                    if (background instanceof ShapeDrawable) {
                        int color = Color.rgb(255, 255, 255);
                        ((ShapeDrawable)background).getPaint().setColor(color);
                        textTime.setBackground(background);
                    }
                    else if (background instanceof GradientDrawable) {
                        int color = Color.rgb(255, 255, 255);
                        ((GradientDrawable)background).setColor(color);
                        textTime.setBackground(background);
                    }
                    else if (background instanceof ColorDrawable) {
                        int color = Color.rgb(255, 255, 255);
                        ((ColorDrawable)background).setColor(color);
                        textTime.setBackground(background);
                    }
                }
                else{
                    if (mPreparationRate - mMinutesLeft < 0) {
                        time_remain = Math.abs(mPreparationRate - mMinutesLeft);
                    }
                    else {
                        time_remain = mPreparationRate - mMinutesLeft;
                    }
                    if (time_remain < 60 && mPreparationRate - mMinutesLeft > 0) {
                        prepRate.setText(time_remain + "m");
                    } else if (time_remain < 60 && mPreparationRate - mMinutesLeft < 0) {
                        prepRate.setText("-" + time_remain + "m");
                    } else if (time_remain > 60 && time_remain < 120) {
                        prepRate.setText("-1h " + (time_remain - 60) + "m");
                    } else if (time_remain > 120 && time_remain < 180) {
                        prepRate.setText("-2h " + (time_remain - 120) + "m");
                    }
                    else if(time_remain > 180 && time_remain < 240){
                        prepRate.setText("-3h " + (time_remain - 240) + "m");
                    }
                    else if(time_remain > 240 && time_remain < 300){
                        prepRate.setText("-4h " + (time_remain - 300) + "m");
                    }
                    else {
                        prepRate.setText(time_remain + "m");
                    }


                    if(mPreparationRate - mMinutesLeft >= 0 && mPreparationRate - mMinutesLeft <= 3){

                        Drawable background = textTime.getBackground();

                        if (background instanceof ShapeDrawable) {
                            int color = Color.rgb(255, 187, 51);
                            ((ShapeDrawable)background).getPaint().setColor(color);
                            textTime.setBackground(background);
                        }
                        else if (background instanceof GradientDrawable) {
                            int color = Color.rgb(255, 187, 51);
                            ((GradientDrawable)background).setColor(color);
                            textTime.setBackground(background);
                        }
                        else if (background instanceof ColorDrawable) {
                            int color = Color.rgb(255, 187, 51);
                            ((ColorDrawable)background).setColor(color);
                            textTime.setBackground(background);
                        }
                    }
                    else if(mPreparationRate - mMinutesLeft < 0){
                        Drawable background = textTime.getBackground();

                        if (background instanceof ShapeDrawable) {
                            int color = Color.rgb(254, 79, 77);
                            ((ShapeDrawable)background).getPaint().setColor(color);
                            textTime.setBackground(background);
                        }
                        else if (background instanceof GradientDrawable) {
                            int color = Color.rgb(254, 79, 77);
                            ((GradientDrawable)background).setColor(color);
                            textTime.setBackground(background);
                        }
                        else if (background instanceof ColorDrawable) {
                            int color = Color.rgb(254, 79, 77);
                            ((ColorDrawable)background).setColor(color);
                            textTime.setBackground(background);
                        }
                    }
                    else{
                        Drawable background = textTime.getBackground();

                        if (background instanceof ShapeDrawable) {
                            int color = Color.rgb(255, 255, 255);
                            ((ShapeDrawable)background).getPaint().setColor(color);
                            textTime.setBackground(background);
                        }
                        else if (background instanceof GradientDrawable) {
                            int color = Color.rgb(255, 255, 255);
                            ((GradientDrawable)background).setColor(color);
                            textTime.setBackground(background);
                        }
                        else if (background instanceof ColorDrawable) {
                            int color = Color.rgb(255, 255, 255);
                            ((ColorDrawable)background).setColor(color);
                            textTime.setBackground(background);
                        }
                    }
                }

            }
            if (number != null) {
                number.setText(String.valueOf(item.getNumber()));
            }
            if (count != null) {
                count.setText(String.format("%.2f", item.getCount()).replace(",","."));
            }
            if (item.getComment() != null && !item.getComment().equals("")) {
                comment.setVisibility(View.VISIBLE);
                comment.setText(item.getComment());
            }
            else
                comment.setVisibility(View.GONE);
        }

        return v;
    }
}
