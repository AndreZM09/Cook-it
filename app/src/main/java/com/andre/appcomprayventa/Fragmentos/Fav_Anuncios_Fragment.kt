package com.andre.appcomprayventa.Fragmentos

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.andre.appcomprayventa.Adaptadores.AdaptadorAnuncio
import com.andre.appcomprayventa.Modelo.ModeloAnuncio
import com.andre.appcomprayventa.R
import com.andre.appcomprayventa.databinding.FragmentFavAnunciosBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlin.collections.ArrayList

class Fav_Anuncios_Fragment : Fragment() {

    private lateinit var binding : FragmentFavAnunciosBinding
    private lateinit var mContext: Context
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var anunciosArrayList: ArrayList<ModeloAnuncio>
    private lateinit var anunciosAdapator: AdaptadorAnuncio

    override fun onAttach(context: Context) {
        this.mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentFavAnunciosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()
        cargarAnunciosFavoritos()

        binding.EtBuscar.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(filtro: CharSequence?, p1: Int, p2: Int, p3: Int) {
                try {
                    val consulta = filtro.toString()
                    anunciosAdapator.filter.filter(consulta)
                } catch (e: Exception) {

                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })

        binding.IbLimpiar.setOnClickListener {
            val consulta = binding.EtBuscar.text.toString().trim()
            if (consulta.isNotEmpty()) {
                binding.EtBuscar.setText("")
                Toast.makeText(context,
                    "Se ha limpiado el campo de búsqueda",
                    Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context,
                    "No se ha ingresado una consulta",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun cargarAnunciosFavoritos() {
        anunciosArrayList = ArrayList()

        var ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child(firebaseAuth.uid!!).child("Favoritos")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    anunciosArrayList.clear()
                    for (ds in snapshot.children) {
                        val idAnuncio = "${ds.child("idAnuncio").value}"

                        val refFavoritos = FirebaseDatabase.getInstance().getReference("Anuncios")
                        refFavoritos.child(idAnuncio)
                            .addListenerForSingleValueEvent(object : ValueEventListener{
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    try {
                                        val modeloAnuncio = snapshot.getValue(ModeloAnuncio::class.java)
                                        anunciosArrayList.add(modeloAnuncio!!)
                                    } catch (e:Exception) {

                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    TODO("Not yet implemented")
                                }
                            })
                    }

                    Handler().postDelayed({
                        anunciosAdapator = AdaptadorAnuncio(mContext, anunciosArrayList)
                        binding.anunciosRv.adapter = anunciosAdapator
                    }, 500)

                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

}