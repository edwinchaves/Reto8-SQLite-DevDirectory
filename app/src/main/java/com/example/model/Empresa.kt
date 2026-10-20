package com.example.model

/**
 * Modelo de datos que representa una Empresa de Software en el directorio.
 *
 * @property id Identificador único autoincremental de la empresa.
 * @property nombre Nombre de la empresa (Obligatorio).
 * @property urlWeb Dirección URL del sitio web.
 * @property telefono Teléfono de contacto.
 * @property email Correo electrónico corporativo.
 * @property productosServicios Descripción de los productos y servicios ofrecidos.
 * @property clasificacion Categoría de la empresa ("Consultoría", "Desarrollo a la medida", "Fábrica de software").
 */
data class Empresa(
    val id: Int = 0,
    val nombre: String,
    val urlWeb: String = "",
    val telefono: String = "",
    val email: String = "",
    val productosServicios: String = "",
    val clasificacion: String = ""
)
