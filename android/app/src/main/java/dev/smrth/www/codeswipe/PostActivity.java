package dev.smrth.www.codeswipe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.UUID;

public class PostActivity extends AppCompatActivity {


    EditText mLang, mSnip, mDesc;
    Button mPost;
    FirebaseFirestore db;
    DatabaseReference UsersRef, PostsRef;
    String saveCurrentDate, saveCurrentTime, postRandomName, current_user_id;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        mLang = findViewById(R.id.edit_language);
        mSnip = findViewById(R.id.edit_snip);
        mDesc = findViewById(R.id.edit_desc);
        mPost = findViewById(R.id.post_btn);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");

        mPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String language = mLang.getText().toString();
                String snippet = mSnip.getText().toString();
                String description = mDesc.getText().toString();


                saveToFirestore(language, snippet, description);

            }
        });

    }


    private void saveToFirestore(String language, String snippet, String description) {

        Calendar calFordDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(calFordDate.getTime());

        Calendar calFordTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
        saveCurrentTime = currentTime.format(calFordTime.getTime());

        postRandomName = saveCurrentDate + saveCurrentTime;

        UsersRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String userFullName = dataSnapshot.child("fullname").getValue().toString();
                    String userProfileImage = dataSnapshot.child("profileimage").getValue().toString();


                    HashMap<String, Object> map = new HashMap<>();
                    map.put("id", current_user_id);
                    map.put("date", saveCurrentDate);
                    map.put("time", saveCurrentTime);
                    map.put("language", language);
                    map.put("code", snippet);
                    map.put("description", description);
                    PostsRef.child(postRandomName).updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(PostActivity.this, "Success!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(PostActivity.this, "Error Occured", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }
}