package com.bingo.manager.presentation.ui.compras

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bingo.manager.R
import com.bingo.manager.domain.model.CompraItem
import com.bingo.manager.presentation.viewmodel.ComprasViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import java.text.NumberFormat
import java.util.Locale

@AndroidEntryPoint
class ComprasFragment : Fragment() {

    private val vm: ComprasViewModel by viewModels()
    private val currency = NumberFormat.getCurrencyInstance(Locale("es", "CO"))

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_compras, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etFecha = view.findViewById<TextInputEditText>(R.id.etFechaBingo)
        val btnCalcular = view.findViewById<MaterialButton>(R.id.btnCalcular)
        val btnPdf = view.findViewById<MaterialButton>(R.id.btnGenerarPdf)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerCompras)
        val tvTotal = view.findViewById<TextView>(R.id.tvTotalCompras)
        val tvInfo = view.findViewById<TextView>(R.id.tvInfoCompras)
        val progress = view.findViewById<CircularProgressIndicator>(R.id.progressCompras)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        btnPdf.isEnabled = false

        btnCalcular.setOnClickListener {
            val fecha = etFecha.text.toString().trim()
            if (fecha.isEmpty()) {
                Toast.makeText(requireContext(), "Ingresa la fecha del bingo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            vm.calcular(fecha)
        }

        btnPdf.setOnClickListener {
            val compra = vm.compraCalculada.value ?: return@setOnClickListener
            vm.guardarYGenerarPdf(requireContext(), compra)
        }

        vm.loading.observe(viewLifecycleOwner) { loading ->
            progress.visibility = if (loading) View.VISIBLE else View.GONE
            btnCalcular.isEnabled = !loading
        }

        vm.compraCalculada.observe(viewLifecycleOwner) { compra ->
            if (compra == null) return@observe
            btnPdf.isEnabled = true
            tvInfo.text = "✅ ${compra.items.size} productos consolidados"
            tvTotal.text = "TOTAL: ${currency.format(compra.total)}"

            recyclerView.adapter = CompraItemAdapter(compra.items, currency)
        }

        vm.pdfFile.observe(viewLifecycleOwner) { file ->
            file ?: return@observe
            Toast.makeText(requireContext(), "PDF generado: ${file.name}", Toast.LENGTH_LONG).show()
            try {
                val uri = FileProvider.getUriForFile(
                    requireContext(),
                    "${requireContext().packageName}.provider",
                    file
                )
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/pdf")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(Intent.createChooser(intent, "Abrir PDF"))
            } catch (e: Exception) {
                AlertDialog.Builder(requireContext())
                    .setTitle("PDF Generado")
                    .setMessage("Guardado en: ${file.absolutePath}")
                    .setPositiveButton("OK", null).show()
            }
        }
    }
}

class CompraItemAdapter(
    private val items: List<CompraItem>,
    private val currency: NumberFormat
) : RecyclerView.Adapter<CompraItemAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombreCompra)
        val tvNecesaria: TextView = view.findViewById(R.id.tvNecesaria)
        val tvInventario: TextView = view.findViewById(R.id.tvInventarioCompra)
        val tvReal: TextView = view.findViewById(R.id.tvReal)
        val tvSubtotal: TextView = view.findViewById(R.id.tvSubtotal)
        val tvLugar: TextView = view.findViewById(R.id.tvLugarCompra)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_compra, parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.tvNombre.text = item.nombreProducto
        holder.tvNecesaria.text = "Necesita: ${item.cantidadNecesaria} ${item.unidad}"
        holder.tvInventario.text = "Stock: ${item.cantidadInventario} ${item.unidad}"
        holder.tvReal.text = "Comprar: ${item.cantidadReal} ${item.unidad}"
        holder.tvSubtotal.text = currency.format(item.subtotal)
        holder.tvLugar.text = "📍 ${item.lugarCompra}"
    }

    override fun getItemCount() = items.size
}
