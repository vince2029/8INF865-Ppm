package com.example.mpp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mpp.data.API
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

enum class Taille { PETIT, MOYEN, GRAND }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewDog(goToHome: () -> Unit){
    var nom by remember { mutableStateOf("") }
    var age by remember { mutableStateOf(0) }
    var taille by remember { mutableStateOf(Taille.MOYEN) }
    var niveauEnergie by remember { mutableStateOf(3) }
    var estTimide by remember { mutableStateOf(false) }
    var tailleMenuExpanded by remember { mutableStateOf(false) }
    val errorMessage =
        when {
            nom.isBlank() -> "Veuillez entrer un nom."
            age <= 0 -> "Veuillez entrer un âge valide."
            age >= 20 -> "L'âge maximum permis est 20 ans."
            else -> null
        }


    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Ajouter un chien",
            style = MaterialTheme.typography.headlineMedium
        )

        // Nom
        OutlinedTextField(
            value = nom,
            onValueChange = { nom = it },
            label = { Text("Nom") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // Âge
        OutlinedTextField(
            value = if (age == 0) "" else age.toString(),
            onValueChange = { age = it.filter { c -> c.isDigit() }.toIntOrNull() ?: 0 },
            label = { Text("Âge (années)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        // Taille
        ExposedDropdownMenuBox(
            expanded = tailleMenuExpanded,
            onExpandedChange = { tailleMenuExpanded = !tailleMenuExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = taille.name.lowercase().replaceFirstChar { it.uppercase() },
                onValueChange = {},
                readOnly = true,
                label = { Text("Taille") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = tailleMenuExpanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = tailleMenuExpanded,
                onDismissRequest = { tailleMenuExpanded = false }
            ) {
                Taille.entries.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.name.lowercase().replaceFirstChar { it.uppercase() }) },
                        onClick = {
                            taille = option
                            tailleMenuExpanded = false
                        }
                    )
                }
            }
        }

        // Niveau d'énergie
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Niveau d'énergie : ${niveauEnergie} / 5")
            Slider(
                value = niveauEnergie.toFloat(),
                onValueChange = { niveauEnergie = it.toInt() },
                valueRange = 1f..5f,
                steps = 3,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Est timide
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Est timide")
            Switch(
                checked = estTimide,
                onCheckedChange = { estTimide = it }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error
            )
        }


        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    coroutineScope.launch {
                        val dog = API.createNewDog(
                            name = nom,
                            age = age,
                            size = taille,
                            energyLevel = niveauEnergie,
                            isShy = estTimide
                        )
                        goToHome()
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = nom.isNotBlank() && 0 < age && age < 20
            ) {
                Text("Enregistrer")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddDogPreview() {
    NewDog(goToHome = {})
}