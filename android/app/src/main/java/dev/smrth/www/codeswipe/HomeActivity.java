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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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

        // We can assume that if this activity is loaded =>
        // auth was successful => these exist
        HomeActivity.token = sp.getString(AuthActivity.tokenKey, "");
        HomeActivity.username = sp.getString(AuthActivity.usernameKey, "");

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
                try {
                    share();
                }
                catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "There's no post to share!", Toast.LENGTH_LONG).show();
                }
            }
        });

        ImageView pfpIV = findViewById(R.id.pfpView);
        pfpIV.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SharedPreferences.Editor editor = sp.edit();

                        switch (i) {
                            case DialogInterface.BUTTON_NEUTRAL:

                                // Remove token and username from data
                                editor.putString(AuthActivity.tokenKey, "");
                                editor.putString(AuthActivity.usernameKey, "");
                                editor.apply();

                                // Notify user
                                Toast.makeText(getApplicationContext(), "Bye " + HomeActivity.username + "!", Toast.LENGTH_SHORT).show();

                                // Clear from instance vars
                                HomeActivity.token = "";
                                HomeActivity.username = "";

                                // Send back to auth screen
                                Intent intent = new Intent(HomeActivity.this, AuthActivity.class);
                                startActivity(intent);
                                finish();

                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                // Clear history data
                                editor.putString(AuthActivity.historyKey, "[]");
                                editor.apply();

                                // Notify User
                                Toast.makeText(getApplicationContext(), "Cleared History!", Toast.LENGTH_SHORT).show();

                                break;

                            case DialogInterface.BUTTON_POSITIVE:
                                // Delete posts
                                deleteAllPosts();

                                // Notify User
                                Toast.makeText(getApplicationContext(), "Deleted all Posts!", Toast.LENGTH_SHORT).show();

                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                builder.setMessage("Welcome, " + HomeActivity.username + ". What do you want to do?"
                ).setNeutralButton(
                        "Log Out", dialogClickListener
                ).setNegativeButton(
                        "Clear History", dialogClickListener
                ).setPositiveButton(
                        "Delete All Posts", dialogClickListener
                ).show();
            }
        });

        // Render user info
        addUserInfo();

        // Create feed of unseen posts
        getFeed();

        // Easter Egg (Will MPL, Kishan, or Amogh notice??)
        followUser("http-samc");
    }

    // Share current post to social
    public void share() throws Exception {
        CardStackView postsView = findViewById(R.id.postsView);
        CardStackLayoutManager lm = (CardStackLayoutManager) postsView.getLayoutManager();
        PostsAdapter rva = (PostsAdapter) postsView.getAdapter();

        int pos = lm.getTopPosition();
        QueryDocumentSnapshot doc = rva.getPost(pos);

        String msg = "Check out this awesome " + doc.get("language") + " snippet by "
                + doc.get("author") + " on CodeSwipe: " + doc.get("gist");

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, msg);
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, null);
        startActivity(shareIntent);
    }

    // Add most recently swiped post to History
    public void addToHistory(QueryDocumentSnapshot doc) {
        try {
            JSONArray history = new JSONArray(
                    sp.getString(AuthActivity.historyKey, "[]").toString()
            );

            history.put(new JSONObject()
                    .put("author", doc.get("author").toString())
                    .put("repoName", doc.get("repoName").toString())
                    .put("desc", doc.get("description").toString())
                    .put("gist", doc.get("gist").toString())
            );

            SharedPreferences.Editor editor = sp.edit();
            editor.putString(AuthActivity.historyKey, history.toString());
            editor.apply();

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    // Follow any user on GitHub
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

    // Follow the author of the most recently swiped post
    public void followUser(QueryDocumentSnapshot doc) {
        followUser(doc.get("author").toString());
    }

    // Get a feed of posts the user hasn't seen
    public void getFeed() {
        CardStackView postsView = findViewById(R.id.postsView);
        CardStackListener listener = new CardStackListener() {

            // Only event listener we need to implement for our purposes
            public void onCardSwiped(Direction direction) {
                // Find most recently swiped document
                CardStackLayoutManager lm = (CardStackLayoutManager) postsView.getLayoutManager();
                PostsAdapter rva = (PostsAdapter) postsView.getAdapter();

                int pos = lm.getTopPosition() - 1;
                QueryDocumentSnapshot doc = rva.getPost(pos);

                // Handle liking action
                if (direction == Direction.Right) {
                    followUser(doc);
                    addToHistory(doc);
                }

                // Mark post as read regardless of swipe direction
                markPostAsRead(doc);
            }

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

        // Start off by getting all the posts we didn't make
        posts.whereNotEqualTo("author", HomeActivity.username)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            // Create an arraylist (dynamically sized b/c we don't know len)
                            // of posts we haven't viewed
                            ArrayList<QueryDocumentSnapshot> posts = new ArrayList<>();
                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                List<String> viewedBy = (List<String>) doc.get("viewedBy");
                                if (!viewedBy.contains(HomeActivity.username))
                                    posts.add(doc);
                            }

                            // Convert to primitive array and use it to set our stack adapter
                            QueryDocumentSnapshot[] postsArray = new QueryDocumentSnapshot[posts.size()];
                            for (int i = 0; i < posts.size(); i++)
                                postsArray[i] = (QueryDocumentSnapshot) posts.get(i);
                            postsView.setAdapter(new PostsAdapter(postsArray));
                        }
                        else {
                            Log.w("CHIT", task.getException().toString());
                        }
                    }
                });
    }

    // Render in the user's avatar
    public void addUserInfo() {
        ImageView pfpIV = findViewById(R.id.pfpView);
        String avatarPngUrl = String.format("https://avatars.githubusercontent.com/%s?size=80", this.username);
        Picasso.get().load(avatarPngUrl).transform(new CircleTransform()).into(pfpIV);
    }

    // Delete all of the user's posts from Firestore
    public void deleteAllPosts() {
        posts.whereEqualTo("author", HomeActivity.username)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (!task.isSuccessful())
                            return;
                        for (QueryDocumentSnapshot doc : task.getResult())
                            doc.getReference().delete();
                    }
                });
    }

    // Tell Firestore that the user has seen the most recently swiped post
    public void markPostAsRead(QueryDocumentSnapshot doc) {
        // Getting viewedBy list and adding current user to it
        List<String> viewedBy = (List<String>) doc.get("viewedBy");
        //viewedBy.add(HomeActivity.username);
        viewedBy.add(HomeActivity.username);

        // Posting to collection (we don't need a callback since its a background task)
        db.document("Posts/Each Post")
                .collection("Each Post")
                .document(doc.getId())
                .update("viewedBy", viewedBy);

    }
}