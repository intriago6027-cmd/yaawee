package com.bingo.manager.data.local.entities

import com.bingo.manager.domain.model.*

// ─── ProductoBase ───
fun ProductoBaseEntity.toDomain() = ProductoBase(id, nombre, cantidad, unidad, lugarCompra, precio)
fun ProductoBase.toEntity() = ProductoBaseEntity(id, nombre, cantidad, unidad, lugarCompra, precio)

// ─── ItemSeleccionado ───
fun ItemSeleccionadoEntity.toDomain() =
    ItemSeleccionado(id, productoId, nombre, cantidad, unidad, lugarCompra, precioEditable, parentType, parentId)
fun ItemSeleccionado.toEntity() =
    ItemSeleccionadoEntity(id, productoId, nombre, cantidad, unidad, lugarCompra, precioEditable, parentType, parentId)

// ─── Canasta ───
fun CanastaEntity.toDomain(items: List<ItemSeleccionado> = emptyList()) =
    Canasta(id, tipo, nombre, items, total)
fun Canasta.toEntity() = CanastaEntity(id, tipo, nombre, total)

// ─── Jugada ───
fun JugadaEntity.toDomain(items: List<ItemSeleccionado> = emptyList()) =
    Jugada(id, tipo, nombre, numero, subTipo, items, total, activa)
fun Jugada.toEntity() = JugadaEntity(id, tipo, nombre, numero, subTipo, total, activa)

// ─── Inventario ───
fun InventarioEntity.toDomain() = InventarioItem(id, productoId, nombreProducto, stockDisponible, unidad)
fun InventarioItem.toEntity() = InventarioEntity(id, productoId, nombreProducto, stockDisponible, unidad)

// ─── Compra ───
fun CompraEntity.toDomain(items: List<CompraItem> = emptyList()) =
    Compra(id, fechaBingo, items, total, fechaGeneracion)
fun Compra.toEntity() = CompraEntity(id, fechaBingo, total, fechaGeneracion)

fun CompraItemEntity.toDomain() = CompraItem(
    id, compraId, nombreProducto, cantidadNecesaria,
    cantidadInventario, cantidadReal, unidad, lugarCompra, precio, subtotal
)
fun CompraItem.toEntity() = CompraItemEntity(
    id, compraId, nombreProducto, cantidadNecesaria,
    cantidadInventario, cantidadReal, unidad, lugarCompra, precio, subtotal
)

// ─── Gasto ───
fun GastoEntity.toDomain() = Gasto(id, descripcion, precio, categoria)
fun Gasto.toEntity() = GastoEntity(id, descripcion, precio, categoria)

// ─── BarItem ───
fun BarItemEntity.toDomain() = BarItem(id, productoId, nombre, precio, cantidad)
fun BarItem.toEntity() = BarItemEntity(id, productoId, nombre, precio, cantidad)
