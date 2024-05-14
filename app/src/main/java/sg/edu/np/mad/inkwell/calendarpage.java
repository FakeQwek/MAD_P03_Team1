package sg.edu.np.mad.inkwell;

import android.os.Bundle;
import android.app.AlertDialog;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class calendarpage extends AppCompatActivity {

    private CalendarView calendarView;
    private TextView eventDescriptionTextView;
    private Map<String, List<String>> eventsMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_calendarpage);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        calendarView = findViewById(R.id.calendarView);
        eventDescriptionTextView = findViewById(R.id.eventDescription);
        eventsMap = new HashMap<>();

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                String date = dayOfMonth + "/" + (month + 1) + "/" + year;
                displayEventsForDate(date);
                eventsList(date);
            }
        });
    }

    private void displayEventsForDate(String date) {
        if (eventsMap.containsKey(date)) {
            List<String> events = eventsMap.get(date);
            StringBuilder eventDescriptions = new StringBuilder("Events on " + date + ":\n");
            for (String event : events) {
                eventDescriptions.append("- ").append(event).append("\n");
            }
            eventDescriptionTextView.setText(eventDescriptions.toString());
        } else {
            eventDescriptionTextView.setText("No Event on " + date);
        }
    }

    private void eventsList(final String date) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Click on event to edit or delete!");

        if (eventsMap.containsKey(date)) {
            List<String> events = eventsMap.get(date);
            String[] eventsArray = events.toArray(new String[0]);
            builder.setItems(eventsArray, (dialog, which) -> editDelete(date, which));
        } else {
            builder.setMessage("No events for this day.");
        }

        builder.setPositiveButton("Create Event", (dialog, which) -> addEvent(date));

        builder.setNegativeButton("Close", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void editDelete(final String date, final int eventIndex) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit or Delete Event");

        builder.setPositiveButton("Edit", (dialog, which) -> editEvent(date, eventIndex));

        builder.setNegativeButton("Delete", (dialog, which) -> {
            eventsMap.get(date).remove(eventIndex);
            if (eventsMap.get(date).isEmpty()) {
                eventsMap.remove(date);
            }
            displayEventsForDate(date);
            Toast.makeText(calendarpage.this, "Event Deleted", Toast.LENGTH_SHORT).show();
        });

        builder.setNeutralButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void editEvent(final String date, final int eventIndex) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Event on " + date);

        final EditText input = new EditText(this);
        input.setText(eventsMap.get(date).get(eventIndex));
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String eventDescription = input.getText().toString();
            if (!eventDescription.isEmpty()) {
                eventsMap.get(date).set(eventIndex, eventDescription);
                displayEventsForDate(date);
                Toast.makeText(calendarpage.this, "Event Edited", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(calendarpage.this, "Event Description Cannot Be Empty", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void addEvent(final String date) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Event on " + date);

        final EditText input = new EditText(this);
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String eventDescription = input.getText().toString();
            if (!eventDescription.isEmpty()) {
                if (!eventsMap.containsKey(date)) {
                    eventsMap.put(date, new ArrayList<>());
                }
                eventsMap.get(date).add(eventDescription);
                displayEventsForDate(date);
                Toast.makeText(calendarpage.this, "Event Saved", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(calendarpage.this, "Event Description Cannot Be Empty", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }
}