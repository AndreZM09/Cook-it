package com.andre.appcomprayventa.Modelo

class ModeloComentario {

    var id = ""
    var idAnuncio = ""
    var tiempo = ""
    var uid = ""
    var comentario = ""
    var idUsuarioAnuncio = ""

    constructor()
    constructor(id: String, idAnuncio: String, tiempo: String, uid: String, comentario: String, idUsuarioAnuncio:String) {
        this.id = id
        this.idAnuncio = idAnuncio
        this.tiempo = tiempo
        this.uid = uid
        this.comentario = comentario
        this.idUsuarioAnuncio = idUsuarioAnuncio
    }

}