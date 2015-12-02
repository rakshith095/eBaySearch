package com.csci571.eBaySearch;


import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.InputStream;


import static java.lang.Float.parseFloat;


public class DetailsActivity extends ActionBarActivity implements  View.OnClickListener {

    JSONObject jsonObj,jsonItem;
    ImageView galleryImageView,topRated,sellerTopRated;
    Button buyNowButton,basicInfoButton, sellerButton, shippingButton;
    LinearLayout basicInfoLayout,sellerLayout,shippingLayout;
    RelativeLayout detailsRelativeLayout;
    View hiddenInfo;
    TextView tvTitle,tvPrice,tvLocations,tvCategoryName,tvCondition,tvBuyingFormat;
    TextView tvUserName,tvFeedbackScore,tvPositiveFeedback,tvFeedbackRating,tvStore;
    TextView tvShippingType,tvHandlingTime,tvShippingLocations;
    ImageView ivExpeditedShipping,ivOneDayShipping,ivReturnsAccepted;
    String url,fbTitle,fbDescription,fbItemURL;
    Facebook fb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.details_activity);
        initializeEverything();
        try {
            extractInfoFromJSON();
        }catch(JSONException e){
            e.printStackTrace();
        }
    }


    private void initializeEverything() {
        tvTitle = (TextView) findViewById(R.id.tvDetailsTitle);
        tvPrice = (TextView) findViewById(R.id.tvDetailsPrice);
        tvLocations = (TextView) findViewById(R.id.tvLocations);
        galleryImageView = (ImageView) findViewById(R.id.galleryImageView);
        topRated = (ImageView) findViewById(R.id.ivTopRated);
        buyNowButton = (Button) findViewById(R.id.buyNowButton);
        basicInfoButton = (Button) findViewById(R.id.basicInfoButton);
        sellerButton = (Button) findViewById(R.id.sellerButton);
        shippingButton = (Button) findViewById(R.id.shippingButton);
        basicInfoButton.setBackgroundResource(R.drawable.btn_yellow);
        sellerButton.setBackgroundResource(android.R.drawable.btn_default);
        shippingButton.setBackgroundResource(android.R.drawable.btn_default);
        basicInfoLayout = (LinearLayout) findViewById(R.id.basicInfoLayout);
        sellerLayout = (LinearLayout) findViewById(R.id.sellerLayout);
        shippingLayout = (LinearLayout) findViewById(R.id.shippingLayout);
        detailsRelativeLayout = (RelativeLayout) findViewById(R.id.detailsRelativeLayout);
        hiddenInfo = getLayoutInflater().inflate(R.layout.basic_info_layout, detailsRelativeLayout, false);
        detailsRelativeLayout.addView(hiddenInfo);
        tvCategoryName = (TextView) findViewById(R.id.tvCategoryName);
        tvCondition = (TextView) findViewById(R.id.tvCondition);
        tvBuyingFormat = (TextView) findViewById(R.id.tvBuyingFormat);
        buyNowButton.setOnClickListener(this);
        basicInfoButton.setOnClickListener(this);
        sellerButton.setOnClickListener(this);
        shippingButton.setOnClickListener(this);
    }


    private void extractInfoFromJSON() throws JSONException {

        jsonObj = new JSONObject(getIntent().getExtras().getString("JSONString")) ;
        jsonItem = new JSONObject(jsonObj.getJSONObject("item"+getIntent().getExtras().getInt("index")).toString());
        tvTitle.setText(jsonItem.getJSONObject("basicInfo").getString("title"));
        fbTitle = jsonItem.getJSONObject("basicInfo").getString("title");
        String temp = "Price: $";
        if(jsonItem.getJSONObject("basicInfo").getString("convertedCurrentPrice").isEmpty())
            temp+="N/A";
        else
            temp+=jsonItem.getJSONObject("basicInfo").getString("convertedCurrentPrice");
        if(!jsonItem.getJSONObject("basicInfo").getString("shippingServiceCost").isEmpty()){

            if(parseFloat(jsonItem.getJSONObject("basicInfo").getString("shippingServiceCost"))>0.0)
                temp+=" (+ $"+jsonItem.getJSONObject("basicInfo").getString("shippingServiceCost")+" Shipping)";
            else
                temp+=" (FREE Shipping)";
        }
        else if(jsonItem.getJSONObject("basicInfo").getString("convertedCurrentPrice").isEmpty()) ;
        else
            temp+=" (FREE Shipping)";
        tvPrice.setText(temp);
        tvLocations.setText(jsonItem.getJSONObject("shippingInfo").getString("shipToLocations"));
        fbDescription = temp+" Location(s): "+jsonItem.getJSONObject("shippingInfo").getString("shipToLocations");
        if(!jsonItem.getJSONObject("basicInfo").getString("topRatedListing").contentEquals("false"))
            topRated.setVisibility(View.VISIBLE);
        if(jsonItem.getJSONObject("basicInfo").getString("categoryName").isEmpty())
            tvCategoryName.setText("N/A");
        else
            tvCategoryName.setText(jsonItem.getJSONObject("basicInfo").getString("categoryName"));
        if(jsonItem.getJSONObject("basicInfo").getString("conditionDisplayName").isEmpty())
            tvCondition.setText("N/A");
        else
            tvCondition.setText(jsonItem.getJSONObject("basicInfo").getString("conditionDisplayName"));
        if(jsonItem.getJSONObject("basicInfo").getString("listingType").isEmpty())
            tvBuyingFormat.setText("N/A");
        else
            tvBuyingFormat.setText(jsonItem.getJSONObject("basicInfo").getString("listingType"));
        if(!jsonItem.getJSONObject("basicInfo").getString("pictureURLSuperSize").isEmpty())
            url = jsonItem.getJSONObject("basicInfo").getString("pictureURLSuperSize");
        else
            url = jsonItem.getJSONObject("basicInfo").getString("galleryURL");
        fbItemURL = jsonItem.getJSONObject("basicInfo").getString("viewItemURL");
        new DownloadImageTask(galleryImageView).execute(url);
    }


    public void fbPost(View v) {

        fb = new Facebook("FACEBOOK_APP_ID");
        //String [] permissions = {"email"};
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(DetailsActivity.this);
        String access_token = prefs.getString("access_token", null);
        Long expires = prefs.getLong("access_expires", -1);


        if (access_token != null && expires != -1)
        {
            fb.setAccessToken(access_token);
            fb.setAccessExpires(expires);
        }

        if(fb.isSessionValid()){
            Bundle params = new Bundle();
            params.putString("name",fbTitle);
            params.putString("caption","www.eBay.com");
            params.putString("description",fbDescription);
            params.putString("link",fbItemURL);
            params.putString("picture",url);
            fb.dialog(DetailsActivity.this,"feed",params,new Facebook.DialogListener() {

                @Override
                public void onComplete(Bundle values) {
                    if(values.getString("post_id")==null)
                        Toast.makeText(DetailsActivity.this, "Post Cancelled", Toast.LENGTH_SHORT).show();
                    else
                    Toast.makeText(DetailsActivity.this, "Posted Story, ID:"+values.getString("post_id"), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFacebookError(FacebookError e) {

                }

                @Override
                public void onError(DialogError e) {

                }

                @Override
                public void onCancel() {
                    Toast.makeText(DetailsActivity.this, "Post Cancelled", Toast.LENGTH_SHORT).show();
                }

            });
        }else{

            fb.authorize(DetailsActivity.this, new Facebook.DialogListener() {
                @Override
                public void onComplete(Bundle values) {
                    String token = fb.getAccessToken();
                    long token_expires = fb.getAccessExpires();

                    SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(DetailsActivity.this);

                    prefs.edit().putLong("access_expires", token_expires).commit();

                    prefs.edit().putString("access_token", token).commit();
                    Bundle params = new Bundle();
                    params.putString("name",fbTitle);
                    params.putString("caption","www.eBay.com");
                    params.putString("description",fbDescription);
                    params.putString("link",fbItemURL);
                    params.putString("picture",url);

                    fb.dialog(DetailsActivity.this,"feed",params,new Facebook.DialogListener() {

                        @Override
                        public void onComplete(Bundle values) {
                            if(values.getString("post_id")==null)
                                Toast.makeText(DetailsActivity.this, "Post Cancelled", Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(DetailsActivity.this, "Posted Story, ID:"+values.getString("post_id"), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFacebookError(FacebookError e) {

                        }

                        @Override
                        public void onError(DialogError e) {

                        }

                        @Override
                        public void onCancel() {
                            Toast.makeText(DetailsActivity.this, "Post Cancelled", Toast.LENGTH_SHORT).show();
                        }

                    });
                }

                @Override
                public void onFacebookError(FacebookError e) {
                    Toast.makeText(DetailsActivity.this, "onFacebookError!!", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(DialogError e) {
                    Toast.makeText(DetailsActivity.this, "onError!!", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCancel() {
                    Toast.makeText(DetailsActivity.this, "Post Cancelled", Toast.LENGTH_SHORT).show();
                }
            });
        }
        //Toast.makeText(getApplicationContext(), "Sucess!!" , Toast.LENGTH_SHORT).show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        fb.authorizeCallback(requestCode, resultCode, data);
    }


    @Override
    public void onClick(View v) {
        basicInfoButton.setBackgroundResource(android.R.drawable.btn_default);
        sellerButton.setBackgroundResource(android.R.drawable.btn_default);
        shippingButton.setBackgroundResource(android.R.drawable.btn_default);
        switch (v.getId()){
            case R.id.buyNowButton:
                try{
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse(jsonItem.getJSONObject("basicInfo").getString("viewItemURL")));
                    startActivity(browserIntent);
                }catch(JSONException e){
                    e.printStackTrace();
                }
                break;
            case R.id.basicInfoButton:
                detailsRelativeLayout.removeView(hiddenInfo);
                basicInfoButton.setBackgroundResource(R.drawable.btn_yellow);
                hiddenInfo = getLayoutInflater().inflate(R.layout.basic_info_layout, detailsRelativeLayout, false);
                detailsRelativeLayout.addView(hiddenInfo);
                try{
                    populateBasicInfo();
                }catch(JSONException e){
                    e.printStackTrace();
                }
                break;
            case R.id.sellerButton:
                detailsRelativeLayout.removeView(hiddenInfo);
                sellerButton.setBackgroundResource(R.drawable.btn_yellow);
                hiddenInfo = getLayoutInflater().inflate(R.layout.seller_layout, detailsRelativeLayout, false);
                detailsRelativeLayout.addView(hiddenInfo);
                try{
                    populateSeller();
                }catch(JSONException e){
                    e.printStackTrace();
                }
                break;
            case R.id.shippingButton:
                detailsRelativeLayout.removeView(hiddenInfo);
                shippingButton.setBackgroundResource(R.drawable.btn_yellow);
                hiddenInfo = getLayoutInflater().inflate(R.layout.shipping_layout, detailsRelativeLayout, false);
                detailsRelativeLayout.addView(hiddenInfo);
                try{
                    populateShipping();
                }catch(JSONException e){
                    e.printStackTrace();
                }
                break;
        }
    }


    private void populateBasicInfo() throws JSONException{

        tvCategoryName = (TextView) findViewById(R.id.tvCategoryName);
        tvCondition = (TextView) findViewById(R.id.tvCondition);
        tvBuyingFormat = (TextView) findViewById(R.id.tvBuyingFormat);
        if(jsonItem.getJSONObject("basicInfo").getString("categoryName").isEmpty())
            tvCategoryName.setText("N/A");
        else
            tvCategoryName.setText(jsonItem.getJSONObject("basicInfo").getString("categoryName"));
        if(jsonItem.getJSONObject("basicInfo").getString("conditionDisplayName").isEmpty())
            tvCondition.setText("N/A");
        else
            tvCondition.setText(jsonItem.getJSONObject("basicInfo").getString("conditionDisplayName"));
        if(jsonItem.getJSONObject("basicInfo").getString("listingType").isEmpty())
            tvBuyingFormat.setText("N/A");
        else
            tvBuyingFormat.setText(jsonItem.getJSONObject("basicInfo").getString("listingType"));
    }


    private void populateSeller() throws JSONException{

        tvUserName = (TextView) findViewById(R.id.tvUserName);
        tvFeedbackScore = (TextView) findViewById(R.id.tvFeedbackScore);
        tvPositiveFeedback = (TextView) findViewById(R.id.tvPositiveFeedback);
        tvFeedbackRating = (TextView) findViewById(R.id.tvFeedbackRating);
        tvStore = (TextView) findViewById(R.id.tvStore);
        sellerTopRated = (ImageView) findViewById(R.id.ivSellerTopRated);
        if(jsonItem.getJSONObject("sellerInfo").getString("sellerUserName").isEmpty())
            tvUserName.setText("N/A");
        else
            tvUserName.setText(jsonItem.getJSONObject("sellerInfo").getString("sellerUserName"));
        if(jsonItem.getJSONObject("sellerInfo").getString("feedbackScore").isEmpty())
            tvFeedbackScore.setText("N/A");
        else
            tvFeedbackScore.setText(jsonItem.getJSONObject("sellerInfo").getString("feedbackScore"));
        if(jsonItem.getJSONObject("sellerInfo").getString("positiveFeedbackPercent").isEmpty())
            tvPositiveFeedback.setText("N/A");
        else
            tvPositiveFeedback.setText(jsonItem.getJSONObject("sellerInfo").getString("positiveFeedbackPercent"));
        if(jsonItem.getJSONObject("sellerInfo").getString("feedbackRatingStar").isEmpty())
            tvFeedbackRating.setText("N/A");
        else
            tvFeedbackRating.setText(jsonItem.getJSONObject("sellerInfo").getString("feedbackRatingStar"));
        if(jsonItem.getJSONObject("sellerInfo").getString("sellerStoreName").isEmpty())
            tvStore.setText("N/A");
        else
            tvStore.setText(jsonItem.getJSONObject("sellerInfo").getString("sellerStoreName"));
        if(jsonItem.getJSONObject("sellerInfo").getString("topRatedSeller").contentEquals("true"))
            sellerTopRated.setImageResource(R.drawable.check);
        else
            sellerTopRated.setImageResource(R.drawable.delete);
    }


    private void populateShipping() throws JSONException{

        tvShippingType = (TextView) findViewById(R.id.tvShippingType);
        tvHandlingTime = (TextView) findViewById(R.id.tvHandlingTime);
        tvShippingLocations = (TextView) findViewById(R.id.tvShippingLocations);
        ivExpeditedShipping = (ImageView) findViewById(R.id.ivExpeditedShipping);
        ivOneDayShipping = (ImageView) findViewById(R.id.ivOneDayShipping);
        ivReturnsAccepted = (ImageView) findViewById(R.id.ivReturnsAccepted);
        if(jsonItem.getJSONObject("shippingInfo").getString("shippingType").isEmpty())
            tvShippingType.setText("N/A");
        else
            tvShippingType.setText(jsonItem.getJSONObject("shippingInfo").getString("shippingType"));
        if(jsonItem.getJSONObject("shippingInfo").getString("handlingTime").isEmpty())
            tvHandlingTime.setText("N/A");
        else
            tvHandlingTime.setText(jsonItem.getJSONObject("shippingInfo").getString("handlingTime")+" day(s)");
        if(jsonItem.getJSONObject("shippingInfo").getString("shipToLocations").isEmpty())
            tvShippingLocations.setText("N/A");
        else
            tvShippingLocations.setText(jsonItem.getJSONObject("shippingInfo").getString("shipToLocations"));
        if(jsonItem.getJSONObject("shippingInfo").getString("expeditedShipping").contentEquals("true"))
            ivExpeditedShipping.setImageResource(R.drawable.check);
        else
            ivExpeditedShipping.setImageResource(R.drawable.delete);
        if(jsonItem.getJSONObject("shippingInfo").getString("oneDayShippingAvailable").contentEquals("true"))
            ivOneDayShipping.setImageResource(R.drawable.check);
        else
            ivOneDayShipping.setImageResource(R.drawable.delete);
        if(jsonItem.getJSONObject("shippingInfo").getString("returnsAccepted").contentEquals("true"))
            ivReturnsAccepted.setImageResource(R.drawable.check);
        else
            ivReturnsAccepted.setImageResource(R.drawable.delete);
    }


    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

        ImageView bmImage;


        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }


        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return mIcon11;
        }


        protected void onPostExecute(Bitmap result) {

            bmImage.setImageBitmap(result);
        }

    }


}
