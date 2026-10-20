package com.example.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.model.Empresa

/**
 * Gestor de persistencia SQLite para el directorio de empresas de software.
 * Cumple con los requerimientos del Reto 8: Almacenamiento en Android con SQLite.
 */
class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "empresas_db"
        const val DATABASE_VERSION = 1

        const val TABLE_EMPRESAS = "empresas"
        const val COLUMN_ID = "id"
        const val COLUMN_NOMBRE = "nombre"
        const val COLUMN_URL_WEB = "url_web"
        const val COLUMN_TELEFONO = "telefono"
        const val COLUMN_EMAIL = "email"
        const val COLUMN_PRODUCTOS_SERVICIOS = "productos_servicios"
        const val COLUMN_CLASIFICACION = "clasificacion"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE $TABLE_EMPRESAS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NOMBRE TEXT NOT NULL,
                $COLUMN_URL_WEB TEXT,
                $COLUMN_TELEFONO TEXT,
                $COLUMN_EMAIL TEXT,
                $COLUMN_PRODUCTOS_SERVICIOS TEXT,
                $COLUMN_CLASIFICACION TEXT
            )
        """.trimIndent()
        db.execSQL(createTableQuery)

        // Insertar datos de prueba iniciales para enriquecer la experiencia de uso
        insertarDatosIniciales(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_EMPRESAS")
        onCreate(db)
    }

    private fun insertarDatosIniciales(db: SQLiteDatabase) {
        val empresasDemo = listOf(
            Empresa(
                nombre = "SoftTech Solutions",
                urlWeb = "https://softtech-solutions.com",
                telefono = "+57 310 456 7890",
                email = "contacto@softtech.com",
                productosServicios = "Arquitectura Cloud, Auditoría de Sistemas y Estrategia Digital",
                clasificacion = "Consultoría"
            ),
            Empresa(
                nombre = "AppNova Studio",
                urlWeb = "https://appnova.dev",
                telefono = "+57 315 987 6543",
                email = "hola@appnova.dev",
                productosServicios = "Desarrollo de Apps Nativas Android / iOS y Plataformas Web a la Medida",
                clasificacion = "Desarrollo a la medida"
            ),
            Empresa(
                nombre = "ByteFactory Labs",
                urlWeb = "https://bytefactorylabs.io",
                telefono = "+57 301 222 3344",
                email = "projects@bytefactory.io",
                productosServicios = "Sourcing de Células Ágiles, QA Automatizado y Mantenimiento de Software",
                clasificacion = "Fábrica de software"
            ),
            Empresa(
                nombre = "CloudNexus Advisors",
                urlWeb = "https://cloudnexus.co",
                telefono = "+57 312 888 9900",
                email = "info@cloudnexus.co",
                productosServicios = "Consultoría en Ciberseguridad, Migración a AWS/GCP y DevOps",
                clasificacion = "Consultoría"
            )
        )

        for (empresa in empresasDemo) {
            val values = ContentValues().apply {
                put(COLUMN_NOMBRE, empresa.nombre)
                put(COLUMN_URL_WEB, empresa.urlWeb)
                put(COLUMN_TELEFONO, empresa.telefono)
                put(COLUMN_EMAIL, empresa.email)
                put(COLUMN_PRODUCTOS_SERVICIOS, empresa.productosServicios)
                put(COLUMN_CLASIFICACION, empresa.clasificacion)
            }
            db.insert(TABLE_EMPRESAS, null, values)
        }
    }

    /**
     * Inserta una nueva empresa en la base de datos.
     * @param empresa Objeto Empresa con los datos a persistir.
     * @return El ID de la fila insertada o -1 si ocurrió un error.
     */
    fun insertarEmpresa(empresa: Empresa): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NOMBRE, empresa.nombre.trim())
            put(COLUMN_URL_WEB, empresa.urlWeb.trim())
            put(COLUMN_TELEFONO, empresa.telefono.trim())
            put(COLUMN_EMAIL, empresa.email.trim())
            put(COLUMN_PRODUCTOS_SERVICIOS, empresa.productosServicios.trim())
            put(COLUMN_CLASIFICACION, empresa.clasificacion.trim())
        }
        return db.insert(TABLE_EMPRESAS, null, values)
    }

    /**
     * Obtiene el listado completo de todas las empresas registradas.
     * @return Lista de objetos Empresa ordenados por ID descendente.
     */
    fun obtenerTodasLasEmpresas(): List<Empresa> {
        val lista = mutableListOf<Empresa>()
        val db = readableDatabase
        val cursor: Cursor = db.query(
            TABLE_EMPRESAS,
            null,
            null,
            null,
            null,
            null,
            "$COLUMN_ID DESC"
        )

        cursor.use {
            if (it.moveToFirst()) {
                do {
                    lista.add(cursorToEmpresa(it))
                } while (it.moveToNext())
            }
        }
        return lista
    }

    /**
     * Busca y obtiene una empresa por su ID primario.
     * @param id Identificador numérico de la empresa.
     * @return Empresa si se encuentra, o null en caso contrario.
     */
    fun obtenerEmpresaPorId(id: Int): Empresa? {
        val db = readableDatabase
        val cursor: Cursor = db.query(
            TABLE_EMPRESAS,
            null,
            "$COLUMN_ID = ?",
            arrayOf(id.toString()),
            null,
            null,
            null
        )

        return cursor.use {
            if (it.moveToFirst()) {
                cursorToEmpresa(it)
            } else {
                null
            }
        }
    }

    /**
     * Actualiza la información de una empresa existente.
     * @param empresa Objeto Empresa con los datos actualizados y su ID correspondiente.
     * @return Número de filas afectadas en la base de datos.
     */
    fun actualizarEmpresa(empresa: Empresa): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NOMBRE, empresa.nombre.trim())
            put(COLUMN_URL_WEB, empresa.urlWeb.trim())
            put(COLUMN_TELEFONO, empresa.telefono.trim())
            put(COLUMN_EMAIL, empresa.email.trim())
            put(COLUMN_PRODUCTOS_SERVICIOS, empresa.productosServicios.trim())
            put(COLUMN_CLASIFICACION, empresa.clasificacion.trim())
        }
        return db.update(
            TABLE_EMPRESAS,
            values,
            "$COLUMN_ID = ?",
            arrayOf(empresa.id.toString())
        )
    }

    /**
     * Elimina una empresa por su identificador único.
     * @param id Identificador de la empresa a eliminar.
     * @return Número de filas eliminadas.
     */
    fun eliminarEmpresa(id: Int): Int {
        val db = writableDatabase
        return db.delete(
            TABLE_EMPRESAS,
            "$COLUMN_ID = ?",
            arrayOf(id.toString())
        )
    }

    /**
     * Filtra empresas dinámicamente según un término de búsqueda en el nombre y/o una clasificación.
     * Construye consultas dinámicas usando SQL WHERE con 'LIKE' y '=' según los filtros activos.
     *
     * @param nombreQuery Texto para filtrar por nombre (LIKE %texto%).
     * @param clasificacionFiltro Categoría específica ("Consultoría", "Desarrollo a la medida", etc.) o "Todas"/null para ignorar.
     * @return Lista de empresas que cumplen con los criterios de filtrado.
     */
    fun filtrarEmpresas(nombreQuery: String?, clasificacionFiltro: String?): List<Empresa> {
        val db = readableDatabase
        val conditions = mutableListOf<String>()
        val args = mutableListOf<String>()

        // Filtro por nombre con LIKE
        if (!nombreQuery.isNullOrBlank()) {
            conditions.add("$COLUMN_NOMBRE LIKE ?")
            args.add("%${nombreQuery.trim()}%")
        }

        // Filtro por clasificación con =
        if (!clasificacionFiltro.isNullOrBlank() && !clasificacionFiltro.equals("Todas", ignoreCase = true)) {
            conditions.add("$COLUMN_CLASIFICACION = ?")
            args.add(clasificacionFiltro.trim())
        }

        val selection = if (conditions.isNotEmpty()) {
            conditions.joinToString(" AND ")
        } else {
            null
        }

        val selectionArgs = if (args.isNotEmpty()) {
            args.toTypedArray()
        } else {
            null
        }

        val cursor: Cursor = db.query(
            TABLE_EMPRESAS,
            null,
            selection,
            selectionArgs,
            null,
            null,
            "$COLUMN_ID DESC"
        )

        val lista = mutableListOf<Empresa>()
        cursor.use {
            if (it.moveToFirst()) {
                do {
                    lista.add(cursorToEmpresa(it))
                } while (it.moveToNext())
            }
        }
        return lista
    }

    private fun cursorToEmpresa(cursor: Cursor): Empresa {
        val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
        val nombre = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOMBRE))
        val urlWeb = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_URL_WEB)) ?: ""
        val telefono = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TELEFONO)) ?: ""
        val email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)) ?: ""
        val productosServicios = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCTOS_SERVICIOS)) ?: ""
        val clasificacion = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CLASIFICACION)) ?: ""

        return Empresa(
            id = id,
            nombre = nombre,
            urlWeb = urlWeb,
            telefono = telefono,
            email = email,
            productosServicios = productosServicios,
            clasificacion = clasificacion
        )
    }
}
