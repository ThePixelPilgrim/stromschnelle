package de.nereide.stromschnelle.ui.common

import android.app.Application
import androidx.lifecycle.viewmodel.CreationExtras
import de.nereide.stromschnelle.AppContainer
import de.nereide.stromschnelle.StromschnelleApp

/** Resolves the [Application] from any [CreationExtras] used by a ViewModel factory. */
fun CreationExtras.application(): Application =
    this[androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application

/** Resolves the app's [AppContainer] from any [CreationExtras] used by a ViewModel factory. */
fun CreationExtras.appContainer(): AppContainer =
    (application() as StromschnelleApp).container
