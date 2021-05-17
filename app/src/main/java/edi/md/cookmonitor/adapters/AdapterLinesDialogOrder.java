package edi.md.cookmonitor.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import edi.md.cookmonitor.NetworkUtils.ServiceResultAndBody.LineOrdersList;
import edi.md.cookmonitor.R;

public class AdapterLinesDialogOrder extends RecyclerView.Adapter<AdapterLinesDialogOrder.MyViewHolder>{
    List<LineOrdersList> lineOrdersLists = new ArrayList<>();

    public AdapterLinesDialogOrder(List<LineOrdersList> list) {
        this.lineOrdersLists = list;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_orders_lines, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterLinesDialogOrder.MyViewHolder holder, int position) {
        LineOrdersList line = lineOrdersLists.get(position);

        holder.name.setText(line.getAssortimentName());
        holder.count.setText(String.valueOf(line.getCount()));
        holder.comments.setText(line.getComment());
    }


    @Override
    public int getItemCount() {
        return lineOrdersLists.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView name;
        private TextView count;
        private TextView comments;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.text_assortment_name_line);
            count = itemView.findViewById(R.id.txt_count_lines);
            comments = itemView.findViewById(R.id.textCommentsLine);
        }
    }
}
