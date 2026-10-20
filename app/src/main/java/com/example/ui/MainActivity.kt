package com.example.ui

import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.R
import com.example.adapter.EmpresaAdapter
import com.example.databinding.ActivityMainBinding
import com.example.databinding.DialogEmpresaBinding
import com.example.db.DatabaseHelper
import com.example.model.Empresa
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * Actividad Principal de DevDirectory.
 * Gestiona el ciclo de vida, la interacción con SQLite vía DatabaseHelper,
 * los filtros combinados dinámicos (nombre y clasificación), y los diálogos de creación, edición y confirmación de borrado.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var empresaAdapter: EmpresaAdapter

    // Opciones del filtro de clasificación en la pantalla principal
    private val opcionesFiltro = listOf(
        "Todas",
        "Consultoría",
        "Desarrollo a la medida",
        "Fábrica de software"
    )

    // Opciones de clasificación para el formulario de alta/edición
    private val opcionesFormulario = listOf(
        "Consultoría",
        "Desarrollo a la medida",
        "Fábrica de software"
    )

    private var queryNombreActual: String = ""
    private var clasificacionFiltroActual: String = "Todas"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)

        configurarRecyclerView()
        configurarFiltros()
        configurarFab()

        // Carga inicial de datos
        aplicarFiltrosYActualizar()
    }

    private fun configurarRecyclerView() {
        empresaAdapter = EmpresaAdapter(
            listaEmpresas = mutableListOf(),
            onEditClick = { empresa ->
                mostrarDialogoFormulario(empresa)
            },
            onDeleteClick = { empresa ->
                mostrarDialogoConfirmarEliminacion(empresa)
            }
        )

        binding.rvEmpresas.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = empresaAdapter
            setHasFixedSize(false)
        }
    }

    private fun configurarFiltros() {
        // 1. Configurar Spinner de clasificación
        val spinnerAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            opcionesFiltro
        )
        binding.spFiltroClasificacion.adapter = spinnerAdapter

        binding.spFiltroClasificacion.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                clasificacionFiltroActual = opcionesFiltro[position]
                aplicarFiltrosYActualizar()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // 2. Configurar búsqueda en tiempo real por nombre
        binding.etBuscarNombre.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                queryNombreActual = s?.toString()?.trim() ?: ""
                aplicarFiltrosYActualizar()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun configurarFab() {
        binding.fabAgregarEmpresa.setOnClickListener {
            mostrarDialogoFormulario(empresaAEditar = null)
        }
    }

    /**
     * Consulta la base de datos aplicando los filtros activos y refresca la vista.
     */
    private fun aplicarFiltrosYActualizar() {
        val empresasFiltradas = dbHelper.filtrarEmpresas(queryNombreActual, clasificacionFiltroActual)
        empresaAdapter.actualizarLista(empresasFiltradas)

        // Actualizar contador del toolbar
        binding.tvTotalCount.text = resources.getQuantityString(
            R.plurals.empresas_count,
            empresasFiltradas.size,
            empresasFiltradas.size
        )

        // Controlar visibilidad del estado vacío
        if (empresasFiltradas.isEmpty()) {
            binding.rvEmpresas.visibility = View.GONE
            binding.layoutEmptyState.visibility = View.VISIBLE

            if (queryNombreActual.isNotBlank() || clasificacionFiltroActual != "Todas") {
                binding.tvEmptyTitle.text = getString(R.string.no_empresas_encontradas)
                binding.tvEmptySubtitle.text = "Intenta buscar con otro término o selecciona 'Todas' en clasificación."
            } else {
                binding.tvEmptyTitle.text = getString(R.string.no_empresas_registradas)
                binding.tvEmptySubtitle.text = "Usa el botón flotante (+) para registrar una nueva empresa."
            }
        } else {
            binding.rvEmpresas.visibility = View.VISIBLE
            binding.layoutEmptyState.visibility = View.GONE
        }
    }

    /**
     * Muestra el diálogo modal para crear una nueva empresa o editar una existente.
     * @param empresaAEditar Si es null, se abre en modo 'Crear'. Si contiene datos, se abre en modo 'Editar'.
     */
    private fun mostrarDialogoFormulario(empresaAEditar: Empresa?) {
        val dialogBinding = DialogEmpresaBinding.inflate(LayoutInflater.from(this))

        // Configurar el spinner del formulario
        val adapterFormulario = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            opcionesFormulario
        )
        dialogBinding.spClasificacionForm.adapter = adapterFormulario

        val esEdicion = empresaAEditar != null

        // Personalizar títulos
        if (esEdicion) {
            dialogBinding.tvDialogTitle.text = getString(R.string.dialog_title_edit)
            dialogBinding.btnGuardarDialog.text = "Actualizar"

            // Pre-llenar campos
            empresaAEditar?.let { emp ->
                dialogBinding.etNombre.setText(emp.nombre)
                dialogBinding.etUrlWeb.setText(emp.urlWeb)
                dialogBinding.etTelefono.setText(emp.telefono)
                dialogBinding.etEmail.setText(emp.email)
                dialogBinding.etProductosServicios.setText(emp.productosServicios)

                val indexClasif = opcionesFormulario.indexOfFirst {
                    it.equals(emp.clasificacion, ignoreCase = true)
                }
                if (indexClasif >= 0) {
                    dialogBinding.spClasificacionForm.setSelection(indexClasif)
                }
            }
        } else {
            dialogBinding.tvDialogTitle.text = getString(R.string.dialog_title_add)
            dialogBinding.btnGuardarDialog.text = getString(R.string.btn_save)
        }

        val alertDialog = MaterialAlertDialogBuilder(this)
            .setView(dialogBinding.root)
            .setCancelable(true)
            .create()

        dialogBinding.btnCancelarDialog.setOnClickListener {
            alertDialog.dismiss()
        }

        dialogBinding.btnGuardarDialog.setOnClickListener {
            val nombre = dialogBinding.etNombre.text?.toString()?.trim() ?: ""
            val urlWeb = dialogBinding.etUrlWeb.text?.toString()?.trim() ?: ""
            val telefono = dialogBinding.etTelefono.text?.toString()?.trim() ?: ""
            val email = dialogBinding.etEmail.text?.toString()?.trim() ?: ""
            val productos = dialogBinding.etProductosServicios.text?.toString()?.trim() ?: ""
            val clasificacion = dialogBinding.spClasificacionForm.selectedItem?.toString() ?: opcionesFormulario[0]

            // Validación de campo obligatorio
            if (nombre.isEmpty()) {
                dialogBinding.tilNombre.error = getString(R.string.error_empty_name)
                dialogBinding.etNombre.requestFocus()
                return@setOnClickListener
            } else {
                dialogBinding.tilNombre.error = null
            }

            if (esEdicion && empresaAEditar != null) {
                // Actualización
                val empresaActualizada = empresaAEditar.copy(
                    nombre = nombre,
                    urlWeb = urlWeb,
                    telefono = telefono,
                    email = email,
                    productosServicios = productos,
                    clasificacion = clasificacion
                )
                val filas = dbHelper.actualizarEmpresa(empresaActualizada)
                if (filas > 0) {
                    Toast.makeText(this, R.string.empresa_actualizada, Toast.LENGTH_SHORT).show()
                    aplicarFiltrosYActualizar()
                    alertDialog.dismiss()
                } else {
                    Toast.makeText(this, R.string.error_guardar, Toast.LENGTH_SHORT).show()
                }
            } else {
                // Creación
                val nuevaEmpresa = Empresa(
                    nombre = nombre,
                    urlWeb = urlWeb,
                    telefono = telefono,
                    email = email,
                    productosServicios = productos,
                    clasificacion = clasificacion
                )
                val idGenerado = dbHelper.insertarEmpresa(nuevaEmpresa)
                if (idGenerado > 0) {
                    Toast.makeText(this, R.string.empresa_guardada, Toast.LENGTH_SHORT).show()
                    aplicarFiltrosYActualizar()
                    alertDialog.dismiss()
                } else {
                    Toast.makeText(this, R.string.error_guardar, Toast.LENGTH_SHORT).show()
                }
            }
        }

        alertDialog.show()
    }

    /**
     * Muestra el cuadro de diálogo de confirmación para eliminar una empresa.
     * Requerimiento 4: MaterialAlertDialogBuilder con título, mensaje personalizado y botones Positivo y Negativo.
     */
    private fun mostrarDialogoConfirmarEliminacion(empresa: Empresa) {
        val mensajeFormateado = getString(R.string.delete_confirm_message, empresa.nombre)

        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete_confirm_title)
            .setMessage(mensajeFormateado)
            .setIcon(R.drawable.ic_delete)
            .setPositiveButton(R.string.delete_confirm_positive) { dialogInterface: DialogInterface, _: Int ->
                val filasEliminadas = dbHelper.eliminarEmpresa(empresa.id)
                if (filasEliminadas > 0) {
                    Toast.makeText(this, R.string.empresa_eliminada, Toast.LENGTH_SHORT).show()
                    aplicarFiltrosYActualizar()
                }
                dialogInterface.dismiss()
            }
            .setNegativeButton(R.string.btn_cancel) { dialogInterface: DialogInterface, _: Int ->
                dialogInterface.dismiss()
            }
            .create()

        dialog.show()

        // Colorear botón positivo en rojo para destacar acción destructiva
        dialog.getButton(DialogInterface.BUTTON_POSITIVE)?.setTextColor(
            getColor(R.color.color_delete)
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        dbHelper.close()
    }
}
