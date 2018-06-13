package com.kbaquri.gaze.First;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.kbaquri.gaze.R;
import com.kbaquri.gaze.Second.MainActivity;

public class ListActivity extends AppCompatActivity {

    Category[] categories;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        categories = CategoryList.CATEGORIES;

        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new MyRecyclerAdapter(categories);
        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), mRecyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                showMap(categories[position].getName());
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

    }

    void showMap(String nearby) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("nearby", nearby);
        startActivity(intent);
    }
}
