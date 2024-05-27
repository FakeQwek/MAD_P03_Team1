package sg.edu.np.mad.inkwell;

import static java.util.TimeZone.getDefault;

import android.app.TimePickerDialog;
import android.app.DatePickerDialog;
import android.content.Context;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
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
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QuerySnapshot;


public class TimetableActivity extends AppCompatActivity {

    private LinearLayout slidingPanel;
    private ArrayList<TimetableData> events;
    private boolean isPanelShown = false;
    private View backgroundOverlay;
    private RecyclerView recyclerView;
    private TimetableAdapter adapter;
    private ImageButton addNewBtn;
    private TextView tvDate;
    private CardView startTime, endTime, startDate, endDate;
    private TextView tvStartTime,tvEndTime;
    private TimePicker selectEndTime, selectStartTime;
    private DatePicker selectEndDate, selectStartDate;
    private int startHour, startMinute, endHour, endMinute;
    private int selectedColor = 0;
    private HashMap<String, Integer> categoryColors;
    private List<TimetableData> dataList = new ArrayList<>();
    private ArrayAdapter<String> spinnerAdapter;
    private List<String> categoryList = new ArrayList<>();
    private int startYear, startMonth, startDayOfMonth;
    private int endYear, endMonth, endDayOfMonth;
    private TextView tvStartDate, tvEndDate;
    private EditText etToDo, etLocation;
    private Spinner categorySpinner;

    public TimetableActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timetable);

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

        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        String currentTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);

        Date date = calendar.getTime();

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
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryList);
        Spinner categorySpinner = findViewById(R.id.categorySpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.category_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        if (currentUser != null) {
            String userId = currentUser.getUid();
            checkAndInitializeCategories(db, userId);
            createEvents(db, userId);

            // Set up RecyclerView
            RecyclerView recyclerView = findViewById(R.id.recyclerView);
            TimetableAdapter timetableAdapter = new TimetableAdapter(eventList);
            recyclerView.setAdapter(timetableAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));

            // Get data from Firestore
            db.collection("users").document(userId).collection("timetable")
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                Log.w("Firestore", "Listen failed.", e);
                                return;
                            }

                            eventList.clear();

                            for (DocumentChange dc : snapshots.getDocumentChanges()) {
                                if (dc.getType() == DocumentChange.Type.ADDED) {
                                    try {
                                        TimetableData data = new TimetableData(
                                                dc.getDocument().getString("eventName"),
                                                dc.getDocument().getString("location"),
                                                dc.getDocument().getString("startTime"),
                                                dc.getDocument().getString("endTime"),
                                                dc.getDocument().getString("category"),
                                                dc.getDocument().getString("startDate"),
                                                dc.getDocument().getString("endDate")
                                        );
                                        eventList.add(data);

                                        // Log each added event for debugging
                                        Log.d("Firestore", "Added event: " + data.getName() + " on " + data.getStartDate());

                                    } catch (Exception ex) {
                                        Log.e("Firestore", "Error parsing document: " + ex.getMessage(), ex);
                                    }
                                }
                            }

                            // Log the size of the eventList after processing the documents
                            Log.d("EventListSize", "Event List Size: " + eventList.size());

                            filter(eventList, today);
                            adapter.notifyDataSetChanged();
                        }
                    });


            // Fetch categories from Firestore and populate the categoryList
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
                                spinnerAdapter.notifyDataSetChanged();
                            } else {
                                Log.w("Firestore", "Error getting documents.", task.getException());
                            }
                        }
                    });

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

                        db.collection("users").document(userId).collection("events").add(eventData);
                        hideSlidingPanel();
                        clearInputData();

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
                    String selectedCategory = categorySpinner.getSelectedItem().toString();
                    if (selectedCategory.equalsIgnoreCase("Add New Option")) {
                        showAddOptionPopup();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });

            spinnerAdapter.notifyDataSetChanged();
        }
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

        String currentTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);

        etToDo.setText("");
        etLocation.setText("");
        tvStartTime.setText(currentTime);
        tvEndTime.setText(currentTime);
    }

    // when add new category selected, open pop-up
    private void showAddOptionPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        String userId = String.valueOf(currentUser);

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.add_new_cat_popup, null);
        builder.setView(dialogView);

        EditText etCategoryName = dialogView.findViewById(R.id.etCategoryName);
        LinearLayout colorLayout = dialogView.findViewById(R.id.colorLayout);
        Button btnAddCategory = dialogView.findViewById(R.id.btnAddCategory);

        final AlertDialog dialog = builder.create();

        // Add color buttons dynamically
        int[] colors = {R.color.pastelCoral, R.color.pastelBlue, R.color.pastelGreen, R.color.pastelPurple, R.color.pastelYellow};
        for (final int color : colors) {
            Button colorButton = new Button(this);
            colorButton.setBackgroundColor(ContextCompat.getColor(this, color));

            // Set the tag to the color resource ID
            colorButton.setTag(color);
            colorButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedColor = (int) v.getTag();
                }
            });

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    getResources().getDimensionPixelSize(R.dimen.color_button_size),
                    getResources().getDimensionPixelSize(R.dimen.color_button_size)
            );
            params.setMargins(10, 10, 10, 10);
            colorButton.setLayoutParams(params);

            // Add circular background to color button
            GradientDrawable shape = new GradientDrawable();
            shape.setShape(GradientDrawable.OVAL);
            shape.setColor(ContextCompat.getColor(this, color));
            colorButton.setBackground(shape);

            colorLayout.addView(colorButton);
        }

        btnAddCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String categoryName = etCategoryName.getText().toString().trim();
                if (!categoryName.isEmpty()) {
                    // Get selected color as a hex string
                    String hexColor = String.format("#%06X", (0xFFFFFF & selectedColor));

                    // Add category to Firestore
                    addCategoryToFirestore(db, userId, categoryName, Integer.parseInt(hexColor));
                    dialog.dismiss(); // Close the dialog after adding the category
                } else {
                    Toast.makeText(TimetableActivity.this, "Enter a category name", Toast.LENGTH_SHORT).show();
                }
            }
        });

        dialog.show();

    }
    // add the collections category to specific user
    private void initCategories(FirebaseFirestore db, String userId) {
        Map<String, Object> classCategory = new HashMap<>();
        classCategory.put("name", "Class");
        classCategory.put("color", ContextCompat.getColor(this, R.color.pastelBlue));

        Map<String, Object> meetingCategory = new HashMap<>();
        meetingCategory.put("name", "Meeting");
        meetingCategory.put("color", ContextCompat.getColor(this, R.color.pastelCoral));

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

    private void addCategoryToFirestore(FirebaseFirestore db, String userId, String categoryName, int color) {
        // Use category name as the document ID
        db.collection("users").document(userId).collection("categories")
                .document(categoryName)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                // Category already exists
                                Log.d("Firestore", "Category already exists");
                            } else {
                                // Category doesn't exist, add it with the category name as ID
                                Map<String, Object> category = new HashMap<>();
                                category.put("name", categoryName);
                                category.put("color", color);

                                db.collection("users").document(userId).collection("categories")
                                        .document(categoryName)
                                        .set(category)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d("Firestore", "Category added successfully");
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.w("Firestore", "Error adding category", e);
                                            }
                                        });
                            }
                        } else {
                            Log.w("Firestore", "Error getting categories", task.getException());
                        }
                    }
                });
    }

    private void createEvents(FirebaseFirestore db, String userId) {
        // Reference the events collection under the user's document
        CollectionReference eventsCollection = db.collection("users").document(userId).collection("events");

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

    private void fetchEventsAndUpdateRecyclerView(FirebaseFirestore db, String userId) {
        db.collection("users").document(userId).collection("events")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<TimetableData> eventsList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        TimetableData event = document.toObject(TimetableData.class);
                        eventsList.add(event);
                    }

                    TimetableAdapter adapter = new TimetableAdapter(eventsList);
                    recyclerView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                    Log.e("FetchEvents", "Error fetching events: " + e.getMessage(), e);
                });
    }

    private void recyclerView(ArrayList<TimetableData> eventList, ArrayList<TimetableData> events) {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        TimetableAdapter adapter = new TimetableAdapter(eventList, events, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        recyclerView.getAdapter().notifyDataSetChanged();
    }

    private void filter(ArrayList<TimetableData> eventList, String date) {
        ArrayList<TimetableData> filterList = new ArrayList<>();
        for (TimetableData data : eventList){
            if(data.getStartDate().equals(date)) {
                filterList.add(data);
            }
        }
        events = filterList;
        recyclerView(eventList, filterList);
        Log.d("FilterTest", "Filter applied. Total events: " + filterList.size());
        Log.d("DebugEvents", "Events size: " + events.size());
        Log.d("DebugEvents", "FilterList size: " + filterList.size());
        Log.d("DebugEvents", "EventList size: " + eventList.size());
    }

}
