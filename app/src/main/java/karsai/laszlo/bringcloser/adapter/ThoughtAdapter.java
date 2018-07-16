package karsai.laszlo.bringcloser.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import karsai.laszlo.bringcloser.model.Event;
import karsai.laszlo.bringcloser.model.Thought;

public class ThoughtAdapter extends RecyclerView.Adapter<ThoughtAdapter.ThoughtViewHolder> {

    private Context mContext;
    private List<Thought> mThoughtList;

    public ThoughtAdapter(Context context, List<Thought> thoughtList) {
        this.mContext = context;
        this.mThoughtList = thoughtList;
    }

    @NonNull
    @Override
    public ThoughtViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull ThoughtViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    class ThoughtViewHolder extends RecyclerView.ViewHolder {

        public ThoughtViewHolder(View itemView) {
            super(itemView);
        }
    }
}
