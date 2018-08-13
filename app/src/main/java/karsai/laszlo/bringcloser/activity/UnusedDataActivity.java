package karsai.laszlo.bringcloser.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import karsai.laszlo.bringcloser.R;
import karsai.laszlo.bringcloser.adapter.UnusedDataAdapter;
import karsai.laszlo.bringcloser.model.UnusedPhotoDetail;
import karsai.laszlo.bringcloser.utils.ApplicationUtils;
import timber.log.Timber;

public class UnusedDataActivity extends CommonActivity {

    @BindView(R.id.tv_no_internet)
    TextView mNoInternetAlertTextView;
    @BindView(R.id.tv_unused_header)
    TextView mUnusedHeaderTextView;
    @BindView(R.id.tv_unused_empty)
    TextView mUnusedDataEmptyTextView;
    @BindView(R.id.btn_unused_delete)
    Button mUnusedDeleteBtn;
    @BindView(R.id.rv_unused)
    RecyclerView mUnusedDataRecyclerView;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUnusedDatabaseRef;
    private ValueEventListener mUnusedValueEventListener;
    private String mCurrentUserUid;
    private List<UnusedPhotoDetail> mUnusedPhotoDetailList;
    private UnusedDataAdapter mUnusedDataAdapter;
    private ProgressDialog mDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.activity_unused);
        ButterKnife.bind(this);
        super.onCreate(savedInstanceState);

        mDialog = new ProgressDialog(this);

        mUnusedPhotoDetailList = new ArrayList<>();
        mUnusedDataAdapter = new UnusedDataAdapter(this, mUnusedPhotoDetailList);
        mUnusedDataRecyclerView.setLayoutManager(
                new StaggeredGridLayoutManager(
                    getResources().getInteger(R.integer.unused_span_count),
                    StaggeredGridLayoutManager.VERTICAL
                )
        );
        mUnusedDataRecyclerView.setHasFixedSize(true);
        mUnusedDataRecyclerView.setAdapter(mUnusedDataAdapter);

        mCurrentUserUid = FirebaseAuth.getInstance().getUid();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mUnusedDatabaseRef = mFirebaseDatabase.getReference()
                .child(ApplicationUtils.UNUSED_NODE);
        mUnusedValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    mUnusedPhotoDetailList.clear();
                    for (DataSnapshot unusedSnapshot : dataSnapshot.getChildren()) {
                        String key = unusedSnapshot.getKey();
                        if (key == null) {
                            Timber.wtf("unused key null: " + mCurrentUserUid);
                            continue;
                        }
                        if (key.matches(".*_" + mCurrentUserUid)) {
                            for (DataSnapshot unusedDetailSnapshot : unusedSnapshot.getChildren()) {
                                UnusedPhotoDetail unusedPhotoDetail
                                        = unusedDetailSnapshot.getValue(UnusedPhotoDetail.class);
                                mUnusedPhotoDetailList.add(unusedPhotoDetail);
                            }
                        }
                    }
                    if (mUnusedPhotoDetailList.isEmpty()) {
                        addNoDataHandling();
                    } else {
                        mUnusedDataEmptyTextView.setVisibility(View.GONE);
                        mUnusedDeleteBtn.setVisibility(View.VISIBLE);
                        mUnusedDataRecyclerView.setVisibility(View.VISIBLE);
                        mUnusedHeaderTextView.setVisibility(View.VISIBLE);
                        mUnusedHeaderTextView.setText(
                                new StringBuilder()
                                        .append(getResources().getString(R.string.activity_unused_header))
                                        .append(" (")
                                        .append(mUnusedPhotoDetailList.size())
                                        .append(")")
                                        .toString()
                        );
                        mUnusedDataAdapter.notifyDataSetChanged();
                    }
                } else {
                    addNoDataHandling();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        mUnusedDeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDialog.setMessage(getResources().getString(R.string.activity_unused_progress));
                mDialog.show();
                mUnusedDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot unusedSnapshot : dataSnapshot.getChildren()) {
                            String key = unusedSnapshot.getKey();
                            if (key == null) {
                                Timber.wtf("unused key null: " + mCurrentUserUid);
                                continue;
                            }
                            if (key.matches(".*_" + mCurrentUserUid)) {
                                for (DataSnapshot unusedDetailSnapshot : unusedSnapshot.getChildren()) {
                                    String subKey = unusedDetailSnapshot.getKey();
                                    if (subKey == null) {
                                        Timber.wtf("unused subkey null: " + mCurrentUserUid);
                                        continue;
                                    }
                                    mUnusedDatabaseRef.child(key).child(subKey).setValue(null);
                                }
                            }
                        }
                        for (UnusedPhotoDetail unusedPhotoDetail : mUnusedPhotoDetailList) {
                            ApplicationUtils.deleteImageFromStorage(
                                    UnusedDataActivity.this,
                                    unusedPhotoDetail.getPhotoUrl(),
                                    null,
                                    null
                            );
                        }
                        if (mDialog.isShowing()) {
                            mDialog.dismiss();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
    }

    private void addNoDataHandling() {
        mUnusedHeaderTextView.setVisibility(View.GONE);
        mUnusedDeleteBtn.setVisibility(View.GONE);
        mUnusedDataRecyclerView.setVisibility(View.GONE);
        mUnusedDataEmptyTextView.setVisibility(View.VISIBLE);
    }

    @Override
    protected TextView getNoInternetAlertTextView() {
        return mNoInternetAlertTextView;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mUnusedDatabaseRef.addValueEventListener(mUnusedValueEventListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mUnusedValueEventListener != null) {
            mUnusedDatabaseRef.removeEventListener(mUnusedValueEventListener);
        }
    }
}
