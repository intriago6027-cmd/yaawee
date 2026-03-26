package com.bingo.manager.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.bingo.manager.data.local.entities.*

// ─────────────────────────────────────────────
// DAO: PRODUCTO BASE
// ─────────────────────────────────────────────
@Dao
interface ProductoBaseDao {
    @Query("SELECT * FROM productos_base ORDER BY nombre ASC")
    fun getAllLive(): LiveData<List<ProductoBaseEntity>>

    @Query("SELECT * FROM productos_base ORDER BY nombre ASC")
    suspend fun getAll(): List<ProductoBaseEntity>

    @Query("SELECT * FROM productos_base WHERE id = :id")
    suspend fun getById(id: Long): ProductoBaseEntity?

    @Query("SELECT COUNT(*) FROM productos_base WHERE LOWER(nombre) = LOWER(:nombre) AND id != :excludeId")
    suspend fun countByNombre(nombre: String, excludeId: Long = -1): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ProductoBaseEntity): Long

    @Update
    suspend fun update(entity: ProductoBaseEntity)

    @Delete
    suspend fun delete(entity: ProductoBaseEntity)

    @Query("DELETE FROM productos_base WHERE id = :id")
    suspend fun deleteById(id: Long)
}

// ─────────────────────────────────────────────
// DAO: ITEM SELECCIONADO
// ─────────────────────────────────────────────
@Dao
interface ItemSeleccionadoDao {
    @Query("SELECT * FROM items_seleccionados WHERE parentType = :type AND parentId = :parentId ORDER BY nombre ASC")
    fun getByParentLive(type: String, parentId: Long): LiveData<List<ItemSeleccionadoEntity>>

    @Query("SELECT * FROM items_seleccionados WHERE parentType = :type AND parentId = :parentId ORDER BY nombre ASC")
    suspend fun getByParent(type: String, parentId: Long): List<ItemSeleccionadoEntity>

    @Query("SELECT * FROM items_seleccionados WHERE parentType = :type ORDER BY nombre ASC")
    suspend fun getAllByType(type: String): List<ItemSeleccionadoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ItemSeleccionadoEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ItemSeleccionadoEntity>)

    @Update
    suspend fun update(entity: ItemSeleccionadoEntity)

    @Delete
    suspend fun delete(entity: ItemSeleccionadoEntity)

    @Query("DELETE FROM items_seleccionados WHERE parentType = :type AND parentId = :parentId")
    suspend fun deleteByParent(type: String, parentId: Long)

    @Query("DELETE FROM items_seleccionados WHERE id = :id")
    suspend fun deleteById(id: Long)
}

// ─────────────────────────────────────────────
// DAO: CANASTA
// ─────────────────────────────────────────────
@Dao
interface CanastaDao {
    @Query("SELECT * FROM canastas ORDER BY tipo ASC, nombre ASC")
    fun getAllLive(): LiveData<List<CanastaEntity>>

    @Query("SELECT * FROM canastas ORDER BY tipo ASC, nombre ASC")
    suspend fun getAll(): List<CanastaEntity>

    @Query("SELECT * FROM canastas WHERE id = :id")
    suspend fun getById(id: Long): CanastaEntity?

    @Query("SELECT * FROM canastas WHERE tipo = :tipo ORDER BY nombre ASC")
    fun getByTipoLive(tipo: String): LiveData<List<CanastaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: CanastaEntity): Long

    @Update
    suspend fun update(entity: CanastaEntity)

    @Delete
    suspend fun delete(entity: CanastaEntity)
}

// ─────────────────────────────────────────────
// DAO: JUGADA
// ─────────────────────────────────────────────
@Dao
interface JugadaDao {
    @Query("SELECT * FROM jugadas ORDER BY tipo ASC, numero ASC")
    fun getAllLive(): LiveData<List<JugadaEntity>>

    @Query("SELECT * FROM jugadas WHERE tipo = :tipo ORDER BY numero ASC")
    fun getByTipoLive(tipo: String): LiveData<List<JugadaEntity>>

    @Query("SELECT * FROM jugadas WHERE tipo = :tipo ORDER BY numero ASC")
    suspend fun getByTipo(tipo: String): List<JugadaEntity>

    @Query("SELECT * FROM jugadas WHERE id = :id")
    suspend fun getById(id: Long): JugadaEntity?

    @Query("SELECT * FROM jugadas ORDER BY tipo ASC, numero ASC")
    suspend fun getAll(): List<JugadaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: JugadaEntity): Long

    @Update
    suspend fun update(entity: JugadaEntity)

    @Delete
    suspend fun delete(entity: JugadaEntity)

    @Query("DELETE FROM jugadas WHERE id = :id")
    suspend fun deleteById(id: Long)
}

// ─────────────────────────────────────────────
// DAO: INVENTARIO
// ─────────────────────────────────────────────
@Dao
interface InventarioDao {
    @Query("SELECT * FROM inventario ORDER BY nombreProducto ASC")
    fun getAllLive(): LiveData<List<InventarioEntity>>

    @Query("SELECT * FROM inventario ORDER BY nombreProducto ASC")
    suspend fun getAll(): List<InventarioEntity>

    @Query("SELECT * FROM inventario WHERE productoId = :productoId LIMIT 1")
    suspend fun getByProductoId(productoId: Long): InventarioEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: InventarioEntity): Long

    @Update
    suspend fun update(entity: InventarioEntity)

    @Delete
    suspend fun delete(entity: InventarioEntity)

    @Query("DELETE FROM inventario WHERE id = :id")
    suspend fun deleteById(id: Long)
}

// ─────────────────────────────────────────────
// DAO: COMPRA
// ─────────────────────────────────────────────
@Dao
interface CompraDao {
    @Query("SELECT * FROM compras ORDER BY fechaBingo DESC")
    fun getAllLive(): LiveData<List<CompraEntity>>

    @Query("SELECT * FROM compras ORDER BY fechaBingo DESC")
    suspend fun getAll(): List<CompraEntity>

    @Query("SELECT * FROM compras WHERE id = :id")
    suspend fun getById(id: Long): CompraEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: CompraEntity): Long

    @Update
    suspend fun update(entity: CompraEntity)

    @Delete
    suspend fun delete(entity: CompraEntity)

    // Items
    @Query("SELECT * FROM compra_items WHERE compraId = :compraId ORDER BY nombreProducto ASC")
    fun getItemsByCompraLive(compraId: Long): LiveData<List<CompraItemEntity>>

    @Query("SELECT * FROM compra_items WHERE compraId = :compraId ORDER BY nombreProducto ASC")
    suspend fun getItemsByCompra(compraId: Long): List<CompraItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(entity: CompraItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<CompraItemEntity>)

    @Query("DELETE FROM compra_items WHERE compraId = :compraId")
    suspend fun deleteItemsByCompra(compraId: Long)
}

// ─────────────────────────────────────────────
// DAO: GASTO
// ─────────────────────────────────────────────
@Dao
interface GastoDao {
    @Query("SELECT * FROM gastos ORDER BY descripcion ASC")
    fun getAllLive(): LiveData<List<GastoEntity>>

    @Query("SELECT * FROM gastos ORDER BY descripcion ASC")
    suspend fun getAll(): List<GastoEntity>

    @Query("SELECT SUM(precio) FROM gastos")
    fun getTotalLive(): LiveData<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: GastoEntity): Long

    @Update
    suspend fun update(entity: GastoEntity)

    @Delete
    suspend fun delete(entity: GastoEntity)

    @Query("DELETE FROM gastos WHERE id = :id")
    suspend fun deleteById(id: Long)
}

// ─────────────────────────────────────────────
// DAO: BAR
// ─────────────────────────────────────────────
@Dao
interface BarItemDao {
    @Query("SELECT * FROM bar_items ORDER BY nombre ASC")
    fun getAllLive(): LiveData<List<BarItemEntity>>

    @Query("SELECT * FROM bar_items ORDER BY nombre ASC")
    suspend fun getAll(): List<BarItemEntity>

    @Query("SELECT SUM(precio * cantidad) FROM bar_items")
    fun getTotalLive(): LiveData<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: BarItemEntity): Long

    @Update
    suspend fun update(entity: BarItemEntity)

    @Delete
    suspend fun delete(entity: BarItemEntity)

    @Query("DELETE FROM bar_items WHERE id = :id")
    suspend fun deleteById(id: Long)
}
