//This class holds functionality for redirecting to activities based on buttong clicked

package sg.edu.np.mad.inkwell;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class Navbar {
    private Activity CurrentActivity;
    private String currentActivityName;
    public Navbar(Activity ReceivedActivity){

        this.CurrentActivity = ReceivedActivity;
        this.currentActivityName = ReceivedActivity.getClass().getSimpleName();
    }
    public Intent redirect(int id, boolean isMain) {
        if (id == R.id.nav_home && isMain){
            Toast toast = Toast.makeText(CurrentActivity.getBaseContext(), "Already in Homepage", Toast.LENGTH_SHORT);
            toast.show();

        }
        return null;
    }

    public Intent redirect(int id) {

        if (id == R.id.nav_notes) {
            Intent newActivity = new Intent(CurrentActivity, NotesActivity.class);
            Log.d( "Alert", "Opening notes");
            return newActivity;
        }
        else if (id == R.id.nav_todo) {
            Intent newActivity = new Intent(CurrentActivity, TodoActivity.class);
            Log.d("Alert", "Opening todo list");
            return newActivity;
        }
        else if (id == R.id.nav_flashcards) {
            Intent newActivity = new Intent(CurrentActivity, FlashcardActivity.class);
            Log.d("Alert", "Opening flashcards");
            return newActivity;
        }
        else if (id == R.id.nav_calendar) {
            Log.d("Alert", "Opening calendar");
        }
        else if (id == R.id.nav_home) {
            Intent newActivity = new Intent(CurrentActivity, MainActivity.class);
            Log.d("Alert", "Opening homepage");
            return newActivity;
        }
        else if (id == R.id.nav_logout) {
            Intent newActivity = new Intent(CurrentActivity, LoginActivity.class);
            Log.d("Alert", "Logging out");
            return newActivity;
        }
        else if (id == R.id.nav_timetable) {
            Intent newActivity = new Intent(CurrentActivity, TimetableActivity.class);
            Log.d("Alert", "Opening timetable");
            return newActivity;
        }
        else {
           Log.d("Alert", "Unknown page!");

        }

        Intent newActivity = null;
        return newActivity;
    }



}
