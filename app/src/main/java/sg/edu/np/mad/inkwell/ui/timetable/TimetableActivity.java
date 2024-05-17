package sg.edu.np.mad.inkwell.ui.timetable;

import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;

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
    private Button slideUpButton;
    private LinearLayout slidingPanel;
    private boolean isPanelShown = false;
    private View backgroundOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timetable);

        // testing recyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        dataList = new ArrayList<>();

        dataList.add(new TimetableData("Title 1", "Description 1", "1300","1400"));
        dataList.add(new TimetableData("Title 2", "Description 2", "1300","1400"));
        dataList.add(new TimetableData("Title 3", "Description 3", "1300","1400"));
        dataList.add(new TimetableData("Title 4", "Description 4", "1300","1400"));

        adapter = new TimetableAdapter(dataList);
        recyclerView.setAdapter(adapter);

        // sliding panel to add new tasks for the day
        slideUpButton = findViewById(R.id.addNewBtn1);
        slidingPanel = findViewById(R.id.slidingPanel);
        backgroundOverlay = findViewById(R.id.backgroundOverlay);

        slideUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isPanelShown) {
                    // Slide up animation
                    Animation slideUp = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up);
                    slidingPanel.startAnimation(slideUp);
                    slidingPanel.setVisibility(View.VISIBLE);
                    backgroundOverlay.setVisibility(View.VISIBLE);
                    isPanelShown = true;
                } else {
                    // Slide down animation
                    Animation slideDown = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_down);
                    slidingPanel.startAnimation(slideDown);
                    slidingPanel.setVisibility(View.GONE);
                    backgroundOverlay.setVisibility(View.GONE);
                    isPanelShown = false;
                }
            }
        });

        backgroundOverlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Slide down animation
                Animation slideDown = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_down);
                slidingPanel.startAnimation(slideDown);
                slidingPanel.setVisibility(View.GONE);
                backgroundOverlay.setVisibility(View.GONE);
                isPanelShown = false;
            }
        });

    }
}
