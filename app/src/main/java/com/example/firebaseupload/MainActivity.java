package com.example.firebaseupload;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Space;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

//import android.widget.Toast;
//import androidx.annotation.NonNull;
//import com.google.android.gms.tasks.OnFailureListener;
//import com.google.android.gms.tasks.OnSuccessListener;
//import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
//import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
//import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
//import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
//import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;


public class MainActivity extends AppCompatActivity {


    private static final int PICK_IMAGE_REQUEST = 1000;
    private static final int CAMERA_PIC_REQUEST = 1001;
    private static final int PERMISSION_CODE = 1002;
    //    private static Boolean TRANSLATOR_DOWNLOADED_SUCCESFULLY = Boolean.FALSE;
    private static int INITIAL_CALL = 1;
    public String cap;
    public String turkishCaption;
    public String phoneLanguage;
    @BindView(R.id.button_choose_image)
    Button buttonChooseImage;
    @BindView(R.id.caption_text_file_name)
    TextView captionTextFileName;
    @BindView(R.id.image_view)
    ImageView imageView;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;
    @BindView(R.id.button_upload)
    Button buttonUpload;
    @BindView(R.id.camera_button)
    Button cameraButton;
    @BindView(R.id.image_button)
    Button imageButton;
    @BindView(R.id.dummy)
    Space dummy;


    private Uri mImageUri;

    private TextToSpeech mTTS;


    private FirebaseStorage storage = FirebaseStorage.getInstance();
    // Write a message to the database
//    Realtime database
//    private FirebaseDatabase database = FirebaseDatabase.getInstance();
//    DatabaseReference myRef = database.getReference("URL");
    // Access a Cloud Firestore instance from your Activity
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    final DocumentReference docRef = db.collection("users").document("CAPTION");

    // Create an English-German translator:
//    FirebaseTranslatorOptions options =
//            new FirebaseTranslatorOptions.Builder()
//                    .setSourceLanguage(FirebaseTranslateLanguage.EN)
//                    .setTargetLanguage(FirebaseTranslateLanguage.TR)
//                    .build();
//    final FirebaseTranslator englishTurkishTranslator =
//            FirebaseNaturalLanguage.getInstance().getTranslator(options);
//    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        progressBar.setVisibility(View.GONE);
        buttonUpload.setEnabled(true);
        imageButton.setEnabled(false);

//        phoneLanguage = Locale.getDefault().toString();
//        Log.d("Language",phoneLanguage);
        phoneLanguage = Locale.getDefault().toLanguageTag();
        Locale pLang = Locale.getDefault();

//        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder()
//                .requireWifi()
//                .build();
//        englishTurkishTranslator.downloadModelIfNeeded(conditions)
//                .addOnSuccessListener(
//                        v -> {
//                            // Model downloaded successfully. Okay to start translating.
//                            // (Set a flag, unhide the translation UI, etc.)
//                            TRANSLATOR_DOWNLOADED_SUCCESFULLY = Boolean.TRUE;
//                            Toast.makeText(getApplicationContext(), "Translator downloaded succesfully.", Toast.LENGTH_LONG).show();
//                        })
//                .addOnFailureListener(
//                        e -> {
//                            // Model couldn’t be downloaded or other internal error.
//                            // ...
//                            Toast.makeText(getApplicationContext(), "Translator download is failed.", Toast.LENGTH_LONG).show();
//
//                        });
        // [END listen_document]



        docRef.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                Log.w("LISTENER", "Listen failed.", e);
                return;
            }
            if (INITIAL_CALL == 1) {
                INITIAL_CALL = 0;
                captionTextFileName.setText(R.string.caption_placeholder);


            } else if (snapshot != null && snapshot.exists()) {

                cap = snapshot.getData().get("caption").toString();
//                if (TRANSLATOR_DOWNLOADED_SUCCESFULLY) {
//                    englishTurkishTranslator.translate(cap)
//                            .addOnSuccessListener(
//                                    new OnSuccessListener<String>() {
//                                        @Override
//                                        public void onSuccess(@NonNull String translatedText) {
//                                            // Translation successful.
//                                            cap = translatedText;
//                                        }
//                                    })
//                            .addOnFailureListener(
//                                    new OnFailureListener() {
//                                        @Override
//                                        public void onFailure(@NonNull Exception e) {
//                                            // Error.
//                                            // ...
//                                        }
//                                    });
//                }


                captionTextFileName.setText(cap);

                imageButton.setEnabled(true);
            } else {
                Log.d("LISTENER", "Current data: null");
            }
            progressBar.setVisibility(View.GONE);
        });

        mTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    // int mTTSresult = mTTS.setLanguage(Locale.US);
                    int mTTSresult = mTTS.setLanguage(pLang);
                    if (mTTSresult == TextToSpeech.LANG_MISSING_DATA
                            || mTTSresult == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("mTTS", "Language not supported");
                    }
                } else {
                    Log.e("mTTS", "Initialization failed");
                }

            }
        });

    }

    @OnClick({R.id.button_choose_image, R.id.button_upload, R.id.camera_button})
    public void onViewClicked(View view) {
        imageButton.setEnabled(false);
        switch (view.getId()) {
            case R.id.button_choose_image:
                openFileChooser();
                break;
            case R.id.button_upload:
                uploadFile();
                break;
            case R.id.camera_button:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.CAMERA) ==
                            PackageManager.PERMISSION_DENIED ||
                            checkSelfPermission(Manifest.permission.CAMERA) ==
                                    PackageManager.PERMISSION_DENIED) {
                        String[] permission = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};

                        requestPermissions(permission, PERMISSION_CODE);

                    } else {
                        openCamera();
                    }
                } else {
                    openCamera();
                }

                break;
        }

    }

    private void uploadFile() {
        String path = "firememes/" + UUID.randomUUID() + ".jpg";

        StorageReference firememeRef = storage.getReference(path);

        UploadTask uploadTask = firememeRef.putFile(mImageUri);
        //progress_bar
        //button_upload
        progressBar.setVisibility(View.VISIBLE);
        buttonUpload.setEnabled(false);

        uploadTask.addOnCompleteListener(MainActivity.this, task -> {
            Log.i("MA", "Upload Task Complete");
//            progressBar.setVisibility(View.GONE);
            buttonUpload.setEnabled(true);
        });

        Task<Uri> getDownloadUriTask = uploadTask.continueWithTask(
                task -> {
                    if (!task.isSuccessful()) {
                        throw Objects.requireNonNull(task.getException());
                    }
                    return firememeRef.getDownloadUrl();
                }
        );

        getDownloadUriTask.addOnCompleteListener(MainActivity.this, task -> {
            if (task.isSuccessful()) {
                Uri downloadUri = task.getResult();

//                    Upload Realtime Database Code
//                    myRef.setValue(downloadUri.toString());

//                    Upload Cloud Firestore
                // Create a new user with a first and last name
                Map<String, Object> user = new HashMap<>();
                if (downloadUri != null) {
                    user.put("URL", downloadUri.toString());
                }


                // Add a new document with a generated ID
                db.collection("users").document("URL")
                        .set(user)
                        .addOnSuccessListener(aVoid -> Log.d("TAG", "Document Succesfully written"))
                        .addOnFailureListener(e -> Log.w("TAG", "Error adding document", e));

            }


        });


    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }
//    private void openCamera() {
//        Log.d("OPENCAmera", "Camera is opening 2");
//        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
//        Log.d("OPENCAmera", "Camera is opening 3");
//
//        Log.d("OPENCAmera", "Camera is opening 4");
//        startActivityForResult(cameraIntent, CAMERA_PIC_REQUEST);
//    }

    private void openCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera");
        mImageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
//        Camera Intent
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
        startActivityForResult(cameraIntent, CAMERA_PIC_REQUEST);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            mImageUri = data.getData();

            Picasso.get().load(mImageUri).into(imageView);
        }


        if (requestCode == CAMERA_PIC_REQUEST && resultCode == Activity.RESULT_OK) {

            Picasso.get().load(mImageUri).into(imageView);
        }
        captionTextFileName.setText(R.string.caption_placeholder);

    }


    @OnClick(R.id.image_button)
    public void onViewClicked() {
        narrate();
    }


    private void narrate() {
        String text = cap;
        float pitch = (float) 1;
        float speed = (float) 1;
        mTTS.setPitch(pitch);
        mTTS.setSpeechRate(speed);
        mTTS.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
