package dev.smrth.www.codeswipe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;
import java.util.HashMap;
import java.util.Map;


public class PostActivity extends AppCompatActivity {

    Spinner mLang;
    EditText mSnip, mDesc, mRepo;

    private static final String TAG = "PostActivity";
    private static final String KEY_LANGUAGE = "language";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_SNIPPET = "snippet";
    private static final String KEY_REPO = "repoName";
    private static final String KEY_AUTHOR = "author";

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    DocumentReference postRef = db.document("Posts/Each Post");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        mLang = findViewById(R.id.language);
        mSnip = findViewById(R.id.snippet);
        mDesc = findViewById(R.id.description);
        mRepo = findViewById(R.id.repoName);

        addLangs();
    }

    @Override
    protected void onStart() {
        super.onStart();
        postRef.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    Toast.makeText(PostActivity.this, "Error while loading!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, e.toString());
                    return;
                }
            }
        });
    }

    public void addLangs() {
        Spinner langs = (Spinner) findViewById(R.id.language);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.languages, android.R.layout.simple_spinner_item);  // FIXME AND MAKE THE COLOR NOT BLACK
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        langs.setAdapter(adapter);
    }

    public void submitPost(View v) {
        String language = mLang.getSelectedItem().toString();
        String snippet = mSnip.getText().toString();
        String description = mDesc.getText().toString();
        String repoName = mRepo.getText().toString();

        // Check for null snippet (lang will always be selected)
        if (snippet.equals("")) {
            Toast.makeText(this, "Please enter a snippet.", Toast.LENGTH_LONG).show();
            return;
        }

        // Check for excessive description
        if (description.length() >= 120) {
            Toast.makeText(
                    this, "Your description must be under 120 characters (current: " + description.length() + ")",
                    Toast.LENGTH_LONG
            ).show();
        }

        // Getting username to add to author attr
        SharedPreferences sp = getSharedPreferences(
                AuthActivity.PREFERENCES,
                Context.MODE_PRIVATE
        );
        String username = sp.getString(AuthActivity.usernameKey, "");

        // Filling in post object with input
        Map<String, Object> post = new HashMap<>();
        post.put("timestamp", FieldValue.serverTimestamp());
        post.put(KEY_LANGUAGE, language);
        post.put(KEY_SNIPPET, snippet);
        post.put(KEY_DESCRIPTION, description);
        post.put(KEY_REPO, repoName);
        post.put(KEY_AUTHOR, username);

        // Send to database w/ callbacks
        postRef.collection("Each Post")
                .add(post)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(PostActivity.this, "Submitted Post Successfully!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(PostActivity.this, HomeActivity.class);
                        startActivity(intent);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(PostActivity.this, "Error adding post! Please try again later.", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(PostActivity.this, HomeActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
    }

    /*
    public void updateDescription(View v) {
        String description = mDesc.getText().toString();
        //Map<String, Object> note = new HashMap<>();
        //note.put(KEY_DESCRIPTION, description);
        //noteRef.set(note, SetOptions.merge());
        postRef.update(KEY_DESCRIPTION, description);
    }
    public void deleteDescription(View v) {
        //Map<String, Object> note = new HashMap<>();
        //note.put(KEY_DESCRIPTION, FieldValue.delete());
        //noteRef.update(note);
        postRef.update(KEY_DESCRIPTION, FieldValue.delete());
    }
    public void deletePost(View v) {
        postRef.delete();
    }

    //maybe need transactions or batched writes later? idk will need it for profile activity or smth
     */

}