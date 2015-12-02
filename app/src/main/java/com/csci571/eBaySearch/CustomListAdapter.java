package com.csci571.eBaySearch;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


public class CustomListAdapter extends ArrayAdapter<String> {

    private final Activity context;
    private final String[] itemTitle;
    private final String[] itemDesc;
    private final byte[][] imageByteArray;
    ImageView image;

    public CustomListAdapter(Activity context, String[] itemTitle, String[] itemDesc, byte[][] imageByteArray) {
        super(context, R.layout.list_layout, itemTitle);
        // TODO Auto-generated constructor stub

        this.context=context;
        this.itemTitle=itemTitle;
        this.itemDesc=itemDesc;
        this.imageByteArray=imageByteArray;
    }

    public View getView(int position,View view,ViewGroup parent) {
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.list_layout, null,true);

        TextView tvTitle = (TextView) rowView.findViewById(R.id.tvTitle);
        image = (ImageView) rowView.findViewById(R.id.image);
        TextView tvDesc = (TextView) rowView.findViewById(R.id.tvDesc);

        tvTitle.setText(itemTitle[position]);
        tvTitle.setTag(new Integer(position));
        tvDesc.setText(itemDesc[position]);
        image.setImageBitmap(BitmapFactory.decodeByteArray(imageByteArray[position], 0, imageByteArray[position].length));
        image.setTag(new Integer(position));
        return rowView;

    }

}
