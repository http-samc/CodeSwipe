package dev.smrth.www.codeswipe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.JsonObject;
import com.squareup.picasso.Picasso;
import com.yuyakaido.android.cardstackview.CardStackLayoutManager;
import com.yuyakaido.android.cardstackview.CardStackListener;
import com.yuyakaido.android.cardstackview.CardStackView;
import com.yuyakaido.android.cardstackview.Direction;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {

    static String token;
    static String username;
    RequestQueue cue;

    SharedPreferences sp;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference posts = db.collection("Posts/Each Post/Each Post");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        sp = getSharedPreferences(
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

        Button historyBtn = findViewById(R.id.historyBtn);
        historyBtn.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, HistoryActivity.class);
                startActivity(intent);
            }
        });

        Button shareBtn = findViewById(R.id.shareBtn);
        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                share();
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

    public void share() {
        CardStackView postsView = findViewById(R.id.postsView);
        CardStackLayoutManager lm = (CardStackLayoutManager) postsView.getLayoutManager();
        PostsAdapter rva = (PostsAdapter) postsView.getAdapter();

        int pos;
        try {
            pos = lm.getTopPosition();
        }
        catch (NullPointerException e) {
            Toast.makeText(this, "There's no post to share!", Toast.LENGTH_LONG).show();
            return;
        }

        QueryDocumentSnapshot doc = rva.getPost(pos);
        String msg = "Check out this awesome " + doc.get("language") + " snippet by "
                + doc.get("author") + " on CodeSwipe:\n\n" + doc.get("snippet");

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, msg);
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, null);
        startActivity(shareIntent);
    }

    public void addToHistory(QueryDocumentSnapshot doc) {
        try {
            JSONArray history = new JSONArray(
                    sp.getString(AuthActivity.historyKey, "[]").toString()
            );

            history.put(new JSONObject()
                    .put("user", doc.get("author").toString())
                    .put("desc", doc.get("description").toString())
            );

            SharedPreferences.Editor editor = sp.edit();
            editor.putString(AuthActivity.historyKey, history.toString());
            editor.apply();

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void followUser(String user) {

        String url = "https://api.github.com/user/following/" + user;

        StringRequest req = new StringRequest(Request.Method.PUT, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.e("Response", response);
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        Log.e("ERROR","error => "+error.toString());
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String>  params = new HashMap<String, String>();

                params.put("Accept", "application/vnd.github.v3+json");
                params.put("Authorization", "token " + HomeActivity.token);

                return params;
            }
        };
        this.cue.add(req);
    }

    public void getFeed() {
        CardStackView postsView = findViewById(R.id.postsView);
        CardStackListener listener = new CardStackListener() {
            public void onCardSwiped(Direction direction) {
                if (direction == Direction.Right) {
                    CardStackLayoutManager lm = (CardStackLayoutManager) postsView.getLayoutManager();
                    PostsAdapter rva = (PostsAdapter) postsView.getAdapter();

                    int pos = lm.getTopPosition() - 1;
                    QueryDocumentSnapshot doc = rva.getPost(pos);

                    // Handle following user
                    followUser(
                            doc.get("author").toString()
                    );

                    // Handle history
                    addToHistory(doc);
                }
            }

            // I think the author of this library didn't know how to make
            // the interface methods optional. . . not much we can do besides have these empty defs
            public void onCardRewound() {}
            public void onCardCanceled() {}
            public void onCardAppeared(View view, int position) {}
            public void onCardDisappeared(View view, int position) {}
            public void onCardDragging(Direction direction, float ratio) {}

        };

        // Config layout manager
        CardStackLayoutManager layoutManager = new CardStackLayoutManager(getApplicationContext(), listener);
        layoutManager.setDirections(Direction.HORIZONTAL);
        layoutManager.setCanScrollHorizontal(true);
        layoutManager.setCanScrollVertical(false);

        postsView.setLayoutManager(layoutManager);

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