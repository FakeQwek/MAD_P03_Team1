//This class

package sg.edu.np.mad.inkwell;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;

public class Navbar {
    private Activity MainActivity;
    public Navbar(Activity MainActivity){
       this.MainActivity = MainActivity;
    }


    public Intent redirect(int id) {

        if (id == R.id.nav_notes) {
            Intent newActivity = new Intent(MainActivity, NotesActivity.class);
            Log.d( "Alert", "Opening notes");
            return newActivity;
        }
        else if (id == R.id.nav_todo) {
            Intent newActivity = new Intent(MainActivity, TodoActivity.class);
            Log.d("Alert", "Opening todo list");
            return newActivity;
        }
        else if (id == R.id.nav_flashcards) {
            Intent newActivity = new Intent(MainActivity, FlashcardActivity.class);
            Log.d("Alert", "Opening flashcards");
            return newActivity;
        }
        else if (id == R.id.nav_calendar) {
            Log.d("Alert", "Opening calendar");
        }
        else if (id == R.id.nav_home) {
            Log.d("Alert", "Opening homepage");
        }
        else if (id == R.id.nav_logout) {
            Intent newActivity = new Intent(MainActivity, LoginActivity.class);
            Log.d("Alert", "Logging out");
        }
        else if (id == R.id.nav_timetable) {
            Intent newActivity = new Intent(MainActivity, TimetableActivity.class);
            Log.d("Alert", "Opening timetable");
        }
        else {
           Log.d("Alert", "Unknown page!");
        }

        Intent newActivity = null;
        return newActivity;
    }



}
