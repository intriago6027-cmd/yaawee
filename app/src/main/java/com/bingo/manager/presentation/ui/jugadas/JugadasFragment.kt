package com.bingo.manager.presentation.ui.jugadas

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bingo.manager.R
import com.bingo.manager.data.local.entities.JugadaEntity
import com.bingo.manager.domain.model.*
import com.bingo.manager.presentation.viewmodel.JugadaViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import java.text.NumberFormat
import java.util.Locale

@AndroidEntryPoint
class JugadasFragment : Fragment() {

    private val vm: JugadaViewModel by viewModels()
    private val currency = NumberFormat.getCurrencyInstance(Locale("es", "CO"))

    private lateinit var tabLayout: TabLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var fab: FloatingActionButton
    private lateinit var tvEmpty: TextView

    private val tiposTabs = listOf(
        TipoJugada.REGALADITA to "🎁 Regaladitas",
        TipoJugada.NORMAL to "🎯 Normales",
        TipoJugada.ADICIONAL to "➕ Adicionales",
        TipoJugada.SORTEO_REGALADITA to "🎰 Sorteo Regalo",
        TipoJugada.SORTEO_TICKET to "🎟 Sorteo Ticket",
        TipoJugada.ACUMULADA to "💰 Acumulada",
        TipoJugada.PUNTUALIDAD to "⏰ Puntualidad",
        TipoJugada.COMPARTIDO to "🤝 Compartido",
        TipoJugada.ABECEDARIO to "🔤 Abecedario"
    )

    private var currentTipo = TipoJugada.REGALADITA

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_jugadas, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tabLayout = view.findViewById(R.id.tabLayoutJugadas)
        recyclerView = view.findViewById(R.id.recyclerViewJugadas)
        fab = view.findViewById(R.id.fabJugada)
        tvEmpty = view.findViewById(R.id.tvEmptyJugadas)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        tiposTabs.forEach { (_, label) -> tabLayout.addTab(tabLayout.newTab().setText(label)) }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                currentTipo = tiposTabs[tab.position].first
                observeJugadasByTipo(currentTipo)
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        observeJugadasByTipo(currentTipo)

        fab.setOnClickListener { showJugadaDialog(null, currentTipo) }
    }

    private fun observeJugadasByTipo(tipo: String) {
        vm.getByTipoLive(tipo).observe(viewLifecycleOwner) { lista ->
            tvEmpty.visibility = if (lista.isEmpty()) View.VISIBLE else View.GONE
            recyclerView.adapter = JugadaAdapter(lista, currency,
                onEdit = { showJugadaDialog(it.id, tipo) },
                onDelete = { confirmarEliminar { vm.eliminar(it.id) } }
            )
        }
        fab.setOnClickListener { showJugadaDialog(null, tipo) }
    }

    private fun showJugadaDialog(id: Long?, tipo: String) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_jugada, null)
        val etNombre = dialogView.findViewById<TextInputEditText>(R.id.etNombreJugada)
        val etNumero = dialogView.findViewById<TextInputEditText>(R.id.etNumeroJugada)
        val spinnerSubTipo = dialogView.findViewById<Spinner>(R.id.spinnerSubTipo)
        val llSubTipo = dialogView.findViewById<View>(R.id.llSubTipo)
        val llItems = dialogView.findViewById<LinearLayout>(R.id.llItemsJugada)
        val btnAgregar = dialogView.findViewById<Button>(R.id.btnAgregarItemJugada)
        val tvTotal = dialogView.findViewById<TextView>(R.id.tvTotalJugada)

        val productos = vm.productos.value ?: emptyList()
        val itemsSeleccionados = mutableListOf<ItemSeleccionado>()

        // Subtipo solo para NORMALES
        if (tipo == TipoJugada.NORMAL) {
            llSubTipo.visibility = View.VISIBLE
            val subtipos = listOf(
                SubTipoJugadaNormal.BINGO_LOCO, SubTipoJugadaNormal.LINEA_4,
                SubTipoJugadaNormal.LINEA_5, SubTipoJugadaNormal.CUATRO_ESQUINAS,
                SubTipoJugadaNormal.LETRA, SubTipoJugadaNormal.TABLA_LLENA
            )
            spinnerSubTipo.adapter = ArrayAdapter(requireContext(),
                android.R.layout.simple_spinner_item, subtipos)
                .also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        } else {
            llSubTipo.visibility = View.GONE
        }

        fun recalcTotal() {
            val total = itemsSeleccionados.sumOf { it.precioEditable * it.cantidad }
            tvTotal.text = "Total: ${currency.format(total)}"
        }

        fun agregarItemRow(item: ItemSeleccionado? = null) {
            val row = LayoutInflater.from(requireContext()).inflate(R.layout.item_canasta_row, llItems, false)
            val spinner = row.findViewById<Spinner>(R.id.spinnerProducto)
            val etCantidad = row.findViewById<TextInputEditText>(R.id.etCantidadItem)
            val etPrecio = row.findViewById<TextInputEditText>(R.id.etPrecioItem)
            val btnRemove = row.findViewById<ImageButton>(R.id.btnRemoveItem)

            val nombres = productos.map { it.nombre }
            spinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, nombres)
                .also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

            val placeholder = ItemSeleccionado(
                productoId = productos.firstOrNull()?.id ?: 0,
                nombre = productos.firstOrNull()?.nombre ?: "",
                cantidad = item?.cantidad ?: 1.0,
                unidad = productos.firstOrNull()?.unidad ?: "",
                lugarCompra = productos.firstOrNull()?.lugarCompra ?: "",
                precioEditable = item?.precioEditable ?: productos.firstOrNull()?.precio ?: 0.0,
                parentType = "JUGADA",
                parentId = id ?: 0
            )
            itemsSeleccionados.add(placeholder)
            val idx = itemsSeleccionados.lastIndex

            item?.let {
                val pos = nombres.indexOf(it.nombre)
                if (pos >= 0) spinner.setSelection(pos)
                etCantidad.setText(it.cantidad.toString())
                etPrecio.setText(it.precioEditable.toString())
            }

            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, i: Long) {
                    val prod = productos[pos]
                    itemsSeleccionados[idx] = itemsSeleccionados[idx].copy(
                        productoId = prod.id, nombre = prod.nombre,
                        unidad = prod.unidad, lugarCompra = prod.lugarCompra,
                        precioEditable = prod.precio
                    )
                    etPrecio.setText(prod.precio.toString())
                    recalcTotal()
                }
                override fun onNothingSelected(p: AdapterView<*>?) {}
            }

            etCantidad.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    itemsSeleccionados[idx] = itemsSeleccionados[idx].copy(
                        cantidad = etCantidad.text.toString().toDoubleOrNull() ?: 1.0)
                    recalcTotal()
                }
            }
            etPrecio.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    itemsSeleccionados[idx] = itemsSeleccionados[idx].copy(
                        precioEditable = etPrecio.text.toString().toDoubleOrNull() ?: 0.0)
                    recalcTotal()
                }
            }
            btnRemove.setOnClickListener {
                itemsSeleccionados.removeAt(idx)
                llItems.removeView(row)
                recalcTotal()
            }
            llItems.addView(row)
        }

        if (id != null) {
            vm.cargarJugada(id)
            vm.jugadaActual.observe(viewLifecycleOwner) { jugada ->
                jugada ?: return@observe
                etNombre.setText(jugada.nombre)
                etNumero.setText(jugada.numero.toString())
                if (tipo == TipoJugada.NORMAL) {
                    val subtipos = listOf(SubTipoJugadaNormal.BINGO_LOCO, SubTipoJugadaNormal.LINEA_4,
                        SubTipoJugadaNormal.LINEA_5, SubTipoJugadaNormal.CUATRO_ESQUINAS,
                        SubTipoJugadaNormal.LETRA, SubTipoJugadaNormal.TABLA_LLENA)
                    spinnerSubTipo.setSelection(subtipos.indexOf(jugada.subTipo).coerceAtLeast(0))
                }
                jugada.items.forEach { agregarItemRow(it) }
                recalcTotal()
            }
        }

        btnAgregar.setOnClickListener {
            if (productos.isEmpty()) {
                Toast.makeText(requireContext(), "Primero agrega productos base", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            agregarItemRow()
        }

        AlertDialog.Builder(requireContext())
            .setTitle("${tiposTabs.find { it.first == tipo }?.second ?: tipo}")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val nombre = etNombre.text.toString().trim().ifEmpty { "$tipo ${etNumero.text}" }
                val numero = etNumero.text.toString().toIntOrNull() ?: 0
                val subTipo = if (tipo == TipoJugada.NORMAL) spinnerSubTipo.selectedItem.toString() else ""
                vm.guardar(
                    Jugada(id = id ?: 0L, tipo = tipo, nombre = nombre,
                        numero = numero, subTipo = subTipo, items = itemsSeleccionados.toList())
                )
                Toast.makeText(requireContext(), "Jugada guardada", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun confirmarEliminar(onConfirm: () -> Unit) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar jugada")
            .setMessage("¿Eliminar esta jugada?")
            .setPositiveButton("Eliminar") { _, _ -> onConfirm() }
            .setNegativeButton("Cancelar", null).show()
    }
}

class JugadaAdapter(
    private val items: List<JugadaEntity>,
    private val currency: NumberFormat,
    private val onEdit: (JugadaEntity) -> Unit,
    private val onDelete: (JugadaEntity) -> Unit
) : RecyclerView.Adapter<JugadaAdapter.VH>() {

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
        holder.tvNombre.text = "${item.nombre} ${if (item.subTipo.isNotEmpty()) "(${item.subTipo})" else ""}"
        holder.tvDetalle.text = "Total: ${currency.format(item.total)}"
        holder.btnEdit.setOnClickListener { onEdit(item) }
        holder.btnDelete.setOnClickListener { onDelete(item) }
    }

    override fun getItemCount() = items.size
}
