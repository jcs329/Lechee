package com.quantatw.roomhub.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.quantatw.myapplication.R;

import java.util.ArrayList;

/**
 * Created by 10110012 on 2016/5/6.
 */
public class ButtonTestAdapter extends BaseAdapter {

    public interface ButtonClickListener
    {
        void onButtonClick(int position, int id);
    }
    class ItemButton_Click implements View.OnClickListener
    {
        private int position;

        ItemButton_Click(int pos) {
            position = pos;
        }

        public void onClick(View v)
        {
            if (mButtonClickListener != null) {
                mButtonClickListener.onButtonClick(position, v.getId());
            }
        }
    }

    class Holder{
        Button btn1;
        Button btn2;
        TextView textStep;
        TextView textAction;
    }

    private LayoutInflater mInflater;
    private Context mContext;
    ButtonClickListener mButtonClickListener;

    private ArrayList<ButtonEntry> list = new ArrayList<ButtonEntry>();

    public ButtonTestAdapter(Context context,ButtonClickListener listener) {
        this.mInflater = LayoutInflater.from(context);
        mContext = context;
        mButtonClickListener = listener;
    }
    public void addTestButton(ButtonEntry buttonEntry){
        list.add(buttonEntry);
    }
    public void updateButtonEnable(int position){
        list.get(position).enable = true;
        notifyDataSetChanged();
    }
    public void updateButtonSelected(int position,int btnIndex){
        if(btnIndex == 0) {
            list.get(position).button1Selected = true;
        }else {
            list.get(position).button2Selected = true;
        }
        notifyDataSetChanged();
    }
    public void clear(){
        list.clear();
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public ButtonEntry getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;
        if(convertView == null){
            convertView = mInflater.inflate(R.layout.ir_pair_test_item, parent, false);
            holder = new Holder();
            holder.btn1 = (Button) convertView.findViewById(R.id.btn1);
            holder.btn2 = (Button) convertView.findViewById(R.id.btn2);
            holder.textStep = (TextView) convertView.findViewById(R.id.txt_step);
            holder.textAction = (TextView) convertView.findViewById(R.id.txt_action);

            convertView.setTag(holder);
        } else{
            holder = (Holder) convertView.getTag();
        }
        ButtonEntry entry = list.get(position);
        holder.btn1.setBackgroundResource(entry.button1ResID);
        holder.btn1.setOnClickListener(new ItemButton_Click(position));
        holder.btn1.setEnabled(entry.enable);
        holder.btn1.setSelected(entry.button1Selected);
        if (entry.button2ResID != 0) {
            holder.btn2.setVisibility(View.VISIBLE);
            holder.btn2.setBackgroundResource(entry.button2ResID);
            holder.btn2.setOnClickListener(new ItemButton_Click(position));
            holder.btn2.setSelected(entry.button2Selected);
        } else {
            holder.btn2.setVisibility(View.GONE);
        }
        holder.btn2.setEnabled(entry.enable);
        holder.textStep.setText(String.format(mContext.getResources().getString(R.string.ir_pairing_step),position+1));
        holder.textAction.setText(entry.actionResID);
        return convertView;
    }
}
