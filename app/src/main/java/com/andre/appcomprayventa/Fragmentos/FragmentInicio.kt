package com.andre.appcomprayventa.Fragmentos

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.andre.appcomprayventa.Adaptadores.AdaptadorAnuncio
import com.andre.appcomprayventa.Adaptadores.AdaptadorCategoria
import com.andre.appcomprayventa.Consantes
import com.andre.appcomprayventa.Modelo.ModeloAnuncio
import com.andre.appcomprayventa.Modelo.ModeloCategoria
import com.andre.appcomprayventa.R
import com.andre.appcomprayventa.RvListenerCategoria
import com.andre.appcomprayventa.databinding.FragmentInicioBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class FragmentInicio : Fragment() {

    private lateinit var binding : FragmentInicioBinding

    private lateinit var mContext: Context

    private lateinit var anuncioArrayList : ArrayList<ModeloAnuncio>
    private lateinit var adaptadorAnuncio : AdaptadorAnuncio

    override fun onAttach(context: Context) {
        mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentInicioBinding.inflate(LayoutInflater.from(mContext), container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cargarCategorias()
        cargarAnuncios("Todos")

        binding.EtBuscar.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(filtro: CharSequence?, p1: Int, p2: Int, p3: Int) {
                try {
                    val consulta = filtro.toString()
                    adaptadorAnuncio.filter.filter(consulta)
                } catch (e:Exception) {

                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })

        binding.IbLimpiar.setOnClickListener{
            val consulta = binding.EtBuscar.text.toString().trim()
            if(consulta.isNotEmpty()) {
                binding.EtBuscar.setText("")
                Toast.makeText(context, "Se ha limpiado el campo de búsqueda", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "No se ha ingresado una consulta", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun cargarCategorias() {
        val categoriaArrayList = ArrayList<ModeloCategoria>()
        for (i in 0 until Consantes.categorias.size){
            val modeloCategoria = ModeloCategoria(Consantes.categorias[i], Consantes.categoriasIcono[i])
            categoriaArrayList.add(modeloCategoria)
        }

        val adaptadorCategoria = AdaptadorCategoria(
            mContext,
            categoriaArrayList,
            object : RvListenerCategoria{
                override fun onCategoriaClick(modeloCategoria: ModeloCategoria) {
                    val categoriaSeleccionada = modeloCategoria.categoria
                    cargarAnuncios(categoriaSeleccionada)
                }
            }
        )

        binding.categoriaRv.adapter = adaptadorCategoria

    }

    private fun cargarAnuncios(categoria: String) {
        anuncioArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                anuncioArrayList.clear()

                for (ds in snapshot.children) {
                    try {
                        val modeloAnuncio = ds.getValue(ModeloAnuncio::class.java)

                        if (categoria == "Todos") {
                            anuncioArrayList.add(modeloAnuncio!!)
                        } else {
                            if (modeloAnuncio!!.categoria.equals(categoria)) {
                                anuncioArrayList.add(modeloAnuncio)
                            }
                        }

                    } catch (e:Exception) {

                    }
                }

                adaptadorAnuncio = AdaptadorAnuncio(mContext, anuncioArrayList)
                binding.anunciosRv.adapter = adaptadorAnuncio

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

}