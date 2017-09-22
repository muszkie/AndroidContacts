package com.example.muszkie.datacollector;

import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private final static String DEFAULT_LANGUAGE = "default"; //ENG
    private final static String PL_LANGUAGE = "PL";

    private final static int ADD_ACTIVITY_REQUEST_CODE = 1;
    private final int TEXT_SIZE = 24;

    private final int PHOTO_SCALE = 30;

    private final String NAME_MESAGE="name";
    private final String AGE_MESAGE="age";
    private final String DATE_MESAGE="date";
    private final String PATH_MESAGE="path";
    private final String ID_MESAGE="id";


    private static String language = PL_LANGUAGE;

    private static boolean edit;

    private DataBase dataBase = new DataBase(this);

    private static String summaryReport = "";

    private ArrayList<TextView> textViewList = new ArrayList<TextView>(){};

    private Button add;
    private Button remove;
    private Button summary;

    private ViewGroup layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    @Override
    public void onResume() {
        super.onResume();
        textViewList.clear();
        readDataFromDatabaseAndDisplayDataOnLayout();
    }

    public static String getLanguage() {
        return language;
    }

    public void clickPL(View v) {
        language = PL_LANGUAGE;
        updateLocale();
    }

    public void clickENG(View v) {
        language = DEFAULT_LANGUAGE;
        updateLocale();
    }

    public void updateLocale() {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        add = (Button) findViewById(R.id.addButton);
        add.setText(R.string.add);
        remove = (Button) findViewById(R.id.removeButton);
        remove.setText(R.string.removeAll);
        summary = (Button) findViewById(R.id.summaryButton);
        summary.setText(R.string.summary);
        readDataFromDatabaseAndDisplayDataOnLayout();
    }

    public void readDataFromDatabaseAndDisplayDataOnLayout() {
        clsLayout();
        Cursor cursor = dataBase.writeAllData();
        while (cursor.moveToNext()) {
            int id = cursor.getInt(0);
            String name = cursor.getString(1);
            int age = cursor.getInt(2);
            String path = cursor.getString(3);
            String date = cursor.getString(4);

            addDataToLayout( id, name, age, path, date);
        }
    }

    public void addDataToLayout(int id, String name, int age, String path, String date){
        layout = (ViewGroup) findViewById(R.id.activity_main);
        TableRow tableRow = new TableRow(this);
        TextView textView = new TextView(this);
        textView.setTextSize(TEXT_SIZE);
        textView.setText("\n" + name + ", " + age + ", " + date);
        textView.setId(id);
        textView.setTag(path);
        textViewList.add(textView);
        ImageView imageView = new ImageView(this);
        imageView.setId(id);
        setPic(path, imageView);
        Button bEdit = new Button(this);
        bEdit.setText(R.string.edit);
        bEdit.setId(id);
        Button bRemove = new Button(this);
        bRemove.setText(R.string.remove);
        bRemove.setId(id);
        tableRow.addView(textView);
        tableRow.addView(imageView);
        tableRow.addView(bEdit);
        tableRow.addView(bRemove);
        layout.addView(tableRow);
        layout.addView(new TextView(this));
        bEdit.setOnClickListener(listenerEdit);
        bRemove.setOnClickListener(listenerRemove);
    }

    View.OnClickListener listenerRemove = new View.OnClickListener() {
        public void onClick(View view) {
            removeRow(view);
        }
    };

    View.OnClickListener listenerEdit = new View.OnClickListener() {
        public void onClick(View view) {
            editRow(view);
        }
    };

    public void setPic(String path, ImageView mImageView) {
        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, bmOptions);

        // Determine how much to scale down the image
        int scaleFactor = PHOTO_SCALE;

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(path, bmOptions);
        mImageView.setImageBitmap(bitmap);
    }

    public void editRow(View button) {

        String nameAgeDate = "";
        String id = "";
        String path = "";

        edit = true;

        for (TextView textView : textViewList) {
            if (textView.getId() == button.getId()) {
                nameAgeDate = textView.getText().toString();
                id += textView.getId();

                if (textView.getTag() != null) {
                    path = textView.getTag().toString();
                }
                break;
            }
        }

        sendDataToEdit( nameAgeDate,  id,  path);
    }

    public static boolean isEdit() { return edit; }

    public static void setEdit(boolean ifedit) { MainActivity.edit = ifedit; }

    public void sendDataToEdit(String nameAgeDate, String id, String path){
        Intent intentEdit = new Intent(this, Add.class);
        Bundle extras = new Bundle();

        String txtSplit[] = nameAgeDate.split(", ");
        String name = txtSplit[0].substring(1);
        String age = txtSplit[1];
        String date = txtSplit[2];

        extras.putString(NAME_MESAGE, name);
        extras.putString(AGE_MESAGE, age);
        extras.putString(ID_MESAGE, id);
        extras.putString(PATH_MESAGE, path);
        extras.putString(DATE_MESAGE, date);

        intentEdit.putExtras(extras);
        startActivity(intentEdit);

        MainActivity.setSummaryReport(MainActivity.getSummaryReport() +
                this.getResources().getString(R.string.editSummary) + name + " " + age);
    }

    public void removeRow(View button) {

        dataBase.removeData(button.getId());

        MainActivity.setSummaryReport(MainActivity.getSummaryReport() +
                this.getResources().getString(R.string.removeSummary) + button.getId());
        readDataFromDatabaseAndDisplayDataOnLayout();
    }

    public void openAdd(View button) {
        Intent intent = new Intent(this, Add.class);
        startActivityForResult(intent, ADD_ACTIVITY_REQUEST_CODE);
    }

    public void clsDB(View button) {
        clsLayout();
        dataBase.removeAllData();
        MainActivity.setSummaryReport(MainActivity.getSummaryReport() +
                this.getResources().getString(R.string.allRemoveSummary));
    }

    public void clsLayout() {
        layout = (ViewGroup) findViewById(R.id.activity_main);
        Button addButton = (Button) findViewById(R.id.addButton);
        Button removeButton = (Button) findViewById(R.id.removeButton);
        layout.removeAllViews();
        layout.addView(addButton);
        layout.addView(removeButton);
    }

    public void openSummary(View button) {
        Intent intentSum = new Intent(this, Summary.class);
        startActivity(intentSum);
    }

    public static String getSummaryReport() {return summaryReport; }

    public static void setSummaryReport(String summaryReport) {MainActivity.summaryReport = summaryReport; }



}
