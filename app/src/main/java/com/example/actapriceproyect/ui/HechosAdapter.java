package com.example.actapriceproyect.ui;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.actapriceproyect.R;
import com.example.actapriceproyect.model.HechoVerificado;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class HechosAdapter extends RecyclerView.Adapter<HechosAdapter.ViewHolder> {

    private final List<HechoVerificado> hechos;

    public HechosAdapter(List<HechoVerificado> hechos) {
        this.hechos = hechos;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hecho_verificado, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HechoVerificado h = hechos.get(position);
        
        holder.tvNombre.setText(h.incumplimientoNombre);
        holder.tvMetaInfo.setText("Inspector: " + h.usuario + " | " + h.fecha);
        
        // Evitar disparos de listener durante el reciclaje
        if (holder.textWatcher != null) {
            holder.etHecho.removeTextChangedListener(holder.textWatcher);
        }
        
        holder.etHecho.setText(h.hechoRedactado);
        holder.etHecho.setHint("Describa: qué se observó, dónde (surtidor/panel) y qué dato se comparó.");

        holder.textWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                h.hechoRedactado = s.toString();
            }
        };
        holder.etHecho.addTextChangedListener(holder.textWatcher);
    }

    @Override
    public int getItemCount() {
        return hechos.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvMetaInfo;
        TextInputEditText etHecho;
        TextWatcher textWatcher;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvIncumplimientoNombre);
            tvMetaInfo = itemView.findViewById(R.id.tvMetaInfo);
            etHecho = itemView.findViewById(R.id.etHechoRedactado);
        }
    }
}
