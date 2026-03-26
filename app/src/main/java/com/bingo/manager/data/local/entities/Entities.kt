package com.bingo.manager.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// ─────────────────────────────────────────────
// 1. PRODUCTO BASE — catálogo reutilizable
// ─────────────────────────────────────────────
@Entity(tableName = "productos_base")
data class ProductoBaseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val cantidad: Double,
    val unidad: String,           // kg, unidad, litro, etc.
    val lugarCompra: String,
    val precio: Double
)

// ─────────────────────────────────────────────
// 2. ITEM SELECCIONADO — snapshot editable
// ─────────────────────────────────────────────
@Entity(
    tableName = "items_seleccionados",
    foreignKeys = [ForeignKey(
        entity = ProductoBaseEntity::class,
        parentColumns = ["id"],
        childColumns = ["productoId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("productoId")]
)
data class ItemSeleccionadoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productoId: Long,
    val nombre: String,
    val cantidad: Double,
    val unidad: String,
    val lugarCompra: String,
    val precioEditable: Double,
    // Contexto: a qué pertenece este item
    val parentType: String,   // "CANASTA", "JUGADA", "BAR", "GASTO"
    val parentId: Long
)

// ─────────────────────────────────────────────
// 3. CANASTA
// ─────────────────────────────────────────────
@Entity(tableName = "canastas")
data class CanastaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tipo: String,   // ARROZ, AZUCAR, ESPECIAL
    val nombre: String,
    val total: Double = 0.0
)

// ─────────────────────────────────────────────
// 4. JUGADA
// ─────────────────────────────────────────────
@Entity(tableName = "jugadas")
data class JugadaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tipo: String,        // REGALADITA, NORMAL, ADICIONAL, SORTEO_REGALADITA,
                             // SORTEO_TICKET, ACUMULADA, PUNTUALIDAD, COMPARTIDO, ABECEDARIO
    val nombre: String,
    val numero: Int = 0,     // orden dentro del tipo
    val subTipo: String = "", // para jugadas normales: BINGO_LOCO, LINEA_4, etc.
    val total: Double = 0.0,
    val activa: Boolean = true
)

// ─────────────────────────────────────────────
// 5. INVENTARIO
// ─────────────────────────────────────────────
@Entity(
    tableName = "inventario",
    foreignKeys = [ForeignKey(
        entity = ProductoBaseEntity::class,
        parentColumns = ["id"],
        childColumns = ["productoId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("productoId")]
)
data class InventarioEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productoId: Long,
    val nombreProducto: String,
    val stockDisponible: Double,
    val unidad: String
)

// ─────────────────────────────────────────────
// 6. COMPRA (resultado calculado)
// ─────────────────────────────────────────────
@Entity(tableName = "compras")
data class CompraEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fechaBingo: String,
    val total: Double = 0.0,
    val fechaGeneracion: String
)

// Item de compra (línea del PDF)
@Entity(
    tableName = "compra_items",
    foreignKeys = [ForeignKey(
        entity = CompraEntity::class,
        parentColumns = ["id"],
        childColumns = ["compraId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("compraId")]
)
data class CompraItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val compraId: Long,
    val nombreProducto: String,
    val cantidadNecesaria: Double,
    val cantidadInventario: Double,
    val cantidadReal: Double,   // necesaria - inventario (mínimo 0)
    val unidad: String,
    val lugarCompra: String,
    val precio: Double,
    val subtotal: Double
)

// ─────────────────────────────────────────────
// 7. GASTO
// ─────────────────────────────────────────────
@Entity(tableName = "gastos")
data class GastoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val descripcion: String,
    val precio: Double,
    val categoria: String = "VIATICO"  // VIATICO u otras categorías futuras
)

// ─────────────────────────────────────────────
// 8. BAR ITEM
// ─────────────────────────────────────────────
@Entity(
    tableName = "bar_items",
    foreignKeys = [ForeignKey(
        entity = ProductoBaseEntity::class,
        parentColumns = ["id"],
        childColumns = ["productoId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("productoId")]
)
data class BarItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productoId: Long,
    val nombre: String,
    val precio: Double,
    val cantidad: Double = 1.0
)
