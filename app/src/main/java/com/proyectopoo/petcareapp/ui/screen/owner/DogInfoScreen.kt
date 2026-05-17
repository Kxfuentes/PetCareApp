package com.proyectopoo.petcareapp.ui.screen.owner

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proyectopoo.petcareapp.ui.theme.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DogInfoScreen(
    onFinish: () -> Unit
) {

    var dogName by remember { mutableStateOf("") }
    var breed by remember { mutableStateOf("") }
    var selectedSize by remember { mutableStateOf("") }

    val sizes = listOf(
        "XS (1-5 kg)",
        "S (5-10 kg)",
        "M (10-20 kg)",
        "L (20-40 kg)",
        "XL (>40 kg)"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp)
    ) {

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "Cuéntanos sobre tu perro",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = CafeOscuro
        )

        Spacer(modifier = Modifier.height(30.dp))

        OutlinedTextField(
            value = dogName,
            onValueChange = { dogName = it },
            label = {
                Text("Nombre del perro")
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Pets,
                    contentDescription = "Dog",
                    tint = CafeMedio
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = FondoCampo,
                unfocusedContainerColor = FondoCampo,
                focusedBorderColor = CafeMedio,
                unfocusedBorderColor = BordeCampo
            )
        )

        Spacer(modifier = Modifier.height(18.dp))

        OutlinedTextField(
            value = breed,
            onValueChange = { breed = it },
            label = {
                Text("Raza")
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = FondoCampo,
                unfocusedContainerColor = FondoCampo,
                focusedBorderColor = CafeMedio,
                unfocusedBorderColor = BordeCampo
            )
        )

        Spacer(modifier = Modifier.height(26.dp))

        Text(
            text = "Tamaño",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = CafeOscuro
        )

        Spacer(modifier = Modifier.height(18.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            sizes.forEach { size ->

                val isSelected = selectedSize == size

                Surface(
                    modifier = Modifier.border(
                        width = 2.dp,
                        color = CafeClaro,
                        shape = RoundedCornerShape(50.dp)
                    ),
                    shape = RoundedCornerShape(50.dp),
                    color =
                        if (isSelected)
                            CafeClaro
                        else
                            FondoCrema,
                    onClick = {
                        selectedSize = size
                    }
                ) {

                    Text(
                        text = size,
                        modifier = Modifier.padding(
                            horizontal = 18.dp,
                            vertical = 12.dp
                        ),
                        color = CafeOscuro,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        TextButton(
            onClick = { }
        ) {

            Text(
                text = "+ Agregar otro perro",
                color = CafeMedio,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onFinish,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CafeMedio
            )
        ) {

            Text(
                text = "Registrarme",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}