package com.example.mpp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mpp.data.API
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewActivity(
    onClose: () -> Unit,
    onCreated: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var locationName by remember { mutableStateOf("") }
    var maxParticipants by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val timePickerState = rememberTimePickerState()
    var tempDateMillis by remember { mutableStateOf<Long?>(null) }
    var selectedDateDisplay by remember { mutableStateOf("") }
    var selectedDateApiFormat by remember { mutableStateOf("") }
    var allowShyDogs by remember { mutableStateOf(true) }
    var energyRange by remember { mutableStateOf(1f..5f) }
    val dogSizes = listOf("PETIT", "MOYEN", "GRAND")
    var minDogSizeExpanded by remember { mutableStateOf(false) }
    var maxDogSizeExpanded by remember { mutableStateOf(false) }
    var minDogSize by remember { mutableStateOf(dogSizes.first()) }
    var maxDogSize by remember { mutableStateOf(dogSizes.last()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    var showDiscardChangesDialog by remember { mutableStateOf(false) }

    val hasEdits by remember(
        title,
        description,
        locationName,
        maxParticipants,
        selectedDateDisplay,
        selectedDateApiFormat,
        allowShyDogs,
        energyRange,
        minDogSize,
        maxDogSize
    ) {
        derivedStateOf {
            title.isNotBlank() ||
                description.isNotBlank() ||
                locationName.isNotBlank() ||
                maxParticipants.isNotBlank() ||
                selectedDateDisplay.isNotBlank() ||
                selectedDateApiFormat.isNotBlank() ||
                !allowShyDogs ||
                energyRange.start != 1f ||
                energyRange.endInclusive != 5f ||
                minDogSize != dogSizes.first() ||
                maxDogSize != dogSizes.last()
        }
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
                title = { Text("Créer une activité") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (hasEdits) {
                            showDiscardChangesDialog = true
                        } else {
                            onClose()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fermer"
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
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Proposer une balade",
                style = MaterialTheme.typography.headlineMedium
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Titre de la balade") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            OutlinedTextField(
                value = locationName,
                onValueChange = { locationName = it },
                label = { Text("Lieu de la balade") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = DividerDefaults.Thickness,
                color = DividerDefaults.color
            )
            Text("Critères de la meute", style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.Start))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Accepter les chiens timides ?")
                Switch(checked = allowShyDogs, onCheckedChange = { allowShyDogs = it })
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Niveau d'énergie : de ${energyRange.start.toInt()} à ${energyRange.endInclusive.toInt()}")
                RangeSlider(
                    value = energyRange,
                    onValueChange = { energyRange = it },
                    valueRange = 1f..5f,
                    steps = 3
                )
            }

            ExposedDropdownMenuBox(
                expanded = minDogSizeExpanded,
                onExpandedChange = { minDogSizeExpanded = !minDogSizeExpanded }
            ) {
            OutlinedTextField(
                value = minDogSize,
                onValueChange = {},
                readOnly = true,
                label = { Text("Taille minimum") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = minDogSizeExpanded) },
                modifier = Modifier.menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = minDogSizeExpanded,
                onDismissRequest = { minDogSizeExpanded = false }
            ) {
                dogSizes.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(selectionOption) },
                        onClick = {
                            minDogSize = selectionOption
                            minDogSizeExpanded = false
                        }
                    )
                }
            }
            }

            ExposedDropdownMenuBox(
                expanded = maxDogSizeExpanded,
                onExpandedChange = { maxDogSizeExpanded = !maxDogSizeExpanded }
            ) {
            OutlinedTextField(
                value = maxDogSize,
                onValueChange = {},
                readOnly = true,
                label = { Text("Taille maximum") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = maxDogSizeExpanded) },
                modifier = Modifier.menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = maxDogSizeExpanded,
                onDismissRequest = { maxDogSizeExpanded = false }
            ) {
                dogSizes.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(selectionOption) },
                        onClick = {
                            maxDogSize = selectionOption
                            maxDogSizeExpanded = false
                        }
                    )
                }
            }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = DividerDefaults.Thickness,
                color = DividerDefaults.color
            )

            OutlinedTextField(
                value = selectedDateDisplay,
                onValueChange = { },
                label = { Text("Date et Heure") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Choisir une date")
                    }
                }
            )

            if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            if (millis < System.currentTimeMillis() - 86400000) {
                                errorMessage = "La date ne peut pas être dans le passé."
                            } else {
                                errorMessage = null
                                tempDateMillis = millis
                                showDatePicker = false
                                showTimePicker = true
                            }
                        }
                    }) { Text("Suivant") }
                },
                dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Annuler") } }
            ) { DatePicker(state = datePickerState) }
            }

            if (showTimePicker) {
            AlertDialog(
                onDismissRequest = { showTimePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        tempDateMillis?.let { millis ->
                            val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                            utcCal.timeInMillis = millis
                            val localCal = Calendar.getInstance()
                            localCal.set(utcCal.get(Calendar.YEAR), utcCal.get(Calendar.MONTH), utcCal.get(Calendar.DAY_OF_MONTH), timePickerState.hour, timePickerState.minute, 0)

                            val displayFormatter = SimpleDateFormat("dd/MM/yyyy 'à' HH:mm", Locale.getDefault())
                            selectedDateDisplay = displayFormatter.format(localCal.time)
                            val apiFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:00.000'Z'", Locale.getDefault())
                            apiFormatter.timeZone = TimeZone.getTimeZone("UTC")
                            selectedDateApiFormat = apiFormatter.format(localCal.time)
                        }
                        showTimePicker = false
                    }) { Text("OK") }
                },
                dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("Annuler") } },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Sélectionnez l'heure", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(16.dp))
                        TimePicker(state = timePickerState)
                    }
                }
            )
            }

            OutlinedTextField(
                value = maxParticipants,
                onValueChange = { maxParticipants = it },
                label = { Text("Nombre de participants max") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            errorMessage?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (title.isBlank() || locationName.isBlank() || selectedDateApiFormat.isBlank() || maxParticipants.isBlank()) {
                        errorMessage = "Veuillez remplir tous les champs obligatoires."
                        return@Button
                    }

                    val participantsInt = maxParticipants.toIntOrNull()
                    if (participantsInt == null || participantsInt <= 0) {
                        errorMessage = "Le nombre de participants doit être un nombre valide."
                        return@Button
                    }

                    isLoading = true
                    errorMessage = null

                    coroutineScope.launch {
                        val success = API.createActivity(
                            title = title,
                            description = description,
                            locationName = locationName,
                            dateTime = selectedDateApiFormat,
                            maxParticipants = participantsInt,
                            minEnergyLevel = energyRange.start.toInt(),
                            maxEnergyLevel = energyRange.endInclusive.toInt(),
                            allowShyDogs = allowShyDogs,
                            minDogSize = minDogSize,
                            maxDogSize = maxDogSize
                        )

                        isLoading = false

                        if (success) {
                            onCreated()
                        } else {
                            errorMessage = "Erreur lors de la création de la balade."
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Créer l'événement")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NewActivityPreview() {
    NewActivity(
        onClose = {},
        onCreated = {}
    )
}
