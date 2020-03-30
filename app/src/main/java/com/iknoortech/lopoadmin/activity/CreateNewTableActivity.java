package com.iknoortech.lopoadmin.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.iknoortech.lopoadmin.R;
import com.iknoortech.lopoadmin.adapter.NewTableItemsAdapter;
import com.iknoortech.lopoadmin.model.newTable.NewTable;
import com.iknoortech.lopoadmin.model.main.MainTableList;

import java.util.ArrayList;

public class CreateNewTableActivity extends AppCompatActivity {

    private EditText edtTableName;
    private RecyclerView rvTable;
    private NewTable createTable;
    private ImageView backImage;
    private ArrayList<NewTable> tableArrayList;
    private ArrayList<MainTableList> tableNameList;
    private NewTableItemsAdapter adapter;
    private static final String TAG = "CreateNewTableActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_table);

        edtTableName = findViewById(R.id.editText2);
        rvTable = findViewById(R.id.recyclerView2);
        backImage = findViewById(R.id.imageView3);
        rvTable.setLayoutManager(new LinearLayoutManager(this));
        tableArrayList = new ArrayList<>();
        createTable = new NewTable();
        tableArrayList.add(createTable);
        adapter = new NewTableItemsAdapter(this, tableArrayList);
        rvTable.setAdapter(adapter);

        tableNameList = new ArrayList<>();
        tableNameList = (ArrayList<MainTableList>) getIntent().getSerializableExtra("tableName");
        Log.d(TAG, "onCreate: " + tableNameList);

        backImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    public void createNewTable(View view) {
        if (tableNameList.size() > 0) {
            for (int i = 0; i < tableNameList.size(); i++) {
                if (tableNameList.get(i).getTitle().equals(edtTableName.getText().toString())) {
                    Toast.makeText(this, "Table name is already exist.", Toast.LENGTH_SHORT).show();
                    break;
                }

                if (i == (tableNameList.size() - 1)) {
                    enterDataInTable();
                }
            }
        } else {
            enterDataInTable();
        }
    }

    private void enterDataInTable() {

    }

    public void addRowInTable(View view) {
        createTable = new NewTable();
        tableArrayList.add(createTable);
        adapter.notifyDataSetChanged();
    }
}
