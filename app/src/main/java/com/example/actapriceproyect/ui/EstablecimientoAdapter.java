package com.example.actapriceproyect.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.actapriceproyect.R;
import com.example.actapriceproyect.model.Establecimiento;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EstablecimientoAdapter extends RecyclerView.Adapter<EstablecimientoAdapter.ViewHolder> {

    private List<Establecimiento> establecimientos = new ArrayList<>();
    private List<Establecimiento> establecimientosFull = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Establecimiento establecimiento);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setEstablecimientos(List<Establecimiento> lista) {
        this.establecimientos = new ArrayList<>(lista);
        this.establecimientosFull = new ArrayList<>(lista);
        notifyDataSetChanged();
    }

    public void filter(String text) {
        establecimientos.clear();
        if (text.isEmpty()) {
            establecimientos.addAll(establecimientosFull);
        } else {
            String query = text.toLowerCase().trim();
            for (Establecimiento item : establecimientosFull) {
                if (item.nombre.toLowerCase().contains(query) || item.ruc.contains(query)) {
                    establecimientos.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_establecimiento, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Establecimiento est = establecimientos.get(position);
        holder.tvNombre.setText(est.nombre);
        holder.tvRuc.setText("RUC: " + est.ruc);
        holder.tvDireccion.setText(est.direccion);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(est);
        });
    }

    @Override
    public int getItemCount() {
        return establecimientos.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvRuc, tvDireccion;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvRuc = itemView.findViewById(R.id.tvRuc);
            tvDireccion = itemView.findViewById(R.id.tvDireccion);
        }
    }
}
