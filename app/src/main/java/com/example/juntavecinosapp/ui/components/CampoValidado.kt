package com.example.juntavecinosapp.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun CampoValidado(
    valor: String,
    onValorChange: (String) -> Unit,
    etiqueta: String,
    error: String?,
    iconoInicial: ImageVector,
    modifier: Modifier = Modifier,
    prefijo: String? = null,
    tipoTeclado: KeyboardType = KeyboardType.Text,
    capitalizacion: KeyboardCapitalization = KeyboardCapitalization.Sentences,
    maxLineas: Int = 1,
    soloLectura: Boolean = false
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onValorChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(etiqueta) },
        leadingIcon = { Icon(iconoInicial, contentDescription = null) },
        trailingIcon = {
            AnimatedVisibility(
                visible = error != null,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                Icon(
                    imageVector = Icons.Filled.Error,
                    contentDescription = "Campo con error",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        },
        prefix = prefijo?.let { texto -> @androidx.compose.runtime.Composable { Text(texto) } },
        supportingText = {
            AnimatedContent(
                targetState = error,
                transitionSpec = {
                    (fadeIn(tween(200)) + expandVertically()) togetherWith fadeOut(tween(100))
                },
                label = "errorCampo"
            ) { mensaje ->
                if (mensaje != null) Text(mensaje) else Spacer(Modifier.height(0.dp))
            }
        },
        isError = error != null,
        readOnly = soloLectura,
        singleLine = maxLineas == 1,
        maxLines = maxLineas,
        keyboardOptions = KeyboardOptions(
            keyboardType = tipoTeclado,
            capitalization = capitalizacion
        )
    )
}