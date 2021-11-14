package dev.smrth.www.codeswipe;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> {

    private QueryDocumentSnapshot[] posts;


    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView snippet, desc, lang, date, author;

        public ViewHolder(View view) {
            super(view);

            snippet = view.findViewById(R.id.postSnippet);
            desc = view.findViewById(R.id.postDesc);
            date = view.findViewById(R.id.postDate);
            lang = view.findViewById(R.id.postLang);
            author = view.findViewById(R.id.postAuthor);

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

        v.author.setText(doc.get("author").toString());
        v.snippet.setText(doc.get("snippet").toString());
        v.desc.setText(doc.get("description").toString());
        v.date.setText(doc.get("timestamp").toString());
        v.lang.setText(doc.get("language").toString());

    }

    @Override
    public int getItemCount() {
        return posts.length;
    }
}
