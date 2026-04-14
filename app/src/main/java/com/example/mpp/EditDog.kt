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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.mpp.data.API
import com.example.mpp.data.models.dog.DogModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDogScreen(onClose: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    val dogSizes = listOf(Taille.PETIT, Taille.MOYEN, Taille.GRAND)

    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var dog by remember { mutableStateOf<DogModel?>(null) }
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf(0) }
    var size by remember { mutableStateOf(Taille.MOYEN) }
    var energyLevel by remember { mutableStateOf(3) }
    var isShy by remember { mutableStateOf(false) }
    var sizeMenuExpanded by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var showDiscardChangesDialog by remember { mutableStateOf(false) }

    val hasEdits by remember(name, age, size, energyLevel, isShy, dog) {
        mutableStateOf(
            dog != null && (
                name != dog?.name ||
                    age != dog?.age ||
                    size.name != dog?.size ||
                    energyLevel != dog?.energyLevel ||
                    isShy != dog?.isShy
                )
        )
    }

    LaunchedEffect(Unit) {
        dog = API.getDog(API.currentUserId ?: "")
        dog?.let {
            name = it.name
            age = it.age
            size = runCatching { Taille.valueOf(it.size.uppercase()) }.getOrDefault(Taille.MOYEN)
            energyLevel = it.energyLevel
            isShy = it.isShy
        }
        isLoading = false
    }

    val errorMessage = when {
        name.isBlank() -> "Veuillez entrer un nom."
        age <= 0 -> "Veuillez entrer un âge valide."
        age >= 20 -> "L'âge maximum permis est 20 ans."
        energyLevel !in 1..5 -> "Le niveau d'énergie doit être entre 1 et 5."
        else -> null
    }

    if (showDiscardChangesDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardChangesDialog = false },
            title = { Text("Êtes-vous sûr ?") },
            text = { Text("Les modifications non enregistrées seront perdues.") },
            confirmButton = {
                TextButton(onClick = {
                    showDiscardChangesDialog = false
                    onClose()
                }) {
                    Text("Quitter")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardChangesDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Modifier mon chien") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (hasEdits) {
                            showDiscardChangesDialog = true
                        } else {
                            onClose()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        message = null
                    },
                    label = { Text("Nom") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = name.isNotBlank() && name.isBlank()
                )

                OutlinedTextField(
                    value = if (age == 0) "" else age.toString(),
                    onValueChange = {
                        age = it.filter { c -> c.isDigit() }.toIntOrNull() ?: 0
                        message = null
                    },
                    label = { Text("Âge (années)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    isError = age <= 0 || age >= 20,
                    supportingText = {
                        if (age <= 0) {
                            Text("Veuillez entrer un âge valide")
                        } else if (age >= 20) {
                            Text("L'âge maximum permis est 20 ans.")
                        }
                    }
                )

                ExposedDropdownMenuBox(
                    expanded = sizeMenuExpanded,
                    onExpandedChange = { sizeMenuExpanded = !sizeMenuExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = size.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Taille") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sizeMenuExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = sizeMenuExpanded,
                        onDismissRequest = { sizeMenuExpanded = false }
                    ) {
                        dogSizes.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.name) },
                                onClick = {
                                    size = option
                                    sizeMenuExpanded = false
                                    message = null
                                }
                            )
                        }
                    }
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Niveau d'énergie : ${energyLevel} / 5")
                    Slider(
                        value = energyLevel.toFloat(),
                        onValueChange = {
                            energyLevel = it.toInt().coerceIn(1, 5)
                            message = null
                        },
                        valueRange = 1f..5f,
                        steps = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Est timide")
                    Switch(
                        checked = isShy,
                        onCheckedChange = {
                            isShy = it
                            message = null
                        }
                    )
                }

                errorMessage?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                message?.let {
                    Text(
                        text = it,
                        color = if (it.startsWith("Échec")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }

                Button(
                    onClick = {
                        if (errorMessage != null) {
                            message = errorMessage
                            return@Button
                        }

                        coroutineScope.launch {
                            isSaving = true
                            val success = API.updateDog(
                                name = name.trim(),
                                age = age,
                                size = size,
                                energyLevel = energyLevel,
                                isShy = isShy
                            )
                            if (success) {
                                onClose()
                            } else {
                                message = "Échec de la mise à jour"
                            }
                            isSaving = false
                        }
                    },
                    enabled = !isSaving,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isSaving) "Enregistrement..." else "Enregistrer")
                }
            }
        }
    }
}
