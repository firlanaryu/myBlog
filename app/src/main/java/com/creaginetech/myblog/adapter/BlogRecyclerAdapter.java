package com.creaginetech.myblog.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.creaginetech.myblog.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class BlogRecyclerAdapter extends RecyclerView.Adapter<BlogRecyclerAdapter.ViewHolder> {

    public List<BlogPost> blog_list;
    public Context context;

    private FirebaseFirestore firebaseFirestore;

    public BlogRecyclerAdapter(List<BlogPost> blog_list){

        this.blog_list = blog_list;

    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        //
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.blog_list_item, parent,false);
        context = parent.getContext();

        firebaseFirestore = FirebaseFirestore.getInstance();

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        //load desc
        String desc_data = blog_list.get(position).getDesc();
        holder.setDescText(desc_data);

        //load image_url
        String image_url = blog_list.get(position).getImage_url();
        holder.setBlogImage(image_url);

        //load user_id NAME from firestore
        String user_id = blog_list.get(position).getUser_id();
        //user data will be retrieved here...
        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()){

                    String userName = task.getResult().getString("name");
                    String userImage = task.getResult().getString("image");

                    holder.setUserData(userName,userImage);

                } else {


                    //firebase exception


                }


            }
        });

        //load date time to string
        long millisecond = blog_list.get(position).getTimestamp().getTime();
        String dateString = android.text.format.DateFormat.format("MM/dd/yyyy",new Date(millisecond)).toString();
        holder.setBlogTime(dateString);
    }

    @Override
    public int getItemCount() {
        //count item in blog_list how many item to show at recyclerview
        //blog_list.size() = all item
        return blog_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private View mView;

        private TextView descView,blogDate,blogUserName;
        private ImageView blogImageView;
        private CircleImageView blogUserImage;

        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

        }

        public void setDescText(String descText){

            //load data desc
            descView = mView.findViewById(R.id.blog_desc);
            descView.setText(descText);

        }

        public void setBlogImage(String downloadUri){

            //load data image_url
            blogImageView = mView.findViewById(R.id.blog_image);

            //set default postImage when loading to load postImage from fireStore
            RequestOptions placeholderOption = new RequestOptions();
            placeholderOption.placeholder(R.drawable.imagepost);

            Glide.with(context).applyDefaultRequestOptions(placeholderOption).load(downloadUri).into(blogImageView);

        }

        public void setBlogTime (String date){

            blogDate = mView.findViewById(R.id.blog_date);
            blogDate.setText(date);

        }

        public void setUserData (String username, String userImage){

            blogUserName = mView.findViewById(R.id.blog_username);
            blogUserImage = mView.findViewById(R.id.blog_user_image);

            blogUserName.setText(username);

            //set default userImage when loading to load userImage from fireStore
            RequestOptions placeholderOption = new RequestOptions();
            placeholderOption.placeholder(R.mipmap.avatar_image);

            Glide.with(context).applyDefaultRequestOptions(placeholderOption).load(userImage).into(blogUserImage);

        }

    }

}
