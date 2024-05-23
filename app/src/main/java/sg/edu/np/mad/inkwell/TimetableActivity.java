package sg.edu.np.mad.inkwell;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import java.util.HashMap;
import android.graphics.Color;

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
import java.util.TimeZone;

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
    private HashMap<String, Integer> categoryColors;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timetable);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        categoryColors = new HashMap<>();
        categoryColors.put("class", Color.RED);
        categoryColors.put("meeting", Color.BLUE);

        dataList = new ArrayList<>();

        dataList.add(new TimetableData("Title 1", "Description 1", "1300", "1400", "class"));
        dataList.add(new TimetableData("Title 2", "Description 2", "1300", "1400","meeting"));
        dataList.add(new TimetableData("Title 3", "Description 3", "1300", "1400","class"));
        dataList.add(new TimetableData("Title 4", "Description 4", "1300", "1400","meeting"));
        adapter = new TimetableAdapter(dataList,categoryColors);
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
        Button btnClear = findViewById(R.id.btnClear);

        TimeZone singaporeTimeZone = TimeZone.getTimeZone("Asia/Singapore");

        Calendar calendar = Calendar.getInstance(singaporeTimeZone);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        String currentTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);

        tvStartTime.setText(currentTime);
        tvEndTime.setText(currentTime);
        Date date = calendar.getTime();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM, yyyy", Locale.getDefault());
        String currentDate = dateFormat.format(date);

        tvDate.setText(currentDate);

        addNewBtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleSlidingPanel();
            }
        });

        backgroundOverlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSlidingPanel();
            }
        });

        slidingPanel.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                selectStartTime.setVisibility(View.GONE);
                selectEndTime.setVisibility(View.GONE);
                return true;
            }
        });

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

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearInputData();
            }
        });
    }

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
    }

    private void clearInputData() {
        EditText etToDo = findViewById(R.id.etToDo);
        EditText etLocation = findViewById(R.id.etLocation);
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

// Optionally, you can format the time as a string
        String currentTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);

        etToDo.setText("");
        etLocation.setText("");
        tvStartTime.setText(currentTime);
        tvEndTime.setText(currentTime);
    }


}
