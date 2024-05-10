package sg.edu.np.mad.inkwell;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.ViewAnimator;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class TodoActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // Get firebase
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private int currentTodoId;

    private String currentStatus;

    private void recyclerView(ArrayList<Todo> allTodos, ArrayList<Todo> todos) {
        RecyclerView recyclerView = findViewById(R.id.todoRecyclerView);
        TodoAdapter adapter = new TodoAdapter(allTodos, todos, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        recyclerView.getAdapter().notifyDataSetChanged();
    }

    private void filter(ArrayList<Todo> todos, String status, String query) {
        ArrayList<Todo> filterList = new ArrayList<>();
        for (Todo todo : todos){
            if(todo.getTodoStatus().equals(status) && todo.getTodoTitle().toLowerCase().contains(query)) {
                filterList.add(todo);
            }
        }
        recyclerView(todos, filterList);
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

        ArrayList<Todo> allTodos = new ArrayList<>();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");

        db.collection("todos")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w("testing", "listen:error", e);
                            return;
                        }

                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            if (dc.getType() == DocumentChange.Type.ADDED) {
                                if (Integer.parseInt(dc.getDocument().getId()) > currentTodoId) {
                                    currentTodoId = Integer.parseInt(dc.getDocument().getId());
                                }
                                Todo todo = new Todo(dc.getDocument().getData().get("title").toString(), Integer.parseInt(dc.getDocument().getId()), dc.getDocument().getData().get("dateTime").toString(), dc.getDocument().getData().get("status").toString());
                                allTodos.add(todo);
                                filter(allTodos, "todo", "");
                            } else if (dc.getType() == DocumentChange.Type.REMOVED) {
                                Log.d("tester", String.valueOf(allTodos.size()));
                            }
                        }
                    }
                });

        Button addTodoButton = findViewById(R.id.addTodoButton);

        addTodoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, Object> todoData = new HashMap<>();
                todoData.put("title", "New todo");
                todoData.put("dateTime", simpleDateFormat.format(Calendar.getInstance().getTime()));
                todoData.put("status", "todo");
                db.collection("todos").document(String.valueOf(currentTodoId + 1)).set(todoData);
            }
        });

        Button todoButton = findViewById(R.id.todoButton);

        todoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentStatus = "todo";

                filter(allTodos, "todo", "");
            }
        });

        Button inProgressButton = findViewById(R.id.inProgressButton);

        inProgressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentStatus = "inProgress";

                filter(allTodos, "inProgress", "");
            }
        });

        Button doneButton = findViewById(R.id.doneButton);

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentStatus = "done";

                filter(allTodos, "done", "");
            }
        });

        SearchView searchView = findViewById(R.id.searchView);

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