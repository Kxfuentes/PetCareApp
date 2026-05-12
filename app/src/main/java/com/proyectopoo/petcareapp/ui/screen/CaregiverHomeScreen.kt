package com.proyectopoo.petcareapp.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proyectopoo.petcareapp.ui.theme.*

@Composable
fun CaregiverHomeScreen(
    onGoToProfile: () -> Unit
) {

    var available by remember {
        mutableStateOf(true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {

        Surface(
            color = CafeOscuro,
            modifier = Modifier.fillMaxWidth()
        ) {

            Text(
                text = "Bienvenido",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(24.dp)
            )
        }

        Column(
            modifier = Modifier.padding(20.dp)
        ) {

            Row {

                AssistChip(
                    onClick = {
                        available = !available
                    },

                    label = {

                        Text(
                            if (available)
                                "Disponible"
                            else
                                "No disponible"
                        )
                    },

                    colors = AssistChipDefaults.assistChipColors(
                        containerColor =
                            if (available)
                                CafeMedio
                            else
                                CafeClaro,

                        labelColor =
                            if (available)
                                Color.White
                            else
                                CafeOscuro
                    )
                )

                Spacer(modifier = Modifier.width(10.dp))

                IconButton(
                    onClick = { }
                ) {

                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = CafeMedio
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Próximos servicios",
                color = CafeOscuro,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(18.dp))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = FondoClaro
                ),
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        2.dp,
                        CafeClaro,
                        RoundedCornerShape(22.dp)
                    )
            ) {

                Row(
                    modifier = Modifier.padding(20.dp)
                ) {

                    Icon(
                        Icons.Default.DirectionsWalk,
                        contentDescription = null,
                        tint = CafeMedio
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "Paseo con Max · Hoy 3PM",
                        color = CafeOscuro,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            Button(
                onClick = { },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CafeMedio
                )
            ) {

                Text(
                    text = "Gestionar mis servicios",
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedButton(
                onClick = onGoToProfile,
                modifier = Modifier.fillMaxWidth(),
                border = ButtonDefaults.outlinedButtonBorder,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = CafeMedio
                )
            ) {

                Text("Ver mi perfil público")
            }
        }
    }
}