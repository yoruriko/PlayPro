package com.ricogao.playpro.Adapter;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ricogao.playpro.R;
import com.ricogao.playpro.model.Field;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ricogao on 2017/4/9.
 */

public class FieldItemAdapter extends RecyclerView.Adapter<FieldItemAdapter.FieldItemHolder> {

    private final static String TAG = FieldItemAdapter.class.getSimpleName();
    private FieldItemListener listener;
    private List<Field> fields;

    public FieldItemAdapter(FieldItemListener listener, List<Field> fields) {
        this.listener = listener;
        this.fields = fields;
    }

    public void setListener(FieldItemListener listener) {
        this.listener = listener;
    }

    public void setFields(List<Field> fields) {
        this.fields = fields;
    }

    @Override
    public FieldItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.field_item_layout, parent, false);
        return new FieldItemHolder(view);
    }

    @Override
    public void onBindViewHolder(FieldItemHolder holder, int position) {
        final Field field = fields.get(position);
        holder.tvField.setText(field.getName());
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null)
                    listener.onFieldItemClick(field.getId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return fields.size();
    }

    public interface FieldItemListener {
        void onFieldItemClick(long fieldId);
    }

    class FieldItemHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_field)
        TextView tvField;

        @BindView(R.id.cardView)
        CardView cardView;

        FieldItemHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
