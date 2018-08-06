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

import com.futuremind.recyclerviewfastscroll.SectionTitleProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Locale;

import karsai.laszlo.bringcloser.utils.ApplicationUtils;
import karsai.laszlo.bringcloser.R;
import karsai.laszlo.bringcloser.model.ConnectionDetail;
import karsai.laszlo.bringcloser.utils.DialogUtils;
import karsai.laszlo.bringcloser.utils.ImageUtils;
import timber.log.Timber;

/**
 * Created by Laci on 11/06/2018.
 * Adapter to handle request from users related information
 */
public class RequestFromUsersAdapter
        extends RecyclerView.Adapter<RequestFromUsersAdapter.RequestToUsersViewHolder>
        implements SectionTitleProvider {

    private Context mContext;
    private List<ConnectionDetail> mConnectionDetailList;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mConnectionsDatabaseReference;

    public RequestFromUsersAdapter(Context context, List<ConnectionDetail> connectionDetailList) {
        this.mContext = context;
        this.mConnectionDetailList = connectionDetailList;
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
    public void onBindViewHolder(@NonNull final RequestToUsersViewHolder holder, final int position) {
        final ConnectionDetail connectionDetail = mConnectionDetailList.get(position);
        ImageUtils.setPhoto(
                mContext,
                connectionDetail.getFromPhotoUrl(),
                holder.requestFromUserPhotoImageView,
                true
        );
        holder.requestFromUserNameTextView.setText(connectionDetail.getFromName());
        holder.requestFromUserTypeTextView.setText(
                ApplicationUtils.getPersonalizedRelationshipType(
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
                        ApplicationUtils.deletePairConnection(
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
                DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        applyDeletion(connectionDetail);
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
                        context.getResources().getString(R.string.dialog_request_approve_title),
                        rejectRequest,
                        onClickListener,
                        R.style.DialogUpDownTheme
                );
            }
        });
    }

    private void applyDeletion(final ConnectionDetail connectionDetail) {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mConnectionsDatabaseReference = mFirebaseDatabase.getReference()
                .child(ApplicationUtils.CONNECTIONS_NODE);
        mConnectionsDatabaseReference
                .orderByChild(ApplicationUtils.CONNECTION_FROM_UID_IDENTIFIER)
                .equalTo(connectionDetail.getFromUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot connectionSnapshot : dataSnapshot.getChildren()) {
                            String key = connectionSnapshot.getKey();
                            if (key == null) {
                                Timber.wtf("key null connection from request from adapter");
                                continue;
                            }
                            String toUidValue = dataSnapshot
                                    .child(key)
                                    .child(ApplicationUtils.CONNECTION_TO_UID_IDENTIFIER)
                                    .getValue(String.class);
                            if (toUidValue == null) {
                                Timber.wtf("to uid null request from adapter");
                                continue;
                            }
                            if (toUidValue.equals(connectionDetail.getToUid())) {
                                mConnectionsDatabaseReference
                                        .child(key)
                                        .child(ApplicationUtils.CONNECTION_CONNECTED_IDENTIFIER)
                                        .setValue(ApplicationUtils.CONNECTION_BIT_POS);
                                mConnectionsDatabaseReference
                                        .child(key)
                                        .child(ApplicationUtils.CONNECTION_TIMESTAMP_IDENTIFIER)
                                        .setValue(ApplicationUtils
                                                .getCurrentUTCDateAndTime()
                                        );
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

    @Override
    public int getItemCount() {
        if (mConnectionDetailList == null) return 0;
        return mConnectionDetailList.size();
    }

    @Override
    public String getSectionTitle(int position) {
        ConnectionDetail connectionDetail = mConnectionDetailList.get(position);
        return connectionDetail.getFromName().substring(0, 1);
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
