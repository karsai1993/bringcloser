package karsai.laszlo.bringcloser.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import karsai.laszlo.bringcloser.utils.ApplicationUtils;
import karsai.laszlo.bringcloser.R;
import karsai.laszlo.bringcloser.activity.AddChosenConnectionActivity;
import karsai.laszlo.bringcloser.model.User;
import karsai.laszlo.bringcloser.utils.ImageUtils;

/**
 * Created by Laci on 07/06/2018.
 * Adapter to handle all user related information
 */
public class AllUsersAdapter extends RecyclerView.Adapter<AllUsersAdapter.AllUsersViewHolder>{

    private Context mContext;
    private Context mAppContext;
    private List<User> mUserList;

    public AllUsersAdapter(Context context, Context appContext, List<User> userList) {
        this.mContext = context;
        this.mAppContext = appContext;
        this.mUserList = userList;
    }

    @NonNull
    @Override
    public AllUsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AllUsersViewHolder(
                LayoutInflater
                        .from(mContext)
                        .inflate(R.layout.list_item_all_users, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull final AllUsersViewHolder holder, int position) {
        final User currentUser = mUserList.get(position);
        holder.nameTextView.setText(currentUser.getUsername());
        ImageUtils.setPhoto(
                mAppContext,
                currentUser.getPhotoUrl(),
                holder.photoImageView,
                true
        );
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addChosenConnectionIntent = new Intent(
                        mContext,
                        AddChosenConnectionActivity.class
                );
                addChosenConnectionIntent
                        .putExtra(ApplicationUtils.INTENT_CHOSEN_USER_KEY, currentUser);
                ActivityOptionsCompat options = ActivityOptionsCompat.
                        makeSceneTransitionAnimation(
                                (Activity) mContext,
                                holder.photoImageView,
                                "image_transition");
                mContext.startActivity(addChosenConnectionIntent, options.toBundle());
            }
        });
    }

    @Override
    public int getItemCount() {
        if (mUserList == null) return 0;
        return mUserList.size();
    }

    class AllUsersViewHolder extends RecyclerView.ViewHolder {

        ImageView photoImageView;
        TextView nameTextView;

        public AllUsersViewHolder(View itemView) {
            super(itemView);
            photoImageView = itemView.findViewById(R.id.iv_photo);
            nameTextView = itemView.findViewById(R.id.tv_name);
        }
    }
}
