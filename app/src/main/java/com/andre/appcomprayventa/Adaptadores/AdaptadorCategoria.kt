package com.andre.appcomprayventa.Adaptadores

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.andre.appcomprayventa.Modelo.ModeloCategoria
import com.andre.appcomprayventa.RvListenerCategoria
import com.andre.appcomprayventa.databinding.ItemCategoriaInicioBinding
import java.util.Random

class AdaptadorCategoria(
    private val context : Context,
    private val categoriaArrayList: ArrayList<ModeloCategoria>,
    private val rvListenerCategoria : RvListenerCategoria
): Adapter<AdaptadorCategoria.HolderCtaegoria>() {

    private lateinit var binding : ItemCategoriaInicioBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderCtaegoria {
        binding = ItemCategoriaInicioBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderCtaegoria(binding.root)
    }

    override fun getItemCount(): Int {
        return categoriaArrayList.size
    }

    override fun onBindViewHolder(holder: HolderCtaegoria, position: Int) {
        val modeloCategoria = categoriaArrayList[position]

        val icono = modeloCategoria.icon
        val categoria = modeloCategoria.categoria

        val random = Random()
        val color = Color.argb(
            255,
            random.nextInt(255),
            random.nextInt(255),
            random.nextInt(255)
        )

        holder.categoriaIconoIv.setImageResource(icono)
        holder.categoriaTv.text = categoria
        holder.categoriaIconoIv.setBackgroundColor(color)

        holder.itemView.setOnClickListener {
            rvListenerCategoria.onCategoriaClick(modeloCategoria)
        }

    }

    inner class HolderCtaegoria(itemView: View) : ViewHolder(itemView) {
        var categoriaIconoIv = binding.categoriaIconoIv
        var categoriaTv = binding.TvCategoria
    }


}