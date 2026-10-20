package com.example.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.R
import com.example.databinding.ItemEmpresaBinding
import com.example.model.Empresa

/**
 * Adapter para el RecyclerView de Empresas de Software.
 * Gestiona el enlace de datos (Data Binding), estilos de badges de clasificación
 * y los eventos de clic en Editar y Eliminar.
 */
class EmpresaAdapter(
    private var listaEmpresas: MutableList<Empresa> = mutableListOf(),
    private val onEditClick: (Empresa) -> Unit,
    private val onDeleteClick: (Empresa) -> Unit
) : RecyclerView.Adapter<EmpresaAdapter.EmpresaViewHolder>() {

    inner class EmpresaViewHolder(private val binding: ItemEmpresaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(empresa: Empresa) {
            val context = binding.root.context

            // 1. Nombre de la empresa
            binding.tvNombre.text = empresa.nombre

            // 2. Badge de Clasificación con colores específicos según guía
            val clasificacionNormalizada = empresa.clasificacion.trim().lowercase()
            when {
                clasificacionNormalizada.contains("consultor") -> {
                    binding.tvClasificacionBadge.text = empresa.clasificacion.ifBlank { "Consultoría" }
                    binding.tvClasificacionBadge.setBackgroundResource(R.drawable.bg_badge_consultoria)
                    binding.tvClasificacionBadge.setTextColor(
                        ContextCompat.getColor(context, R.color.badge_consultoria_text)
                    )
                }
                clasificacionNormalizada.contains("medida") -> {
                    binding.tvClasificacionBadge.text = empresa.clasificacion.ifBlank { "Desarrollo a la medida" }
                    binding.tvClasificacionBadge.setBackgroundResource(R.drawable.bg_badge_desarrollo)
                    binding.tvClasificacionBadge.setTextColor(
                        ContextCompat.getColor(context, R.color.badge_desarrollo_text)
                    )
                }
                clasificacionNormalizada.contains("fábrica") || clasificacionNormalizada.contains("fabrica") -> {
                    binding.tvClasificacionBadge.text = empresa.clasificacion.ifBlank { "Fábrica de software" }
                    binding.tvClasificacionBadge.setBackgroundResource(R.drawable.bg_badge_fabrica)
                    binding.tvClasificacionBadge.setTextColor(
                        ContextCompat.getColor(context, R.color.badge_fabrica_text)
                    )
                }
                else -> {
                    binding.tvClasificacionBadge.text = empresa.clasificacion.ifBlank { "General" }
                    binding.tvClasificacionBadge.setBackgroundResource(R.drawable.bg_badge_consultoria)
                    binding.tvClasificacionBadge.setTextColor(
                        ContextCompat.getColor(context, R.color.badge_consultoria_text)
                    )
                }
            }

            // 3. Teléfono
            if (empresa.telefono.isNotBlank()) {
                binding.layoutTelefono.visibility = View.VISIBLE
                binding.tvTelefono.text = empresa.telefono
            } else {
                binding.layoutTelefono.visibility = View.GONE
            }

            // 4. Sitio Web
            if (empresa.urlWeb.isNotBlank()) {
                binding.layoutWeb.visibility = View.VISIBLE
                binding.tvUrlWeb.text = empresa.urlWeb
            } else {
                binding.layoutWeb.visibility = View.GONE
            }

            // 5. Email
            if (empresa.email.isNotBlank()) {
                binding.layoutEmail.visibility = View.VISIBLE
                binding.tvEmail.text = empresa.email
            } else {
                binding.layoutEmail.visibility = View.GONE
            }

            // 6. Productos y Servicios
            if (empresa.productosServicios.isNotBlank()) {
                binding.tvProductosServicios.visibility = View.VISIBLE
                binding.tvProductosServicios.text = empresa.productosServicios
            } else {
                binding.tvProductosServicios.visibility = View.GONE
            }

            // 7. Eventos de los botones Editar y Eliminar
            binding.btnEditar.setOnClickListener {
                onEditClick(empresa)
            }

            binding.btnEliminar.setOnClickListener {
                onDeleteClick(empresa)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmpresaViewHolder {
        val binding = ItemEmpresaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return EmpresaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EmpresaViewHolder, position: Int) {
        holder.bind(listaEmpresas[position])
    }

    override fun getItemCount(): Int = listaEmpresas.size

    /**
     * Actualiza la lista interna con animación eficiente mediante DiffUtil.
     */
    fun actualizarLista(nuevaLista: List<Empresa>) {
        val diffCallback = EmpresaDiffCallback(this.listaEmpresas, nuevaLista)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        this.listaEmpresas.clear()
        this.listaEmpresas.addAll(nuevaLista)
        diffResult.dispatchUpdatesTo(this)
    }

    private class EmpresaDiffCallback(
        private val oldList: List<Empresa>,
        private val newList: List<Empresa>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}
