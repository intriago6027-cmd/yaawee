package com.bingo.manager.presentation.ui.gastos

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bingo.manager.R
import com.bingo.manager.domain.model.Gasto
import com.bingo.manager.presentation.viewmodel.GastosViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import java.text.NumberFormat
import java.util.Locale

@AndroidEntryPoint
class GastosFragment : Fragment() {

    private val vm: GastosViewModel by viewModels()
    private val currency = NumberFormat.getCurrencyInstance(Locale("es", "CO"))

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_gastos, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerGastos)
        val fab = view.findViewById<FloatingActionButton>(R.id.fabGasto)
        val tvTotal = view.findViewById<TextView>(R.id.tvTotalGastos)
        val tvEmpty = view.findViewById<TextView>(R.id.tvEmptyGastos)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        vm.gastos.observe(viewLifecycleOwner) { lista ->
            tvEmpty.visibility = if (lista.isEmpty()) View.VISIBLE else View.GONE
            recyclerView.adapter = GastoAdapter(lista, currency,
                onEdit = { showDialog(it) },
                onDelete = {
                    AlertDialog.Builder(requireContext())
                        .setMessage("¿Eliminar este gasto?")
                        .setPositiveButton("Eliminar") { _, _ -> vm.eliminar(it.id) }
                        .setNegativeButton("Cancelar", null).show()
                }
            )
        }

        vm.total.observe(viewLifecycleOwner) { total ->
            tvTotal.text = "Total Viáticos: ${currency.format(total ?: 0.0)}"
        }

        fab.setOnClickListener { showDialog(null) }
    }

    private fun showDialog(gasto: Gasto?) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_gasto, null)
        val etDesc = dialogView.findViewById<TextInputEditText>(R.id.etDescGasto)
        val etPrecio = dialogView.findViewById<TextInputEditText>(R.id.etPrecioGasto)

        gasto?.let {
            etDesc.setText(it.descripcion)
            etPrecio.setText(it.precio.toString())
        }

        AlertDialog.Builder(requireContext())
            .setTitle(if (gasto == null) "Nuevo Gasto" else "Editar Gasto")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val desc = etDesc.text.toString().trim()
                val precio = etPrecio.text.toString().toDoubleOrNull() ?: 0.0
                if (desc.isEmpty()) {
                    Toast.makeText(requireContext(), "Descripción obligatoria", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                vm.guardar(Gasto(id = gasto?.id ?: 0L, descripcion = desc, precio = precio))
            }
            .setNegativeButton("Cancelar", null).show()
    }
}

class GastoAdapter(
    private val items: List<Gasto>,
    private val currency: NumberFormat,
    private val onEdit: (Gasto) -> Unit,
    private val onDelete: (Gasto) -> Unit
) : RecyclerView.Adapter<GastoAdapter.VH>() {

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
        holder.tvNombre.text = item.descripcion
        holder.tvDetalle.text = currency.format(item.precio)
        holder.btnEdit.setOnClickListener { onEdit(item) }
        holder.btnDelete.setOnClickListener { onDelete(item) }
    }

    override fun getItemCount() = items.size
}
