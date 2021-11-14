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

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> {

    private QueryDocumentSnapshot[] posts;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView snippet, desc, lang, author;

        public ViewHolder(View view) {
            super(view);

            snippet = view.findViewById(R.id.postSnippet);
            desc = view.findViewById(R.id.postDesc);
            lang = view.findViewById(R.id.postLang);
            author = view.findViewById(R.id.postAuthor);

            author.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String url = "https://github.com/" + ((TextView) view).getText();
                    /*
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(browserIntent);
                    TODO implement (optional)
                     */
                }
            });
        }

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
        v.snippet.setText(doc.get("snippet").toString());
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
