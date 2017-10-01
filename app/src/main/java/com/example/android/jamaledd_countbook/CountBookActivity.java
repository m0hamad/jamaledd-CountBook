package com.example.android.jamaledd_countbook;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

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
import java.util.ArrayList;

import static android.R.id.edit;

public class CountBookActivity extends AppCompatActivity {

    private static final String FILENAME = "file.sav";
    private ArrayList<Count> countList = new ArrayList<Count>();
    private ArrayAdapter<Count> adapter;
    private ListView countListView;
    private TextView countTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button addCount = (Button) findViewById(R.id.add_count);
        countListView = (ListView) findViewById(R.id.count_list_view);
        countTracker = (TextView) findViewById(R.id.count_tracker);

        countListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            /**
             * Opens the correct record which the user wants to examine
             * @param parent
             * @param view
             * @param position
             * @param id
             */
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                viewCount(position);

            }
        });

        addCount.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Intent intent = new Intent(CountBookActivity.this, AddCount.class);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter = new ArrayAdapter<Count>(this,
                R.layout.count, countList);
        countListView.setAdapter(adapter);
        loadAllRecord();
        countTracker.setText(String.format("Counts Listed: %s", countList.size()));
    }

    @Override
    protected void onResume() {
        super.onResume();
        countList.clear();
        loadAllRecord();

        adapter.notifyDataSetChanged();
    }

    private void viewCount(final int position) {

        AlertDialog.Builder builder = new AlertDialog.Builder(CountBookActivity.this);
        builder.setTitle("Edit record")
                .setNegativeButton("DELETE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        adapter.remove(countList.get(position));
                        countList.remove(position);
                        adapter.notifyDataSetChanged();
                        saveInFile();
                        countTracker.setText(String.format("Counts Listed: %s", countList.size()));

                    }
                })
                .setPositiveButton("EDIT", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(CountBookActivity.this, AddCount.class);
                        Gson gson = new Gson();
                        String recordStr = gson.toJson(countList.get(position));
                        intent.putExtra("EDIT",recordStr);
                        startActivity(intent);
                    }
                });


        AlertDialog dialog = builder.create();
        dialog.show();

    }

    private void loadAllRecord() {
        try {
            FileInputStream fis = openFileInput(FILENAME);
            BufferedReader in = new BufferedReader(new InputStreamReader(fis));

            Gson gson = new Gson();

            countList = gson.fromJson(in, new TypeToken<ArrayList<Count>>(){}.getType());
            adapter.clear();
            adapter.addAll(countList);
            adapter.notifyDataSetChanged();
            fis.close();
        } catch (FileNotFoundException e) {
            countList = new ArrayList<>();
        } catch (IOException e) {
            throw new RuntimeException();
        }
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

}
