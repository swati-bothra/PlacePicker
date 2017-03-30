package com.example.android.demo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResult;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;

import static android.R.attr.data;
import static android.R.attr.start;
import static android.R.attr.theme;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener{

    TextView txt;
    private final int REQ_CODE = 1 ;
    GoogleApiClient mGoogleApiClient;
    ImageView mImageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txt = (TextView)findViewById(R.id.place);
        txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
     //           new MyTask().execute("");
                startPlacePickerActivity();
            }
        });
        mImageView = (ImageView)findViewById(R.id.image3);


        mGoogleApiClient  = new GoogleApiClient.Builder(this).enableAutoManage(this,0,this).addApi(Places.GEO_DATA_API).addApi(Places.PLACE_DETECTION_API).build();
    }

    private void startPlacePickerActivity() {
//        Toast.makeText(getApplicationContext(),"1111111111111111",Toast.LENGTH_LONG).show();
    PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();
        try {

            Intent intent = intentBuilder.build(MainActivity.this);
//            Toast.makeText(getApplicationContext(),"****************",Toast.LENGTH_LONG).show();

            MainActivity.this.startActivityForResult(intent,REQ_CODE);
//            Toast.makeText(getApplicationContext(),"////////////////////////////",Toast.LENGTH_LONG).show();

        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
//        Toast.makeText(getApplicationContext(),"22222222222222222222222222",Toast.LENGTH_LONG).show();

        if (requestCode==REQ_CODE && resultCode==Activity.RESULT_OK){
//            Toast.makeText(getApplicationContext(),"3333333333333333333",Toast.LENGTH_LONG).show();
            Place place = PlacePicker.getPlace(this,data);
            String address = place.getAddress().toString();
            txt.setText(address);

//
//            PlacePhotoMetadataResult result3 = Places.GeoDataApi.getPlacePhotos(mGoogleApiClient3,place.getId()).await();
//            if (result3.getStatus().isSuccess() && result3!= null){
//                PlacePhotoMetadataBuffer placePhotoMetadataBuffer = result3.getPhotoMetadata();
//                PlacePhotoMetadata photo = placePhotoMetadataBuffer.get(0);
//                Bitmap image = photo.getPhoto(mGoogleApiClient3).await().getBitmap();
//                imageView = (ImageView)findViewById(R.id.image3);
//                imageView.setImageBitmap(image);
//            }



            final String placeId = place.getId();
            new PhotoTask(mImageView.getWidth(),mImageView.getHeight()) {

//                @Override
//                protected void onPreExecute() {
//                    super.onPreExecute();
//                }

                @Override
                protected void onPostExecute(AttributedPhoto attributedPhoto) {
                    if (attributedPhoto!=null){
                        mImageView.setImageBitmap(attributedPhoto.bitmap);
                        if (attributedPhoto.attribution == null) {
                            txt.setVisibility(View.GONE);
                        } else {
                            txt.setVisibility(View.VISIBLE);
                            txt.setText(Html.fromHtml(attributedPhoto.attribution.toString()));
                        }
                    }
//                    super.onPostExecute(attributedPhoto);
                }



            }.execute(placeId);

        }
        else {
//            Toast.makeText(getApplicationContext(),"4444444444",Toast.LENGTH_LONG).show();
        }


    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


//    private void displaySelcetedPlace() {
//
//        Place place = PlacePicker.getPlace(this,)
//
// }

//    private class MyTask extends AsyncTask<String,Integer,String >{
//
//        @Override
//        protected String doInBackground(String... params) {
//            startPlacePickerActivity();
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(String s) {
//            super.onPostExecute(s);
//        }
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//        }
//
//        @Override
//
//        protected void onProgressUpdate(Integer... values) {
//            super.onProgressUpdate(values);
//        }
//    }



    abstract class PhotoTask extends AsyncTask<String, Void, PhotoTask.AttributedPhoto> {

        private int mHeight;

        private int mWidth;

        public PhotoTask(int width, int height) {
            mHeight = height;
            mWidth = width;
        }


        @Override
        protected AttributedPhoto doInBackground(String... params) {
            if (params.length != 1) {
                return null;
            }
            final String placeId = params[0];
            AttributedPhoto attributedPhoto = null;

            PlacePhotoMetadataResult result = Places.GeoDataApi
                    .getPlacePhotos(mGoogleApiClient, placeId).await();

            if (result.getStatus().isSuccess()) {
                PlacePhotoMetadataBuffer photoMetadataBuffer = result.getPhotoMetadata();
                if (photoMetadataBuffer.getCount() > 0 && !isCancelled()) {
                    PlacePhotoMetadata photo = photoMetadataBuffer.get(0);
                    CharSequence attribution = photo.getAttributions();
                    Bitmap image = photo.getScaledPhoto(mGoogleApiClient, mWidth, mHeight).await()
                            .getBitmap();

                    attributedPhoto = new AttributedPhoto(attribution, image);
                }
                photoMetadataBuffer.release();
            }
            return attributedPhoto;
        }

        class AttributedPhoto {

            public final CharSequence attribution;

            public final Bitmap bitmap;

            public AttributedPhoto(CharSequence attribution, Bitmap bitmap) {
                this.attribution = attribution;
                this.bitmap = bitmap;
            }
        }
    }


}
