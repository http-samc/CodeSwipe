package dev.smrth.www.codeswipe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;
import com.yuyakaido.android.cardstackview.CardStackView;

import org.w3c.dom.Text;

public class HomeActivity extends AppCompatActivity {

    static String token;
    static String username;
    RequestQueue cue;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference posts = db.collection("Posts/Each Post/Each Post");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        SharedPreferences sp = getSharedPreferences(
                AuthActivity.PREFERENCES,
                Context.MODE_PRIVATE
        );

        // We can assume that if this activity is loaded, auth was successful
        this.token = sp.getString(AuthActivity.tokenKey, "");
        this.username = sp.getString(AuthActivity.usernameKey, "");

        this.cue = Volley.newRequestQueue(this);

        // Set click listeners up
        Button createBtn = findViewById(R.id.createBtn);
        createBtn.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, PostActivity.class);
                startActivity(intent);
            }
        });

        ImageView pfpIV = findViewById(R.id.pfpView);
        pfpIV.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case DialogInterface.BUTTON_POSITIVE:
                                Intent intent = new Intent(HomeActivity.this, AuthActivity.class);
                                startActivity(intent);
                                finish();
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                // No (ignore)
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                builder.setMessage("Do you want to log out of '" + HomeActivity.username + "'?").setPositiveButton(
                        "Log Out", dialogClickListener
                ).setNegativeButton(
                        "Cancel", dialogClickListener
                ).show();

            }
        });

        // Render user info
        addUserInfo();

        getFeed();
    }

    public void getFeed() {
        CardStackView postsView = findViewById(R.id.postsView);
        //PostsAdapter postsAdapter = new PostsAdapter(getApplicationContext(), 0);

        posts.whereEqualTo("author", this.username) // FIXME change to "whereNotEqualTo" in production to get OTHER ppls posts
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QueryDocumentSnapshot[] posts = new QueryDocumentSnapshot[task.getResult().size()];
                            int i = 0;
                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                posts[i] = doc;
                                i++;
                            }
                            postsView.setAdapter(new PostsAdapter(posts));
                        }
                        else {
                            Log.w("CHIT", task.getException().toString());
                        }
                    }
                });
    }
    public void addUserInfo() {
        ImageView pfpIV = findViewById(R.id.pfpView);
        String avatarPngUrl = String.format("https://avatars.githubusercontent.com/%s?size=80", this.username);
        Picasso.get().load(avatarPngUrl).transform(new CircleTransform()).into(pfpIV);
    }
}