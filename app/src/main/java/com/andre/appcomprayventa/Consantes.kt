package com.andre.appcomprayventa

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.text.format.DateFormat
import android.widget.Toast
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Calendar
import java.util.Locale

object Consantes {

    const val anuncio_disponible = "Disponible"
    const val anuncio_vendido = "Vendido"

    val categorias = arrayOf(
        "Todos",
        "Desayunos",
        "Postres",
        "Pollo",
        "Carne y cortes",
        "Comida Mexicana",
        "Ensaladas",
        "Sopas",
        "Mariscos",
        "Comida China",
        "Pastas",
        "Comida niños",
        "Salsas",
        "Comida Italiana"
    )

    val categoriasIcono = arrayOf(
        R.drawable.ic_tcategoria_todos,
        R.drawable.ic_desayuno,
        R.drawable.ic_postre,
        R.drawable.ic_pollo,
        R.drawable.ic_carne,
        R.drawable.ic_mexico,
        R.drawable.ic_ensalada,
        R.drawable.ic_sopa,
        R.drawable.ic_mariscos,
        R.drawable.ic_china,
        R.drawable.ic_pastas,
        R.drawable.ic_child,
        R.drawable.ic_salsa,
        R.drawable.ic_italia
    )

    val condiciones = arrayOf(
        "Nuevo",
        "Usado",
        "Renovado"
    )

    fun obtenerTiempoDis(): Long{
        return System.currentTimeMillis()
    }

    fun obtenerFecha(tiempo : Long): String {
        val calendario = Calendar.getInstance(Locale.ENGLISH)
        calendario.timeInMillis = tiempo

        return  DateFormat.format("dd/MM/yyyy", calendario).toString()
    }

    fun agregarAnunciofav (context : Context, idAnuncio : String) {
        val firebaseAuth = FirebaseAuth.getInstance()
        val tiempo = Consantes.obtenerTiempoDis()

        val hashMap = HashMap<String, Any>()
        hashMap["idAnuncio"] = idAnuncio
        hashMap["tiempo"] = tiempo

        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child(firebaseAuth.uid!!).child("Favoritos").child(idAnuncio)
            .setValue(hashMap)
            .addOnSuccessListener {
                Toast.makeText(context, "Anuncio agregado a favoritos", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {e->
                Toast.makeText(context, "${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    fun eliminarAnuncioFav (context: Context, idAnuncio: String) {
        val firebaseAuth = FirebaseAuth.getInstance()

        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child(firebaseAuth.uid!!).child("Favoritos").child(idAnuncio)
            .removeValue()
            .addOnSuccessListener {
                Toast.makeText(context, "Anuncio eliminado de favoritos", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {e->
                Toast.makeText(context, "${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    fun incrementarVistas(idAnuncio: String) {
        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
        ref.child(idAnuncio)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    var vistasActuales = "${snapshot.child("contadorVistas").value}"
                    if (vistasActuales == "" || vistasActuales == "null") {
                        vistasActuales = "0"
                    }

                    val nuevaVista = vistasActuales.toLong() + 1

                    val hashMap = HashMap<String,Any> ()
                    hashMap["contadorVistas"] = nuevaVista

                    val dbRef = FirebaseDatabase.getInstance().getReference("Anuncios")
                    dbRef.child(idAnuncio)
                        .updateChildren(hashMap)

                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    fun mostrarCuadroDialogo (context : Context) {
        val Btn_login : MaterialButton
        val Btn_registrarme : MaterialButton
        val dialog = Dialog(context)

        dialog.setContentView(R.layout.cuadro_de_dialogo_logue_registro)

        Btn_login = dialog.findViewById(R.id.Btn_iniciar_sesion)
        Btn_registrarme = dialog.findViewById(R.id.Btn_registrarme)

        Btn_login.setOnClickListener {
            context.startActivity(Intent(context, OpcionesLogin::class.java))
        }

        Btn_registrarme.setOnClickListener {
            context.startActivity(Intent(context, Registro_email::class.java))
        }
        dialog.show()
    }

}