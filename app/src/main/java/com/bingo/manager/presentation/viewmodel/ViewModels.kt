package com.bingo.manager.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.*
import com.bingo.manager.data.repository.*
import com.bingo.manager.domain.model.*
import com.bingo.manager.domain.usecase.CalcularComprasUseCase
import com.bingo.manager.util.PdfGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

// ─────────────────────────────────────────────
// VIEWMODEL: PRODUCTOS BASE
// ─────────────────────────────────────────────
@HiltViewModel
class ProductoBaseViewModel @Inject constructor(
    private val repo: ProductoBaseRepository
) : ViewModel() {

    val productos: LiveData<List<ProductoBase>> = repo.getAllLive()

    private val _operationResult = MutableLiveData<Result<String>>()
    val operationResult: LiveData<Result<String>> = _operationResult

    fun guardar(p: ProductoBase) = viewModelScope.launch {
        val result = if (p.id == 0L) repo.insert(p) else repo.update(p)
        _operationResult.value = result.fold(
            onSuccess = { Result.success("Producto guardado correctamente") },
            onFailure = { Result.failure(it) }
        )
    }

    fun eliminar(id: Long) = viewModelScope.launch { repo.delete(id) }
}

// ─────────────────────────────────────────────
// VIEWMODEL: CANASTA
// ─────────────────────────────────────────────
@HiltViewModel
class CanastaViewModel @Inject constructor(
    private val canastaRepo: CanastaRepository,
    private val productoRepo: ProductoBaseRepository
) : ViewModel() {

    val canastas: LiveData<List<CanastaEntity>> = canastaRepo.getAllLive()
    val productos: LiveData<List<ProductoBase>> = productoRepo.getAllLive()

    private val _canastaActual = MutableLiveData<Canasta?>()
    val canastaActual: LiveData<Canasta?> = _canastaActual

    private val _saved = MutableLiveData<Long?>()
    val saved: LiveData<Long?> = _saved

    fun cargarCanasta(id: Long) = viewModelScope.launch {
        _canastaActual.value = canastaRepo.getCanastaConItems(id)
    }

    fun nuevaCanasta(tipo: String) {
        _canastaActual.value = Canasta(tipo = tipo, nombre = "Nueva Canasta $tipo")
    }

    fun guardar(canasta: Canasta) = viewModelScope.launch {
        val id = canastaRepo.saveCanasta(canasta)
        _saved.value = id
    }

    fun eliminar(id: Long) = viewModelScope.launch { canastaRepo.delete(id) }
}

// ─────────────────────────────────────────────
// VIEWMODEL: JUGADAS
// ─────────────────────────────────────────────
@HiltViewModel
class JugadaViewModel @Inject constructor(
    private val jugadaRepo: JugadaRepository,
    private val productoRepo: ProductoBaseRepository
) : ViewModel() {

    val jugadas: LiveData<List<JugadaEntity>> = jugadaRepo.getAllLive()
    val productos: LiveData<List<ProductoBase>> = productoRepo.getAllLive()

    private val _jugadaActual = MutableLiveData<Jugada?>()
    val jugadaActual: LiveData<Jugada?> = _jugadaActual

    private val _saved = MutableLiveData<Long?>()
    val saved: LiveData<Long?> = _saved

    fun cargarJugada(id: Long) = viewModelScope.launch {
        _jugadaActual.value = jugadaRepo.getJugadaConItems(id)
    }

    fun nuevaJugada(tipo: String, nombre: String, numero: Int = 0, subTipo: String = "") {
        _jugadaActual.value = Jugada(tipo = tipo, nombre = nombre, numero = numero, subTipo = subTipo)
    }

    fun guardar(jugada: Jugada) = viewModelScope.launch {
        val id = jugadaRepo.saveJugada(jugada)
        _saved.value = id
    }

    fun eliminar(id: Long) = viewModelScope.launch { jugadaRepo.delete(id) }

    fun getByTipoLive(tipo: String): LiveData<List<JugadaEntity>> = jugadaRepo.getByTipoLive(tipo)
}

// ─────────────────────────────────────────────
// VIEWMODEL: INVENTARIO
// ─────────────────────────────────────────────
@HiltViewModel
class InventarioViewModel @Inject constructor(
    private val inventarioRepo: InventarioRepository,
    private val productoRepo: ProductoBaseRepository
) : ViewModel() {

    val inventario: LiveData<List<InventarioItem>> = inventarioRepo.getAllLive()
    val productos: LiveData<List<ProductoBase>> = productoRepo.getAllLive()

    fun guardar(item: InventarioItem) = viewModelScope.launch { inventarioRepo.save(item) }
    fun eliminar(id: Long) = viewModelScope.launch { inventarioRepo.delete(id) }
}

// ─────────────────────────────────────────────
// VIEWMODEL: COMPRAS
// ─────────────────────────────────────────────
@HiltViewModel
class ComprasViewModel @Inject constructor(
    private val calcularUseCase: CalcularComprasUseCase,
    private val compraRepo: CompraRepository
) : ViewModel() {

    private val _compraCalculada = MutableLiveData<Compra?>()
    val compraCalculada: LiveData<Compra?> = _compraCalculada

    private val _pdfFile = MutableLiveData<File?>()
    val pdfFile: LiveData<File?> = _pdfFile

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    val historialCompras: LiveData<List<CompraEntity>> = compraRepo.getAllLive()

    fun calcular(fechaBingo: String) = viewModelScope.launch {
        _loading.value = true
        try {
            val compra = calcularUseCase(fechaBingo)
            _compraCalculada.value = compra
        } finally {
            _loading.value = false
        }
    }

    fun guardarYGenerarPdf(context: Context, compra: Compra) = viewModelScope.launch {
        _loading.value = true
        try {
            val id = compraRepo.saveCompra(compra)
            val compraGuardada = compraRepo.getCompraConItems(id)
            compraGuardada?.let {
                val file = PdfGenerator.generarComprasPdf(context, it)
                _pdfFile.value = file
            }
        } finally {
            _loading.value = false
        }
    }
}

// ─────────────────────────────────────────────
// VIEWMODEL: GASTOS
// ─────────────────────────────────────────────
@HiltViewModel
class GastosViewModel @Inject constructor(
    private val repo: GastoRepository
) : ViewModel() {

    val gastos: LiveData<List<Gasto>> = repo.getAllLive()
    val total: LiveData<Double?> = repo.getTotalLive()

    fun guardar(g: Gasto) = viewModelScope.launch {
        if (g.id == 0L) repo.insert(g) else repo.update(g)
    }

    fun eliminar(id: Long) = viewModelScope.launch { repo.delete(id) }
}

// ─────────────────────────────────────────────
// VIEWMODEL: BAR
// ─────────────────────────────────────────────
@HiltViewModel
class BarViewModel @Inject constructor(
    private val barRepo: BarRepository,
    private val productoRepo: ProductoBaseRepository
) : ViewModel() {

    val barItems: LiveData<List<BarItem>> = barRepo.getAllLive()
    val total: LiveData<Double?> = barRepo.getTotalLive()
    val productos: LiveData<List<ProductoBase>> = productoRepo.getAllLive()

    fun guardar(item: BarItem) = viewModelScope.launch {
        if (item.id == 0L) barRepo.insert(item) else barRepo.update(item)
    }

    fun eliminar(id: Long) = viewModelScope.launch { barRepo.delete(id) }
}

// ─────────────────────────────────────────────
// VIEWMODEL: SUMATORIA
// ─────────────────────────────────────────────
@HiltViewModel
class SumatoriaViewModel @Inject constructor(
    private val gastosRepo: GastoRepository,
    private val barRepo: BarRepository,
    private val compraRepo: CompraRepository
) : ViewModel() {

    val totalGastos: LiveData<Double?> = gastosRepo.getTotalLive()
    val totalBar: LiveData<Double?> = barRepo.getTotalLive()
    val historialCompras: LiveData<List<CompraEntity>> = compraRepo.getAllLive()

    private val _totalCompras = MutableLiveData(0.0)
    val totalCompras: LiveData<Double> = _totalCompras

    fun setTotalCompras(v: Double) { _totalCompras.value = v }

    val totalGeneral: LiveData<Double> = MediatorLiveData<Double>().apply {
        fun recalc() {
            val g = totalGastos.value ?: 0.0
            val b = totalBar.value ?: 0.0
            val c = _totalCompras.value ?: 0.0
            value = g + b + c
        }
        addSource(totalGastos) { recalc() }
        addSource(totalBar) { recalc() }
        addSource(_totalCompras) { recalc() }
    }
}

// placeholder alias para usar en fragments sin romper imports
typealias CanastaEntity = com.bingo.manager.data.local.entities.CanastaEntity
typealias JugadaEntity = com.bingo.manager.data.local.entities.JugadaEntity
typealias CompraEntity = com.bingo.manager.data.local.entities.CompraEntity
