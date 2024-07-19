package com.andre.appcomprayventa.Adaptadores

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.andre.appcomprayventa.Modelo.ModeloImgSlider
import com.andre.appcomprayventa.R
import com.andre.appcomprayventa.databinding.ItemImagenSliderBinding
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView

class AdaptadorImgSlider : Adapter<AdaptadorImgSlider.HolderImagenSlider> {

    private lateinit var binding: ItemImagenSliderBinding
    private var context : Context
    private var imgArrayList : ArrayList<ModeloImgSlider>

    constructor(contexto: Context, imgArrayList: ArrayList<ModeloImgSlider>) {
        this.context = contexto
        this.imgArrayList = imgArrayList
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderImagenSlider {
        binding = ItemImagenSliderBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderImagenSlider(binding.root)
    }

    override fun getItemCount(): Int {
        return imgArrayList.size
    }

    override fun onBindViewHolder(holder: HolderImagenSlider, position: Int) {
        val modeloImagenSlider = imgArrayList[position]

        val imagenUrl = modeloImagenSlider.imagenUrl
        val imagenContador = "${position+1} / ${imgArrayList.size}"

        holder.imagenContadorTv.text = imagenContador

        try {
            Glide.with(context).load(imagenUrl).placeholder(R.drawable.ic_imagen).into(holder.imagenIv)
        } catch (e:Exception) {

        }

        holder.itemView.setOnClickListener {

        }
    }

    inner class HolderImagenSlider(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imagenIv : ShapeableImageView = binding.ImagenIv
        var imagenContadorTv : TextView = binding.imagenContadorTv
    }

}