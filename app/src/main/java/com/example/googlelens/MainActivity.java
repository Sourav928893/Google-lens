package com.example.googlelens;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String API_KEY = "bcefa083ee4037d4d0c11a52a8be1f6ef76a3d9fe14369b37465c640337027d1";
    private static final String LOCATION = "Delhi,India";

    private ImageView img;
    private Button snap, searchResultsBtn;
    private Bitmap imageBitmap;
    private RecyclerView resultRV;
    private SearchResultsRVAdapter searchResultsRVAdapter;
    private ArrayList<dataModal> dataModalArrayList;
    private ActivityResultLauncher<Intent> takeImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        initializeRecyclerView();
        setupTakeImageLauncher();

        snap.setOnClickListener(v -> dispatchTakePictureIntent());
        searchResultsBtn.setOnClickListener(v -> processCapturedImage());
    }

    private void initializeViews() {
        img = findViewById(R.id.image);
        snap = findViewById(R.id.snapbtn);
        searchResultsBtn = findViewById(R.id.idBtnSearchResuts);
        resultRV = findViewById(R.id.idRVSearchResults);
        dataModalArrayList = new ArrayList<>();
        searchResultsRVAdapter = new SearchResultsRVAdapter(dataModalArrayList, MainActivity.this);
    }

    private void initializeRecyclerView() {
        LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        resultRV.setLayoutManager(manager);
        resultRV.setAdapter(searchResultsRVAdapter);
    }

    private void setupTakeImageLauncher() {
        takeImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            Bundle extras = data.getExtras();
                            if (extras != null) {
                                imageBitmap = (Bitmap) extras.get("data");
                                img.setImageBitmap(imageBitmap);
                            } else {
                                showToast("Error: Unable to capture image.");
                            }
                        } else {
                            showToast("Error: No data from camera.");
                        }
                    }
                }
        );
    }

    private void processCapturedImage() {
        if (imageBitmap == null) {
            showToast("Please capture an image first!");
            return;
        }

        InputImage image = InputImage.fromBitmap(imageBitmap, 0);
        ImageLabeler labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS);

        labeler.process(image)
                .addOnSuccessListener(labels -> {
                    if (!labels.isEmpty()) {
                        String searchQuery = labels.get(0).getText();
                        searchData(searchQuery);
                    } else {
                        showToast("No label detected for the image.");
                    }
                })
                .addOnFailureListener(e -> showToast("Image processing failed: " + e.getMessage()));
    }

    private void searchData(String searchQuery) {
        String url = "https://serpapi.com/search.json?q=" + searchQuery.trim() +
                "&location=" + LOCATION + "&hl=en&gl=us&google_domain=google.com&api_key=" + API_KEY;

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray organicResultsArray = response.getJSONArray("organic_results");
                        dataModalArrayList.clear();
                        for (int i = 0; i < organicResultsArray.length(); i++) {
                            JSONObject organicObj = organicResultsArray.getJSONObject(i);
                            String title = organicObj.optString("title", "N/A");
                            String link = organicObj.optString("link", "N/A");
                            String displayed_link = organicObj.optString("displayed_link", "N/A");
                            String snippet = organicObj.optString("snippet", "N/A");

                            dataModalArrayList.add(new dataModal(title, link, displayed_link, snippet));
                        }
                        searchResultsRVAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        showToast("Error parsing response: " + e.getMessage());
                    }
                },
                error -> showToast("Error fetching search results: " + error.getMessage()));

        queue.add(jsonObjectRequest);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            takeImageLauncher.launch(takePictureIntent);
        } else {
            showToast("No camera app available to capture images.");
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
