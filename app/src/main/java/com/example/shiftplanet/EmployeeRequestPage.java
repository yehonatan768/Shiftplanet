package com.example.shiftplanet;

import static android.content.ContentValues.TAG;
import static android.widget.Toast.LENGTH_SHORT;

import android.util.Base64;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EmployeeRequestPage extends AppCompatActivity {

    String[] requestTypes = {"Sick Day", "Vacation"};

    AutoCompleteTextView autoCompleteTextView;
    ArrayAdapter<String> adapterItems;
    private EditText startDateEditText, endDateEditText, detailsEditText;
    private AutoCompleteTextView reasonDropdown;
    private String managerEmail;
    private int businessCode;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private FirebaseUser current = FirebaseAuth.getInstance().getCurrentUser();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String employeeEmail;


    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAPTURE_IMAGE_REQUEST = 2;
    private FirebaseUser currentUser;
    private String imageBase64 = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String email = getIntent().getStringExtra("LOGIN_EMAIL");

        if (email != null) {
            Log.d(TAG, "Received email: " + email);
            employeeEmail = email;
        } else {
            Log.e(TAG, "No email received");
        }
        setContentView(R.layout.employee_request_page);
        initializeUI();


        startDateEditText.setOnClickListener(v -> showDatePicker((date) -> startDateEditText.setText(date)));
        endDateEditText.setOnClickListener(v -> showDatePicker((date) -> endDateEditText.setText(date)));


        drawerLayout = findViewById(R.id.employee_request_drawer_layout);
        navigationView = findViewById(R.id.employee_request_nav_view);
        toolbar = findViewById(R.id.employee_request_toolbar);


        setSupportActionBar(toolbar);


        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();


        navigationView.setNavigationItemSelectedListener(menuItem -> {
            handleNavigationItemSelected(menuItem);
            drawerLayout.closeDrawer(Gravity.LEFT);
            return true;
        });


        adapterItems = new ArrayAdapter<>(this, R.layout.request_type_list, requestTypes);
        autoCompleteTextView.setAdapter(adapterItems);
        autoCompleteTextView.setOnItemClickListener((parent, view, position, id) -> {
            String item = parent.getItemAtPosition(position).toString();
            Toast.makeText(EmployeeRequestPage.this, "Selected: " + item, LENGTH_SHORT).show();
        });


        Button submitRequestButton = findViewById(R.id.submit_request_button);
        submitRequestButton.setOnClickListener(v -> submitRequest());

        Button addDocumentButton = findViewById(R.id.add_document_button);
        addDocumentButton.setOnClickListener(v -> showImagePickerDialog());

    }

    private void initializeUI() {
        startDateEditText = findViewById(R.id.start_date);
        endDateEditText = findViewById(R.id.end_date);
        detailsEditText = findViewById(R.id.details);
        autoCompleteTextView = findViewById(R.id.autoCompleteRequestType);
    }
    private void showImagePickerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("בחר אפשרות")
                .setItems(new CharSequence[]{"צלם תמונה", "בחר מהגלריה"}, (dialog, which) -> {
                    if (which == 0) {
                        openCamera();
                    } else {
                        openGallery();
                    }
                })
                .show();
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, CAPTURE_IMAGE_REQUEST);
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Bitmap bitmap = null;
            if (requestCode == PICK_IMAGE_REQUEST && data != null) {
                Uri imageUri = data.getData();
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (requestCode == CAPTURE_IMAGE_REQUEST && data.getExtras() != null) {
                bitmap = (Bitmap) data.getExtras().get("data");
            }
            if (bitmap != null) {
                compressImage(bitmap);
            }
        }
    }

    private void compressImage(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        byte[] imageData = baos.toByteArray();
        imageBase64 = Base64.encodeToString(imageData, Base64.DEFAULT);
    }

    private void submitRequest() {
        // קבלת הנתונים מה-UI
        String requestType = autoCompleteTextView.getText().toString().trim();
        String startDate = startDateEditText.getText().toString().trim();
        String endDate = endDateEditText.getText().toString().trim();
        String details = detailsEditText.getText().toString().trim();
        String employeeEmail = currentUser.getEmail();

        // בדיקה שכל השדות מולאו
        if (requestType.isEmpty() || startDate.isEmpty() || endDate.isEmpty()) {
            Toast.makeText(this, "please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // שליפת פרטי המשתמש מקולקציית "users"
        db.collection("users").document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // קבלת הנתונים מהמסמך
                    String businessCode = documentSnapshot.getString("businessCode");
                    String managerEmail = documentSnapshot.getString("managerEmail");

                    // בדיקה שהערכים אינם ריקים
                    if (businessCode == null || managerEmail == null) {
                        Toast.makeText(this, "Missing user details", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // קבלת מספר הבקשה
                    getNextRequestNumber(requestNumber -> {
                        if (requestNumber == -1) {
                            Toast.makeText(EmployeeRequestPage.this, "Error generating request number", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // יצירת האובייקט עם כל הנתונים
                        Map<String, Object> request = new HashMap<>();
                        request.put("requestType", requestType);
                        request.put("startDate", startDate);
                        request.put("endDate", endDate);
                        request.put("details", details);
                        request.put("status", "pending");
                        request.put("employeeEmail", employeeEmail);
                        request.put("managerEmail", managerEmail);
                        request.put("businessCode", businessCode);
                        request.put("requestNumber", requestNumber);
                        request.put("timestamp", FieldValue.serverTimestamp());

                        // הוספת תמונה אם קיימת
                        if (imageBase64 != null) {
                            request.put("imageBase64", imageBase64);
                        }

                        // שליחת הנתונים לפיירבייס
                        db.collection("Requests")
                                .add(request)
                                .addOnSuccessListener(documentReference ->
                                        Toast.makeText(EmployeeRequestPage.this, "Request submitted successfully", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e ->
                                        Toast.makeText(EmployeeRequestPage.this, "Error submitting request", Toast.LENGTH_SHORT).show());
                    });
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error fetching user details", Toast.LENGTH_SHORT).show()
                );
    }


    private void getNextRequestNumber(OnRequestNumberGeneratedListener listener) {
        db.collection("RequestCounters").document("RequestsCounter")
                .update("counter", FieldValue.increment(1))
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        db.collection("RequestCounters").document("RequestsCounter")
                                .get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (documentSnapshot.exists()) {
                                        long counter = documentSnapshot.getLong("counter");
                                        listener.onRequestNumberGenerated(counter);
                                    } else {
                                        listener.onRequestNumberGenerated(-1);
                                    }
                                });
                    } else {
                        listener.onRequestNumberGenerated(-1);
                    }
                });
    }

    interface OnRequestNumberGeneratedListener {
        void onRequestNumberGenerated(long requestNumber);
    }

    private void handleNavigationItemSelected(MenuItem item) {
        Intent intent = null;
        if (item.getItemId() == R.id.e_home_page) {
            Toast.makeText(EmployeeRequestPage.this, "Home Page clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeRequestPage.this, EmployeeHomePage.class);
            intent.putExtra("LOGIN_EMAIL", employeeEmail);
        } else if (item.getItemId() == R.id.e_my_profile) {
            Toast.makeText(EmployeeRequestPage.this, "My profile clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeRequestPage.this, EmployeeProfile.class);
            intent.putExtra("LOGIN_EMAIL", employeeEmail);
        } else if (item.getItemId() == R.id.e_work_arrangement) {
            Toast.makeText(EmployeeRequestPage.this, "Work arrangement clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeRequestPage.this, EmployeeHomePage.class);
            intent.putExtra("LOGIN_EMAIL", employeeEmail);
        } else if (item.getItemId() == R.id.constraints) {
            Toast.makeText(EmployeeRequestPage.this, "Constraints clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeRequestPage.this, EmployeeSubmitConstraintsPage.class);
            intent.putExtra("LOGIN_EMAIL", employeeEmail);
        } else if (item.getItemId() == R.id.day_off) {
            Toast.makeText(EmployeeRequestPage.this, "Day off clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeRequestPage.this, EmployeeRequestPage.class);
            intent.putExtra("LOGIN_EMAIL", employeeEmail);
        } else if (item.getItemId() == R.id.shift_change) {
            Toast.makeText(EmployeeRequestPage.this, "Shift change clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeRequestPage.this, EmployeeShiftChangeRequest.class);
            intent.putExtra("LOGIN_EMAIL", employeeEmail);
        } else if (item.getItemId() == R.id.requests_status) {
            Toast.makeText(EmployeeRequestPage.this, "Requests status clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeRequestPage.this, EmployeeRequestStatus.class);
            intent.putExtra("LOGIN_EMAIL", employeeEmail);
        } else if (item.getItemId() == R.id.notification) {
            Toast.makeText(EmployeeRequestPage.this, "Notifications clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeRequestPage.this, EmployeeNotificationsPage.class);
            intent.putExtra("LOGIN_EMAIL", employeeEmail);
        } else if (item.getItemId() == R.id.e_log_out) {
            Toast.makeText(EmployeeRequestPage.this, "Log out clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeRequestPage.this, Login.class);
            intent.putExtra("LOGIN_EMAIL", employeeEmail);
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        startActivity(intent);
        finish();
    }

    private void showDatePicker(OnDateSelectedListener listener) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
            String date = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
            listener.onDateSelected(date);
        }, year, month, day).show();
    }

    interface OnDateSelectedListener {
        void onDateSelected(String date);
    }

    static boolean requestFieldsCheck(String requestType, String startDate, String endDate ) {
        if (requestType.isEmpty()) {
            return false;
        }
        if (startDate.isEmpty()) {
            return false;
        }
        if (endDate.isEmpty()) {
            return false;
        }
        else{
            return true;
        }
    }

}