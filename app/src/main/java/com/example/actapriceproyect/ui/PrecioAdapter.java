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
import com.example.actapriceproyect.model.ProductoPrecio;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class PrecioAdapter extends RecyclerView.Adapter<PrecioAdapter.ViewHolder> {

    private final List<ProductoPrecio> productos;

    public PrecioAdapter(List<ProductoPrecio> productos) {
        this.productos = productos;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_producto_precio, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ProductoPrecio p = productos.get(position);
        holder.tvNombre.setText(p.nombre);
        
        // Limpiar listeners anteriores para evitar duplicidad de datos al reciclar vistas
        if (holder.twPrice != null) holder.etPrice.removeTextChangedListener(holder.twPrice);
        if (holder.twPub != null) holder.etPub.removeTextChangedListener(holder.twPub);
        if (holder.twSur != null) holder.etSur.removeTextChangedListener(holder.twSur);
        if (holder.twDesc != null) holder.etDesc.removeTextChangedListener(holder.twDesc);

        holder.etPrice.setText(p.precioPrice);
        holder.etPub.setText(p.precioPublicado);
        holder.etSur.setText(p.precioSurtidor);
        holder.etDesc.setText(p.precioDescuento);

        // Crear nuevos listeners que actualicen el modelo directamente
        holder.twPrice = createWatcher(s -> p.precioPrice = s);
        holder.twPub = createWatcher(s -> p.precioPublicado = s);
        holder.twSur = createWatcher(s -> p.precioSurtidor = s);
        holder.twDesc = createWatcher(s -> p.precioDescuento = s);

        holder.etPrice.addTextChangedListener(holder.twPrice);
        holder.etPub.addTextChangedListener(holder.twPub);
        holder.etSur.addTextChangedListener(holder.twSur);
        holder.etDesc.addTextChangedListener(holder.twDesc);
    }

    private TextWatcher createWatcher(final OnTextChange listener) {
        return new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) { listener.onChange(s.toString()); }
        };
    }

    interface OnTextChange { void onChange(String s); }

    @Override
    public int getItemCount() { return productos.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre;
        TextInputEditText etPrice, etPub, etSur, etDesc;
        TextWatcher twPrice, twPub, twSur, twDesc;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvProductoNombre);
            etPrice = itemView.findViewById(R.id.etPrecioPrice);
            etPub = itemView.findViewById(R.id.etPrecioPublicado);
            etSur = itemView.findViewById(R.id.etPrecioSurtidor);
            etDesc = itemView.findViewById(R.id.etPrecioDescuento);
        }
    }
}
