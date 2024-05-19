package sg.edu.np.mad.inkwell;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.RemoteAction;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.TimePicker;
import android.widget.ViewAnimator;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class TodoActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // Get firebase
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Get id of current user
    String currentFirebaseUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

    // Declaration of variables

    // currentTodoId keeps track of the ids that have already been assigned
    private int currentTodoId;

    public static String currentStatus = "todo";

    int hour = 0;

    int minute = 0;

    int year;

    int month;

    int day;

    Date date;

    // Method to set items in the recycler view
    private void recyclerView(ArrayList<Todo> allTodos, ArrayList<Todo> todos) {
        RecyclerView recyclerView = findViewById(R.id.todoRecyclerView);
        TodoAdapter adapter = new TodoAdapter(allTodos, todos, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        recyclerView.getAdapter().notifyDataSetChanged();
    }

    // Method to filter items already in the recycler view
    private void filter(ArrayList<Todo> todos, String status, String query) {
        ArrayList<Todo> filterList = new ArrayList<>();
        for (Todo todo : todos){
            if(todo.getTodoStatus().equals(status) && todo.getTodoTitle().toLowerCase().contains(query)) {
                filterList.add(todo);
            }
        }
        recyclerView(todos, filterList);
    }

    // Create notification channel for sending notifications
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Todo";
            String description = "todo";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("Todo", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo);

        //Sets toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //Finds drawer and nav view before setting listener
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

        createNotificationChannel();

        ArrayList<Todo> allTodos = new ArrayList<>();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");

        // Read from firebase and create todos on create
        db.collection("todos")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w("testing", "listen:error", e);
                            return;
                        }

                        // Adds items to recycler view on create and everytime new data is added to firebase
                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            if (Integer.parseInt(dc.getDocument().getId()) > currentTodoId) {
                                currentTodoId = Integer.parseInt(dc.getDocument().getId());
                            }
                            String docTodoUid = String.valueOf(dc.getDocument().getData().get("uid"));
                            if (dc.getType() == DocumentChange.Type.ADDED && docTodoUid.equals(currentFirebaseUserUid)) {
                                Todo todo = new Todo(dc.getDocument().getData().get("title").toString(), Integer.parseInt(dc.getDocument().getId()), dc.getDocument().getData().get("description").toString(), dc.getDocument().getData().get("dateTime").toString(), dc.getDocument().getData().get("status").toString());
                                allTodos.add(todo);
                                filter(allTodos, "todo", "");
                            }
                        }
                    }
                });

        ImageButton addTodoButton = findViewById(R.id.addTodoButton);

        // Brings up a menu to create a todo
        addTodoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(TodoActivity.this);
                View view = LayoutInflater.from(TodoActivity.this).inflate(R.layout.add_todo_bottom_sheet, null);
                bottomSheetDialog.setContentView(view);
                bottomSheetDialog.show();

                TextInputEditText titleEditText = view.findViewById(R.id.titleEditText);
                TextInputEditText descriptionEditText = view.findViewById(R.id.descriptionEditText);

                Button timePickerButton = view.findViewById(R.id.timePickerButton);

                timePickerButton.setText("00:00");

                // Brings up a time picker
                timePickerButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener()
                        {
                            @Override
                            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute)
                            {
                                hour = selectedHour;
                                minute = selectedMinute;
                                timePickerButton.setText(String.format(Locale.getDefault(), "%02d:%02d",hour, minute));
                            }
                        };

                        TimePickerDialog timePickerDialog = new TimePickerDialog(TodoActivity.this, onTimeSetListener, hour, minute, true);

                        timePickerDialog.show();
                    }
                });

                Button datePickerButton = view.findViewById(R.id.datePickerButton);

                Calendar calendar = Calendar.getInstance();

                datePickerButton.setText(String.format(Locale.getDefault(), "%02d/%02d/%02d", calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR)));

                // Brings up a date picker
                datePickerButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Calendar calendar = Calendar.getInstance();

                        DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
                                year = selectedYear;
                                month = selectedMonth + 1;
                                day = selectedDay;

                                datePickerButton.setText(String.format(Locale.getDefault(), "%02d/%02d/%02d", day, month, year));
                            }
                        };

                        DatePickerDialog datePickerDialog = new DatePickerDialog(TodoActivity.this, onDateSetListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

                        datePickerDialog.show();
                    }
                });

                Button createTodoButton = view.findViewById(R.id.createTodoButton);

                // Adds a todo to firebase
                createTodoButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Map<String, Object> todoData = new HashMap<>();
                        todoData.put("title", titleEditText.getText().toString());
                        todoData.put("description", descriptionEditText.getText().toString());
                        todoData.put("dateTime", simpleDateFormat.format(Calendar.getInstance().getTime()));
                        todoData.put("status", "todo");
                        todoData.put("uid", currentFirebaseUserUid);
                        db.collection("todos").document(String.valueOf(currentTodoId + 1)).set(todoData);
                        bottomSheetDialog.dismiss();

                        Intent intent = new Intent(TodoActivity.this, TodoBroadcast.class);
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(TodoActivity.this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
                        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

                        try {
                            date = simpleDateFormat.parse(datePickerButton.getText().toString());
                        } catch (ParseException e) {
                            throw new RuntimeException(e);
                        }

                        long time = hour * 3600000 + minute * 60000;

                        alarmManager.set(AlarmManager.RTC_WAKEUP, date.getTime() + time, pendingIntent);
                    }
                });

                Button cancelButton = view.findViewById(R.id.cancelButton);

                // Cancels the process
                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bottomSheetDialog.dismiss();
                    }
                });
            }
        });

        Button todoButton = findViewById(R.id.todoButton);

        // Set recycler view to only show todos with status todo
        todoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentStatus = "todo";

                filter(allTodos, "todo", "");
            }
        });

        Button inProgressButton = findViewById(R.id.inProgressButton);

        // Set recycler view to only show todos with status inProgress
        inProgressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentStatus = "inProgress";

                filter(allTodos, "inProgress", "");
            }
        });

        Button doneButton = findViewById(R.id.doneButton);

        // Set recycler view to only show todos with status done
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentStatus = "done";

                filter(allTodos, "done", "");
            }
        });

        SearchView searchView = findViewById(R.id.searchView);

        // Search the items in recycler view
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filter(allTodos, currentStatus, query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(allTodos, currentStatus, newText);
                return false;
            }
        });
    }

    //Allows movement between activities upon clicking
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.nav_notes) {
            Intent notesActivity = new Intent(TodoActivity.this, NotesActivity.class);
            startActivity(notesActivity);
            Log.d( "Message", "Opening notes");
        }
        else if (menuItem.getItemId() == R.id.nav_todo) {
//            Intent todoActivity = new Intent(TodoActivity.this, TodoActivity.class);
//            startActivity(todoActivity);
            Log.d("Message", "Opening home");
            return true;
        }
        else if (menuItem.getItemId() == R.id.nav_calendar) {
            Log.d("Message", "Opening calendar");
        }
        else if (menuItem.getItemId() == R.id.nav_timetable) {
            Log.d("Message", "Opening timetable");
        }
        else {
            Log.d("Message", "Unknown page!");
        }
        return true;
    }
}