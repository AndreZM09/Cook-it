package com.andre.appcomprayventa.Filtro

import android.widget.Filter
import com.andre.appcomprayventa.Adaptadores.AdaptadorAnuncio
import com.andre.appcomprayventa.Modelo.ModeloAnuncio
import java.util.Locale

class FiltrarAnuncio(
    private val adaptador : AdaptadorAnuncio,
    private val filtroLista : ArrayList<ModeloAnuncio>
): Filter() {
    override fun performFiltering(filtro: CharSequence?): FilterResults {
        var filtro = filtro
        val resultados = FilterResults()

        if (!filtro.isNullOrEmpty()) {
            filtro = filtro.toString().uppercase(Locale.getDefault())
            val filtroModelo = ArrayList<ModeloAnuncio>()
            for (i in filtroLista.indices) {
                if (filtroLista[i].nombre.uppercase(Locale.getDefault()).contains(filtro) ||
                    filtroLista[i].categoria.uppercase(Locale.getDefault()).contains(filtro) ||
                    filtroLista[i].calorias.uppercase(Locale.getDefault()).contains(filtro) ||
                    filtroLista[i].descripcion.uppercase(Locale.getDefault()).contains(filtro)) {
                    filtroModelo.add(filtroLista[i])
                }
            }
            resultados.count = filtroModelo.size
            resultados.values = filtroModelo
        } else {
            resultados.count = filtroLista.size
            resultados.values = filtroLista
        }
        return resultados
    }

    override fun publishResults(filtro: CharSequence?, resultados: FilterResults) {
        adaptador.anuncioArrayList = resultados.values as ArrayList<ModeloAnuncio>
        adaptador.notifyDataSetChanged()
    }
}