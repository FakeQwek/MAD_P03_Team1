package sg.edu.np.mad.inkwell.ui.timetable;

import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TimePicker;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
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
    private LinearLayout slidingPanel;
    private boolean isPanelShown = false;
    private View backgroundOverlay;
    private View addNewBtn1;
    private CardView startTime;
    private CardView endTime;
    private TimePicker selectEndTime;
    private TimePicker selectStartTime;

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

        addNewBtn1 = findViewById(R.id.addNewBtn1);
        slidingPanel = findViewById(R.id.slidingPanel);
        backgroundOverlay = findViewById(R.id.backgroundOverlay);
        selectStartTime = findViewById(R.id.selectStartTime);
        startTime = findViewById(R.id.startTime);
        endTime = findViewById(R.id.endTime);
        selectStartTime = findViewById(R.id.selectStartTime);
        selectEndTime = findViewById(R.id.selectEndTime);

        // add sliding up panel when addNew clicked
        addNewBtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleSlidingPanel();
            }
        });

        // show timePicker on click
        startTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectStartTime.setVisibility(View.VISIBLE);
            }
        });

        endTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectEndTime.setVisibility(View.VISIBLE);
            }
        });

        // close timePicker if user clicks panel
        slidingPanel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectStartTime.getVisibility() == View.VISIBLE) {
                    hideStartTimePicker();
                } else if (selectEndTime.getVisibility() == View.VISIBLE) {
                    hideEndTimePicker();
                }
            }
        });
    }


    private void toggleSlidingPanel() {
        if (!isPanelShown) {
            // Slide up animation
            Animation slideUp = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up);
            slidingPanel.startAnimation(slideUp);
            slidingPanel.setVisibility(View.VISIBLE);
            backgroundOverlay.setVisibility(View.VISIBLE);
            isPanelShown = true;
        }
    }

    private void hideSlidingPanel() {
        // Slide down animation
        Animation slideDown = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_down);
        slidingPanel.startAnimation(slideDown);
        slidingPanel.setVisibility(View.GONE);
        backgroundOverlay.setVisibility(View.GONE);
        isPanelShown = false;
    }

    private void hideStartTimePicker() {
        selectStartTime.setVisibility(View.GONE);
    }
    private void hideEndTimePicker() {
        selectEndTime.setVisibility(View.GONE);
    }

}