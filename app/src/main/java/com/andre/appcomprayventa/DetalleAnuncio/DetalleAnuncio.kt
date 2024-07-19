package com.andre.appcomprayventa.DetalleAnuncio

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.PopupMenu
import android.widget.RatingBar
import android.widget.Toast
import com.andre.appcomprayventa.Adaptadores.AdaptadorImgSlider
import com.andre.appcomprayventa.Comentarios
import com.andre.appcomprayventa.Consantes
import com.andre.appcomprayventa.MainActivity
import com.andre.appcomprayventa.Modelo.ModeloAnuncio
import com.andre.appcomprayventa.Modelo.ModeloImgSlider
import com.andre.appcomprayventa.R
import com.andre.appcomprayventa.anuncios.CrearAnuncio
import com.andre.appcomprayventa.databinding.ActivityDetalleAnuncioBinding
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DetalleAnuncio : AppCompatActivity() {

    private lateinit var binding : ActivityDetalleAnuncioBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var ratingBar : RatingBar
    private var idAnuncio = ""

    private var uidVendedor = ""

    private var favorito = false

    private lateinit var imagenSliderArrayList: ArrayList<ModeloImgSlider>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleAnuncioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.IbEditar.visibility = View.GONE
        binding.IbEliminar.visibility = View.GONE


        firebaseAuth = FirebaseAuth.getInstance()

        idAnuncio = intent.getStringExtra("idAnuncio").toString()

        Consantes.incrementarVistas(idAnuncio)

        ratingBar = findViewById(R.id.ratingBar)
        configurarRatingBar()

        obtenerCalificacionPromedio(idAnuncio)

        binding.IbRegresar.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        comprobarAnuncioFav()
        cargarInfoAnuncio()
        cargarImagenesDelAnuncio()

        if (firebaseAuth.currentUser == null) {
            binding.IbFavorito.visibility = View.GONE
        }
        binding.IbEditar.setOnClickListener {
            opcionesDialog()
        }

        binding.IbFavorito.setOnClickListener {
            if(favorito) {
                Consantes.eliminarAnuncioFav(this, idAnuncio)
            } else {
                Consantes.agregarAnunciofav(this, idAnuncio)
            }
        }

        binding.IbEliminar.setOnClickListener {
            val alertDialog = MaterialAlertDialogBuilder(this)
            alertDialog.setTitle("Eliminar anuncio").setMessage("¿Estas seguro de eliminar este anuncio?")
                .setPositiveButton("Eliminar") {dialog, wich ->
                    eliminarAnuncio()
                }
                .setNegativeButton("Cancelar") {dialog, wich ->
                    dialog.dismiss()
                }.show()
        }

        binding.IvComentarios.setOnClickListener {
            val intent = Intent(this, Comentarios::class.java)
            intent.putExtra("idAnuncio", idAnuncio)
            startActivity(intent)
        }

        binding.icAnuncioVerificado.visibility = View.GONE
        comprobarAnuncioVerificado()

    }

    private fun comprobarAnuncioVerificado() {
        val ref = FirebaseDatabase.getInstance().getReference("Anuncios").child(idAnuncio)

        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val verificado = snapshot.child("verificado").getValue(String::class.java)
                if (verificado == "si") {
                    binding.icAnuncioVerificado.visibility = View.VISIBLE
                } else {
                    binding.icAnuncioVerificado.visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun configurarRatingBar() {
        ratingBar.isClickable = false
        ratingBar.isFocusable = false
    }

    private fun obtenerCalificacionPromedio(idAnuncio: String) {
        val ref = FirebaseDatabase.getInstance().getReference("CalificacionesUsuarios").child(idAnuncio).child("Valoraciones")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var totalCalificacion = 0f
                var contador = 0
                for (ds in snapshot.children) {
                    val calificacion = ds.child("calificacion").getValue(Float::class.java)
                    calificacion?.let {
                        totalCalificacion += it
                        contador++
                    }
                }
                if (contador != 0) {
                    val promedio = totalCalificacion / contador
                    ratingBar.rating = promedio
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejar el error
            }
        })
    }

    private fun opcionesDialog() {
        val intent = Intent(this, CrearAnuncio::class.java)
        intent.putExtra("Edicion", true)
        intent.putExtra("idAnuncio", idAnuncio)
        startActivity(intent)
    }

    private fun cargarInfoAnuncio() {
        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
        ref.child(idAnuncio)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        val modeloAnuncio = snapshot.getValue(ModeloAnuncio::class.java)

                        uidVendedor = "${modeloAnuncio!!.uid}"
                        val nombre = modeloAnuncio.nombre
                        val descripcion = modeloAnuncio.descripcion
                        val categoria = modeloAnuncio.categoria
                        val calorias = modeloAnuncio.calorias
                        val ingredientes = modeloAnuncio.ingredientes
                        val pasos = modeloAnuncio.pasos
                        val tiempo = modeloAnuncio.tiempo
                        val vista = modeloAnuncio.contadorVistas

                        val formatoFecha = Consantes.obtenerFecha(tiempo)

                        val ingredientesList = ingredientes.split("\n")
                        val pasosList = pasos.split("\n")

                        if (uidVendedor == firebaseAuth.uid) {
                            // Si el usuario que ha realizado la publicaicon, visualiza la informacion del anuncio

                            // Tendrá disponible
                            binding.IbEditar.visibility = View.VISIBLE
                            binding.IbEliminar.visibility = View.VISIBLE

                        } else {

                            // No tendrá disponible
                            binding.IbEditar.visibility = View.GONE
                            binding.IbEliminar.visibility = View.GONE
                        }

                        // Seteamos la informacion en las vistas
                        binding.TvNombre.text = nombre
                        binding.TvDescr.text = descripcion
                        binding.TvCat.text = categoria
                        binding.TvCalorias.text = calorias
                        binding.TvFecha.text = formatoFecha
                        binding.TvVistas.text = vista.toString()
                        binding.TvIngredientes.text = ingredientesList.joinToString("\n")
                        binding.TvPasos.text = pasosList.joinToString("\n")

                        // Informacion del vendedor
                        cargarInfoVendedor()

                    } catch (e:Exception) {

                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    private fun cargarInfoVendedor() {
        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child(uidVendedor)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val codTel = "${snapshot.child("codigoTelefono").value}"
                    val nombres = "${snapshot.child("nombres").value}"
                    val imagenPerfil = "${snapshot.child("urlImagenPerfil").value}"
                    val tiempoReg = snapshot.child("tiempo").value as Long

                    val formatoFecha = Consantes.obtenerFecha(tiempoReg)

                    binding.TvNombres.text = nombres
                    binding.TvMiembro.text = formatoFecha

                    try {
                        Glide.with(this@DetalleAnuncio)
                            .load(imagenPerfil)
                            .placeholder(R.drawable.img_perfil)
                            .into(binding.ImgPerfil)
                    } catch (e:Exception) {

                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    private fun cargarImagenesDelAnuncio() {
        imagenSliderArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
        ref.child(idAnuncio).child("Imagenes")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    imagenSliderArrayList.clear()
                    for (ds in snapshot.children) {
                        try {
                            val modeloImgSlider = ds.getValue(ModeloImgSlider::class.java)
                            imagenSliderArrayList.add(modeloImgSlider!!)
                        } catch (e:Exception) {

                        }
                    }

                    val adaptadorImgSlider = AdaptadorImgSlider(this@DetalleAnuncio, imagenSliderArrayList)
                    binding.imagenSliderVP.adapter = adaptadorImgSlider

                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    private fun comprobarAnuncioFav() {
        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child("${firebaseAuth.uid}").child("Favoritos").child(idAnuncio)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    favorito = snapshot.exists()

                    if (favorito) {
                        // Favorito existe
                        binding.IbFavorito.setImageResource(R.drawable.ic_anuncio_es_favorito)
                    } else {
                        // Favorito no existe
                        binding.IbFavorito.setImageResource(R.drawable.ic_no_favorito)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    private fun eliminarAnuncio() {
        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
        ref.child(idAnuncio)
            .removeValue()
            .addOnSuccessListener {
                startActivity(Intent(this@DetalleAnuncio, MainActivity::class.java))
                finishAffinity()
                Toast.makeText(
                    this,
                    "Se eliminó el anuncio con éxito",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener{e->
                Toast.makeText(
                    this,
                    "${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

}