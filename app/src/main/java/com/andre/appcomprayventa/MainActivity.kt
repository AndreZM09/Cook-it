package com.andre.appcomprayventa

import android.app.Dialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.andre.appcomprayventa.Fragmentos.FragmentChats
import com.andre.appcomprayventa.Fragmentos.FragmentCuenta
import com.andre.appcomprayventa.Fragmentos.FragmentInicio
import com.andre.appcomprayventa.Fragmentos.FragmentMisAnuncios
import com.andre.appcomprayventa.anuncios.CrearAnuncio
import com.andre.appcomprayventa.databinding.ActivityMainBinding
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var mContext: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mContext = this

        firebaseAuth = FirebaseAuth.getInstance()
        comprobarSesion()

        binding.TvLogin.setOnClickListener {
            startActivity(Intent(this, OpcionesLogin::class.java))
        }
        binding.TvRegistro.setOnClickListener {
            startActivity(Intent(this, Registro_email::class.java))
        }

        verFragmentInicio()

        binding.BottomNV.setOnItemSelectedListener { item->
            when(item.itemId){
                R.id.Item_Inicio->{
                    verFragmentInicio()
                    true
                }

                R.id.Item_Chats->{
                    verFragmentChats()
                    true
                }

                R.id.Item_Mis_Anuncions->{
                    if (firebaseAuth.currentUser != null) {
                        verFragmentMisAnucnios()
                    } else {
                        Consantes.mostrarCuadroDialogo(mContext)
                    }
                    true
                }

                R.id.Item_Cuenta->{
                    if (firebaseAuth.currentUser != null) {
                        verFragmentCuenta()
                    } else {
                        Consantes.mostrarCuadroDialogo(mContext)
                    }
                    true
                }

                else->{
                    false
                }
            }
        }

        binding.FAB.setOnClickListener {
            if (firebaseAuth.currentUser != null) {
                val intent = Intent(this, CrearAnuncio::class.java)
                intent.putExtra("Edicion", false)
                startActivity(intent)
            } else {
                Consantes.mostrarCuadroDialogo(mContext)

            }
        }

    }

    private fun comprobarSesion(){
        if(firebaseAuth.currentUser == null){
            binding.TvLogin.visibility = View.VISIBLE
            binding.TvSlash.visibility = View.VISIBLE
            binding.TvRegistro.visibility = View.VISIBLE
            verFragmentInicio()
        } else {
            binding.TvLogin.visibility = View.GONE
            binding.TvSlash.visibility = View.GONE
            binding.TvRegistro.visibility = View.GONE
        }
    }

    private fun verFragmentInicio(){
        binding.TituloRl.text = "Inicio"
        val fragment = FragmentInicio()
        val fragmentTransition = supportFragmentManager.beginTransaction()
        fragmentTransition.replace(binding.FragmentL1.id, fragment, "FragmentInicio")
        fragmentTransition.commit()

    }

    private fun verFragmentChats(){
        binding.TituloRl.text = "Chats"
        val fragment = FragmentChats()
        val fragmentTransition = supportFragmentManager.beginTransaction()
        fragmentTransition.replace(binding.FragmentL1.id, fragment, "FragmentChats")
        fragmentTransition.commit()

    }

    private fun verFragmentMisAnucnios(){
            binding.TituloRl.text = "Anuncios"
        val fragment = FragmentMisAnuncios()
        val fragmentTransition = supportFragmentManager.beginTransaction()
        fragmentTransition.replace(binding.FragmentL1.id, fragment, "FragmentAnuncion")
        fragmentTransition.commit()

    }

    private fun verFragmentCuenta(){
        binding.TituloRl.text = "Cuenta"
        val fragment = FragmentCuenta()
        val fragmentTransition = supportFragmentManager.beginTransaction()
        fragmentTransition.replace(binding.FragmentL1.id, fragment, "FragmentCuenta")
        fragmentTransition.commit()

    }
}