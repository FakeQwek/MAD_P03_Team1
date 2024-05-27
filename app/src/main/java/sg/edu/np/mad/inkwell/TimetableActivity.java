package sg.edu.np.mad.inkwell;

import android.app.TimePickerDialog;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import android.app.AlertDialog;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.ImageButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class TimetableActivity extends AppCompatActivity {

    private LinearLayout slidingPanel;
    private boolean isPanelShown = false;
    private View backgroundOverlay;
    private RecyclerView recyclerView;
    private TimetableAdapter adapter;
    private ImageButton addNewBtn1;
    private TextView tvDate;
    private CardView startTime, endTime;
    private TextView tvStartTime,tvEndTime;
    private TimePicker selectEndTime, selectStartTime;
    private int startHour, startMinute, endHour, endMinute;
    private HashMap<String, Integer> categoryColors;
    private List<TimetableData> dataList = new ArrayList<>();
    private ArrayAdapter<String> spinnerAdapter;
    private List<String> categoryList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timetable);

        addNewBtn1 = findViewById(R.id.addNewTaskbtn);
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

        // init Firebase
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();

            // set up RecyclerView
            recyclerView = findViewById(R.id.recyclerView);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            categoryColors = new HashMap<>();

            adapter = new TimetableAdapter(this, dataList, categoryColors);
            recyclerView.setAdapter(adapter);

            // get data from Firestore
            db.collection("users").document(userId).collection("timetable")
                    .addSnapshotListener(new com.google.firebase.firestore.EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@NonNull QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                Log.w("Firestore", "Listen failed.", e);
                                return;
                            }

                            dataList.clear();
                            for (QueryDocumentSnapshot doc : snapshots) {
                                TimetableData data = doc.toObject(TimetableData.class);
                                dataList.add(data);
                            }
                            adapter.notifyDataSetChanged();
                        }
                    });

            // add categories to Firestore
            addCategoryToFirestore(db, userId, "Class", ContextCompat.getColor(this, R.color.pastelCoral));
            addCategoryToFirestore(db, userId, "Meeting", ContextCompat.getColor(this, R.color.pastelBlue));

            addNewBtn1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String title = "New Title";
                    String description = "New Description";
                    String startTime = "1300";
                    String endTime = "1400";
                    String category = "Class";

                    addTimetableData(db, userId, title, description, startTime, endTime, category);
                }
            });
        }

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

        categoryList.add("class");
        categoryList.add("meeting");

        Spinner spinner = findViewById(R.id.categorySpinner);
        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryList);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCategory = parent.getItemAtPosition(position).toString();
                if (selectedCategory.equals("Add New Option")) {
                    showAddOptionPopup();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        categoryList.add("Add New Option");
        spinnerAdapter.notifyDataSetChanged();
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
        TimeZone singaporeTimeZone = TimeZone.getTimeZone("Asia/Singapore");
        EditText etToDo = findViewById(R.id.etToDo);
        EditText etLocation = findViewById(R.id.etLocation);
        Calendar calendar = Calendar.getInstance(singaporeTimeZone);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        String currentTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);

        etToDo.setText("");
        etLocation.setText("");
        tvStartTime.setText(currentTime);
        tvEndTime.setText(currentTime);
    }

    // when add new category selected, open pop-up
    private void showAddOptionPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.add_new_task_popup, null);
        builder.setView(dialogView);

        final EditText etCategoryName = dialogView.findViewById(R.id.etCategoryName);
        final LinearLayout colorLayout = dialogView.findViewById(R.id.colorLayout);
        Button btnAddCategory = dialogView.findViewById(R.id.btnAddCategory);

        final AlertDialog dialog = builder.create();

        // add color buttons dynamically
        int[] colors = {R.color.pastelCoral, R.color.pastelBlue, R.color.pastelGreen, R.color.pastelPurple, R.color.pastelYellow};
        for (final int color : colors) {
            Button colorButton = new Button(this);
            colorButton.setBackgroundColor(ContextCompat.getColor(this, color));
            colorButton.setTag(color);
            colorButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.setSelected(true);
                    for (int i = 0; i < colorLayout.getChildCount(); i++) {
                        View child = colorLayout.getChildAt(i);
                        if (child != v) {
                            child.setSelected(false);
                        }
                    }
                }
            });

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    getResources().getDimensionPixelSize(R.dimen.color_button_size),
                    getResources().getDimensionPixelSize(R.dimen.color_button_size)
            );
            params.setMargins(10, 10, 10, 10);
            colorButton.setLayoutParams(params);

            // add circular bg to colour
            GradientDrawable shape = new GradientDrawable();
            shape.setShape(GradientDrawable.OVAL);
            shape.setColor(ContextCompat.getColor(this, color));
            colorButton.setBackground(shape);

            colorLayout.addView(colorButton);

            colorButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean isSelected = v.isSelected();
                    v.setSelected(!isSelected);

                    if (isSelected) {
                        // go back to original size when selected
                        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) v.getLayoutParams();
                        params.width = getResources().getDimensionPixelSize(R.dimen.color_button_size);
                        params.height = getResources().getDimensionPixelSize(R.dimen.color_button_size);
                        v.setLayoutParams(params);
                    } else {
                        // make color circle bigger when selected
                        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) v.getLayoutParams();
                        params.width = getResources().getDimensionPixelSize(R.dimen.color_button_selected_size);
                        params.height = getResources().getDimensionPixelSize(R.dimen.color_button_selected_size);
                        v.setLayoutParams(params);
                    }

                    for (int i = 0; i < colorLayout.getChildCount(); i++) {
                        View child = colorLayout.getChildAt(i);
                        if (child != v) {
                            child.setSelected(false);
                            // bring back original size for unselected buttons
                            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) child.getLayoutParams();
                            params.width = getResources().getDimensionPixelSize(R.dimen.color_button_size);
                            params.height = getResources().getDimensionPixelSize(R.dimen.color_button_size);
                            child.setLayoutParams(params);
                        }
                    }
                }
            });
        }

        btnAddCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String categoryName = etCategoryName.getText().toString().trim();
                int selectedColor = -1;
                for (int i = 0; i < colorLayout.getChildCount(); i++) {
                    View child = colorLayout.getChildAt(i);
                    if (child.isSelected()) {
                        selectedColor = (int) child.getTag();
                        break;
                    }
                }
                if (!categoryName.isEmpty() && selectedColor != -1) {
                    categoryList.add(categoryList.size() - 1, categoryName); // Add before "Add New Option"
                    categoryColors.put(categoryName, ContextCompat.getColor(TimetableActivity.this, selectedColor));
                    spinnerAdapter.notifyDataSetChanged();
                    dialog.dismiss();
                } else {
                    // Show error message
                    if (categoryName.isEmpty()) {
                        etCategoryName.setError("Category name required");
                    }
                    if (selectedColor == -1) {
                        TextView selectColorTextView = dialogView.findViewById(R.id.tvSelectColor);
                        selectColorTextView.setError("Select a color");
                    }
                }
            }
        });

        dialog.show();
    }

    // add the collections category to specific user
    private void addCategoryToFirestore(FirebaseFirestore db, String userId, String category, int color) {
        Map<String, Object> categoryData = new HashMap<>();
        categoryData.put("color", color);

        db.collection("users")
                .document(userId)
                .collection("categories")
                .document(category)
                .set(categoryData)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Category added successfully"))
                .addOnFailureListener(e -> Log.w("Firestore", "Error adding category", e));
    }

    private void addTimetableData(FirebaseFirestore db, String userId, String title, String description, String startTime, String endTime, String category) {
        Map<String, Object> timetableData = new HashMap<>();
        timetableData.put("title", title);
        timetableData.put("description", description);
        timetableData.put("startTime", startTime);
        timetableData.put("endTime", endTime);
        timetableData.put("category", category);

        db.collection("users")
                .document(userId)
                .collection("timetable")
                .add(timetableData)
                .addOnSuccessListener(documentReference -> Log.d("Firestore", "Timetable data added successfully"))
                .addOnFailureListener(e -> Log.w("Firestore", "Error adding timetable data", e));
    }
}
