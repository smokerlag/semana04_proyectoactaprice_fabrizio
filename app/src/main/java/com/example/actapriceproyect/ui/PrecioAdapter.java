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
import com.google.android.material.textfield.TextInputLayout;

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

        if (holder.twPrice != null) holder.etPrice.removeTextChangedListener(holder.twPrice);
        if (holder.twPub != null) holder.etPub.removeTextChangedListener(holder.twPub);
        if (holder.twSur != null) holder.etSur.removeTextChangedListener(holder.twSur);
        if (holder.twDesc != null) holder.etDesc.removeTextChangedListener(holder.twDesc);
        if (holder.twMarca != null) holder.etMarca.removeTextChangedListener(holder.twMarca);

        holder.etPrice.setText(p.precioPrice);
        holder.etPub.setText(p.precioPublicado);
        holder.etSur.setText(p.precioSurtidor);
        holder.etDesc.setText(p.precioDescuento);
        holder.etMarca.setText(p.marca);

        if (p.requiereMarca) {
            holder.tilMarca.setVisibility(View.VISIBLE);
            holder.tvAyuda.setVisibility(View.VISIBLE);
            holder.tvAyuda.setText("En el acta: los precios van en la columna OTROS y el nombre en la columna MARCA.");
        } else {
            holder.tilMarca.setVisibility(View.GONE);
            holder.tvAyuda.setVisibility(View.GONE);
        }

        holder.twPrice = createWatcher(s -> p.precioPrice = s);
        holder.twPub = createWatcher(s -> p.precioPublicado = s);
        holder.twSur = createWatcher(s -> p.precioSurtidor = s);
        holder.twDesc = createWatcher(s -> p.precioDescuento = s);
        holder.twMarca = createWatcher(s -> p.marca = s);

        holder.etPrice.addTextChangedListener(holder.twPrice);
        holder.etPub.addTextChangedListener(holder.twPub);
        holder.etSur.addTextChangedListener(holder.twSur);
        holder.etDesc.addTextChangedListener(holder.twDesc);
        holder.etMarca.addTextChangedListener(holder.twMarca);
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
        TextView tvNombre, tvAyuda;
        TextInputLayout tilMarca;
        TextInputEditText etPrice, etPub, etSur, etDesc, etMarca;
        TextWatcher twPrice, twPub, twSur, twDesc, twMarca;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvProductoNombre);
            tvAyuda = itemView.findViewById(R.id.tvAyudaProducto);
            tilMarca = itemView.findViewById(R.id.tilMarca);
            etMarca = itemView.findViewById(R.id.etMarca);
            etPrice = itemView.findViewById(R.id.etPrecioPrice);
            etPub = itemView.findViewById(R.id.etPrecioPublicado);
            etSur = itemView.findViewById(R.id.etPrecioSurtidor);
            etDesc = itemView.findViewById(R.id.etPrecioDescuento);
        }
    }
}
