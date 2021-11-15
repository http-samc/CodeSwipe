package dev.smrth.www.codeswipe;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.pddstudio.highlightjs.HighlightJsView;
import com.pddstudio.highlightjs.models.Language;

import java.util.Locale;

import io.github.kbiakov.codeview.CodeView;
import io.github.kbiakov.codeview.highlight.ColorTheme;
import io.github.kbiakov.codeview.highlight.ColorThemeData;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> {

    private QueryDocumentSnapshot[] posts;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        HighlightJsView snippet;
        TextView desc, lang, author;

        public ViewHolder(View view) {
            super(view);

            snippet = view.findViewById(R.id.postSnippet);
            desc = view.findViewById(R.id.postDesc);
            lang = view.findViewById(R.id.postLang);
            author = view.findViewById(R.id.postAuthor);

            snippet.setHighlightLanguage(Language.AUTO_DETECT);
        }

    }

    // Prevent text overflow in CodeView
    public static String calcOverflow(String str) {
        int t = 45;
        String ret = "";

        int c = 0; // what col we're at
        for (int i = 0; i < str.length(); i++) { // iter through str
            if (str.charAt(i) == '\n') {
                c = -1;
            }
            if (c >= t) {
                ret += '\n';
                c = 0;
            }
            ret += str.charAt(i);
            c++;
        }

        return ret;
    }

    public PostsAdapter(QueryDocumentSnapshot[] dataSet) {
        posts = dataSet;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.post_view, viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder v, final int position) {

        QueryDocumentSnapshot doc = posts[position];

        String author = doc.get("author").toString();
        String repo = doc.get("repoName").toString();

        // Adds in routing to specific repo if provided
        if (!repo.equals("")) {
            author += "/" + repo;
        }

        v.author.setText(author);
        v.snippet.setSource(
                PostsAdapter.calcOverflow(doc.get("snippet").toString())
        );
        v.desc.setText(doc.get("description").toString());
        v.lang.setText(doc.get("language").toString());

    }

    public QueryDocumentSnapshot getPost(int pos) {
        return this.posts[pos];
    }

    @Override
    public int getItemCount() {
        return posts.length;
    }
}
