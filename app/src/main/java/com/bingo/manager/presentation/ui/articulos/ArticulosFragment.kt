package com.bingo.manager.presentation.ui.articulos

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bingo.manager.R
import com.bingo.manager.domain.model.*
import com.bingo.manager.presentation.viewmodel.ProductoBaseViewModel
import com.bingo.manager.presentation.viewmodel.CanastaViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import java.text.NumberFormat
import java.util.Locale

@AndroidEntryPoint
class ArticulosFragment : Fragment() {

    private val productoVm: ProductoBaseViewModel by viewModels()
    private val canastaVm: CanastaViewModel by viewModels()

    private lateinit var tabLayout: TabLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var fab: FloatingActionButton
    private lateinit var tvEmpty: TextView

    private val currency = NumberFormat.getCurrencyInstance(Locale("es", "CO"))

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_articulos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tabLayout = view.findViewById(R.id.tabLayout)
        recyclerView = view.findViewById(R.id.recyclerView)
        fab = view.findViewById(R.id.fab)
        tvEmpty = view.findViewById(R.id.tvEmpty)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        setupTabs()
        showProductosTab()

        productoVm.operationResult.observe(viewLifecycleOwner) { result ->
            result.fold(
                onSuccess = { Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show() },
                onFailure = { Toast.makeText(requireContext(), it.message, Toast.LENGTH_LONG).show() }
            )
        }
    }

    private fun setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("📦 Productos Base"))
        tabLayout.addTab(tabLayout.newTab().setText("🛒 Canasta Arroz"))
        tabLayout.addTab(tabLayout.newTab().setText("🧂 Canasta Azúcar"))
        tabLayout.addTab(tabLayout.newTab().setText("⭐ Canasta Especial"))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> showProductosTab()
                    1 -> showCanastaTab(TipoCanasta.ARROZ)
                    2 -> showCanastaTab(TipoCanasta.AZUCAR)
                    3 -> showCanastaTab(TipoCanasta.ESPECIAL)
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    // ─── PRODUCTOS BASE ───────────────────────
    private fun showProductosTab() {
        fab.setOnClickListener { showProductoDialog(null) }
        productoVm.productos.observe(viewLifecycleOwner) { lista ->
            tvEmpty.visibility = if (lista.isEmpty()) View.VISIBLE else View.GONE
            recyclerView.adapter = ProductoAdapter(lista,
                onEdit = { showProductoDialog(it) },
                onDelete = { confirmarEliminar { productoVm.eliminar(it.id) } },
                currency = currency
            )
        }
    }

    private fun showProductoDialog(producto: ProductoBase?) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_producto, null)
        val etNombre = dialogView.findViewById<TextInputEditText>(R.id.etNombre)
        val etCantidad = dialogView.findViewById<TextInputEditText>(R.id.etCantidad)
        val etUnidad = dialogView.findViewById<TextInputEditText>(R.id.etUnidad)
        val etLugar = dialogView.findViewById<TextInputEditText>(R.id.etLugar)
        val etPrecio = dialogView.findViewById<TextInputEditText>(R.id.etPrecio)

        producto?.let {
            etNombre.setText(it.nombre)
            etCantidad.setText(it.cantidad.toString())
            etUnidad.setText(it.unidad)
            etLugar.setText(it.lugarCompra)
            etPrecio.setText(it.precio.toString())
        }

        AlertDialog.Builder(requireContext())
            .setTitle(if (producto == null) "Nuevo Producto" else "Editar Producto")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val nombre = etNombre.text.toString().trim()
                val cantidad = etCantidad.text.toString().toDoubleOrNull() ?: 0.0
                val unidad = etUnidad.text.toString().trim()
                val lugar = etLugar.text.toString().trim()
                val precio = etPrecio.text.toString().toDoubleOrNull() ?: 0.0

                if (nombre.isEmpty()) {
                    Toast.makeText(requireContext(), "El nombre es obligatorio", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                productoVm.guardar(
                    ProductoBase(
                        id = producto?.id ?: 0L,
                        nombre = nombre,
                        cantidad = cantidad,
                        unidad = unidad,
                        lugarCompra = lugar,
                        precio = precio
                    )
                )
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // ─── CANASTAS ────────────────────────────
    private fun showCanastaTab(tipo: String) {
        fab.setOnClickListener { showCanastaDialog(null, tipo) }
        canastaVm.canastas.observe(viewLifecycleOwner) { lista ->
            val filtradas = lista.filter { it.tipo == tipo }
            tvEmpty.visibility = if (filtradas.isEmpty()) View.VISIBLE else View.GONE
            recyclerView.adapter = CanastaListAdapter(filtradas,
                onEdit = { canastaVm.cargarCanasta(it.id); showCanastaDialog(it.id, tipo) },
                onDelete = { confirmarEliminar { canastaVm.eliminar(it.id) } },
                currency = currency
            )
        }
    }

    private fun showCanastaDialog(id: Long?, tipo: String) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_canasta, null)
        val etNombre = dialogView.findViewById<TextInputEditText>(R.id.etNombreCanasta)
        val llItems = dialogView.findViewById<LinearLayout>(R.id.llItems)
        val btnAgregar = dialogView.findViewById<Button>(R.id.btnAgregarItem)
        val tvTotal = dialogView.findViewById<TextView>(R.id.tvTotalCanasta)

        val itemsSeleccionados = mutableListOf<ItemSeleccionado>()
        val productos = productoVm.productos.value ?: emptyList()

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

            val nombresProductos = productos.map { it.nombre }
            spinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, nombresProductos)
                .also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

            val placeholder = ItemSeleccionado(
                productoId = productos.firstOrNull()?.id ?: 0,
                nombre = productos.firstOrNull()?.nombre ?: "",
                cantidad = item?.cantidad ?: 1.0,
                unidad = productos.firstOrNull()?.unidad ?: "",
                lugarCompra = productos.firstOrNull()?.lugarCompra ?: "",
                precioEditable = item?.precioEditable ?: productos.firstOrNull()?.precio ?: 0.0,
                parentType = "CANASTA",
                parentId = id ?: 0
            )
            itemsSeleccionados.add(placeholder)
            val idx = itemsSeleccionados.lastIndex

            item?.let {
                val pos = nombresProductos.indexOf(it.nombre)
                if (pos >= 0) spinner.setSelection(pos)
                etCantidad.setText(it.cantidad.toString())
                etPrecio.setText(it.precioEditable.toString())
            }

            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
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
                        cantidad = etCantidad.text.toString().toDoubleOrNull() ?: 1.0
                    )
                    recalcTotal()
                }
            }

            etPrecio.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    itemsSeleccionados[idx] = itemsSeleccionados[idx].copy(
                        precioEditable = etPrecio.text.toString().toDoubleOrNull() ?: 0.0
                    )
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

        // Si es edición, cargar items existentes
        if (id != null) {
            canastaVm.canastaActual.observe(viewLifecycleOwner) { canasta ->
                canasta?.let {
                    etNombre.setText(it.nombre)
                    it.items.forEach { item -> agregarItemRow(item) }
                    recalcTotal()
                }
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
            .setTitle("Canasta ${tipo.lowercase().replaceFirstChar { it.uppercase() }}")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val nombre = etNombre.text.toString().trim().ifEmpty { "Canasta $tipo" }
                val canasta = Canasta(
                    id = id ?: 0L,
                    tipo = tipo,
                    nombre = nombre,
                    items = itemsSeleccionados.toList()
                )
                canastaVm.guardar(canasta)
                Toast.makeText(requireContext(), "Canasta guardada", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun confirmarEliminar(onConfirm: () -> Unit) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar")
            .setMessage("¿Estás seguro de eliminar este elemento?")
            .setPositiveButton("Eliminar") { _, _ -> onConfirm() }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}

// ─── Adapter Productos ───
class ProductoAdapter(
    private val items: List<ProductoBase>,
    private val onEdit: (ProductoBase) -> Unit,
    private val onDelete: (ProductoBase) -> Unit,
    private val currency: NumberFormat
) : RecyclerView.Adapter<ProductoAdapter.VH>() {

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
        holder.tvDetalle.text = "${item.cantidad} ${item.unidad} | ${item.lugarCompra} | ${currency.format(item.precio)}"
        holder.btnEdit.setOnClickListener { onEdit(item) }
        holder.btnDelete.setOnClickListener { onDelete(item) }
    }

    override fun getItemCount() = items.size
}

// ─── Adapter Canastas Lista ───
class CanastaListAdapter(
    private val items: List<com.bingo.manager.data.local.entities.CanastaEntity>,
    private val onEdit: (com.bingo.manager.data.local.entities.CanastaEntity) -> Unit,
    private val onDelete: (com.bingo.manager.data.local.entities.CanastaEntity) -> Unit,
    private val currency: NumberFormat
) : RecyclerView.Adapter<CanastaListAdapter.VH>() {

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
        holder.tvDetalle.text = "Total: ${currency.format(item.total)}"
        holder.btnEdit.setOnClickListener { onEdit(item) }
        holder.btnDelete.setOnClickListener { onDelete(item) }
    }

    override fun getItemCount() = items.size
}
