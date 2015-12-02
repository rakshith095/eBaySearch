package com.csci571.eBaySearch;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URLEncoder;


public class EbaySearch extends ActionBarActivity implements AdapterView.OnItemSelectedListener {

    TextView tvResults1,tvResults2,tvResults3,tvResults4;
    EditText etKeyword,etPriceFrom,etPriceTo;
    Button clear,search;
    Spinner spinner;
    String keyword,sortBy,parameters;
    String [] sortByList = {"Best Match","Price: highest first","Price + Shipping: highest first","Price + Shipping: lowest first"};
    boolean valid,priceValid;
    float toPrice,fromPrice;
    ArrayAdapter<String> adapterSortBy;
    JSONObject jsonObj;
    HttpClient client;
    final static String URL = "http://cs-server.usc.edu:14748/HW9.php?";
    Bitmap image1,image2,image3,image4,image5;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ebay_search_activity);
        client = new DefaultHttpClient();
        initializeEverything();

        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             tvResults1.setText("");
             tvResults2.setText("");
             tvResults3.setText("");
             tvResults4.setText("");
             etKeyword.setText("");
             etPriceTo.setText("");
             etPriceFrom.setText("");
             spinner.setSelection(0);
            }
        });


        search.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
             inputValidation();
             if(valid){
                 parameters = "&keywords="+ URLEncoder.encode(keyword);
                 if(etPriceFrom.getText().toString().isEmpty())parameters+="&fromRange=";
                 else parameters+="&fromRange="+fromPrice;
                 if(etPriceTo.getText().toString().isEmpty())parameters+="&toRange=";
                 else parameters+="&toRange="+toPrice;
                 parameters+="&sortBy="+sortBy;
                 new GetJSONFromPHP().execute("ack");
             }

            }
        });


    }


    private void inputValidation() {
        valid = true;
        priceValid = true;
        tvResults1.setText("");
        tvResults2.setTextSize(14);
        tvResults2.setText("");
        tvResults3.setText("");
        tvResults4.setText("");

        if(etKeyword.getText().toString().isEmpty())
        {
            tvResults1.setText("Please enter a keyword");
            valid = false;
        }
        else
         keyword = etKeyword.getText().toString();


        if (!etPriceFrom.getText().toString().isEmpty() || !etPriceTo.getText().toString().isEmpty()){

            if(!etPriceFrom.getText().toString().isEmpty()) {
                try {
                    fromPrice = Float.parseFloat(etPriceFrom.getText().toString());
                    if(fromPrice < 0){
                     tvResults2.setText("'Price From' must be a positive valid number");
                     valid = false; priceValid = false;
                    }
                } catch (NumberFormatException ex) {
                    tvResults2.setText("'Price From' must be a positive and a valid number");
                    valid = false; priceValid = false;
                }
            }

            if(!etPriceTo.getText().toString().isEmpty()) {
                try {
                    toPrice = Float.parseFloat(etPriceTo.getText().toString());
                    if(toPrice < 0){
                        tvResults3.setText("'Price To' must be a positive number");
                        valid = false; priceValid = false;
                    }
                } catch (NumberFormatException ex) {
                    tvResults3.setText("'Price To' must be a positive and a valid number");
                    valid = false; priceValid = false;
                }
            }
        }

        if(!etPriceFrom.getText().toString().isEmpty() && !etPriceTo.getText().toString().isEmpty() && priceValid){
            if(toPrice<fromPrice){
                valid = false;
                tvResults4.setText("'Price To' cannot be less than 'Price From'");
            }

        }
    }


    private void initializeEverything() {
        tvResults1 = (TextView) findViewById(R.id.tvResult1);
        tvResults2 = (TextView) findViewById(R.id.tvResult2);
        tvResults3 = (TextView) findViewById(R.id.tvResult3);
        tvResults4 = (TextView) findViewById(R.id.tvResult4);
        etKeyword = (EditText) findViewById(R.id.etKeyword);
        etPriceFrom = (EditText) findViewById(R.id.etPriceFrom);
        etPriceTo = (EditText) findViewById(R.id.etPriceTo);
        clear = (Button) findViewById(R.id.btClear);
        search = (Button) findViewById(R.id.btSearch);
        spinner = (Spinner) findViewById(R.id.spSortBy);
        adapterSortBy = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,sortByList);
        adapterSortBy.setDropDownViewResource(android.R.layout.simple_expandable_list_item_1);
        spinner.setAdapter(adapterSortBy);
        spinner.setOnItemSelectedListener(this);
    }


    public JSONObject sendData(String parametersToPHP)throws ClientProtocolException, IOException, JSONException {
        StringBuilder stringBuilder = null;
        InputStream inputStream = null;
        JSONObject result;
        StringBuilder url = new StringBuilder(URL);
        url.append(parametersToPHP);
        HttpGet get = new HttpGet(url.toString());
        HttpResponse r = client.execute(get);
        int status = r.getStatusLine().getStatusCode();
        if (status == 200) {
            HttpEntity e = r.getEntity();
            inputStream = e.getContent();
            BufferedReader bufferReader = new BufferedReader(new InputStreamReader(inputStream));
            stringBuilder = new StringBuilder();
            String line = null;
            while ((line = bufferReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            result = new JSONObject(stringBuilder.toString());
            return result;
        } else {
            Toast.makeText(EbaySearch.this, "error", Toast.LENGTH_SHORT).show();
            return null;
        }
    }


    public class GetJSONFromPHP extends AsyncTask<String, Integer, String> {

        private ProgressDialog progressDialog = new ProgressDialog(EbaySearch.this);

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            progressDialog.show();
            progressDialog.setMessage("Loading...");
            progressDialog.setCancelable(true);
        }

        @Override
        protected String doInBackground(String... params) {
            // TODO Auto-generated method stub
            try {
                jsonObj = sendData(parameters);
                getImageFromJSON();
                return jsonObj.getString(params[0]);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                progressDialog.dismiss();
            }
            return "Failure";
        }


        private void getImageFromJSON() throws JSONException{
            image1 = getImage(jsonObj.getJSONObject("item0").getJSONObject("basicInfo").getString("galleryURL"));
            image2 = getImage(jsonObj.getJSONObject("item1").getJSONObject("basicInfo").getString("galleryURL"));
            image3 = getImage(jsonObj.getJSONObject("item2").getJSONObject("basicInfo").getString("galleryURL"));
            image4 = getImage(jsonObj.getJSONObject("item3").getJSONObject("basicInfo").getString("galleryURL"));
            image5 = getImage(jsonObj.getJSONObject("item4").getJSONObject("basicInfo").getString("galleryURL"));
        }


        private Bitmap getImage(String imageURL) {
            // TODO Auto-generated method stub
            try {
                java.net.URL url = new java.net.URL(imageURL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                return BitmapFactory.decodeStream(input);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }


        @Override
        protected void onPostExecute(String result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            if(result.contentEquals("Success")){
                Intent myIntent = new Intent(EbaySearch.this,ResultActivity.class);
                myIntent.putExtra("JSONString",jsonObj.toString());
                myIntent.putExtra("keywords",keyword);
                myIntent.putExtra("image1",convertBitmapToByteArray(image1));
                myIntent.putExtra("image2",convertBitmapToByteArray(image2));
                myIntent.putExtra("image3",convertBitmapToByteArray(image3));
                myIntent.putExtra("image4",convertBitmapToByteArray(image4));
                myIntent.putExtra("image5",convertBitmapToByteArray(image5));
                progressDialog.dismiss();
                startActivity(myIntent);
            }else{
                tvResults2.setText("No Results Found");
                tvResults2.setTextSize(25);
                progressDialog.dismiss();
            }
        }


        private byte [] convertBitmapToByteArray(Bitmap bmp) {
            // TODO Auto-generated method stub
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            return byteArray;
        }

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        spinner.setSelection(position);
        if(spinner.getSelectedItem().toString().contentEquals("Best Match"))sortBy = "BestMatch";
        else if(spinner.getSelectedItem().toString().contentEquals("Price: highest first"))sortBy = "CurrentPriceHighest";
        else if(spinner.getSelectedItem().toString().contentEquals("Price + Shipping: highest first"))sortBy = "PricePlusShippingHighest";
        else sortBy = "PricePlusShippingLowest";
    }


    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

}
