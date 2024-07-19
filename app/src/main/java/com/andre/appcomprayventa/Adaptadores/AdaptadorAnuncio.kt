package com.andre.appcomprayventa.Adaptadores

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.PopupMenu
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.andre.appcomprayventa.Consantes
import com.andre.appcomprayventa.DetalleAnuncio.DetalleAnuncio
import com.andre.appcomprayventa.Filtro.FiltrarAnuncio
import com.andre.appcomprayventa.MainActivity
import com.andre.appcomprayventa.Modelo.ModeloAnuncio
import com.andre.appcomprayventa.R
import com.andre.appcomprayventa.databinding.ItemAnuncioNuevaVersionBinding
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue

class AdaptadorAnuncio : RecyclerView.Adapter<AdaptadorAnuncio.HolderAnuncio>, Filterable {

    private lateinit var binding: ItemAnuncioNuevaVersionBinding

    private var context: Context
    var anuncioArrayList : ArrayList<ModeloAnuncio>
    private var firebaseAuth : FirebaseAuth
    private var filtroLista : ArrayList<ModeloAnuncio>
    private var filtro : FiltrarAnuncio ?= null

    constructor(context: Context, anuncioArrayList: ArrayList<ModeloAnuncio>) {
        this.context = context
        this.anuncioArrayList = anuncioArrayList
        firebaseAuth = FirebaseAuth.getInstance()
        this.filtroLista = anuncioArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderAnuncio {
        binding = ItemAnuncioNuevaVersionBinding.inflate(LayoutInflater.from(context),parent,false)
        return HolderAnuncio(binding.root)

    }

    override fun getItemCount(): Int {
        return anuncioArrayList.size
    }

    override fun onBindViewHolder(holder: HolderAnuncio, position: Int) {
        val modeloAnuncio = anuncioArrayList[position]

        val nombre = modeloAnuncio.nombre
        val descripcion = modeloAnuncio.descripcion
        val tiempo = modeloAnuncio.tiempo

        val formatoFecha = Consantes.obtenerFecha(tiempo)

        cargarPrimeraImagenDelAnuncio(modeloAnuncio,holder)

        comprobarFavorito(modeloAnuncio, holder)

        obtenerCalificacionPromedio(modeloAnuncio.id, holder.ratingBar)
        obtenerNumeroReseñas(modeloAnuncio.id, holder.Txt_num_val)

        comprobarPermisosAdministrador(modeloAnuncio, holder)

        holder.Tv_nombre.text = nombre
        holder.Tv_descripcion.text = descripcion
        holder.Tv_fecha.text = formatoFecha

        holder.itemView.setOnClickListener {
            val intent = Intent(context, DetalleAnuncio::class.java)
            intent.putExtra("idAnuncio", modeloAnuncio.id)
            context.startActivity(intent)
        }

        holder.Ib_fav.setOnClickListener {
            val favorito = modeloAnuncio.favorito

            if(favorito) {
                //Favorito = true
                Consantes.eliminarAnuncioFav(context, modeloAnuncio.id)
            } else {
                // Favorito = falso
                Consantes.agregarAnunciofav(context, modeloAnuncio.id)
            }
        }

        holder.Ic_opciones.setOnClickListener {
            opcionesDialog(holder.itemView, modeloAnuncio)
        }

        holder.Ic_verificado.visibility = View.GONE
        comprobarAnuncioVerificado(modeloAnuncio, holder)

    }

    private fun comprobarAnuncioVerificado(modeloAnuncio: ModeloAnuncio, holder: AdaptadorAnuncio.HolderAnuncio) {
        val ref = FirebaseDatabase.getInstance().getReference("Anuncios").child(modeloAnuncio.id)

        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val verificado = snapshot.child("verificado").getValue(String::class.java)
                if (verificado == "si") {
                    holder.Ic_verificado.visibility = View.VISIBLE
                } else {
                    holder.Ic_verificado.visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun comprobarPermisosAdministrador(modeloAnuncio: ModeloAnuncio, holder: AdaptadorAnuncio.HolderAnuncio) {
        if (firebaseAuth.currentUser != null) {
            val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
            val uidUsuarioActual = FirebaseAuth.getInstance().currentUser?.uid

            ref.child(uidUsuarioActual!!).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val tipoUsuario = snapshot.child("tipoUsuario").value
                    if (tipoUsuario == "admin") {
                        // Si el usuario es administrador, habilita el botón de opciones
                        holder.Ic_opciones.visibility = View.VISIBLE
                    } else {
                        // Si el usuario no es administrador, deshabilita el botón de opciones
                        holder.Ic_opciones.visibility = View.GONE
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Manejar el error
                }
            })
        } else {
            holder.Ic_opciones.visibility = View.GONE
        }
    }

    private fun opcionesDialog(itemView: View, modeloAnuncio : ModeloAnuncio) {
        val popupMenu = PopupMenu(itemView.context, itemView.findViewById(R.id.Ic_opciones))

        popupMenu.menu.add(Menu.NONE, 0, 0, "Verificar receta")
        popupMenu.menu.add(Menu.NONE, 1, 1, "Quitar verificación")
        popupMenu.menu.add(Menu.NONE, 2, 2, "Eliminar receta")

        popupMenu.show()

        popupMenu.setOnMenuItemClickListener { item->
            val itemId = item.itemId

            if (itemId == 0) {
                verificarReceta(modeloAnuncio)
            } else if (itemId == 1) {
                quitarVerificacion(modeloAnuncio)
            } else if (itemId == 2) {
                eliminarReceta(context ,modeloAnuncio)
            }

            return@setOnMenuItemClickListener true
        }

    }

    private fun verificarReceta(modeloAnuncio: ModeloAnuncio) {
        val ref = FirebaseDatabase.getInstance().getReference("Anuncios").child(modeloAnuncio.id)
        ref.child("verificado").setValue("si")
            .addOnSuccessListener {
                // Actualización exitosa
                Toast.makeText(context, "Receta verificada con éxito", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                // Manejar errores en la actualización
                Toast.makeText(context, "Receta no se pudo verificar debido a: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun quitarVerificacion(modeloAnuncio: ModeloAnuncio) {
        val ref = FirebaseDatabase.getInstance().getReference("Anuncios").child(modeloAnuncio.id)
        ref.child("verificado").setValue("no")
            .addOnSuccessListener {
                // Actualización exitosa
                Toast.makeText(context, "Receta verificada con éxito", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                // Manejar errores en la actualización
                Toast.makeText(context, "Receta no se pudo verificar debido a: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun eliminarReceta(context: Context ,modeloAnuncio: ModeloAnuncio) {
        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
        ref.child(modeloAnuncio.id)
            .removeValue()
            .addOnSuccessListener {
                // Eliminación exitosa
                val intent = Intent(context, MainActivity::class.java)
                context.startActivity(intent)
                Toast.makeText(context, "Receta eliminada con éxito", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                // Manejar errores en la eliminación
                Toast.makeText(context, "Receta no se pudo eliminar debido a: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun obtenerCalificacionPromedio(idAnuncio: String, ratingBar: RatingBar) {
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

    private fun obtenerNumeroReseñas(idAnuncio : String, txtNumVal: TextView) {
        val ref = FirebaseDatabase.getInstance().getReference("CalificacionesUsuarios").child(idAnuncio).child("Valoraciones")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val numeroReseñas = snapshot.childrenCount
                txtNumVal.text = "($numeroReseñas calificaciones)"
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejar el error
            }
        })
    }

    private fun comprobarFavorito(modeloAnuncio: ModeloAnuncio, holder: AdaptadorAnuncio.HolderAnuncio) {
        if (firebaseAuth.currentUser != null) {
            val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
            ref.child(firebaseAuth.uid!!).child("Favoritos").child(modeloAnuncio.id)
                .addValueEventListener(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val favorito = snapshot.exists()
                        modeloAnuncio.favorito = favorito

                        if (favorito) {
                            holder.Ib_fav.setImageResource(R.drawable.ic_anuncio_es_favorito)
                        } else {
                            holder.Ib_fav.setImageResource(R.drawable.ic_no_favorito)
                        }

                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
        }
        else {
            holder.Ib_fav.visibility = View.GONE
        }
    }

    private fun cargarPrimeraImagenDelAnuncio(modeloAnuncio: ModeloAnuncio, holder: AdaptadorAnuncio.HolderAnuncio) {
        val idAnuncio = modeloAnuncio.id

        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
        ref.child(idAnuncio).child("Imagenes").limitToFirst(1).addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for (ds in snapshot.children) {
                    val imagenUrl = "${ds.child("imagenUrl").value}"
                    try {
                        Glide.with(context)
                            .load(imagenUrl)
                            .placeholder(R.drawable.ic_imagen)
                            .into(holder.imagenTv)
                    } catch (e:Exception) {

                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    inner class HolderAnuncio(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imagenTv = binding.imagenIv
        var Tv_nombre = binding.TvNombre
        var Tv_descripcion = binding.TvDescripcion
        var Tv_fecha = binding.TvFecha
        var Ib_fav = binding.IbFav
        var ratingBar: RatingBar = binding.ratingBar
        var Txt_num_val = binding.TxtNumVal
        var Ic_opciones = binding.IcOpciones
        var Ic_verificado = binding.icAnuncioVerificado

        init {
            ratingBar = itemView.findViewById(R.id.ratingBar)
        }
    }

    override fun getFilter(): Filter {
        if (filtro == null) {
            filtro = FiltrarAnuncio(this, filtroLista)
        }
        return filtro as FiltrarAnuncio
    }

}