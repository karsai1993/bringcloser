package karsai.laszlo.bringcloser.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.List;

import karsai.laszlo.bringcloser.R;
import karsai.laszlo.bringcloser.model.UnusedPhotoDetail;
import karsai.laszlo.bringcloser.utils.ImageUtils;

public class UnusedDataAdapter extends RecyclerView.Adapter<UnusedDataAdapter.UnusedDataViewHolder> {

    private Context mContext;
    private List<UnusedPhotoDetail> mUnusedPhotoDetailList;

    public UnusedDataAdapter(Context context, List<UnusedPhotoDetail> unusedPhotoDetailList) {
        this.mContext = context;
        this.mUnusedPhotoDetailList = unusedPhotoDetailList;
    }

    @NonNull
    @Override
    public UnusedDataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new UnusedDataViewHolder(
                LayoutInflater.from(mContext).inflate(
                        R.layout.list_item_unused,
                        parent,
                        false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull UnusedDataViewHolder holder, int position) {
        ImageView imageView = (ImageView) holder.itemView;
        ImageUtils.setPhoto(
                mContext,
                mUnusedPhotoDetailList.get(position).getPhotoUrl(),
                imageView,
                false);
    }

    @Override
    public int getItemCount() {
        return mUnusedPhotoDetailList.size();
    }

    class UnusedDataViewHolder extends RecyclerView.ViewHolder {

        UnusedDataViewHolder(View itemView) {
            super(itemView);
        }
    }
}
