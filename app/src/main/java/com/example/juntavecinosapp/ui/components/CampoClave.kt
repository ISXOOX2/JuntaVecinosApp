package com.example.juntavecinosapp.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

/**
 * Campo de clave: oculta lo escrito y tiene un ojito para mostrarlo.
 * El error se muestra con el mismo estilo animado que CampoValidado.
 */
@Composable
fun CampoClave(
    valor: String,
    onValorChange: (String) -> Unit,
    etiqueta: String,
    error: String?,
    modifier: Modifier = Modifier
) {
    var visible by rememberSaveable { mutableStateOf(false) }

    OutlinedTextField(
        value = valor,
        onValueChange = onValorChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(etiqueta) },
        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
        trailingIcon = {
            IconButton(onClick = { visible = !visible }) {
                Icon(
                    imageVector = if (visible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                    contentDescription = if (visible) "Ocultar clave" else "Mostrar clave"
                )
            }
        },
        supportingText = {
            AnimatedContent(
                targetState = error,
                transitionSpec = {
                    (fadeIn(tween(200)) + expandVertically()) togetherWith fadeOut(tween(100))
                },
                label = "errorClave"
            ) { mensaje ->
                if (mensaje != null) Text(mensaje) else Spacer(Modifier.height(0.dp))
            }
        },
        isError = error != null,
        singleLine = true,
        visualTransformation = if (visible) VisualTransformation.None
        else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
    )
}