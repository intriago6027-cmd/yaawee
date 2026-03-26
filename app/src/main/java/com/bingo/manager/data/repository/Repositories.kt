package com.bingo.manager.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.bingo.manager.data.local.dao.*
import com.bingo.manager.data.local.entities.*
import com.bingo.manager.domain.model.*
import javax.inject.Inject
import javax.inject.Singleton

// ─────────────────────────────────────────────
// REPOSITORY: PRODUCTO BASE
// ─────────────────────────────────────────────
@Singleton
class ProductoBaseRepository @Inject constructor(private val dao: ProductoBaseDao) {
    fun getAllLive(): LiveData<List<ProductoBase>> = dao.getAllLive().map { it.map { e -> e.toDomain() } }
    suspend fun getAll(): List<ProductoBase> = dao.getAll().map { it.toDomain() }
    suspend fun getById(id: Long): ProductoBase? = dao.getById(id)?.toDomain()

    suspend fun insert(p: ProductoBase): Result<Long> {
        if (dao.countByNombre(p.nombre, p.id) > 0)
            return Result.failure(Exception("Ya existe un producto con ese nombre"))
        return Result.success(dao.insert(p.toEntity()))
    }

    suspend fun update(p: ProductoBase): Result<Unit> {
        if (dao.countByNombre(p.nombre, p.id) > 0)
            return Result.failure(Exception("Ya existe un producto con ese nombre"))
        dao.update(p.toEntity())
        return Result.success(Unit)
    }

    suspend fun delete(id: Long) = dao.deleteById(id)
}

// ─────────────────────────────────────────────
// REPOSITORY: ITEM SELECCIONADO
// ─────────────────────────────────────────────
@Singleton
class ItemSeleccionadoRepository @Inject constructor(private val dao: ItemSeleccionadoDao) {
    fun getByParentLive(type: String, parentId: Long): LiveData<List<ItemSeleccionado>> =
        dao.getByParentLive(type, parentId).map { it.map { e -> e.toDomain() } }

    suspend fun getByParent(type: String, parentId: Long): List<ItemSeleccionado> =
        dao.getByParent(type, parentId).map { it.toDomain() }

    suspend fun getAllByType(type: String): List<ItemSeleccionado> =
        dao.getAllByType(type).map { it.toDomain() }

    suspend fun insert(item: ItemSeleccionado): Long = dao.insert(item.toEntity())
    suspend fun insertAll(items: List<ItemSeleccionado>) = dao.insertAll(items.map { it.toEntity() })
    suspend fun update(item: ItemSeleccionado) = dao.update(item.toEntity())
    suspend fun delete(id: Long) = dao.deleteById(id)
    suspend fun deleteByParent(type: String, parentId: Long) = dao.deleteByParent(type, parentId)
}

// ─────────────────────────────────────────────
// REPOSITORY: CANASTA
// ─────────────────────────────────────────────
@Singleton
class CanastaRepository @Inject constructor(
    private val dao: CanastaDao,
    private val itemRepo: ItemSeleccionadoRepository
) {
    fun getAllLive(): LiveData<List<CanastaEntity>> = dao.getAllLive()

    suspend fun getCanastaConItems(id: Long): Canasta? {
        val entity = dao.getById(id) ?: return null
        val items = itemRepo.getByParent("CANASTA", id)
        return entity.toDomain(items)
    }

    suspend fun getAll(): List<Canasta> {
        return dao.getAll().map { entity ->
            val items = itemRepo.getByParent("CANASTA", entity.id)
            entity.toDomain(items)
        }
    }

    suspend fun saveCanasta(canasta: Canasta): Long {
        val total = canasta.items.sumOf { it.precioEditable * it.cantidad }
        val entity = canasta.copy(total = total).toEntity()
        val id = if (canasta.id == 0L) dao.insert(entity) else { dao.update(entity); canasta.id }
        // Reemplazar items
        itemRepo.deleteByParent("CANASTA", id)
        itemRepo.insertAll(canasta.items.map { it.copy(parentType = "CANASTA", parentId = id) })
        return id
    }

    suspend fun delete(id: Long) {
        val entity = dao.getById(id) ?: return
        itemRepo.deleteByParent("CANASTA", id)
        dao.delete(entity)
    }
}

// ─────────────────────────────────────────────
// REPOSITORY: JUGADA
// ─────────────────────────────────────────────
@Singleton
class JugadaRepository @Inject constructor(
    private val dao: JugadaDao,
    private val itemRepo: ItemSeleccionadoRepository
) {
    fun getAllLive(): LiveData<List<JugadaEntity>> = dao.getAllLive()

    fun getByTipoLive(tipo: String): LiveData<List<JugadaEntity>> = dao.getByTipoLive(tipo)

    suspend fun getJugadaConItems(id: Long): Jugada? {
        val entity = dao.getById(id) ?: return null
        val items = itemRepo.getByParent("JUGADA", id)
        return entity.toDomain(items)
    }

    suspend fun getAllConItems(): List<Jugada> {
        return dao.getAll().map { entity ->
            val items = itemRepo.getByParent("JUGADA", entity.id)
            entity.toDomain(items)
        }
    }

    suspend fun saveJugada(jugada: Jugada): Long {
        val total = jugada.items.sumOf { it.precioEditable * it.cantidad }
        val entity = jugada.copy(total = total).toEntity()
        val id = if (jugada.id == 0L) dao.insert(entity) else { dao.update(entity); jugada.id }
        itemRepo.deleteByParent("JUGADA", id)
        itemRepo.insertAll(jugada.items.map { it.copy(parentType = "JUGADA", parentId = id) })
        return id
    }

    suspend fun delete(id: Long) {
        itemRepo.deleteByParent("JUGADA", id)
        dao.deleteById(id)
    }
}

// ─────────────────────────────────────────────
// REPOSITORY: INVENTARIO
// ─────────────────────────────────────────────
@Singleton
class InventarioRepository @Inject constructor(private val dao: InventarioDao) {
    fun getAllLive(): LiveData<List<InventarioItem>> = dao.getAllLive().map { it.map { e -> e.toDomain() } }
    suspend fun getAll(): List<InventarioItem> = dao.getAll().map { it.toDomain() }
    suspend fun getByProductoId(productoId: Long): InventarioItem? = dao.getByProductoId(productoId)?.toDomain()

    suspend fun save(item: InventarioItem): Long =
        if (item.id == 0L) dao.insert(item.toEntity())
        else { dao.update(item.toEntity()); item.id }

    suspend fun delete(id: Long) = dao.deleteById(id)
}

// ─────────────────────────────────────────────
// REPOSITORY: COMPRA
// ─────────────────────────────────────────────
@Singleton
class CompraRepository @Inject constructor(private val dao: CompraDao) {
    fun getAllLive(): LiveData<List<CompraEntity>> = dao.getAllLive()

    suspend fun saveCompra(compra: Compra): Long {
        val id = if (compra.id == 0L) dao.insert(compra.toEntity())
        else {
            dao.update(compra.toEntity())
            dao.deleteItemsByCompra(compra.id)
            compra.id
        }
        dao.insertItems(compra.items.map { it.copy(compraId = id).toEntity() })
        return id
    }

    suspend fun getCompraConItems(id: Long): Compra? {
        val entity = dao.getById(id) ?: return null
        val items = dao.getItemsByCompra(id).map { it.toDomain() }
        return entity.toDomain(items)
    }

    fun getItemsLive(compraId: Long): LiveData<List<CompraItemEntity>> = dao.getItemsByCompraLive(compraId)
}

// ─────────────────────────────────────────────
// REPOSITORY: GASTO
// ─────────────────────────────────────────────
@Singleton
class GastoRepository @Inject constructor(private val dao: GastoDao) {
    fun getAllLive(): LiveData<List<Gasto>> = dao.getAllLive().map { it.map { e -> e.toDomain() } }
    fun getTotalLive(): LiveData<Double?> = dao.getTotalLive()
    suspend fun insert(g: Gasto): Long = dao.insert(g.toEntity())
    suspend fun update(g: Gasto) = dao.update(g.toEntity())
    suspend fun delete(id: Long) = dao.deleteById(id)
}

// ─────────────────────────────────────────────
// REPOSITORY: BAR
// ─────────────────────────────────────────────
@Singleton
class BarRepository @Inject constructor(private val dao: BarItemDao) {
    fun getAllLive(): LiveData<List<BarItem>> = dao.getAllLive().map { it.map { e -> e.toDomain() } }
    fun getTotalLive(): LiveData<Double?> = dao.getTotalLive()
    suspend fun getAll(): List<BarItem> = dao.getAll().map { it.toDomain() }
    suspend fun insert(b: BarItem): Long = dao.insert(b.toEntity())
    suspend fun update(b: BarItem) = dao.update(b.toEntity())
    suspend fun delete(id: Long) = dao.deleteById(id)
}
