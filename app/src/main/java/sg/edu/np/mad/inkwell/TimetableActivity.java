package sg.edu.np.mad.inkwell;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TimetableActivity extends AppCompatActivity {

    private LinearLayout slidingPanel;
    private boolean isPanelShown = false;
    private View backgroundOverlay;
    private RecyclerView recyclerView;
    private TimetableAdapter adapter;
    private List<TimetableData> dataList;
    private Button addNewBtn1;
    private TextView tvDate;
    private CardView startTime, endTime;
    private TextView tvStartTime,tvEndTime;
    private TimePicker selectEndTime, selectStartTime;
    private int startHour, startMinute, endHour, endMinute;

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
        tvStartTime = findViewById(R.id.tvStartTime);
        tvEndTime = findViewById(R.id.tvEndTime);
        startTime = findViewById(R.id.startTime);
        endTime = findViewById(R.id.endTime);
        selectStartTime = findViewById(R.id.selectStartTime);
        selectEndTime = findViewById(R.id.selectEndTime);
        tvDate = findViewById(R.id.tvDate);

        // add sliding up panel when addNew clicked
        addNewBtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleSlidingPanel();
            }
        });

        // lowers sliding panel when overlay clicked
        backgroundOverlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSlidingPanel();
            }
        });

        // close timePicker if slidingPanel touched
        slidingPanel.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                selectStartTime.setVisibility(View.GONE);
                selectEndTime.setVisibility(View.GONE);
                return true;
            }
        });

        // show timePicker on click
        startTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog(true);
            }
        });

        endTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog(false);
            }
        });

        // get current date
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM, yyyy", Locale.getDefault());
        String currentDate = dateFormat.format(date);

        // set current date
        tvDate.setText(currentDate);
    }

    // timePicker
    private void showTimePickerDialog(final boolean isStartTime) {
        int hour = isStartTime ? startHour : endHour;
        int minute = isStartTime ? startMinute : endMinute;

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {
                if (isStartTime) {
                    startHour = selectedHour;
                    startMinute = selectedMinute;
                    tvStartTime.setText(String.format("%02d:%02d", startHour, startMinute));
                } else {
                    endHour = selectedHour;
                    endMinute = selectedMinute;
                    tvEndTime.setText(String.format("%02d:%02d", endHour, endMinute));
                }
            }
        }, hour, minute, true);
        timePickerDialog.show();
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
    }}