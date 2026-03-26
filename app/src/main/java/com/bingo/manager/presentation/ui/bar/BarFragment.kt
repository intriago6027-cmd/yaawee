package com.bingo.manager.presentation.ui.bar

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bingo.manager.R
import com.bingo.manager.domain.model.BarItem
import com.bingo.manager.presentation.viewmodel.BarViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import java.text.NumberFormat
import java.util.Locale

@AndroidEntryPoint
class BarFragment : Fragment() {

    private val vm: BarViewModel by viewModels()
    private val currency = NumberFormat.getCurrencyInstance(Locale("es", "CO"))

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_bar, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerBar)
        val fab = view.findViewById<FloatingActionButton>(R.id.fabBar)
        val tvTotal = view.findViewById<TextView>(R.id.tvTotalBar)
        val tvEmpty = view.findViewById<TextView>(R.id.tvEmptyBar)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        vm.barItems.observe(viewLifecycleOwner) { lista ->
            tvEmpty.visibility = if (lista.isEmpty()) View.VISIBLE else View.GONE
            recyclerView.adapter = BarAdapter(lista, currency,
                onEdit = { showDialog(it) },
                onDelete = {
                    AlertDialog.Builder(requireContext())
                        .setMessage("¿Eliminar este item del bar?")
                        .setPositiveButton("Eliminar") { _, _ -> vm.eliminar(it.id) }
                        .setNegativeButton("Cancelar", null).show()
                }
            )
        }

        vm.total.observe(viewLifecycleOwner) { total ->
            tvTotal.text = "Total Bar: ${currency.format(total ?: 0.0)}"
        }

        fab.setOnClickListener { showDialog(null) }
    }

    private fun showDialog(barItem: BarItem?) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_bar, null)
        val spinnerProducto = dialogView.findViewById<Spinner>(R.id.spinnerProductoBar)
        val etPrecio = dialogView.findViewById<TextInputEditText>(R.id.etPrecioBar)
        val etCantidad = dialogView.findViewById<TextInputEditText>(R.id.etCantidadBar)

        val productos = vm.productos.value ?: emptyList()
        val nombres = productos.map { it.nombre }
        spinnerProducto.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, nombres)
            .also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        spinnerProducto.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                if (barItem == null) etPrecio.setText(productos[pos].precio.toString())
            }
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }

        barItem?.let {
            val pos = nombres.indexOf(it.nombre)
            if (pos >= 0) spinnerProducto.setSelection(pos)
            etPrecio.setText(it.precio.toString())
            etCantidad.setText(it.cantidad.toString())
        }

        AlertDialog.Builder(requireContext())
            .setTitle(if (barItem == null) "Agregar al Bar" else "Editar Item Bar")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val posSelected = spinnerProducto.selectedItemPosition
                if (posSelected < 0 || posSelected >= productos.size) return@setPositiveButton
                val prod = productos[posSelected]
                val precio = etPrecio.text.toString().toDoubleOrNull() ?: prod.precio
                val cantidad = etCantidad.text.toString().toDoubleOrNull() ?: 1.0
                vm.guardar(BarItem(
                    id = barItem?.id ?: 0L,
                    productoId = prod.id,
                    nombre = prod.nombre,
                    precio = precio,
                    cantidad = cantidad
                ))
            }
            .setNegativeButton("Cancelar", null).show()
    }
}

class BarAdapter(
    private val items: List<BarItem>,
    private val currency: NumberFormat,
    private val onEdit: (BarItem) -> Unit,
    private val onDelete: (BarItem) -> Unit
) : RecyclerView.Adapter<BarAdapter.VH>() {

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
        holder.tvNombre.text = item.nombre
        holder.tvDetalle.text = "${item.cantidad} × ${currency.format(item.precio)} = ${currency.format(item.cantidad * item.precio)}"
        holder.btnEdit.setOnClickListener { onEdit(item) }
        holder.btnDelete.setOnClickListener { onDelete(item) }
    }

    override fun getItemCount() = items.size
}
