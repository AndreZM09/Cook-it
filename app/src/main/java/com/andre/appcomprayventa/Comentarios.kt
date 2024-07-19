
package com.andre.appcomprayventa

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.RatingBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.andre.appcomprayventa.Adaptadores.AdaptadorComentario
import com.andre.appcomprayventa.Modelo.ModeloComentario
import com.andre.appcomprayventa.databinding.ActivityComentariosBinding
import com.andre.appcomprayventa.databinding.CuadroDeDialogoAgregarComentarioBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Comentarios : AppCompatActivity() {

    private lateinit var binding : ActivityComentariosBinding
    private lateinit var firebaseAuth : FirebaseAuth
    private lateinit var progressDialog : ProgressDialog
    private lateinit var ratingBar : RatingBar
    private var idAnuncio = ""

    private lateinit var comentarioArrayList : ArrayList<ModeloComentario>
    private lateinit var adaptadorComentario: AdaptadorComentario

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityComentariosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        idAnuncio = intent.getStringExtra("idAnuncio").toString()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Porfavor espere")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()

        obtenerCalificacionUsuarioActual()

        binding.ratingBar.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
            agregarValoracion(rating)
        }

        binding.IbRegresar.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.IbAgregarComentario.setOnClickListener {
            dialogComentar()
        }

        listarComentarios()

    }

    private fun obtenerCalificacionUsuarioActual() {
        val ref = FirebaseDatabase.getInstance().getReference("CalificacionesUsuarios")
            .child(idAnuncio).child("Valoraciones").child(firebaseAuth.uid!!)

        // Obtener la calificación del usuario actual
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Verificar si la calificación existe para el usuario actual
                if (snapshot.exists()) {
                    val valoracion = snapshot.child("calificacion").value.toString().toDouble()
                    val valoracionFloat = valoracion.toFloat()
                    // Configurar la calificación en la RatingBar
                    binding.ratingBar.rating = valoracionFloat
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejar el error
            }
        })
    }

    private fun agregarValoracion(valoracion: Float) {
        progressDialog.setMessage("Agregando Valoración")
        progressDialog.show()

        val tiempo = "${Consantes.obtenerTiempoDis()}"

        val hashMap = HashMap<String, Any>()
        hashMap["id"] = "${tiempo}"
        hashMap["uid"] = "${firebaseAuth.uid}"
        hashMap["idAnuncio"] = "${idAnuncio}"
        hashMap["calificacion"] = valoracion

        val ref = FirebaseDatabase.getInstance().getReference("CalificacionesUsuarios")
        ref.child(idAnuncio).child("Valoraciones").child("${firebaseAuth.uid}")
            .setValue(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Receta calificada", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "${e}", Toast.LENGTH_SHORT).show()
            }

        sumarTotalCalificaciones()

    }

    private fun sumarTotalCalificaciones() {

        val ref = FirebaseDatabase.getInstance().getReference("CalificacionesUsuarios").child(idAnuncio).child("Valoraciones")

        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var totalValoraciones = snapshot.childrenCount.toInt()
                setTotalValoraciones(totalValoraciones)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun setTotalValoraciones(totalValoraciones: Int) {

        val ref = FirebaseDatabase.getInstance().getReference("CalificacionesUsuarios").child(idAnuncio)

        val hashMap = HashMap<String,Any>()
        hashMap["totalCalificaciones"] = totalValoraciones

        ref.updateChildren(hashMap)
    }

    private fun listarComentarios() {
        comentarioArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("ComentariosAnuncios")
        ref.child(idAnuncio).child("Comentarios")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    comentarioArrayList.clear()
                    for (ds in snapshot.children) {
                        val modelo = ds.getValue(ModeloComentario::class.java)
                        comentarioArrayList.add(modelo!!)
                    }

                    adaptadorComentario = AdaptadorComentario(this@Comentarios, comentarioArrayList)
                    binding.RvComentarios.adapter = adaptadorComentario

                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    private var comentario = ""
    private fun dialogComentar() {
        val agregar_comentario_binding = CuadroDeDialogoAgregarComentarioBinding.inflate(LayoutInflater.from(this))

        val builder = AlertDialog.Builder(this)
        builder.setView(agregar_comentario_binding.root)

        val alertDialog = builder.create()
        alertDialog.show()

        alertDialog.setCanceledOnTouchOutside(false)

        agregar_comentario_binding.IbCerrar.setOnClickListener {
            alertDialog.dismiss()
        }

        agregar_comentario_binding.BtnComentar.setOnClickListener {
            comentario = agregar_comentario_binding.EtAgregarComentario.text.toString()
            if(comentario.isEmpty()) {
                Toast.makeText(this, "Ingrese un comentario", Toast.LENGTH_SHORT).show()
            } else {
                alertDialog.dismiss()
                agregarComentario()
            }
        }

    }

    private fun agregarComentario() {
        progressDialog.setMessage("Agregando comentario")
        progressDialog.show()

        val tiempo = "${Consantes.obtenerTiempoDis()}"

        val hashMap = HashMap<String, Any>()
        hashMap["id"] = "${tiempo}"
        hashMap["tiempo"] = "${tiempo}"
        hashMap["uid"] = "${firebaseAuth.uid}"
        hashMap["idAnuncio"] = "${idAnuncio}"
        hashMap["comentario"] = "${comentario}"

        // Obtener el ID del usuario que publicó el anuncio
        obtenerIdUsuarioDelAnuncio(idAnuncio) { idUsuario ->
            hashMap["idUsuarioAnuncio"] = idUsuario

            val ref = FirebaseDatabase.getInstance().getReference("ComentariosAnuncios")
            ref.child(idAnuncio).child("Comentarios").child(tiempo)
                .setValue(hashMap)
                .addOnSuccessListener {
                    progressDialog.dismiss()
                    Toast.makeText(this, "Su comentario se ha publicado", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    progressDialog.dismiss()
                    Toast.makeText(this, "${e}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun obtenerIdUsuarioDelAnuncio(idAnuncio: String, callback: (String) -> Unit) {
        val ref = FirebaseDatabase.getInstance().getReference("Anuncios").child(idAnuncio)
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val idUsuario = snapshot.child("uid").value.toString()
                // Llamar al callback con el ID del usuario
                callback(idUsuario)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, "Error al obtener el ID del usuario", Toast.LENGTH_SHORT).show()
            }
        })
    }
}