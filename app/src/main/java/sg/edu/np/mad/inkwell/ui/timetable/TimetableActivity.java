package sg.edu.np.mad.inkwell.ui.timetable;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import sg.edu.np.mad.inkwell.R;

public class TimetableActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TimetableAdapter adapter;
    private List<TimetableData> dataList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timetable);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        dataList = new ArrayList<>();

        dataList.add(new TimetableData("Title 1", "Description 1", "1300","1400"));
        dataList.add(new TimetableData("Title 2", "Description 2", "1300","1400"));
        dataList.add(new TimetableData("Title 3", "Description 3", "1300","1400"));
        dataList.add(new TimetableData("Title 4", "Description 4", "1300","1400"));

        adapter = new TimetableAdapter(dataList);
        recyclerView.setAdapter(adapter);

    }
}
