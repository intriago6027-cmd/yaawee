package com.bingo.manager

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Clase Application principal.
 * @HiltAndroidApp inicializa el grafo de dependencias de Hilt.
 */
@HiltAndroidApp
class BingoApp : Application()
