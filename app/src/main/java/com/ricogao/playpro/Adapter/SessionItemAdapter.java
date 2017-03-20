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

import java.text.SimpleDateFormat;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ricogao on 2017/3/18.
 */

public class SessionItemAdapter extends RecyclerView.Adapter<SessionItemAdapter.SessionItemHolder> {

    private final static String TAG = SessionItemAdapter.class.getSimpleName();

    private List<Event> events;
    private SessionItemListener listener;
    private SimpleDateFormat durationFormat, timestampFormat;

    public interface SessionItemListener {
        void onSessionItemClick(long eventId);
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


    public SessionItemAdapter(SessionItemListener listener, List<Event> events) {
        this.events = events;
        this.listener = listener;
        durationFormat = new SimpleDateFormat("HH ':' mm ':' ss");
        timestampFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    }

    public void setListener(SessionItemListener listener) {
        this.listener = listener;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }

    public List<Event> getEvents() {
        return events;
    }

    @Override
    public SessionItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.session_item_layout, parent, false);
        SessionItemHolder holder = new SessionItemHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(SessionItemHolder holder, int position) {
        final Event event = events.get(position);

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onSessionItemClick(event.getId());
            }
        });
        holder.tv_distance.setText(event.getDistance() + "");
        holder.tv_time.setText(timestampFormat.format(event.getTimestamp()));
        holder.tv_duration.setText(durationFormat.format(event.getDuration()));

    }

    @Override
    public int getItemCount() {
        return events.size();
    }


}
