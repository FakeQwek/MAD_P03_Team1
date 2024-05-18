package sg.edu.np.mad.inkwell;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ViewAnimator;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class QuizFlashcardActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private int currentFlashcardPosition = 0;

    private int correct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_flashcard);

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

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.


        View decorView = getWindow().getDecorView();

        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;

        decorView.setSystemUiVisibility(uiOptions);

        ViewAnimator viewAnimator = findViewById(R.id.viewAnimator);

        ArrayList<String> questionList = new ArrayList<>();

        ArrayList<String> answerList = new ArrayList<>();

        TextView question1 = findViewById(R.id.question1);

        TextView question2 = findViewById(R.id.question2);

        db.collection("flashcardCollections").document(String.valueOf(FlashcardActivity.selectedFlashcardCollectionId)).collection("flashcards")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                questionList.add(document.getData().get("question").toString());
                                answerList.add(document.getData().get("answer").toString());
                            }
                        } else {
                            Log.d("testing", "Error getting documents: ", task.getException());
                        }
                        question1.setText(questionList.get(currentFlashcardPosition));
                    }
                });

        Button wrongButton = findViewById(R.id.wrongButton);

        wrongButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentFlashcardPosition++;
                if (currentFlashcardPosition >= questionList.size()) {
                    Intent flashcardActivity = new Intent(QuizFlashcardActivity.this, FlashcardActivity.class);
                    startActivity(flashcardActivity);

                    db.collection("flashcardCollections").document(String.valueOf(FlashcardActivity.selectedFlashcardCollectionId)).update("correct", correct);
                } else {
                    if (viewAnimator.getDisplayedChild() == 0) {
                        question2.setText(questionList.get(currentFlashcardPosition));
                        viewAnimator.setDisplayedChild(1);
                    } else {
                        question1.setText(questionList.get(currentFlashcardPosition));
                        viewAnimator.setDisplayedChild(0);
                    }
                }
            }
        });

        Button correctButton = findViewById(R.id.correctButton);

        correctButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentFlashcardPosition++;
                correct++;
                if (currentFlashcardPosition >= questionList.size()) {
                    Intent flashcardActivity = new Intent(QuizFlashcardActivity.this, FlashcardActivity.class);
                    startActivity(flashcardActivity);

                    db.collection("flashcardCollections").document(String.valueOf(FlashcardActivity.selectedFlashcardCollectionId)).update("correct", correct);
                } else {
                    if (viewAnimator.getDisplayedChild() == 0) {
                        question2.setText(questionList.get(currentFlashcardPosition));
                        viewAnimator.setDisplayedChild(1);
                    } else {
                        question1.setText(questionList.get(currentFlashcardPosition));
                        viewAnimator.setDisplayedChild(0);
                    }
                }
            }
        });

        Button answerButton1 = findViewById(R.id.answerButton1);

        answerButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (question1.getText() == answerList.get(currentFlashcardPosition)) {
                    question1.setText(questionList.get(currentFlashcardPosition));
                } else {
                    question1.setText(answerList.get(currentFlashcardPosition));
                }
            }
        });

        Button answerButton2 = findViewById(R.id.answerButton2);

        answerButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (question2.getText() == answerList.get(currentFlashcardPosition)) {
                    question2.setText(questionList.get(currentFlashcardPosition));
                } else {
                    question2.setText(answerList.get(currentFlashcardPosition));
                }
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();
        Navbar navbar = new Navbar(this);
        Intent newActivity = navbar.redirect(id);
        startActivity(newActivity);
        return true;
    }
}