package com.example.medicinereminder;

import android.app.TimePickerDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private EditText editMedicineName, editDosage;
    private Button buttonTime, buttonSave;
    private ListView listMedicines;

    private DatabaseHelper databaseHelper;
    private final ArrayList<Integer> medicineIds = new ArrayList<>();
    private final ArrayList<String> medicineRecords = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    private String scheduledTime = "";
    private int selectedId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editMedicineName = findViewById(R.id.editMedicineName);
        editDosage = findViewById(R.id.editDosage);
        buttonTime = findViewById(R.id.buttonTime);
        buttonSave = findViewById(R.id.buttonSave);
        listMedicines = findViewById(R.id.listMedicines);

        databaseHelper = new DatabaseHelper(this);

        adapter = new ArrayAdapter<>(
                this,
                R.layout.item_medicine,
                R.id.textMedicineDetails,
                medicineRecords
        );

        listMedicines.setAdapter(adapter);

        buttonTime.setOnClickListener(v -> showTimePicker());
        buttonSave.setOnClickListener(v -> saveMedicine());

        listMedicines.setOnItemClickListener((parent, view, position, id) ->
                showRecordOptions(medicineIds.get(position))
        );

        loadMedicines();
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();

        TimePickerDialog dialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selected.set(Calendar.MINUTE, minute);

                    SimpleDateFormat format =
                            new SimpleDateFormat("hh:mm a", Locale.getDefault());

                    scheduledTime = format.format(selected.getTime());
                    buttonTime.setText(scheduledTime);
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
        );

        dialog.show();
    }

    private void saveMedicine() {
        String name = editMedicineName.getText().toString().trim();
        String dosage = editDosage.getText().toString().trim();

        if (name.isEmpty() || dosage.isEmpty() || scheduledTime.isEmpty()) {
            Toast.makeText(
                    this,
                    "Please enter all medicine details",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        boolean success;

        if (selectedId == -1) {
            success = databaseHelper.insertMedicine(name, dosage, scheduledTime);
        } else {
            success = databaseHelper.updateMedicine(
                    selectedId, name, dosage, scheduledTime
            );
        }

        if (success) {
            Toast.makeText(
                    this,
                    selectedId == -1
                            ? "Medicine added successfully"
                            : "Medicine updated successfully",
                    Toast.LENGTH_SHORT
            ).show();

            clearForm();
            loadMedicines();
        } else {
            Toast.makeText(this, "Operation failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadMedicines() {
        medicineIds.clear();
        medicineRecords.clear();

        try (Cursor cursor = databaseHelper.getAllMedicines()) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String name = cursor.getString(
                        cursor.getColumnIndexOrThrow("name"));
                String dosage = cursor.getString(
                        cursor.getColumnIndexOrThrow("dosage"));
                String time = cursor.getString(
                        cursor.getColumnIndexOrThrow("scheduled_time"));

                medicineIds.add(id);
                medicineRecords.add(
                        "Medicine: " + name +
                                "\nDosage: " + dosage +
                                "\nScheduled Time: " + time +
                                "\nTap to edit or delete"
                );
            }
        }

        adapter.notifyDataSetChanged();
    }

    private void showRecordOptions(int medicineId) {
        new AlertDialog.Builder(this)
                .setTitle("Medicine Record")
                .setItems(new String[]{"Update", "Delete"}, (dialog, which) -> {
                    if (which == 0) {
                        editMedicine(medicineId);
                    } else {
                        confirmDelete(medicineId);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void editMedicine(int medicineId) {
        try (Cursor cursor = databaseHelper.getAllMedicines()) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));

                if (id == medicineId) {
                    editMedicineName.setText(
                            cursor.getString(cursor.getColumnIndexOrThrow("name"))
                    );

                    editDosage.setText(
                            cursor.getString(cursor.getColumnIndexOrThrow("dosage"))
                    );

                    scheduledTime = cursor.getString(
                            cursor.getColumnIndexOrThrow("scheduled_time")
                    );

                    buttonTime.setText(scheduledTime);
                    selectedId = medicineId;
                    buttonSave.setText(R.string.btn_update_medicine);
                    return;
                }
            }
        }
    }

    private void confirmDelete(int medicineId) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Medicine")
                .setMessage("Are you sure you want to delete this record?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    boolean success = databaseHelper.deleteMedicine(medicineId);

                    if (success) {
                        Toast.makeText(
                                this,
                                "Medicine deleted successfully",
                                Toast.LENGTH_SHORT
                        ).show();

                        if (selectedId == medicineId) {
                            clearForm();
                        }

                        loadMedicines();
                    } else {
                        Toast.makeText(
                                this,
                                "Unable to delete medicine",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void clearForm() {
        editMedicineName.setText("");
        editDosage.setText("");
        scheduledTime = "";
        selectedId = -1;
        buttonTime.setText(R.string.btn_select_time);
        buttonSave.setText(R.string.btn_add_medicine);
    }

    @Override
    protected void onDestroy() {
        if (databaseHelper != null) {
            databaseHelper.close();
        }
        super.onDestroy();
    }
}
