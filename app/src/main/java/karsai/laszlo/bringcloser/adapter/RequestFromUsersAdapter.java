package karsai.laszlo.bringcloser.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
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

public class RequestFromUsersAdapter
        extends RecyclerView.Adapter<RequestFromUsersAdapter.RequestToUsersViewHolder>{

    private Context mContext;
    private List<ConnectionDetail> mConnectionDetailList;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mFromUserDatabaseRef;
    private DatabaseReference mToUserDatabaseRef;
    private DatabaseReference mConnectionsDatabaseReference;

    public RequestFromUsersAdapter(Context context, List<ConnectionDetail> connectionDetailList) {
        this.mContext = context;
        this.mConnectionDetailList = connectionDetailList;
        //this.mFirebaseDatabase = FirebaseDatabase.getInstance();
    }

    public RequestFromUsersAdapter() {}

    @NonNull
    @Override
    public RequestToUsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RequestToUsersViewHolder(
                LayoutInflater.from(mContext)
                .inflate(R.layout.list_item_request_from_users, parent, false)
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
        mFromUserDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final User user = dataSnapshot.getValue(User.class);
                ImageUtils.setUserPhoto(
                        mContext,
                        user.getPhotoUrl(),
                        holder.requestFromUserPhotoImageView
                );
                holder.requestFromUserNameTextView.setText(user.getUsername());
                mToUserDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        User otherUser = dataSnapshot.getValue(User.class);
                        holder.requestFromUserTypeTextView.setText(
                                ApplicationHelper.getPersonalizedRelationshipType(
                                        mContext,
                                        connection.getType(),
                                        otherUser.getGender(),
                                        user.getGender(),
                                        true
                                ).toUpperCase(Locale.getDefault())
                        );
                        setOnClickListeners(mContext, holder, user, otherUser);
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
                connectionDetail.getFromPhotoUrl(),
                holder.requestFromUserPhotoImageView
        );
        holder.requestFromUserNameTextView.setText(connectionDetail.getFromName());
        holder.requestFromUserTypeTextView.setText(
                ApplicationHelper.getPersonalizedRelationshipType(
                        mContext,
                        connectionDetail.getType(),
                        connectionDetail.getToGender(),
                        connectionDetail.getFromGender(),
                        true
                ).toUpperCase(Locale.getDefault())
        );
        holder.requestFromUserWithdrawImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ApplicationHelper.deletePairConnection(
                                connectionDetail.getFromUid(),
                                connectionDetail.getToUid(),
                                mContext,
                                connectionDetail.getFromName()
                        );
                    }
                };
                Context context = view.getContext();
                TextView rejectRequest = new TextView(context);
                rejectRequest.setText(
                        new StringBuilder()
                                .append("\n")
                                .append(connectionDetail.getFromName())
                                .append("\n")
                                .append(holder.requestFromUserTypeTextView.getText().toString())
                                .toString()
                );
                rejectRequest.setGravity(Gravity.CENTER);
                DialogUtils.onDialogRequest(
                        context,
                        context.getResources().getString(R.string.dialog_request_from_delete_title),
                        rejectRequest,
                        onClickListener,
                        R.style.DialogLeftRightTheme
                );
            }
        });
        holder.requestFromUserApproveImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFirebaseDatabase = FirebaseDatabase.getInstance();
                mConnectionsDatabaseReference = mFirebaseDatabase.getReference()
                        .child(ApplicationHelper.CONNECTIONS_NODE);
                mConnectionsDatabaseReference
                        .orderByChild(ApplicationHelper.CONNECTION_FROM_UID_IDENTIFIER)
                        .equalTo(connectionDetail.getFromUid())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot connectionSnapshot : dataSnapshot.getChildren()) {
                            String key = connectionSnapshot.getKey();
                            if (dataSnapshot
                                    .child(key)
                                    .child(ApplicationHelper.CONNECTION_TO_UID_IDENTIFIER)
                                    .getValue(String.class).equals(connectionDetail.getToUid())) {
                                mConnectionsDatabaseReference
                                        .child(key)
                                        .child(ApplicationHelper.CONNECTION_CONNECTED_IDENTIFIER)
                                        .setValue(ApplicationHelper.CONNECTION_BIT_POS);
                            }
                        }
                        Toast.makeText(
                                mContext,
                                new StringBuilder()
                                        .append(mContext
                                                .getResources()
                                                .getString(
                                                        R.string.congrats_on_accepted_request_start)
                                        ).append(" ")
                                        .append(connectionDetail.getFromName())
                                        .append("!")
                                        .toString(),
                                Toast.LENGTH_LONG
                        ).show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {}
                });
            }
        });
    }

    private void setOnClickListeners(
            final Context mContext,
            final RequestToUsersViewHolder holder,
            final User user,
            final User otherUser) {
        holder.requestFromUserWithdrawImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ApplicationHelper.deletePairConnection(
                                user.getUid(),
                                otherUser.getUid(),
                                mContext,
                                user.getUsername()
                        );
                    }
                };
                Context context = view.getContext();
                TextView rejectRequest = new TextView(context);
                rejectRequest.setText(
                        new StringBuilder()
                                .append("\n")
                                .append(user.getUsername())
                                .append("\n")
                                .append(holder.requestFromUserTypeTextView.getText().toString())
                                .toString()
                );
                rejectRequest.setGravity(Gravity.CENTER);
                DialogUtils.onDialogRequest(
                        context,
                        context.getResources().getString(R.string.dialog_request_from_delete_title),
                        rejectRequest,
                        onClickListener,
                        R.style.DialogLeftRightTheme
                );
            }
        });
        holder.requestFromUserApproveImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFirebaseDatabase = FirebaseDatabase.getInstance();
                mConnectionsDatabaseReference = mFirebaseDatabase.getReference()
                        .child(ApplicationHelper.CONNECTIONS_NODE);
                mConnectionsDatabaseReference
                        .orderByChild(ApplicationHelper.CONNECTION_FROM_UID_IDENTIFIER)
                        .equalTo(user.getUid())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot connectionSnapshot : dataSnapshot.getChildren()) {
                                    String key = connectionSnapshot.getKey();
                                    if (dataSnapshot
                                            .child(key)
                                            .child(ApplicationHelper.CONNECTION_TO_UID_IDENTIFIER)
                                            .getValue(String.class).equals(otherUser.getUid())) {
                                        mConnectionsDatabaseReference
                                                .child(key)
                                                .child(ApplicationHelper.CONNECTION_CONNECTED_IDENTIFIER)
                                                .setValue(ApplicationHelper.CONNECTION_BIT_POS);
                                    }
                                }
                                //MainActivity.sViewPager.setCurrentItem(0);
                                Toast.makeText(
                                        mContext,
                                        new StringBuilder()
                                                .append(mContext
                                                        .getResources()
                                                        .getString(
                                                                R.string.congrats_on_accepted_request_start)
                                                ).append(" ")
                                                .append(user.getUsername())
                                                .append("!")
                                                .toString(),
                                        Toast.LENGTH_LONG
                                ).show();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {}
                        });
            }
        });
    }

    @Override
    public int getItemCount() {
        if (mConnectionDetailList == null) return 0;
        return mConnectionDetailList.size();
    }

    class RequestToUsersViewHolder extends RecyclerView.ViewHolder{

        ImageView requestFromUserPhotoImageView;
        TextView requestFromUserNameTextView;
        TextView requestFromUserTypeTextView;
        ImageView requestFromUserWithdrawImageView;
        ImageView requestFromUserApproveImageView;

        public RequestToUsersViewHolder(View itemView) {
            super(itemView);
            requestFromUserPhotoImageView = itemView.findViewById(R.id.iv_requested_from_user_photo);
            requestFromUserNameTextView = itemView.findViewById(R.id.tv_requested_from_user_name);
            requestFromUserTypeTextView = itemView.findViewById(R.id.tv_requested_from_user_type);
            requestFromUserWithdrawImageView
                    = itemView.findViewById(R.id.iv_requested_from_user_delete);
            requestFromUserApproveImageView
                    = itemView.findViewById(R.id.iv_requested_from_user_approve);
        }
    }
}
