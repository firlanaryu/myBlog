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
import com.creaginetech.myblog.R;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class BlogRecyclerAdapter extends RecyclerView.Adapter<BlogRecyclerAdapter.ViewHolder> {

    public List<BlogPost> blog_list;
    public Context context;

    public BlogRecyclerAdapter(List<BlogPost> blog_list){

        this.blog_list = blog_list;

    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        //
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.blog_list_item, parent,false);
        context = parent.getContext();

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        //load desc
        String desc_data = blog_list.get(position).getDesc();
        holder.setDescText(desc_data);

        //load image_url
        String image_url = blog_list.get(position).getImage_url();
        holder.setBlogImage(image_url);

        String user_id = blog_list.get(position).getUser_id();
        //user data will be retrieved here...

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

        private TextView descView,blogDate;
        private ImageView blogImageView;

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
            Glide.with(context).load(downloadUri).into(blogImageView);

        }

        public void setBlogTime (String date){

            blogDate = mView.findViewById(R.id.blog_date);
            blogDate.setText(date);

        }

    }

}
