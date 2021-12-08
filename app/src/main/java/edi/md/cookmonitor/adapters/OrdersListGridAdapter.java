package edi.md.cookmonitor.adapters;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

import edi.md.cookmonitor.NetworkUtils.ServiceResultAndBody.LineOrdersList;
import edi.md.cookmonitor.NetworkUtils.ServiceResultAndBody.OrdersList;
import edi.md.cookmonitor.Obejcts.Order;
import edi.md.cookmonitor.R;

/**
 * Created by Igor on 10.02.2020
 */

public class OrdersListGridAdapter extends ArrayAdapter<Order> {
    Context context;
    SimpleDateFormat simpleDateFormatMD;
    TimeZone timeZoneMD;

    public OrdersListGridAdapter(@NonNull Context context, int resource, @NonNull List<Order> objects) {
        super(context, resource, objects);
        this.context = context;

        simpleDateFormatMD = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        timeZoneMD = TimeZone.getTimeZone("Europe/Chisinau");
        simpleDateFormatMD.setTimeZone(timeZoneMD);

    }

    private static class ViewHolder {
        TextView number, date , lines, state , linesCount, linesState;
        ConstraintLayout parent;
        ImageView deliveryType;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        ViewHolder viewHolder;
        if(convertView == null){
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());

            convertView = inflater.inflate(R.layout.item_grid_orders_list,parent,false);

            viewHolder.number = convertView.findViewById(R.id.number_order);
            viewHolder.date = convertView.findViewById(R.id.textView21);
            viewHolder.state = convertView.findViewById(R.id.tv_state_bill);

            viewHolder.lines = convertView.findViewById(R.id.lines_order);
            viewHolder.linesCount = convertView.findViewById(R.id.tv_count_lines);
            viewHolder.linesState = convertView.findViewById(R.id.tv_lines_state);

            viewHolder.deliveryType = convertView.findViewById(R.id.img_delivery_type);

            viewHolder.parent = convertView.findViewById(R.id.ll_item_grid_view);


            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Order item = getItem(position);
        if (item != null) {
            viewHolder.number.setText(String.valueOf(item.getNumber()));
            viewHolder.date.setText(simpleDateFormatMD.format(item.getDateCreated()));

            int states = item.getState();
            String orderState = "";
            switch (states){
                case 0: {
                    orderState = context.getString(R.string.order_Not_set);
                }break;
                case 1: {
                    orderState = context.getString(R.string.order_Indefinite);
                }break;
                case 2: {
                    orderState = context.getString(R.string.order_Received);
                    viewHolder.parent.setBackgroundColor(context.getResources().getColor(R.color.color_new_order));
                }break;
                case 3: {
                    orderState = context.getString(R.string.order_Working);
                }break;
                case 4:{
                    orderState = context.getString(R.string.order_Executed);
                }break;
                case 5: {
                    orderState = context.getString(R.string.order_Canceled);
                }break;
                case 6: {
                    orderState = context.getString(R.string.order_Prepared);
                }break;
            }

            if(item.isDone())
                viewHolder.parent.setBackgroundColor(context.getResources().getColor(R.color.executeColor));

            int delivery = 0;
            if(item.getDeliveryType() != null)
                delivery = item.getDeliveryType();

            switch (delivery){
                case 0: {
                    viewHolder.deliveryType.setImageResource(R.drawable.local_0);
                }break;
                case 1: {
                    viewHolder.deliveryType.setImageResource(R.drawable.pb_logo);
                }break;
                case 2: {
                    viewHolder.deliveryType.setImageResource(R.drawable.bag_icon);
                }break;
                case 3: {
                    viewHolder.deliveryType.setImageResource(R.drawable.table);
                }break;
            }

            viewHolder.state.setText(orderState);

            if(item.getLines() != null && item.getLines().size() > 0){
                String linesName = "";
                String linesCount = "";
                String linesState = "";
                for(LineOrdersList lineOrdersList : item.getLines()){
                    linesName = linesName + "\n" + lineOrdersList.getAssortimentName();
                    linesCount = linesCount + "\n" + String.format("%.0f",lineOrdersList.getCount());

                    int stateLine = lineOrdersList.getState();
                    switch (stateLine) {
                        case 0: {
                            linesState = linesState + "\n" + context.getString(R.string.order_Not_set);
                        }
                        break;
                        case 1: {
                            linesState = linesState + "\n" + context.getString(R.string.order_Indefinite);
                        }
                        break;
                        case 2: {
                            linesState = linesState + "\n" + context.getString(R.string.order_Received);
                        }
                        break;
                        case 3: {
                            linesState = linesState + "\n" + context.getString(R.string.order_Working);
                        }
                        break;
                        case 4: {
                            linesState = linesState + "\n" + context.getString(R.string.order_Executed);
                        }
                        break;
                        case 5: {
                            linesState = linesState + "\n" + context.getString(R.string.order_Canceled);
                        }
                        break;
                        case 6: {
                            linesState = linesState + "\n" + context.getString(R.string.order_Prepared);
                        }
                    }

                }

                    viewHolder.lines.setText(linesName);
                    viewHolder.linesCount.setText(linesCount);
                    viewHolder.linesState.setText(linesState);
            }
        }
        else{
            viewHolder.number.setText("");
            viewHolder.date.setText("");
        }

        return convertView;
    }
}
