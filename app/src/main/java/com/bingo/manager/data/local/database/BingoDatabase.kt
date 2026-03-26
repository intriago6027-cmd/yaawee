package com.bingo.manager.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.bingo.manager.data.local.dao.*
import com.bingo.manager.data.local.entities.*

/**
 * Base de datos principal Room.
 * Versión 1 — contiene todas las entidades del sistema.
 */
@Database(
    entities = [
        ProductoBaseEntity::class,
        ItemSeleccionadoEntity::class,
        CanastaEntity::class,
        JugadaEntity::class,
        InventarioEntity::class,
        CompraEntity::class,
        CompraItemEntity::class,
        GastoEntity::class,
        BarItemEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class BingoDatabase : RoomDatabase() {
    abstract fun productoBaseDao(): ProductoBaseDao
    abstract fun itemSeleccionadoDao(): ItemSeleccionadoDao
    abstract fun canastaDao(): CanastaDao
    abstract fun jugadaDao(): JugadaDao
    abstract fun inventarioDao(): InventarioDao
    abstract fun compraDao(): CompraDao
    abstract fun gastoDao(): GastoDao
    abstract fun barItemDao(): BarItemDao
}
