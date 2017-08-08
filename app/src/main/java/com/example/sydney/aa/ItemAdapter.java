package com.example.sydney.aa;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

/**
 * Created by Poging Adam on 8/4/2017.
 */

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.MyViewHolder> {
    private Context mContext;
    private List<Item> itemList = Collections.emptyList();

    ItemAdapter(Context mContext, List<Item> itemList) {
        this.mContext = mContext;
        this.itemList = itemList;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView itemCode, itemDesc;
        ImageView imageStatus;
        CardView cv;

        MyViewHolder(View view) {
            super(view);
            cv = (CardView) view.findViewById(R.id.cv_item);
            itemCode = (TextView) view.findViewById(R.id.txtCardCode);
            itemDesc = (TextView) view.findViewById(R.id.txtCardDesc);
            imageStatus = (ImageView) view.findViewById(R.id.imageStatus);
        }
    }
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card, parent, false);
        MyViewHolder holder = new MyViewHolder(itemView);
        return holder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position){
//        final Item item = itemList.get(position);

        holder.itemCode.setText(itemList.get(position).getBarcode());
        holder.itemDesc.setText(itemList.get(position).getDescription());
        String s = itemList.get(position).getStatus();
        if(s.equals("1")){
            holder.imageStatus.setImageResource(R.drawable.ic_done_black_1024px);
        }
        else {
            holder.imageStatus.setImageResource(R.drawable.ic_error_black_48px);
        }
    }

    @Override
    public int getItemCount(){return itemList.size();}

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public void insert(int position, Item item){
        itemList.add(position,item);
        notifyItemInserted(position);
    }
}