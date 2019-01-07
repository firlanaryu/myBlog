package com.creaginetech.myblog;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.creaginetech.myblog.adapter.BlogPost;
import com.creaginetech.myblog.adapter.BlogRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    private RecyclerView blog_list_view;
    private List<BlogPost> blog_list;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private BlogRecyclerAdapter blogRecyclerAdapter;

    private DocumentSnapshot lastVisible;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        blog_list = new ArrayList<>();
        blog_list_view = view.findViewById(R.id.blog_list_view);

        firebaseAuth = FirebaseAuth.getInstance();

        blogRecyclerAdapter = new BlogRecyclerAdapter(blog_list);
        blog_list_view.setLayoutManager(new LinearLayoutManager(container.getContext()));
        blog_list_view.setAdapter(blogRecyclerAdapter);


        if (firebaseAuth.getCurrentUser() != null) {

            firebaseFirestore = FirebaseFirestore.getInstance();


            blog_list_view.setOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    Boolean reachedBottom = !recyclerView.canScrollVertically(1);

                    if (reachedBottom){

                        String desc = lastVisible.getString("desc");
                        Toast.makeText(container.getContext(), "Reached : " + desc, Toast.LENGTH_SHORT).show();

                        loadMorePost();
                    }

                }
            });

            //to sequence post from new post to old post (DESCENDING) and limit 3 post
            //for next Query search on firebase firestore document

            Query firstQuery = firebaseFirestore.collection("Posts").orderBy("timestamp",Query.Direction.DESCENDING).limit(3);
            firstQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {


                    // Get the last visible document
                    lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() -1);


                    for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {

                        //if check data was added or change/edit
                        if (doc.getType() == DocumentChange.Type.ADDED) {

                            BlogPost blogPost = doc.getDocument().toObject(BlogPost.class);
                            blog_list.add(blogPost);

                            blogRecyclerAdapter.notifyDataSetChanged();

                        }

                    }

                }
            });

        }

        // Inflate the layout for this fragment
        return view;
    }

    public void loadMorePost(){

        Query nextQuery = firebaseFirestore.collection("Posts")
                .orderBy("timestamp",Query.Direction.DESCENDING)
                .startAfter(lastVisible)
                .limit(3);


        nextQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                //if load data until finished
                if (!queryDocumentSnapshots.isEmpty()) {

                    lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);

                    for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {

                        //if check data was added or change/edit
                        if (doc.getType() == DocumentChange.Type.ADDED) {

                            BlogPost blogPost = doc.getDocument().toObject(BlogPost.class);
                            blog_list.add(blogPost);

                            blogRecyclerAdapter.notifyDataSetChanged();

                        }

                    }
                }

            }
        });

    }

}
