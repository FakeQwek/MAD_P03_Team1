package sg.edu.np.mad.inkwell;

import android.app.TimePickerDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
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
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QuerySnapshot;


public class TimetableActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private LinearLayout slidingPanel;
    private Spinner categorySpinner;
    private ArrayAdapter<String> adapter;
    private List<String> categoryList;
    private ArrayList<TimetableData> events;
    private boolean isPanelShown = false;
    private View backgroundOverlay;
    private RecyclerView recyclerView;
    private ImageButton addNewBtn, leftBtn, rightBtn;
    private TextView tvDate;
    private CardView startTime, endTime, startDate, endDate;
    private TextView tvStartTime,tvEndTime;
    private TimePicker selectEndTime, selectStartTime;
    private DatePicker selectEndDate, selectStartDate;
    private int startHour, startMinute, endHour, endMinute;
    private HashMap<String, Integer> categoryColors;
    private int startYear, startMonth, startDayOfMonth;
    private int endYear, endMonth, endDayOfMonth;
    private TextView tvStartDate, tvEndDate;
    private EditText etToDo, etLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timetable);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // finds drawer and nav view before setting listener
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav,
                R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        View decorView = getWindow().getDecorView();

        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;

        decorView.setSystemUiVisibility(uiOptions);

        addNewBtn = findViewById(R.id.addNewTaskbtn);
        slidingPanel = findViewById(R.id.slidingPanel);
        backgroundOverlay = findViewById(R.id.backgroundOverlay);
        tvStartTime = findViewById(R.id.tvStartTime);
        tvEndTime = findViewById(R.id.tvEndTime);
        startTime = findViewById(R.id.startTime);
        endTime = findViewById(R.id.endTime);
        startDate = findViewById(R.id.startDate);
        endDate = findViewById(R.id.endDate);
        selectStartTime = findViewById(R.id.selectStartTime);
        selectEndTime = findViewById(R.id.selectEndTime);
        tvDate = findViewById(R.id.tvDate);
        Button btnClear = findViewById(R.id.btnClear);
        tvStartDate = findViewById(R.id.tvStartDate);
        tvEndDate = findViewById(R.id.tvEndDate);
        etToDo = findViewById(R.id.etToDo);
        etLocation = findViewById(R.id.etLocation);
        tvStartDate = findViewById(R.id.tvStartDate);
        tvStartTime = findViewById(R.id.tvStartTime);
        tvEndDate = findViewById(R.id.tvEndDate);
        tvEndTime = findViewById(R.id.tvEndTime);
        Button btnSave = findViewById(R.id.btnSave);
        leftBtn = findViewById(R.id.leftArrow);
        rightBtn = findViewById(R.id.rightArrow);

        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        String currentTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);

        Date date = calendar.getTime();

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM, yyyy", Locale.getDefault());
        SimpleDateFormat dateFormat2 = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String currentDate = dateFormat.format(date);
        String today = dateFormat2.format(date);
        Log.d("today", today);

        // Initialize Firebase
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        ArrayList<TimetableData> eventList = new ArrayList<>();

        // Initialize category list and spinner adapter
        categoryList = new ArrayList<>();
        categorySpinner = findViewById(R.id.categorySpinner);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        if (currentUser != null) {
            String userId = currentUser.getUid();
            checkAndInitializeCategories(db, userId);
            createEvents(db, userId);

            // Get data from Firestore
            fetchEventData(db, userId, eventList,today);

            // Fetch categories from Firestore and populate the categoryList
            fetchCategoriesFromFirestore();

            TimeZone singaporeTimeZone = TimeZone.getTimeZone("Asia/Singapore");

            tvStartTime.setText(currentTime);
            tvEndTime.setText(currentTime);

            startYear = endYear = calendar.get(Calendar.YEAR);
            startMonth = endMonth = calendar.get(Calendar.MONTH);
            startDayOfMonth = endDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
            tvStartDate.setText(formatDate(startYear, startMonth, startDayOfMonth));
            tvEndDate.setText(formatDate(endYear, endMonth, endDayOfMonth));

            tvDate.setText(currentDate);

            addNewBtn.setOnClickListener(new View.OnClickListener() {
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

            startDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDatePickerDialog(false);
                }
            });

            endTime.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    showTimePickerDialog(false);
                }
            });

            endDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDatePickerDialog(false);
                }
            });

            leftBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Decrement the calendar by one day
                    calendar.add(Calendar.DAY_OF_MONTH, -1);
                    int year = calendar.get(Calendar.YEAR);
                    int month = calendar.get(Calendar.MONTH);
                    int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

                    // Format the previous day date
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM, yyyy", Locale.getDefault());
                    String previousDay = dateFormat.format(calendar.getTime());

                    // Update tvDate with the formatted previous day date
                    tvDate.setText(previousDay);

                    // Format the previous day date in the "dd-MM-yyyy" format for filtering
                    SimpleDateFormat dateFormat2 = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                    String prevDay = dateFormat2.format(calendar.getTime());
                    Log.d("PREV DAY",prevDay);

                    // Filter the event list based on the previous day date
                    fetchEventData(db, userId, eventList,prevDay);
                }
            });

            // Right button click listener
            rightBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Increment the calendar by one day
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                    int year = calendar.get(Calendar.YEAR);
                    int month = calendar.get(Calendar.MONTH);
                    int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
                    // Format the previous day date
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM, yyyy", Locale.getDefault());
                    String tmr = dateFormat.format(calendar.getTime());

                    // Update tvDate with the formatted previous day date
                    tvDate.setText(tmr);

                    // Format the previous day date in the "dd-MM-yyyy" format for filtering
                    SimpleDateFormat dateFormat2 = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                    String nextDay = dateFormat2.format(calendar.getTime());

                    Log.d("NEXT DAY",nextDay);

                    // Filter the event list based on the previous day date
                    fetchEventData(db, userId, eventList,nextDay);
                }
            });

            btnClear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clearInputData();
                }
            });

            btnSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Map<String, Object> eventData = new HashMap<>();
                        eventData.put("eventName", etToDo.getText().toString());
                        eventData.put("eventLocation", etLocation.getText().toString());
                        eventData.put("startTime", tvStartTime.getText().toString());
                        eventData.put("startDate", tvStartDate.getText().toString());
                        eventData.put("endTime", tvEndTime.getText().toString());
                        eventData.put("endDate", tvEndDate.getText().toString());
                        eventData.put("category", categorySpinner.getSelectedItem().toString());

                        TimetableData data = new TimetableData(etToDo.getText().toString(), etLocation.getText().toString(),
                                tvStartTime.getText().toString(),tvStartDate.getText().toString(),tvEndTime.getText().toString(),
                                tvEndDate.getText().toString(),categorySpinner.getSelectedItem().toString());

                        db.collection("users").document(userId).collection("timetableEvents").add(eventData);
                        eventList.add(data);
                        hideSlidingPanel();
                        clearInputData();

                        filter(eventList,today);

                        Toast toast = new Toast(TimetableActivity.this);
                        toast.setDuration(Toast.LENGTH_SHORT);

                        LayoutInflater layoutInflater = (LayoutInflater) TimetableActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        View view = layoutInflater.inflate(R.layout.toast_added, null);
                        toast.setView(view);
                        toast.show();

                        Toast.makeText(TimetableActivity.this, "Data saved successfully.", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Log.e("SaveButtonClick", "Error saving data: " + e.getMessage(), e);

                        Toast.makeText(TimetableActivity.this, "Error saving data. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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

            adapter.notifyDataSetChanged();
        }
    }

    private void fetchEventData(FirebaseFirestore db, String userId, ArrayList<TimetableData> eventList, String date) {
        db.collection("users").document(userId).collection("timetableEvents")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            eventList.clear(); // Clear existing data
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String name = document.getString("eventName");
                                String loc = document.getString("eventLocation");
                                String startTime = document.getString("startTime");
                                String endTime = document.getString("endTime");
                                String startDate = document.getString("startDate");
                                String endDate = document.getString("endDate");
                                String category = document.getString("category");
                                Log.d("firestore", "getting data");
                                if (name != null && name != "" && startDate.equals(date)) {
                                    TimetableData data = new TimetableData(name, loc, startTime, startDate, endTime, endDate, category);
                                    eventList.add(data);
                                }
                            }
                        } else {
                            Log.w("Firestore", "Error getting documents.", task.getException());
                        }

                        Log.d("firestore", "running filter");
                        filter(eventList,date);
                    }
                });
    }

    private void fetchCategoriesFromFirestore() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users").document(userId).collection("categories")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    String category = document.getString("name");
                                    if (category != null) {
                                        categoryList.add(category);
                                    }
                                }
                                // Add "Add New Option" at the end of the list
                                categoryList.add("Add New Option");
                                adapter.notifyDataSetChanged();
                            } else {
                                Log.w("Firestore", "Error getting documents.", task.getException());
                            }
                        }
                    });
        }
    }

    private void showAddOptionPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        String userId = currentUser != null ? currentUser.getUid() : null;

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.add_new_cat_popup, null);
        builder.setView(dialogView);

        final EditText etCategoryName = dialogView.findViewById(R.id.etCategoryName);
        final LinearLayout colorLayout = dialogView.findViewById(R.id.colorLayout);
        Button btnAddCategory = dialogView.findViewById(R.id.btnAddCategory);

        final AlertDialog dialog = builder.create();

        // Add color buttons dynamically
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
            params.setMargins(10, 10, 10, 10); // Adjust margins as needed
            colorButton.setLayoutParams(params);

            // Add a circular background programmatically
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
                        // Restore original size
                        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) v.getLayoutParams();
                        params.width = getResources().getDimensionPixelSize(R.dimen.color_button_size);
                        params.height = getResources().getDimensionPixelSize(R.dimen.color_button_size);
                        v.setLayoutParams(params);
                    } else {
                        // Enlarge button when selected
                        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) v.getLayoutParams();
                        params.width = getResources().getDimensionPixelSize(R.dimen.color_button_selected_size);
                        params.height = getResources().getDimensionPixelSize(R.dimen.color_button_selected_size);
                        v.setLayoutParams(params);
                    }

                    for (int i = 0; i < colorLayout.getChildCount(); i++) {
                        View child = colorLayout.getChildAt(i);
                        if (child != v) {
                            child.setSelected(false);
                            // Restore original size for unselected buttons
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

                if (!categoryName.isEmpty() && selectedColor != -1 && userId != null) {
                    // Get selected color as a hex string
                    String hexColor = String.format("#%06X", (0xFFFFFF & ContextCompat.getColor(TimetableActivity.this, selectedColor)));

                    // Add category to Firestore
                    addCategoryToFirestore(userId, categoryName, hexColor);
                    dialog.dismiss(); // Close the dialog after adding the category
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

    private void addCategoryToFirestore(String userId, String categoryName, String colorHex) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Create a new category object
        Map<String, Object> category = new HashMap<>();
        category.put("name", categoryName);
        category.put("color", colorHex);

        // Add the category to Firestore under the user's collection
        db.collection("users").document(userId).collection("categories").document(categoryName)
                .set(category)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(TimetableActivity.this, "Category added", Toast.LENGTH_SHORT).show();

                        // Update the spinner with the new category
                        categoryList.add(categoryList.size() - 1, categoryName); // Add before "Add New Option"
                        adapter.notifyDataSetChanged();

                        // Set the spinner to the newly added category
                        categorySpinner.setSelection(categoryList.size() - 2); // Select the new category
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(TimetableActivity.this, "Error adding category", Toast.LENGTH_SHORT).show();
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

    private void showDatePickerDialog(final boolean isStartDate) {
        int year = isStartDate ? startYear : endYear;
        int month = isStartDate ? startMonth : endMonth;
        int dayOfMonth = isStartDate ? startDayOfMonth : endDayOfMonth;

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        if (isStartDate) {
                            startYear = year;
                            startMonth = monthOfYear;
                            startDayOfMonth = dayOfMonth;
                            tvStartDate.setText(formatDate(startYear, startMonth, startDayOfMonth));
                        } else {
                            endYear = year;
                            endMonth = monthOfYear;
                            endDayOfMonth = dayOfMonth;
                            tvEndDate.setText(formatDate(endYear, endMonth, endDayOfMonth));
                        }
                    }
                }, year, month, dayOfMonth);

        datePickerDialog.show();
    }
    private String formatDate(int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, dayOfMonth);
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        return sdf.format(calendar.getTime());
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
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        String currentTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
        String currentDate = String.format(Locale.getDefault(), "dd-MM-yyyy",day,month,year);

        etToDo.setText("");
        etLocation.setText("");
        tvStartTime.setText(currentTime);
        tvEndTime.setText(currentTime);
        tvStartDate.setText(currentDate);
        tvEndDate.setText(currentDate);
    }

    // when add new category selected, open pop-up
    // add the collections category to specific user
    private void initCategories(FirebaseFirestore db, String userId) {
        Map<String, Object> classCategory = new HashMap<>();
        classCategory.put("name", "Class");
        classCategory.put("color", "#ffc6ff");

        Map<String, Object> meetingCategory = new HashMap<>();
        meetingCategory.put("name", "Meeting");
        meetingCategory.put("color", "#ffc09f");

        // Add "class" category
        db.collection("users").document(userId).collection("categories").document("Class")
                .set(classCategory)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Firestore", "Class category added successfully");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Firestore", "Error adding class category", e);
                    }
                });

        // Add "meeting" category
        db.collection("users").document(userId).collection("categories").document("Meeting")
                .set(meetingCategory)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Firestore", "Meeting category added successfully");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Firestore", "Error adding meeting category", e);
                    }
                });
    }

    private void createEvents(FirebaseFirestore db, String userId) {
        // Reference the events collection under the user's document
        CollectionReference eventsCollection = db.collection("users").document(userId).collection("timetableEvents");

        // Check if the events collection already exists
        eventsCollection.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult().isEmpty()) {
                                // Events collection doesn't exist, create it with fields
                                Map<String, Object> eventData = new HashMap<>();
                                eventData.put("eventName", "");
                                eventData.put("eventLocation","");
                                eventData.put("startTime", "");
                                eventData.put("endTime", "");
                                eventData.put("category", "");

                                // Create the events collection with a dummy document and fields
                                eventsCollection.document("0").set(eventData)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d("Firestore", "Empty events collection created for user: " + userId);
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.e("Firestore", "Error creating empty events collection", e);
                                            }
                                        });
                            } else {
                                // Events collection already exists, check if it's empty and log a message
                                eventsCollection.limit(1).get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    if (task.getResult().isEmpty()) {
                                                        Log.d("Firestore", "Events collection already exists but is empty for user: " + userId);
                                                    } else {
                                                        Log.d("Firestore", "Events collection already exists and is not empty for user: " + userId);
                                                    }
                                                } else {
                                                    Log.e("Firestore", "Error checking events collection", task.getException());
                                                }
                                            }
                                        });
                            }
                        } else {
                            Log.e("Firestore", "Error checking events collection", task.getException());
                        }
                    }
                });
    }

    private void checkAndInitializeCategories(FirebaseFirestore db, String userId) {
        db.collection("users").document(userId).collection("categories")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult().isEmpty()) {
                                // Categories collection is empty, initialize categories
                                initCategories(db, userId);
                            }
                        } else {
                            Log.e("Firestore", "Error getting categories", task.getException());
                        }
                    }
                });
    }

    private void recyclerView(ArrayList<TimetableData> eventList, ArrayList<TimetableData> event) {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        TimetableAdapter adapter = new TimetableAdapter(eventList, event, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        recyclerView.getAdapter().notifyDataSetChanged();
    }

    private void filter(ArrayList<TimetableData> eventList, String date) {
        ArrayList<TimetableData> filterList = new ArrayList<>();
        for (TimetableData data : eventList) {
            if (data.getStartDate() != null && data.getStartDate().equals(date)) {
                filterList.add(data);
            }
        }

        // Sort the filterList based on startTime and endTime
        Collections.sort(filterList, new Comparator<TimetableData>() {
            @Override
            public int compare(TimetableData data1, TimetableData data2) {
                // Compare by startTime first
                int startTimeComparison = data1.getStartTime().compareTo(data2.getStartTime());
                if (startTimeComparison != 0) {
                    return startTimeComparison;
                }
                // If startTime is the same, compare by endTime
                return data1.getEndTime().compareTo(data2.getEndTime());
            }
        });

        this.events = filterList;
        recyclerView(eventList, filterList);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.nav_main) {
            Intent notesActivity = new Intent(TimetableActivity.this, MainActivity.class);
            startActivity(notesActivity);
            return true;
        }
        else if (menuItem.getItemId() == R.id.nav_notes) {
            Intent timetableActivity = new Intent(TimetableActivity.this, NotesActivity.class);
            startActivity(timetableActivity);
            return true;
        }
        else if (menuItem.getItemId() == R.id.nav_todos) {
            Intent timetableActivity = new Intent(TimetableActivity.this, TodoActivity.class);
            startActivity(timetableActivity);
            return true;
        }
        else if (menuItem.getItemId() == R.id.nav_flashcards) {
            Intent timetableActivity = new Intent(TimetableActivity.this, FlashcardActivity.class);
            startActivity(timetableActivity);
            return true;
        }
        else if (menuItem.getItemId() == R.id.nav_calendar) {
            Intent timetableActivity = new Intent(TimetableActivity.this, TimetableActivity.class);
            startActivity(timetableActivity);
            return true;
        }
        else if (menuItem.getItemId() == R.id.nav_timetable) {
            Intent timetableActivity = new Intent(TimetableActivity.this, TimetableActivity.class);
            startActivity(timetableActivity);
            return true;
        }
        else if (menuItem.getItemId() == R.id.nav_settings) {
            Intent timetableActivity = new Intent(TimetableActivity.this, SettingsActivity.class);
            startActivity(timetableActivity);
            return true;
        }
        else if (menuItem.getItemId() == R.id.nav_logout) {
            Log.d("Message", "Logout");
        }
        else {
            Log.d("Message", "Unknown page!");
        }

        int id = menuItem.getItemId();
        Navbar navbar = new Navbar(this);
        Intent newActivity = navbar.redirect(id);
        startActivity(newActivity);

        return true;
    }

}
