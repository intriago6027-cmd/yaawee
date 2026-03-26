package com.bingo.manager.domain.model

// ─────────────────────────────────────────────
// Modelos de dominio (independientes de Room)
// ─────────────────────────────────────────────

data class ProductoBase(
    val id: Long = 0,
    val nombre: String,
    val cantidad: Double,
    val unidad: String,
    val lugarCompra: String,
    val precio: Double
)

data class ItemSeleccionado(
    val id: Long = 0,
    val productoId: Long,
    val nombre: String,
    val cantidad: Double,
    val unidad: String,
    val lugarCompra: String,
    val precioEditable: Double,
    val parentType: String,
    val parentId: Long
)

data class Canasta(
    val id: Long = 0,
    val tipo: String,
    val nombre: String,
    val items: List<ItemSeleccionado> = emptyList(),
    val total: Double = 0.0
)

data class Jugada(
    val id: Long = 0,
    val tipo: String,
    val nombre: String,
    val numero: Int = 0,
    val subTipo: String = "",
    val items: List<ItemSeleccionado> = emptyList(),
    val total: Double = 0.0,
    val activa: Boolean = true
)

data class InventarioItem(
    val id: Long = 0,
    val productoId: Long,
    val nombreProducto: String,
    val stockDisponible: Double,
    val unidad: String
)

data class Compra(
    val id: Long = 0,
    val fechaBingo: String,
    val items: List<CompraItem> = emptyList(),
    val total: Double = 0.0,
    val fechaGeneracion: String
)

data class CompraItem(
    val id: Long = 0,
    val compraId: Long = 0,
    val nombreProducto: String,
    val cantidadNecesaria: Double,
    val cantidadInventario: Double,
    val cantidadReal: Double,
    val unidad: String,
    val lugarCompra: String,
    val precio: Double,
    val subtotal: Double
)

data class Gasto(
    val id: Long = 0,
    val descripcion: String,
    val precio: Double,
    val categoria: String = "VIATICO"
)

data class BarItem(
    val id: Long = 0,
    val productoId: Long,
    val nombre: String,
    val precio: Double,
    val cantidad: Double = 1.0
)

// Tipos de jugadas como constantes
object TipoJugada {
    const val REGALADITA = "REGALADITA"
    const val NORMAL = "NORMAL"
    const val ADICIONAL = "ADICIONAL"
    const val SORTEO_REGALADITA = "SORTEO_REGALADITA"
    const val SORTEO_TICKET = "SORTEO_TICKET"
    const val ACUMULADA = "ACUMULADA"
    const val PUNTUALIDAD = "PUNTUALIDAD"
    const val COMPARTIDO = "COMPARTIDO"
    const val ABECEDARIO = "ABECEDARIO"
}

object TipoCanasta {
    const val ARROZ = "ARROZ"
    const val AZUCAR = "AZUCAR"
    const val ESPECIAL = "ESPECIAL"
}

// Subtipo de jugadas normales
object SubTipoJugadaNormal {
    const val BINGO_LOCO = "Bingo Loco"
    const val LINEA_4 = "Línea 4"
    const val LINEA_5 = "Línea 5"
    const val CUATRO_ESQUINAS = "4 Esquinas"
    const val LETRA = "Letra"
    const val TABLA_LLENA = "Tabla Llena"
}
