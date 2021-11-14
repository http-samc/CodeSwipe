package dev.smrth.www.codeswipe;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.widget.LinearLayout;
import android.widget.TextView;

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

        sp = getSharedPreferences(AuthActivity.PREFERENCES, Context.MODE_PRIVATE);
        ll = findViewById(R.id.historyParent);

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

                // Creating TV and formatting
                TextView container = new TextView(this);
                container.setTextAppearance(this, R.style.regTxt);
                String html = "<b>" + post.getString("user") + "</b><p>" + post.getString("desc") + "</p>";
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    container.setText(Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT));
                } else {
                    container.setText(Html.fromHtml(html));
                }

                // Adding to layout
                this.ll.addView(container);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}