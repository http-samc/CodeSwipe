package dev.smrth.www.codeswipe;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.firebase.firestore.QueryDocumentSnapshot;

public class PostsAdapter extends ArrayAdapter<QueryDocumentSnapshot> {

    public PostsAdapter(Context context, int id) {
        super(context, id);
    }

    @Override
    public View getView(int position, final View v, ViewGroup parent) {

        QueryDocumentSnapshot doc = getItem(position);

        TextView snippet = (TextView) v.findViewById(R.id.postSnippet);
        TextView author = (TextView) v.findViewById(R.id.postAuthor);
        TextView lang = (TextView) v.findViewById(R.id.postLang);
        TextView date = (TextView) v.findViewById(R.id.postDate);
        TextView desc = (TextView) v.findViewById(R.id.postDesc);

        // Make snippet a codeview and use lang
        snippet.setText(doc.get("snippet").toString());
        author.setText(doc.get("author").toString());
        lang.setText(doc.get("language").toString());
        date.setText(doc.get("date").toString());
        desc.setText(doc.get("description").toString());

        return v;
    }
}

