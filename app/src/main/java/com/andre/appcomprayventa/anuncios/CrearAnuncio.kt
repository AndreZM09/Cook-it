package com.andre.appcomprayventa.anuncios

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.andre.appcomprayventa.Adaptadores.AdaptadorImagenSeleccionada
import com.andre.appcomprayventa.Consantes
import com.andre.appcomprayventa.DetalleAnuncio.DetalleAnuncio
import com.andre.appcomprayventa.EditarPerfil
import com.andre.appcomprayventa.MainActivity
import com.andre.appcomprayventa.Modelo.ModeloImagenSeleccionada
import com.andre.appcomprayventa.R
import com.andre.appcomprayventa.databinding.ActivityCrearAnuncioBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class CrearAnuncio : AppCompatActivity() {

    private lateinit var binding : ActivityCrearAnuncioBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    private var imagenUri : Uri?= null

    private lateinit var imagenSeleccionadaArrayList : ArrayList<ModeloImagenSeleccionada>
    private lateinit var adapatadorImagenSeleccionada : AdaptadorImagenSeleccionada

    private var Edicion = false
    private var idAnuncioEditar = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCrearAnuncioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Espere pofavor")
        progressDialog.setCanceledOnTouchOutside(false)

        val adaptadorCat = ArrayAdapter(this, R.layout.item_categoria, Consantes.categorias)
        binding.Categoria.setAdapter(adaptadorCat)

        Edicion = intent.getBooleanExtra("Edicion", false)

        // Identificamos de que actividad estamos llegando
        if (Edicion) {
            //Llegamos de la actividad detalle anuncio
            idAnuncioEditar = intent.getStringExtra("idAnuncio") ?: ""
            cargarDetalles()
            binding.BtnCrearAnuncio.text = "Actualizar Anuncio"
        } else {
            //Llegando de la actividad MainActivity
            binding.BtnCrearAnuncio.text = "Crear anuncio"

        }

        imagenSeleccionadaArrayList = ArrayList()
        cargarImagenes()

        binding.agregarImagen.setOnClickListener {
            mostrarOpciones()
        }

        binding.BtnCrearAnuncio.setOnClickListener {
            validarDatos()
        }
    }

    private fun cargarDetalles() {
        var ref = FirebaseDatabase.getInstance().getReference("Anuncios")
        ref.child(idAnuncioEditar)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Obtener de la base de datos la informacion del anuncio
                    val nombre = "${snapshot.child("nombre").value}"
                    val categoria = "${snapshot.child("categoria").value}"
                    val calorias = "${snapshot.child("calorias").value}"
                    val descripcion = "${snapshot.child("descripcion").value}"
                    val ingredientes = "${snapshot.child("ingredientes").value}"
                    val pasos = "${snapshot.child("pasos").value}"

                    //Setear informacion en las vistas
                    binding.EtNombre.setText(nombre)
                    binding.Categoria.setText(categoria)
                    binding.Categoria.isEnabled = false
                    binding.EtCalorias.setText(calorias)
                    binding.EtDescripcion.setText(descripcion)
                    binding.EtIngredientes.setText(ingredientes)
                    binding.EtPasos.setText(pasos)

                    val refImagenes = snapshot.child("Imagenes").ref
                    refImagenes.addListenerForSingleValueEvent(object : ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for (ds in snapshot.children) {
                                val id = "${ds.child("id").value}"
                                val imagenUrl = "${ds.child("imagenUrl").value}"

                                val modeloImagenSeleccionada = ModeloImagenSeleccionada(id, null, imagenUrl, true)
                                imagenSeleccionadaArrayList.add(modeloImagenSeleccionada)

                                cargarImagenes()

                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                        }
                    })
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    private var nombre = ""
    private var categoria = ""
    private var calorias = ""
    private var descripcion = ""
    private var ingredientes = ""
    private var pasos = ""

    private fun validarDatos() {
        nombre = binding.EtNombre.text.toString().trim()
        categoria = binding.Categoria.text.toString().trim()
        calorias = binding.EtCalorias.text.toString().trim()
        descripcion = binding.EtDescripcion.text.toString().trim()
        ingredientes = binding.EtIngredientes.text.toString().trim()
        pasos = binding.EtPasos.text.toString().trim()

        if (nombre.isEmpty()) {
            binding.EtNombre.error = "Ingrese el nombre de la receta"
            binding.EtNombre.requestFocus()
        }
        else if (categoria.isEmpty()) {
            binding.Categoria.error = "Ingrese la categoría"
            binding.Categoria.requestFocus()
        }
        else if (calorias.isEmpty()) {
            binding.EtCalorias.error = "Ingrese el total de calorías de la receta"
            binding.EtCalorias.requestFocus()
        }
        else if (descripcion.isEmpty()) {
            binding.EtDescripcion.error = "Ingrese una breve descripción de su receta"
            binding.EtDescripcion.requestFocus()
        }
        else if (ingredientes.isEmpty()) {
            binding.EtIngredientes.error = "Ingrese ingredientes"
            binding.EtIngredientes.requestFocus()
        }
        else if (pasos.isEmpty()) {
            binding.EtPasos.error = "Ingrese los pasos de la receta"
            binding.EtPasos.requestFocus()
        }
        else {
            if (Edicion) {
                actualizarAnuncio()
            } else {
                if (imagenUri == null) {
                    Toast.makeText(this, "Agregue al menos una imagen", Toast.LENGTH_SHORT).show()
                } else {
                    agregarAnuncio()
                }
            }
        }
    }

    private fun actualizarAnuncio() {
        progressDialog.setMessage("Actualizando anuncio")
        progressDialog.show()

        val hashMap = HashMap<String,Any>()

        hashMap["nombre"] = "${nombre}"
        hashMap["categoria"] = "${categoria}"
        hashMap["calorias"] = "${calorias}"
        hashMap["descripcion"] = "${descripcion}"
        hashMap["ingredientes"] = "${ingredientes}"
        hashMap["pasos"] = "${pasos}"

        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
        ref.child(idAnuncioEditar)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                cargarImagenesStorage(idAnuncioEditar)
                val intent = Intent(this@CrearAnuncio, MainActivity::class.java)
                Toast.makeText(this, "Se actualizó la información del anuncio", Toast.LENGTH_SHORT).show()
                startActivity(intent)
                finishAffinity()
            }
            .addOnFailureListener{e->
                progressDialog.dismiss()
                Toast.makeText(this, "Falló la actualización debido a ${e}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun agregarAnuncio() {
        progressDialog.setMessage("Agregando anuncio")
        progressDialog.show()

        val tiempo = Consantes.obtenerTiempoDis()

        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
        val keyId = ref.push().key

        val hashMap = HashMap<String,Any>()
        hashMap["id"] = "${keyId}"
        hashMap["uid"] = "${firebaseAuth.uid}"
        hashMap["nombre"] = "${nombre}"
        hashMap["categoria"] = "${categoria}"
        hashMap["calorias"] = "${calorias}"
        hashMap["descripcion"] = "${descripcion}"
        hashMap["ingredientes"] = "${ingredientes}"
        hashMap["pasos"] = "${pasos}"
        hashMap["tiempo"] = tiempo
        hashMap["contadorVistas"] = 0
        hashMap["verificado"] = "no"


        ref.child(keyId!!)
            .setValue(hashMap)
            .addOnSuccessListener {
                cargarImagenesStorage(keyId)
            }
            .addOnFailureListener { e->
                Toast.makeText(
                    this,
                "${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun esAdmin() : String {
        val uidUsuario = firebaseAuth.currentUser?.uid
        var tipoUsuario = ""

        val ref = FirebaseDatabase.getInstance().getReference("Usuarios").child(uidUsuario!!)

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                tipoUsuario = snapshot.child("tipoUsuario").value.toString()
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
        println("tipo de usuario es ${tipoUsuario}")
        return tipoUsuario
    }

    private fun cargarImagenesStorage(keyId: String) {
        for (i in imagenSeleccionadaArrayList.indices) {
            val modeloImagenSeleccionada = imagenSeleccionadaArrayList[i]

            if (!modeloImagenSeleccionada.deInternet) {
                val nombreImagen = modeloImagenSeleccionada.id
                val rutaNombreImagen = "Anuncios/$nombreImagen"

                val storageRefernce = FirebaseStorage.getInstance().getReference(rutaNombreImagen)
                storageRefernce.putFile(modeloImagenSeleccionada.imagenUri!!)
                    .addOnSuccessListener {taskSnapshot->
                        val uriTask = taskSnapshot.storage.downloadUrl
                        while(!uriTask.isSuccessful);
                        val urlImagenCargada = uriTask.result

                        if(uriTask.isSuccessful) {
                            val hashmap = HashMap<String,Any>()
                            hashmap["id"] = "${modeloImagenSeleccionada.id}"
                            hashmap["imagenUrl"] = "${urlImagenCargada}"

                            val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
                            ref.child(keyId).child("Imagenes")
                                .child(nombreImagen)
                                .updateChildren(hashmap)
                        }

                        if (Edicion) {
                            progressDialog.dismiss()
                            val intent = Intent(this@CrearAnuncio, MainActivity::class.java)
                            startActivity(intent)
                            Toast.makeText(this, "Se actualizó la información del anuncio", Toast.LENGTH_SHORT).show()
                            finishAffinity()
                        } else {
                            progressDialog.dismiss()
                            Toast.makeText(this, "Se publicó su anuncio", Toast.LENGTH_SHORT).show()
                            limpiarCampos()
                        }

                    }
                    .addOnFailureListener{e->
                        Toast.makeText(
                            this, "${e.message}",Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        }
    }

    private fun limpiarCampos() {
        imagenSeleccionadaArrayList.clear()
        adapatadorImagenSeleccionada.notifyDataSetChanged()
        binding.EtNombre.setText("")
        binding.Categoria.setText("")
        binding.EtCalorias.setText("")
        binding.EtDescripcion.setText("")
        binding.EtIngredientes.setText("")
        binding.EtPasos.setText("")
    }

    private fun mostrarOpciones() {
        val popupMenu = PopupMenu(this, binding.agregarImagen)

        popupMenu.menu.add(Menu.NONE, 1, 1, "Cámara")
        popupMenu.menu.add(Menu.NONE, 2, 2, "Galería")

        popupMenu.show()

        popupMenu.setOnMenuItemClickListener { item ->
            val itemId = item.itemId
            if (itemId == 1) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    solicitarPermisoCamara.launch(arrayOf(android.Manifest.permission.CAMERA))
                } else {
                    solicitarPermisoCamara.launch(arrayOf(
                        android.Manifest.permission.CAMERA,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ))
                }
            } else if (itemId == 2) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    imagenGaleria()
                } else {
                    solicitarPermisoAlmacenamiento.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }
            true
        }
    }

    private val solicitarPermisoAlmacenamiento = registerForActivityResult(ActivityResultContracts.RequestPermission()) { esConcedido ->
        if (esConcedido) {
            imagenGaleria()
        } else {
            Toast.makeText(
                this,
                "El permiso del almacenamiento ha sido denegado",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun imagenGaleria() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        resultadoGaleria_ARL.launch(intent)
    }

    private val resultadoGaleria_ARL = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { resultado ->
        if (resultado.resultCode == Activity.RESULT_OK) {
            val data = resultado.data
            imagenUri = data!!.data

            val tiempo = "${Consantes.obtenerTiempoDis()}"
            val modeloImgSel = ModeloImagenSeleccionada(
                tiempo, imagenUri, null, false
            )
            imagenSeleccionadaArrayList.add(modeloImgSel)
            cargarImagenes()

        } else {
            Toast.makeText(
                this,
                "Cancelado",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private val solicitarPermisoCamara = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { resultado ->
        var todosConcedidos = true
        for (esConcedido in resultado.values) {
            todosConcedidos = todosConcedidos && esConcedido
        }

        if (todosConcedidos) {
            imagenCamara()
        } else {
            Toast.makeText(
                this,
                "El permiso de la camara o almacenamiento ha sido denegado, o ambas fueron denegados",
                Toast.LENGTH_SHORT
            ).show()
        }

    }

    private fun imagenCamara() {
        val contentValues = ContentValues()
        contentValues.put(MediaStore.Images.Media.TITLE, "Titulo_imagen")
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Descripcion_imagen")
        imagenUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imagenUri)
        resultadoCamara_ARL.launch(intent)

    }

    private val resultadoCamara_ARL = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { resultado ->
        if (resultado.resultCode == Activity.RESULT_OK) {

            val tiempo = "${Consantes.obtenerTiempoDis()}"
            val modeloImgSel = ModeloImagenSeleccionada(
                tiempo, imagenUri, null, false
            )
            imagenSeleccionadaArrayList.add(modeloImgSel)
            cargarImagenes()

        } else {
            Toast.makeText(
                this,
                "Cancelado",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun cargarImagenes() {
        adapatadorImagenSeleccionada = AdaptadorImagenSeleccionada(this, imagenSeleccionadaArrayList, idAnuncioEditar)
        binding.RVImagenes.adapter = adapatadorImagenSeleccionada
    }
}