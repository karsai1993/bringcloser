package karsai.laszlo.bringcloser.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Locale;

import karsai.laszlo.bringcloser.ApplicationHelper;
import karsai.laszlo.bringcloser.R;
import karsai.laszlo.bringcloser.activity.MainActivity;
import karsai.laszlo.bringcloser.model.Connection;
import karsai.laszlo.bringcloser.model.ConnectionDetail;
import karsai.laszlo.bringcloser.model.User;
import karsai.laszlo.bringcloser.utils.DialogUtils;
import karsai.laszlo.bringcloser.utils.ImageUtils;

/**
 * Created by Laci on 11/06/2018.
 */

public class RequestToUsersAdapter
        extends RecyclerView.Adapter<RequestToUsersAdapter.RequestToUsersViewHolder>{

    private Context mContext;
    private List<ConnectionDetail> mConnectionDetailList;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mFromUserDatabaseRef;
    private DatabaseReference mToUserDatabaseRef;

    public RequestToUsersAdapter(Context context, List<ConnectionDetail> connectionDetailList) {
        this.mContext = context;
        this.mConnectionDetailList = connectionDetailList;
        //this.mFirebaseDatabase = FirebaseDatabase.getInstance();
    }

    public RequestToUsersAdapter() {}

    @NonNull
    @Override
    public RequestToUsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RequestToUsersViewHolder(
                LayoutInflater.from(mContext)
                .inflate(R.layout.list_item_request_to_users, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull final RequestToUsersViewHolder holder, int position) {
        final ConnectionDetail connectionDetail = mConnectionDetailList.get(position);
        String fromUid = connectionDetail.getFromUid();
        String toUid = connectionDetail.getToUid();
        /*mFromUserDatabaseRef = mFirebaseDatabase.getReference()
                .child(ApplicationHelper.USERS_NODE)
                .child(fromUid);
        mToUserDatabaseRef = mFirebaseDatabase.getReference()
                .child(ApplicationHelper.USERS_NODE)
                .child(toUid);
        mToUserDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final User user = dataSnapshot.getValue(User.class);
                ImageUtils.setUserPhoto(
                        mContext,
                        user.getPhotoUrl(),
                        holder.requestToUserPhotoImageView
                );
                holder.requestToUserNameTextView.setText(user.getUsername());
                mFromUserDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        User otherUser = dataSnapshot.getValue(User.class);
                        holder.requestToUserTypeTextView.setText(
                                ApplicationHelper.getPersonalizedRelationshipType(
                                        mContext,
                                        connection.getType(),
                                        user.getGender(),
                                        otherUser.getGender(),
                                        false
                                ).toUpperCase(Locale.getDefault())
                        );
                        setOnClickListener(holder, user, otherUser);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });*/
        ImageUtils.setUserPhoto(
                mContext,
                connectionDetail.getToPhotoUrl(),
                holder.requestToUserPhotoImageView
        );
        holder.requestToUserNameTextView.setText(connectionDetail.getToName());
        holder.requestToUserTypeTextView.setText(
                ApplicationHelper.getPersonalizedRelationshipType(
                        mContext,
                        connectionDetail.getType(),
                        connectionDetail.getToGender(),
                        connectionDetail.getFromGender(),
                        false
                ).toUpperCase(Locale.getDefault())
        );
        holder.requestToUserWithdrawImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ApplicationHelper.deletePairConnection(
                                connectionDetail.getFromUid(),
                                connectionDetail.getToUid(),
                                mContext,
                                connectionDetail.getToName()
                        );
                    }
                };
                Context context = view.getContext();
                TextView withdrawRequest = new TextView(context);
                withdrawRequest.setText(
                        new StringBuilder()
                                .append("\n")
                                .append(connectionDetail.getToName())
                                .append("\n")
                                .append(holder.requestToUserTypeTextView.getText().toString())
                                .toString()
                );
                withdrawRequest.setGravity(Gravity.CENTER);
                DialogUtils.onDialogRequest(
                        context,
                        context.getResources().getString(R.string.dialog_request_to_delete_title),
                        withdrawRequest,
                        onClickListener,
                        R.style.DialogLeftRightTheme
                );
            }
        });
    }

    private void setOnClickListener(final RequestToUsersViewHolder holder, final User user, final User otherUser) {
        holder.requestToUserWithdrawImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ApplicationHelper.deletePairConnection(
                                otherUser.getUid(),
                                user.getUid(),
                                mContext,
                                user.getUsername()
                        );
                    }
                };
                Context context = view.getContext();
                TextView withdrawRequest = new TextView(context);
                withdrawRequest.setText(
                        new StringBuilder()
                                .append("\n")
                                .append(user.getUsername())
                                .append("\n")
                                .append(holder.requestToUserTypeTextView.getText().toString())
                                .toString()
                );
                withdrawRequest.setGravity(Gravity.CENTER);
                DialogUtils.onDialogRequest(
                        context,
                        context.getResources().getString(R.string.dialog_request_to_delete_title),
                        withdrawRequest,
                        onClickListener,
                        R.style.DialogLeftRightTheme
                );
            }
        });
    }

    @Override
    public int getItemCount() {
        if (mConnectionDetailList == null) return 0;
        return mConnectionDetailList.size();
    }

    class RequestToUsersViewHolder extends RecyclerView.ViewHolder{

        ImageView requestToUserPhotoImageView;
        TextView requestToUserNameTextView;
        TextView requestToUserTypeTextView;
        ImageView requestToUserWithdrawImageView;

        public RequestToUsersViewHolder(View itemView) {
            super(itemView);
            requestToUserPhotoImageView = itemView.findViewById(R.id.iv_requested_to_user_photo);
            requestToUserNameTextView = itemView.findViewById(R.id.tv_requested_to_user_name);
            requestToUserTypeTextView = itemView.findViewById(R.id.tv_requested_to_user_type);
            requestToUserWithdrawImageView = itemView.findViewById(R.id.iv_requested_to_user_delete);
        }
    }
}
