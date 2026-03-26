package com.bingo.manager.presentation.ui.inventario

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bingo.manager.R
import com.bingo.manager.domain.model.InventarioItem
import com.bingo.manager.presentation.viewmodel.InventarioViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InventarioFragment : Fragment() {

    private val vm: InventarioViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_inventario, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerInventario)
        val fab = view.findViewById<FloatingActionButton>(R.id.fabInventario)
        val tvEmpty = view.findViewById<TextView>(R.id.tvEmptyInventario)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        vm.inventario.observe(viewLifecycleOwner) { lista ->
            tvEmpty.visibility = if (lista.isEmpty()) View.VISIBLE else View.GONE
            recyclerView.adapter = InventarioAdapter(lista,
                onEdit = { showDialog(it) },
                onDelete = {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Eliminar").setMessage("¿Eliminar este stock?")
                        .setPositiveButton("Eliminar") { _, _ -> vm.eliminar(it.id) }
                        .setNegativeButton("Cancelar", null).show()
                }
            )
        }

        fab.setOnClickListener { showDialog(null) }
    }

    private fun showDialog(item: InventarioItem?) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_inventario, null)
        val spinnerProducto = dialogView.findViewById<Spinner>(R.id.spinnerProductoInv)
        val etStock = dialogView.findViewById<TextInputEditText>(R.id.etStock)

        val productos = vm.productos.value ?: emptyList()
        val nombres = productos.map { it.nombre }
        spinnerProducto.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, nombres)
            .also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        item?.let {
            val pos = nombres.indexOfFirst { n -> n == it.nombreProducto }
            if (pos >= 0) spinnerProducto.setSelection(pos)
            etStock.setText(it.stockDisponible.toString())
        }

        AlertDialog.Builder(requireContext())
            .setTitle(if (item == null) "Agregar Stock" else "Editar Stock")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val posSelected = spinnerProducto.selectedItemPosition
                if (posSelected < 0 || posSelected >= productos.size) return@setPositiveButton
                val prod = productos[posSelected]
                val stock = etStock.text.toString().toDoubleOrNull() ?: 0.0
                vm.guardar(InventarioItem(
                    id = item?.id ?: 0L,
                    productoId = prod.id,
                    nombreProducto = prod.nombre,
                    stockDisponible = stock,
                    unidad = prod.unidad
                ))
            }
            .setNegativeButton("Cancelar", null).show()
    }
}

class InventarioAdapter(
    private val items: List<InventarioItem>,
    private val onEdit: (InventarioItem) -> Unit,
    private val onDelete: (InventarioItem) -> Unit
) : RecyclerView.Adapter<InventarioAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombre)
        val tvDetalle: TextView = view.findViewById(R.id.tvDetalle)
        val btnEdit: ImageButton = view.findViewById(R.id.btnEdit)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_producto, parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.tvNombre.text = item.nombreProducto
        holder.tvDetalle.text = "Stock: ${item.stockDisponible} ${item.unidad}"
        holder.btnEdit.setOnClickListener { onEdit(item) }
        holder.btnDelete.setOnClickListener { onDelete(item) }
    }

    override fun getItemCount() = items.size
}
