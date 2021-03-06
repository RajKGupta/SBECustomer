package com.example.soumyaagarwal.customerapp.adapter;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.soumyaagarwal.customerapp.Model.measurement;
import com.example.soumyaagarwal.customerapp.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class measurement_adapter extends RecyclerView.Adapter<measurement_adapter.MyViewHolder> {
    List<measurement> list = new ArrayList<>();
    private Context context;
    measurement_adapterListener measurement_adapterListener;

    public measurement_adapter(List<measurement> list, Context context, measurement_adapterListener measurement_adapterListener) {
        this.list = list;
        this.context = context;
        this.measurement_adapterListener = measurement_adapterListener;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tag, width, height, unit;
        CircleImageView fleximage;
        LinearLayout ll_amount;

        public MyViewHolder(View itemView) {
            super(itemView);
            tag = (TextView) itemView.findViewById(R.id.tag);
            width = (TextView) itemView.findViewById(R.id.width);
            height = (TextView) itemView.findViewById(R.id.height);
            fleximage = (CircleImageView) itemView.findViewById(R.id.fleximage);
            unit = (TextView) itemView.findViewById(R.id.unit);
            ll_amount = (LinearLayout)itemView.findViewById(R.id.ll_amount);
        }

    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.measurement_row, parent, false);
        return new MyViewHolder(view);

    }

    @Override
    public void onBindViewHolder(final measurement_adapter.MyViewHolder holder, int position) {
        measurement msr = list.get(position);
        String unit = msr.getUnit();
        holder.width.setText(msr.getWidth()+ " "+ unit);
        holder.height.setText(msr.getHeight()+" "+ unit);
        holder.unit.setText(msr.getUnit());
        holder.tag.setText(msr.getTag());
        if(!msr.getFleximage().equals(""))
            Picasso.with(context).load(msr.getFleximage()).placeholder(R.drawable.wait).into(holder.fleximage);
        if(msr.getAmount()!=null&&!msr.getAmount().equals("")) {
            holder.ll_amount.setVisibility(View.VISIBLE);
            holder.unit.setText("Rs."+msr.getAmount());
        }
        else
            holder.ll_amount.setVisibility(View.GONE);

        applyClickEvents(holder, position);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public interface measurement_adapterListener {
        void onImageClicked(int position,MyViewHolder holder);
    }
    private void applyClickEvents(final MyViewHolder holder, final int position) {

        holder.fleximage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                measurement_adapterListener.onImageClicked(position,holder);
            }
        });
    }
}


