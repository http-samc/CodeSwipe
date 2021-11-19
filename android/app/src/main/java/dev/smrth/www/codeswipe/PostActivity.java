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

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PostActivity extends AppCompatActivity {

    Spinner mLang;
    EditText mSnip, mDesc, mRepo;
    RequestQueue cue;

    public static final String[] toFileExt = {
            "Java",
            "java",
            "Kotlin",
            "kt",
            "C",
            "c",
            "C#",
            "cs",
            "C++",
            "cpp",
            "Python",
            "py",
            "Ruby",
            "rb",
            "JavaScript",
            "js",
            "Go",
            "go",
            "R",
            "r",
            "LaTex",
            "tex",
            "MatLab",
            "m",
            "Bash",
            "sh",
            "YAML",
            "yaml"
    };

    private static final String TAG = "PostActivity";
    private static final String KEY_LANGUAGE = "language";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_SNIPPET = "snippet";
    private static final String KEY_REPO = "repoName";
    private static final String KEY_AUTHOR = "author";
    private static final String KEY_VIEWED_BY = "viewedBy";
    private static final String KEY_GIST = "gist";

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    DocumentReference postRef = db.document("Posts/Each Post");

    static DocumentReference mostRecentPost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        mLang = findViewById(R.id.language);
        mSnip = findViewById(R.id.snippet);
        mDesc = findViewById(R.id.description);
        mRepo = findViewById(R.id.repoName);

        this.cue = Volley.newRequestQueue(this);

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
                R.array.languages, R.layout.selected_item);
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
        if (description.length() > 120) {
            Toast.makeText(
                    this, "Your description can't be longer than 120 characters (current: " + description.length() + ")",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        // Check # of lines
        char newLine = '\n';
        int c = 0;

        for (int i = 0; i < snippet.length(); i++) {
            if (snippet.charAt(i) == newLine)
                c++;
        }

        if (c > 150) {
            Toast.makeText(
                    this, "Your snippet can't be longer than 150 lines (current: " + snippet.length() + ")",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        // Getting username to add to author attr
        SharedPreferences sp = getSharedPreferences(
                AuthActivity.PREFERENCES,
                Context.MODE_PRIVATE
        );
        String username = sp.getString(AuthActivity.usernameKey, "");

        // Viewed By (only has current user to start)
        List<String> viewedBy = Arrays.asList(HomeActivity.username);

        // Filling in post object with input
        Map<String, Object> post = new HashMap<>();
        post.put("timestamp", FieldValue.serverTimestamp());
        post.put(KEY_LANGUAGE, language);
        post.put(KEY_SNIPPET, snippet);
        post.put(KEY_DESCRIPTION, description);
        post.put(KEY_REPO, repoName);
        post.put(KEY_AUTHOR, username);
        post.put(KEY_VIEWED_BY, viewedBy);
        post.put(KEY_GIST, "https://www.smrth.dev/codeswipe");

        // Create Gist
        try {
            createGist(post);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Send to database w/ callbacks
        postRef.collection("Each Post")
                .add(post)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        PostActivity.mostRecentPost = documentReference;

                        Toast.makeText(PostActivity.this, "Submitted Post Successfully!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(PostActivity.this, HomeActivity.class);
                        startActivity(intent);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        PostActivity.mostRecentPost = null;

                        Toast.makeText(PostActivity.this, "Error adding post! Please try again later.", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(PostActivity.this, HomeActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
    }

    // Create a gist for the snippet
    public void createGist(Map<String, Object> doc) throws JSONException {
        String url = "https://api.github.com/gists";
        // Create request payload
        JSONObject fileDesc = new JSONObject();
        fileDesc.put(
                "content",
                doc.get("snippet")
        );
        JSONObject files = new JSONObject();
        int idx = 0; // Make it so idx + 1 is our file extension
        String language = doc.get("language").toString();
        for (int i = 0; i < PostActivity.toFileExt.length; i++) {
            if (PostActivity.toFileExt[i].equals(language)) {
                idx = i;
                break;
            }
        }
        files.put(
                "CodeSwipeSnippet." + PostActivity.toFileExt[idx+1],
                fileDesc
        );
        JSONObject body = new JSONObject();
        body.put("files", files);
        body.put("public", true);
        body.put("description", doc.get("description"));

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, url, body, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String gistUrl = response.getString("html_url");
                    while (PostActivity.mostRecentPost == null) // wait for document creation
                        assert true;
                    PostActivity.mostRecentPost.update("gist", gistUrl);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }) {
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
}