package dev.smrth.www.codeswipe;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonObject;
import com.google.rpc.context.AttributeContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HistoryActivity extends AppCompatActivity {

    SharedPreferences sp;
    LinearLayout ll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        this.sp = getSharedPreferences(AuthActivity.PREFERENCES, Context.MODE_PRIVATE);
        this.ll = findViewById(R.id.historyParent);

        Toast.makeText(this,
                "Here's everything you've liked! Click on a post to open it in the browser!",
                Toast.LENGTH_LONG
        ).show();

        renderHistory();
    }

    public void renderHistory() {

        // A string of our history
        String historyStr = sp.getString(AuthActivity.historyKey, "[]");
        try {
            // Convert to our array and iter over
            JSONArray posts = new JSONArray(historyStr);

            for (int i = 0; i < posts.length(); i++) {
                // Getting post
                JSONObject post = (JSONObject) posts.get(i);

                // Getting author
                String author = post.getString("author");
                String repo = post.getString("repoName");
                if (!repo.equals(""))
                    author += "/" + repo;

                // Creating TV and formatting
                TextView container = new TextView(this);
                container.setTextAppearance(this, R.style.regTxt);
                String desc = post.getString("desc");
                if (desc.length() > 35)
                    desc = desc.substring(0, 35) + "...";
                String html = "<b>&#9" + author + "</b><p>     " + desc + "</p>";
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    container.setText(Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT));
                } else {
                    container.setText(Html.fromHtml(html));
                }
                container.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        openInBrowser(view);
                    }
                });
                // Adding to layout
                this.ll.addView(container);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void openInBrowser(View v) {
        // Find idx of View
        int i = this.ll.indexOfChild(v);

        // Open in browser
        try {
            JSONArray history = new JSONArray(sp.getString(AuthActivity.historyKey, "[]"));
            JSONObject post = (JSONObject) history.get(i-1); // account for header spot

            String author = post.getString("author");
            String repoName = post.getString("repoName");

            if (!repoName.equals("")) // add in repo if given
                author += "/" + post.getString("repoName");

            String url = post.getString("gist");

            Log.w("CHIT", url);
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(browserIntent);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}