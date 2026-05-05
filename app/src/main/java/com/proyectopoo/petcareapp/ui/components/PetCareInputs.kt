package com.proyectopoo.petcareapp.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proyectopoo.petcareapp.ui.theme.BordeCampo
import com.proyectopoo.petcareapp.ui.theme.CafeMedio
import com.proyectopoo.petcareapp.ui.theme.CafeOscuro
import com.proyectopoo.petcareapp.ui.theme.FondoCampo
import com.proyectopoo.petcareapp.ui.theme.TextoSuave

@Composable
fun CampoLogin(
    label: String,
    valor: String,
    placeholder: String,
    onChange: (String) -> Unit,
    esPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            color = CafeOscuro,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 6.dp, bottom = 6.dp)
        )

        OutlinedTextField(
            value = valor,
            onValueChange = onChange,
            placeholder = {
                Text(placeholder, color = TextoSuave)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            shape = RoundedCornerShape(14.dp),
            singleLine = true,
            visualTransformation = if (esPassword) {
                PasswordVisualTransformation()
            } else {
                androidx.compose.ui.text.input.VisualTransformation.None
            },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = CafeOscuro,
                unfocusedTextColor = CafeOscuro,
                cursorColor = CafeOscuro,
                focusedContainerColor = FondoCampo,
                unfocusedContainerColor = FondoCampo,
                focusedBorderColor = CafeMedio,
                unfocusedBorderColor = BordeCampo
            )
        )

        Spacer(modifier = Modifier.height(14.dp))
    }
}