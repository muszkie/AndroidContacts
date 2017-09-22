package com.example.muszkie.datacollector;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;


import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Add extends AppCompatActivity {

    private static final int REQUEST_TAKE_PHOTO = 1;
    private final String NAME_MESAGE="name";
    private final String AGE_MESAGE="age";
    private final String DATE_MESAGE="date";
    private final String PATH_MESAGE="path";
    private final String ID_MESAGE="id";
    private final String MESAGE_SPLIT="/";

    private final String DATE_FORMAT = "yyyyMMdd_HHmmss";
    private final String FILE_NAME_FORMAT = "JPEG_";
    private final String FILE_FORMAT = ".jpg";
    private final String AUTHORITY = "com.example.android.fileprovider";
    private final int PHOTO_SCALE = 10;

    private static String mCurrentPhotoPath;
    private static Intent intentEdit;

    private String editId;
    private boolean ifPhoto = false;

    private Button save;
    private Button takePhoto;
    private Button removePhoto;

    private EditText name;
    private EditText age;

    private DatePicker date;
    private DataBase dataBase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        updateLocale();
    }

    @Override
    public void onResume() {
        super.onResume();
        if( ifEditDisplayPhoto() ){ return; }

        name = (EditText) findViewById(R.id.editTextImie);
        age = (EditText) findViewById(R.id.editTextWiek);
        date = (DatePicker) findViewById(R.id.datePicker);

        ifEditDisplayAllDataWithoutPhoto();
    }

    public boolean ifEditDisplayPhoto(){
        if (mCurrentPhotoPath != null) {
            setPic();
            if (ifPhoto && MainActivity.isEdit()) {
                ifPhoto = false;
                return true;
            }
        }
        return false;
    }

    public void ifEditDisplayAllDataWithoutPhoto(){

        intentEdit = getIntent();

        if (MainActivity.isEdit()) {
            Bundle extras = intentEdit.getExtras();

            name.setText((String) extras.get(NAME_MESAGE));
            age.setText((String) extras.get(AGE_MESAGE));
            String data = (String) extras.get(DATE_MESAGE);
            String dataSplit[] = data.split(MESAGE_SPLIT);

            date.updateDate(Integer.parseInt(dataSplit[2]),Integer.parseInt(dataSplit[1]), Integer.parseInt(dataSplit[0]));

            try {
                mCurrentPhotoPath = (String) extras.get(PATH_MESAGE);
                setPic();
            }
            catch (ArrayIndexOutOfBoundsException a) {}

            editId = (String) extras.get(ID_MESAGE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        rememberInsertedData(); //on orientation change
    }

    @Override
    public void finish() {
        super.finish();
        mCurrentPhotoPath = null;
        MainActivity.setEdit(false);
    }

    public void updateLocale() {
        Locale locale = new Locale(MainActivity.getLanguage());
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        save = (Button) findViewById(R.id.saveButton);
        save.setText(R.string.save);
        takePhoto = (Button) findViewById(R.id.photoButton);
        takePhoto.setText(R.string.takePhoto);
        removePhoto = (Button) findViewById(R.id.removePhotoButton);
        removePhoto.setText(R.string.removePhoto);
    }

    public void rememberInsertedData(){
        if(MainActivity.isEdit()) {
            Bundle extras = new Bundle();
            extras.putString(NAME_MESAGE, name.getText().toString());
            extras.putString(AGE_MESAGE, age.getText().toString());
            extras.putString(PATH_MESAGE, mCurrentPhotoPath);
            extras.putString(DATE_MESAGE, String.valueOf(date.getDayOfMonth()) + "/" +
                    String.valueOf(date.getMonth()) + "/" +
                    String.valueOf(date.getYear()));
            intentEdit.putExtras(extras);
        }
    }

    public void save(View view) {
        name = (EditText) findViewById(R.id.editTextImie);
        age = (EditText) findViewById(R.id.editTextWiek);
        date = (CustomDatePicker) findViewById(R.id.datePicker);
        dataBase = new DataBase(this);


        if (MainActivity.isEdit()) {

            MainActivity.setEdit(false);

            dataBase.updateData(Integer.parseInt(editId),
                    name.getText().toString(), Integer.parseInt(age.getText().toString()),
                    mCurrentPhotoPath,
                    String.valueOf(date.getDayOfMonth()) + "/" +
                            String.valueOf(date.getMonth()) + "/" +
                            String.valueOf(date.getYear()));

        } else if( nameAndAgeIsNotEmpty() ){

            dataBase.addData(name.getText().toString(), Integer.parseInt(age.getText().toString()),
                    mCurrentPhotoPath,
                    String.valueOf(date.getDayOfMonth()) + "/" +
                            String.valueOf(date.getMonth()) + "/" +
                            String.valueOf(date.getYear()));

            MainActivity.setSummaryReport(MainActivity.getSummaryReport() +
                    this.getResources().getString(R.string.addSummary) +
                    name.getText().toString() + " " +
                    age.getText().toString());
        }else{
            return;
        }
        this.finish();
    }

    public boolean nameAndAgeIsNotEmpty(){ return age.getText().length() != 0 && name.getText().length() != 0; }

    public void removePic(View button){
        mCurrentPhotoPath=null;
        setPic();
    }

    public void dispatchTakePictureIntent(View button) {
        ifPhoto = true;
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                //...
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        AUTHORITY,
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);

            }
        }
    }

    public File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat(DATE_FORMAT).format(new Date());
        String imageFileName = FILE_NAME_FORMAT + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                FILE_FORMAT,         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    public void setPic() {
        ImageView mImageView = (ImageView) findViewById(R.id.imageView);

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);

        // Determine how much to scale down the image
        //int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = PHOTO_SCALE;
        bmOptions.inPurgeable = true;


        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        mImageView.setImageBitmap(bitmap);
    }
}
