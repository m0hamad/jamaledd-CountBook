/**
 * AddCount
 *
 * October 2nd, 2017
 */

package com.example.android.jamaledd_countbook;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.Calendar;
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

import static android.R.attr.value;
import static com.example.android.jamaledd_countbook.R.id.decrement;
import static com.example.android.jamaledd_countbook.R.id.increment;
import static com.example.android.jamaledd_countbook.R.layout.count;

/**
 * Creates the count by inputting values.
 * User must have a name, and can enter a value, date, and optional comment.
 * User can add new count or edit a previous count.
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

    /**
     * @param savedInstanceState
     *
     * Opens a new count
     * or
     * Opens an existing count and loads previous inputs
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_count);

        countName = (EditText) findViewById(R.id.count_name);
        countDateEditText = (EditText) findViewById(R.id.count_date);
        countValue = (EditText) findViewById(R.id.count_value);
        countComment = (EditText) findViewById(R.id.count_comment);

        done = (Button) findViewById(R.id.done);
        done.setOnClickListener(this);

        increment = (Button) findViewById(R.id.increment);
        increment.setOnClickListener(this);

        decrement = (Button) findViewById(R.id.decrement);
        decrement.setOnClickListener(this);

        resetToInitialValue = (Button) findViewById(R.id.reset_to_initial_value);
        resetToInitialValue.setOnClickListener(this);

        countDateEditText.setFocusable(false);
        countDateEditText.setOnClickListener(this);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MMM-dd");

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

        loadCounts();

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
            if (newCount.getNewValue() != -1) {
                countValue.setText(Integer.toString(newCount.getNewValue()));
            }
            if (newCount.getComment() != null) {
                countComment.setText(newCount.getComment());
            }
        }
    }

    /**
     * @param view
     *
     * Saves new count or existing count.
     */
    @Override
    public void onClick(View view) {

        int value = Integer.parseInt(countValue.getText().toString());

        if (view == done) {
            addNewCount();
        }
        if (view == resetToInitialValue) {
            countValue.setText(Integer.toString(newCount.getInitialValue()));
        }
        if (view == increment) {
            value++;
            countValue.setText(Integer.toString(value));
        }
        if (view == decrement) {
            value--;
            countValue.setText(Integer.toString(value));
        }
    }

    /**
     * https://stackoverflow.com/questions/15027454/how-to-get-onclick-in-datepickerdialog-ondatesetlistener
     *
     */
    DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {

        /**
         *
         * @param view
         * @param year
         * @param month
         * @param day
         *
         * Implements the date widget when user is selecting date.
         */
        @Override
        public void onDateSet(DatePicker view, int year, int month, int day) {
            countDate.set(Calendar.YEAR, year);
            countDate.set(Calendar.MONTH, month);
            countDate.set(Calendar.DAY_OF_MONTH, day);
            countDateEditText.setText(year + "-" + (month + 1) + "-" + day);
        }
    };

    /**
     * Allows user to enter inputs for count.
     * Inputs are then saved and stored.
     */
    protected void addNewCount() {

        String name = countName.getText().toString();
        Count newCount = new Count();

        //https://stackoverflow.com/questions/6835980/android-converting-string-to-int
        int value = Integer.parseInt(countValue.getText().toString());

        if (name.isEmpty()) {
            countName.setError("Enter name");
            return;
        }

        if (!countValue.getText().toString().isEmpty()) {

            if (!checkCountValue(value, countValue)) return;
            newCount.setNewValue(value);
            newCount.setInitialValue(value);
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

    /**
     * @param checkValue
     * @param editText
     * @return
     *
     * Checks for negative integer value.
     */
    private Boolean checkCountValue(int checkValue, EditText editText) {
        if (checkValue >= 0 ){
            editText.setText(Integer.toString(checkValue));
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

    /**
     * Loads information for count.
     */
    private void loadCounts() {
        try {
            FileInputStream fis = openFileInput(FILENAME);
            BufferedReader in = new BufferedReader(new InputStreamReader(fis));

            Gson gson = new Gson();

            countList = gson.fromJson(in, new TypeToken<ArrayList<Count>>(){}.getType());
            fis.close();
        } catch (FileNotFoundException e) {
            countList = new ArrayList<>();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            throw new RuntimeException();
        }
    }

    /**
     * @param count
     * @return
     *
     * Gets index for count.
     */
    private int getIndex(Count count) {
        int index = 0;
        for (Count r : countList) {
            if (r.getName().equals(count.getName())) {
                break;
            }
            ++index;
        }
        return index;
    }

}
