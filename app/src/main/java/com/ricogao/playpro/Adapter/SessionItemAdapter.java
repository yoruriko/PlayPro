package com.ricogao.playpro.Adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ricogao.playpro.R;
import com.ricogao.playpro.model.Event;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ricogao on 2017/3/18.
 */

public class SessionItemAdapter extends RecyclerView.Adapter<SessionItemAdapter.SessionItemHolder> {

    private final static String TAG = SessionItemAdapter.class.getSimpleName();

    private List<Event> list;
    private SessionItemListener listener;

    public interface SessionItemListener {
        void onSessionItemClick(int position);
    }

    public class SessionItemHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.cardView)
        CardView cardView;
        @BindView(R.id.tv_time)
        TextView tv_time;
        @BindView(R.id.tv_distance)
        TextView tv_distance;
        @BindView(R.id.tv_duration)
        TextView tv_duration;

        public SessionItemHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }


    public SessionItemAdapter(SessionItemListener listener, List<Event> list) {
        this.list = list;
        this.listener = listener;
    }

    @Override
    public SessionItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.session_item_layout, parent, false);
        SessionItemHolder holder = new SessionItemHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(SessionItemHolder holder, int position) {
        //// TODO: 2017/3/18 bining layout

        final int id = position;

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onSessionItemClick(id);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


}
