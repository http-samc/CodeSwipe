package dev.smrth.www.codeswipe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
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


    EditText mLang, mSnip, mDesc;
    private static final String TAG = "PostActivity";
    private static final String KEY_LANGUAGE = "language";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_SNIPPET = "snippet";

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    DocumentReference postRef = db.document("Posts/Each Post");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        mLang = findViewById(R.id.edit_language);
        mSnip = findViewById(R.id.edit_snip);
        mDesc = findViewById(R.id.edit_desc);

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

    public void savePost(View v) {
        String language = mLang.getText().toString();
        String snippet = mSnip.getText().toString();
        String description = mDesc.getText().toString();
        Map<String, Object> post = new HashMap<>();
        post.put("timestamp", FieldValue.serverTimestamp());
        post.put(KEY_LANGUAGE, language);
        post.put(KEY_SNIPPET, snippet);
        post.put(KEY_DESCRIPTION, description);
        postRef.collection("Each Post")
                .add(post)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding post", e);
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