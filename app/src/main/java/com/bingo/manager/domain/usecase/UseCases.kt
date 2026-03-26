package com.bingo.manager.domain.usecase

import com.bingo.manager.data.repository.*
import com.bingo.manager.domain.model.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * UseCase: Calcular lista de compras consolidada.
 * Lee todas las jugadas + canastas, suma cantidades, resta inventario.
 */
class CalcularComprasUseCase @Inject constructor(
    private val jugadaRepo: JugadaRepository,
    private val canastaRepo: CanastaRepository,
    private val inventarioRepo: InventarioRepository
) {
    data class ProductoConsolidado(
        val productoId: Long,
        val nombre: String,
        val cantidadNecesaria: Double,
        val unidad: String,
        val lugarCompra: String,
        val precio: Double
    )

    suspend operator fun invoke(fechaBingo: String): Compra {
        // 1. Recopilar todos los items de jugadas
        val jugadas = jugadaRepo.getAllConItems()
        val canastas = canastaRepo.getAll()

        // 2. Consolidar por nombre de producto
        val mapa = mutableMapOf<String, ProductoConsolidado>()

        fun agregarItem(item: ItemSeleccionado) {
            val key = item.nombre.lowercase().trim()
            val existente = mapa[key]
            if (existente != null) {
                mapa[key] = existente.copy(cantidadNecesaria = existente.cantidadNecesaria + item.cantidad)
            } else {
                mapa[key] = ProductoConsolidado(
                    productoId = item.productoId,
                    nombre = item.nombre,
                    cantidadNecesaria = item.cantidad,
                    unidad = item.unidad,
                    lugarCompra = item.lugarCompra,
                    precio = item.precioEditable
                )
            }
        }

        jugadas.forEach { jugada -> jugada.items.forEach { agregarItem(it) } }
        canastas.forEach { canasta -> canasta.items.forEach { agregarItem(it) } }

        // 3. Leer inventario y restar
        val inventario = inventarioRepo.getAll()
        val inventarioMap = inventario.associateBy { it.productoId }

        val fechaGen = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

        val items = mapa.values.sortedBy { it.nombre }.map { consolidado ->
            val stock = inventarioMap[consolidado.productoId]?.stockDisponible ?: 0.0
            val cantidadReal = maxOf(0.0, consolidado.cantidadNecesaria - stock)
            CompraItem(
                nombreProducto = consolidado.nombre,
                cantidadNecesaria = consolidado.cantidadNecesaria,
                cantidadInventario = stock,
                cantidadReal = cantidadReal,
                unidad = consolidado.unidad,
                lugarCompra = consolidado.lugarCompra,
                precio = consolidado.precio,
                subtotal = cantidadReal * consolidado.precio
            )
        }

        val total = items.sumOf { it.subtotal }

        return Compra(
            fechaBingo = fechaBingo,
            items = items,
            total = total,
            fechaGeneracion = fechaGen
        )
    }
}

/**
 * UseCase: Calcular sumatoria general del bingo.
 */
class CalcularSumatoriaUseCase @Inject constructor(
    private val compraRepo: CompraRepository,
    private val gastoRepo: GastoRepository,
    private val barRepo: BarRepository
) {
    data class SumatoriaResult(
        val totalCompras: Double,
        val totalGastos: Double,
        val totalBar: Double,
        val totalGeneral: Double
    )

    suspend operator fun invoke(): SumatoriaResult {
        val gastos = gastoRepo.getAllLive().value ?: emptyList()
        val bar = barRepo.getAll()
        // Para compras usamos la última generada
        val totalGastos = gastos.sumOf { it.precio }
        val totalBar = bar.sumOf { it.precio * it.cantidad }

        return SumatoriaResult(
            totalCompras = 0.0, // se actualiza desde ViewModel con la compra guardada
            totalGastos = totalGastos,
            totalBar = totalBar,
            totalGeneral = totalGastos + totalBar
        )
    }
}
