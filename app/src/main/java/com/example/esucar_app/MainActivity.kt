package com.example.esucar_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.esucar_app.ui.AppNavigation
import com.example.esucar_app.ui.theme.ESUCAR_APPTheme
import com.example.esucar_app.util.NotificacionHelper
import com.example.esucar_app.viewmodel.TutorViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        NotificacionHelper.crearCanalNotificaciones(this)

        setContent {
            ESUCAR_APPTheme(darkTheme = false, dynamicColor = false) {
                val tutorViewModel: TutorViewModel = viewModel(
                    factory = TutorViewModel.provideFactory(applicationContext)
                )
                AppNavigation(viewModel = tutorViewModel)
            }
        }
    }
}