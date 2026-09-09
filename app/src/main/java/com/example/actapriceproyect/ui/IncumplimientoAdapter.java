package com.example.actapriceproyect.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.actapriceproyect.R;

import java.util.List;

public class IncumplimientoAdapter extends RecyclerView.Adapter<IncumplimientoAdapter.ViewHolder> {

    private final List<String> incumplimientos;
    private final OnIncumplimientoListener listener;

    public interface OnIncumplimientoListener {
        void onCorregir(String incumplimiento);
    }

    public IncumplimientoAdapter(List<String> incumplimientos, OnIncumplimientoListener listener) {
        this.incumplimientos = incumplimientos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_incumplimiento, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String item = incumplimientos.get(position);
        holder.tvDesc.setText(item);
        holder.btnCorregir.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCorregir(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return incumplimientos.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDesc;
        Button btnCorregir;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDesc = itemView.findViewById(R.id.tvIncumplimientoDesc);
            btnCorregir = itemView.findViewById(R.id.btnCorregir);
        }
    }
}
