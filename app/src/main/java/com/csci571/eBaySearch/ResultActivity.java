package com.csci571.eBaySearch;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import static java.lang.Float.parseFloat;


public class ResultActivity extends ActionBarActivity{

    String[] itemTitle = { "",  "",  "",  "",  "" };
    String[] itemDesc = { "",  "",  "",  "",  "" };
    byte[][] byteArray = new byte[5][];
    TextView tvHeader;
    ListView listView;
    JSONObject jsonObj;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        try {
            parseJSONData();
        }catch(JSONException e){
            e.printStackTrace();
        }
        setContentView(R.layout.result_activity);
        CustomListAdapter adapter = new CustomListAdapter(this,itemTitle,itemDesc,byteArray);
        listView = (ListView) findViewById(R.id.listView);
        listView.addHeaderView(getLayoutInflater().inflate(R.layout.header_layout, null, false),null,true);
        listView.setAdapter(adapter);
        tvHeader = (TextView)findViewById(R.id.tvHeader);
        tvHeader.setText("Results for '"+getIntent().getExtras().getString("keywords")+"'");
    }

    private void parseJSONData() throws  JSONException{

        jsonObj = new JSONObject(getIntent().getExtras().getString("JSONString"));

        for(int i = 0;i<5;i++){
            itemTitle[i] = jsonObj.getJSONObject("item"+i).getJSONObject("basicInfo").getString("title");
        }

        for(int i = 0;i<5;i++){
            String temp = "Price: $";
            if(jsonObj.getJSONObject("item"+i).getJSONObject("basicInfo").getString("convertedCurrentPrice").isEmpty())
                temp+="N/A";
            else
            temp+=jsonObj.getJSONObject("item"+i).getJSONObject("basicInfo").getString("convertedCurrentPrice");
            if(!jsonObj.getJSONObject("item" + i).getJSONObject("basicInfo").getString("shippingServiceCost").isEmpty()){

               if(parseFloat(jsonObj.getJSONObject("item" + i).getJSONObject("basicInfo").getString("shippingServiceCost"))>0.0)
                temp+=" (+ $"+jsonObj.getJSONObject("item" + i).getJSONObject("basicInfo").getString("shippingServiceCost")+" Shipping)";
               else
                temp+=" (FREE Shipping)";
            }
            else if(jsonObj.getJSONObject("item"+i).getJSONObject("basicInfo").getString("convertedCurrentPrice").isEmpty()) ;
            else
              temp+=" (FREE Shipping)";
            itemDesc[i] = temp;
        }

        byteArray[0] = getIntent().getExtras().getByteArray("image1");
        byteArray[1] = getIntent().getExtras().getByteArray("image2");
        byteArray[2] = getIntent().getExtras().getByteArray("image3");
        byteArray[3] = getIntent().getExtras().getByteArray("image4");
        byteArray[4] = getIntent().getExtras().getByteArray("image5");

    }

    public void onClick(View v) {
        TextView tv = (TextView) v;
        Intent myIntent = new Intent(ResultActivity.this,DetailsActivity.class);
        myIntent.putExtra("JSONString",jsonObj.toString());
        myIntent.putExtra("index",(Integer)v.getTag());
        startActivity(myIntent);
    }

    public void showPage(View v) {
        try{
            Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse(jsonObj.getJSONObject("item"+(Integer)v.getTag()).getJSONObject("basicInfo").getString("viewItemURL")));
            startActivity(browserIntent);
        }catch(JSONException e){
            e.printStackTrace();
        }
    }

}
