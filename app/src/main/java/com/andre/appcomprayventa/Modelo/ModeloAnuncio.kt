package com.andre.appcomprayventa.Modelo

class ModeloAnuncio {
    var id : String = ""
    var uid : String = ""
    var categoria : String = ""
    var nombre : String = ""
    var calorias : String = ""
    var descripcion : String = ""
    var ingredientes : String = ""
    var pasos : String = ""
    var tiempo : Long = 0
    var favorito : Boolean = false
    var contadorVistas : Int = 0
    var verificado : String = "no"

    constructor()
    constructor(
        id: String,
        uid: String,
        categoria: String,
        nombre: String,
        calorias: String,
        descripcion: String,
        ingredientes: String,
        pasos: String,
        tiempo: Long,
        favorito: Boolean,
        contadorVistas: Int,
        verificado: String
    ) {
        this.id = id
        this.uid = uid
        this.categoria = categoria
        this.nombre = nombre
        this.calorias = calorias
        this.descripcion = descripcion
        this.ingredientes = ingredientes
        this.pasos = pasos
        this.tiempo = tiempo
        this.favorito = favorito
        this.contadorVistas = contadorVistas
        this.verificado = verificado
    }

}