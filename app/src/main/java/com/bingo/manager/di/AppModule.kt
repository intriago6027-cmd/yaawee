package com.bingo.manager.di

import android.content.Context
import androidx.room.Room
import com.bingo.manager.data.local.dao.*
import com.bingo.manager.data.local.database.BingoDatabase
import com.bingo.manager.data.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): BingoDatabase =
        Room.databaseBuilder(ctx, BingoDatabase::class.java, "bingo_db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideProductoBaseDao(db: BingoDatabase): ProductoBaseDao = db.productoBaseDao()
    @Provides fun provideItemDao(db: BingoDatabase): ItemSeleccionadoDao = db.itemSeleccionadoDao()
    @Provides fun provideCanastaDao(db: BingoDatabase): CanastaDao = db.canastaDao()
    @Provides fun provideJugadaDao(db: BingoDatabase): JugadaDao = db.jugadaDao()
    @Provides fun provideInventarioDao(db: BingoDatabase): InventarioDao = db.inventarioDao()
    @Provides fun provideCompraDao(db: BingoDatabase): CompraDao = db.compraDao()
    @Provides fun provideGastoDao(db: BingoDatabase): GastoDao = db.gastoDao()
    @Provides fun provideBarDao(db: BingoDatabase): BarItemDao = db.barItemDao()
}

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideProductoBaseRepo(dao: ProductoBaseDao): ProductoBaseRepository =
        ProductoBaseRepository(dao)

    @Provides
    @Singleton
    fun provideItemRepo(dao: ItemSeleccionadoDao): ItemSeleccionadoRepository =
        ItemSeleccionadoRepository(dao)

    @Provides
    @Singleton
    fun provideCanastaRepo(dao: CanastaDao, itemRepo: ItemSeleccionadoRepository): CanastaRepository =
        CanastaRepository(dao, itemRepo)

    @Provides
    @Singleton
    fun provideJugadaRepo(dao: JugadaDao, itemRepo: ItemSeleccionadoRepository): JugadaRepository =
        JugadaRepository(dao, itemRepo)

    @Provides
    @Singleton
    fun provideInventarioRepo(dao: InventarioDao): InventarioRepository =
        InventarioRepository(dao)

    @Provides
    @Singleton
    fun provideCompraRepo(dao: CompraDao): CompraRepository = CompraRepository(dao)

    @Provides
    @Singleton
    fun provideGastoRepo(dao: GastoDao): GastoRepository = GastoRepository(dao)

    @Provides
    @Singleton
    fun provideBarRepo(dao: BarItemDao): BarRepository = BarRepository(dao)
}
