package com.example.android.jamaledd_countbook;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by Moe on 2017-09-29.
 */

public class AddCount extends AppCompatActivity implements View.OnClickListener{

    private static final String FILENAME = "file.sav";
    private ArrayList<Count> countList = new ArrayList<>();
    private int index = -1;
    private Count newCount;
    private EditText countName;
    private Calendar countDate;
    private EditText countDateEditText;
    private EditText countValue;
    private EditText countComment;
    private Button resetToInitialValue;
    private Button done;
    private Button increment;
    private Button decrement;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_count);

        countName = (EditText) findViewById(R.id.count_name);
        countDateEditText = (EditText) findViewById(R.id.count_date);
        countValue = (EditText) findViewById(R.id.count_value);
        countComment = (EditText) findViewById(R.id.count_comment);
        resetToInitialValue = (Button) findViewById(R.id.reset_to_initial_value);
        done = (Button) findViewById(R.id.done);
        done.setOnClickListener(this);

        countDateEditText.setFocusable(false);
        countDateEditText.setOnClickListener(this);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MMM-dd");

        //This is where the widget DatePicker is implemented and where it gets the date
        countDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                countDate = Calendar.getInstance();
                new DatePickerDialog(AddCount.this, onDateSetListener,
                        countDate.get(Calendar.YEAR),
                        countDate.get(Calendar.MONTH),
                        countDate.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        loadAllRecord();

        Gson gson = new Gson();
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            String jsonStr = extras.getString("EDIT");
            newCount = gson.fromJson(jsonStr, Count.class);
            index = getIndex(newCount);
            countDate = newCount.getDate();
            countName.setText(newCount.getName());

            if (newCount.getDate() != null) {
                countDateEditText.setText(sdf.format(countDate.getTime()));
            }
            if (newCount.getNewValue() != 0) {
                countValue.setText(Integer.toString(newCount.getNewValue()));
            }
            if (newCount.getComment() != null) {
                countComment.setText(newCount.getComment());
            }
        }
    }

    /**
     * https://stackoverflow.com/questions/15027454/how-to-get-onclick-in-datepickerdialog-ondatesetlistener
     */
    DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            countDate.set(Calendar.YEAR, year);
            countDate.set(Calendar.MONTH, monthOfYear);
            countDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            countDateEditText.setText(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);
        }
    };

    @Override
    public void onClick(View view) {
        if (view == done) {
            addNewCount();
        }
    }
    private void addNewCount() {

        String name = countName.getText().toString();
        Count count = new Count();

        if (name.isEmpty()) {
            countName.setError("Enter name");
            return;
        }

        if (!countValue.getText().toString().isEmpty()) {

            int value = Integer.parseInt(countValue.getText().toString());
            if (!checkCountValue(value, countValue)) return;
            newCount.setNewValue(value);
        }

        if (!countComment.getText().toString().isEmpty()) {

            String comment = countComment.getText().toString();
            newCount.setComment(comment);
        }
        if (!countDateEditText.getText().toString().isEmpty()) {
            newCount.setDate(countDate);
        }
        try {
            newCount.setName(name);
            if (index != -1) {
                countList.set(index, newCount);
            } else {
                countList.add(newCount);
            }
            saveInFile();
            finish();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    private Boolean checkCountValue(int checkNumber, EditText editText) {
        if (checkNumber > 0 ){
            editText.setText(Double.toString(checkNumber));
            return true;
        }
        editText.setError("Needs pos+ value");
        return false;
    }

    private void saveInFile() {
        try {
            FileOutputStream fos = openFileOutput(FILENAME,
                    Context.MODE_PRIVATE);
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(fos));

            Gson gson = new Gson();

            gson.toJson(countList, out);

            out.flush();

            fos.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException();
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    private void loadAllRecord() {
        try {
            FileInputStream fis = openFileInput(FILENAME);
            BufferedReader in = new BufferedReader(new InputStreamReader(fis));

            Gson gson = new Gson();
            // Taken from http://stackoverflow.com/questions/12384064/gson-convert-from-json-to-a-typed-arraylistt
            // 2017-01-26

            countList = gson.fromJson(in, new TypeToken<ArrayList<Count>>(){}.getType());
            fis.close();
        } catch (FileNotFoundException e) {
            countList = new ArrayList<>();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            throw new RuntimeException();
        }
    }

    private int getIndex(Count record) {
        int index = 0;
        for (Count r : countList) {
            if (r.getName().equals(record.getName())) {
                break;
            }
            ++index;
        }
        return index;
    }

}
