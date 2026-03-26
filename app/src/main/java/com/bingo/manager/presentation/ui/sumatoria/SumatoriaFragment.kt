package com.bingo.manager.presentation.ui.sumatoria

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bingo.manager.R
import com.bingo.manager.data.local.entities.CompraEntity
import com.bingo.manager.presentation.viewmodel.SumatoriaViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.NumberFormat
import java.util.Locale

@AndroidEntryPoint
class SumatoriaFragment : Fragment() {

    private val vm: SumatoriaViewModel by viewModels()
    private val currency = NumberFormat.getCurrencyInstance(Locale("es", "CO"))

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_sumatoria, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvCompras = view.findViewById<TextView>(R.id.tvSumCompras)
        val tvGastos = view.findViewById<TextView>(R.id.tvSumGastos)
        val tvBar = view.findViewById<TextView>(R.id.tvSumBar)
        val tvTotal = view.findViewById<TextView>(R.id.tvSumTotal)
        val spinnerCompras = view.findViewById<Spinner>(R.id.spinnerComprasSel)

        // Cargar historial de compras para seleccionar cuál incluir
        vm.historialCompras.observe(viewLifecycleOwner) { compras ->
            val labels = compras.map { "Bingo ${it.fechaBingo} — ${currency.format(it.total)}" }
            spinnerCompras.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, labels)
                .also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

            spinnerCompras.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                    vm.setTotalCompras(compras[pos].total)
                    tvCompras.text = "🛒 Compras: ${currency.format(compras[pos].total)}"
                }
                override fun onNothingSelected(p: AdapterView<*>?) {}
            }

            if (compras.isEmpty()) {
                tvCompras.text = "🛒 Compras: ${currency.format(0.0)}"
            }
        }

        vm.totalGastos.observe(viewLifecycleOwner) { total ->
            tvGastos.text = "💸 Gastos/Viáticos: ${currency.format(total ?: 0.0)}"
        }

        vm.totalBar.observe(viewLifecycleOwner) { total ->
            tvBar.text = "🍺 Bar: ${currency.format(total ?: 0.0)}"
        }

        vm.totalGeneral.observe(viewLifecycleOwner) { total ->
            tvTotal.text = currency.format(total)
        }
    }
}
