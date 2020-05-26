package edi.md.cookmonitor.adapters;
import android.content.Context;
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

public class InQueueListAdapter extends ArrayAdapter<OrdersLinesList> {

    private int resourceLayout;
    private Context mContext;

    public InQueueListAdapter(Context context, int resource, List<OrdersLinesList> items) {
        super(context, resource, items);
        this.resourceLayout = resource;
        this.mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(mContext);
            v = vi.inflate(resourceLayout, null);
        }

        OrdersLinesList p = getItem(position);

        if (p != null) {
            TextView name = (TextView) v.findViewById(R.id.text_assortment_name);
            TextView prepRate = (TextView) v.findViewById(R.id.time_cook);
            TextView number = (TextView) v.findViewById(R.id.txt_no_order);
            TextView count = (TextView) v.findViewById(R.id.txt_count);
            TextView comment = (TextView) v.findViewById(R.id.txt_comment_line);

            if (name != null) {
                name.setText(p.getAssortimentName());
            }
            if (prepRate != null) {
                prepRate.setText(String.valueOf(p.getPreparationRate()));
            }
            if (number != null) {
                number.setText(String.valueOf(p.getNumber()));
            }
            if (count != null) {
                count.setText(String.format("%.2f", p.getCount()));
            }
            if (comment != null) {
                comment.setText(p.getComment());
            }
        }

        return v;
    }

}