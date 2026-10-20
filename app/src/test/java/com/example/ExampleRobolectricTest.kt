package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.db.DatabaseHelper
import com.example.model.Empresa
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var context: Context
    private lateinit var dbHelper: DatabaseHelper

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        dbHelper = DatabaseHelper(context)
    }

    @After
    fun tearDown() {
        dbHelper.close()
    }

    @Test
    fun `verificar nombre de aplicacion`() {
        val appName = context.getString(R.string.app_name)
        assertEquals("DevDirectory", appName)
    }

    @Test
    fun `verificar operaciones CRUD de SQLite en DatabaseHelper`() {
        // 1. Insertar nueva empresa
        val nuevaEmpresa = Empresa(
            nombre = "Prueba Tech SAS",
            urlWeb = "https://pruebatech.com",
            telefono = "3001234567",
            email = "info@pruebatech.com",
            productosServicios = "Sistemas embebidos y consultoría",
            clasificacion = "Consultoría"
        )
        val idGenerado = dbHelper.insertarEmpresa(nuevaEmpresa)
        assertTrue("El ID generado debe ser mayor que 0", idGenerado > 0)

        // 2. Obtener por ID
        val empresaObtenida = dbHelper.obtenerEmpresaPorId(idGenerado.toInt())
        assertNotNull("La empresa debe existir en la BD", empresaObtenida)
        assertEquals("Prueba Tech SAS", empresaObtenida?.nombre)
        assertEquals("Consultoría", empresaObtenida?.clasificacion)

        // 3. Actualizar
        val empresaModificada = empresaObtenida!!.copy(
            nombre = "Prueba Tech Modificada",
            clasificacion = "Fábrica de software"
        )
        val filasActualizadas = dbHelper.actualizarEmpresa(empresaModificada)
        assertEquals(1, filasActualizadas)

        val empresaVerificada = dbHelper.obtenerEmpresaPorId(idGenerado.toInt())
        assertEquals("Prueba Tech Modificada", empresaVerificada?.nombre)
        assertEquals("Fábrica de software", empresaVerificada?.clasificacion)

        // 4. Filtrar por nombre y clasificación
        val filtradasPorNombre = dbHelper.filtrarEmpresas("Modificada", "Todas")
        assertTrue(filtradasPorNombre.any { it.id == idGenerado.toInt() })

        val filtradasPorClasificacion = dbHelper.filtrarEmpresas(null, "Fábrica de software")
        assertTrue(filtradasPorClasificacion.any { it.id == idGenerado.toInt() })

        // 5. Eliminar
        val filasEliminadas = dbHelper.eliminarEmpresa(idGenerado.toInt())
        assertEquals(1, filasEliminadas)

        val empresaEliminada = dbHelper.obtenerEmpresaPorId(idGenerado.toInt())
        assertNull("La empresa eliminada ya no debe existir", empresaEliminada)
    }
}
