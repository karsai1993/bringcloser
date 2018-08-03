package karsai.laszlo.bringcloser.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.futuremind.recyclerviewfastscroll.SectionTitleProvider;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;
import java.util.Locale;

import karsai.laszlo.bringcloser.ApplicationHelper;
import karsai.laszlo.bringcloser.R;
import karsai.laszlo.bringcloser.activity.ConnectionActivity;
import karsai.laszlo.bringcloser.model.ConnectionDetail;
import karsai.laszlo.bringcloser.utils.ImageUtils;

/**
 * Created by Laci on 15/06/2018.
 * Adapter to handle connected users related information
 */
public class ConnectedPeopleAdapter
        extends RecyclerView.Adapter<ConnectedPeopleAdapter.ConnectedPeopleViewHolder>
        implements SectionTitleProvider{

    private Context mContext;
    private List<ConnectionDetail> mConnectionDetailList;
    private List<ConnectionDetail> mFilteredConnectionDetailList;
    private String mCurrentUserUid;

    public ConnectedPeopleAdapter() {}

    public ConnectedPeopleAdapter(
            Context context,
            List<ConnectionDetail> connectionDetailList) {
        this.mContext = context;
        this.mConnectionDetailList = connectionDetailList;
        this.mCurrentUserUid = FirebaseAuth.getInstance().getUid();
    }

    @NonNull
    @Override
    public ConnectedPeopleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ConnectedPeopleViewHolder(
                LayoutInflater.from(mContext)
                .inflate(R.layout.list_item_connected_users, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull final ConnectedPeopleViewHolder holder, int position) {
        final ConnectionDetail connectionDetail = mConnectionDetailList.get(position);
        String fromUid = connectionDetail.getFromUid();
        if (fromUid.equals(mCurrentUserUid)) {
            ImageUtils.setPhoto(
                    mContext,
                    connectionDetail.getToPhotoUrl(),
                    holder.connectedUserPhotoImageView,
                    true
            );
            holder.connectedUserNameTextView.setText(connectionDetail.getToName());
            holder.connectedUserType.setText(
                    ApplicationHelper.getPersonalizedRelationshipType(
                            mContext,
                            connectionDetail.getType(),
                            connectionDetail.getToGender(),
                            connectionDetail.getFromGender(),
                            false
                    ).toUpperCase(Locale.getDefault())
            );
        } else {
            ImageUtils.setPhoto(
                    mContext,
                    connectionDetail.getFromPhotoUrl(),
                    holder.connectedUserPhotoImageView,
                    true
            );
            holder.connectedUserNameTextView.setText(connectionDetail.getFromName());
            holder.connectedUserType.setText(
                    ApplicationHelper.getPersonalizedRelationshipType(
                            mContext,
                            connectionDetail.getType(),
                            connectionDetail.getToGender(),
                            connectionDetail.getFromGender(),
                            true
                    ).toUpperCase(Locale.getDefault())
            );
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent connectionIntent = new Intent(mContext, ConnectionActivity.class);
                connectionIntent.putExtra(ApplicationHelper.CONNECTION_KEY, connectionDetail);
                mContext.startActivity(connectionIntent);
            }
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
        String fromUid = connectionDetail.getFromUid();
        if (fromUid.equals(mCurrentUserUid)) {
            return connectionDetail.getToName().substring(0, 1);
        } else {
            return connectionDetail.getFromName().substring(0, 1);
        }
    }

    class ConnectedPeopleViewHolder extends RecyclerView.ViewHolder {

        ImageView connectedUserPhotoImageView;
        TextView connectedUserNameTextView;
        TextView connectedUserType;

        ConnectedPeopleViewHolder(View itemView) {
            super(itemView);
            connectedUserPhotoImageView = itemView.findViewById(R.id.iv_connected_user_photo);
            connectedUserNameTextView = itemView.findViewById(R.id.tv_connected_user_name);
            connectedUserType = itemView.findViewById(R.id.tv_connected_user_type);
        }
    }
}
