package com.otavio14.roadreport;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RegistrarOcorrenciaActivity extends AppCompatActivity {
    EditText editDescricao;
    Button buttonRegistrar, buttonLocalAtual, buttonEscolherLocal;
    ImageButton imageButtonUpload;

    // Lista de dados a serem inseridos
    public Map<String, Object> registro = new HashMap<>();

    //Acesso a intância do Cloud Firestore
    FirebaseFirestore database = FirebaseFirestore.getInstance();

    //Acesso a intância do Cloud Storage
    FirebaseStorage storage = FirebaseStorage.getInstance();

    // Create a Cloud Storage reference from the app
    StorageReference storageRef = storage.getReference();

    //Variáveis para o upload da imagem
    private static final int PICK_IMAGE_REQUEST = 2;
    private Uri mImageUri;

    //Variável para acesso a localização atual
    FusedLocationProviderClient fusedLocationClient;

    //Variável para verificar se a localização foi escolhida
    boolean local = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registrar_ocorrencia);

        //bloqueia o modo escuro
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        editDescricao = findViewById(R.id.editDescricao);
        buttonRegistrar = findViewById(R.id.buttonRegistrar);
        imageButtonUpload = findViewById(R.id.imageButtonUpload);
        buttonLocalAtual = findViewById(R.id.buttonLocalAtual);
        buttonEscolherLocal = findViewById(R.id.buttonEscolherLocal);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        buttonLocalAtual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(RegistrarOcorrenciaActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    getLocation();
                } else {
                    ActivityCompat.requestPermissions(RegistrarOcorrenciaActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
                }
            }
        });

        buttonEscolherLocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(RegistrarOcorrenciaActivity.this, EscolherLocalActivity.class);
                startActivityForResult(i, 1);
            }
        });

        buttonRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(editDescricao.getText().toString()) || mImageUri == null || local == false) {
                    Toast.makeText(getApplicationContext(), "Preencha todos os campos", Toast.LENGTH_LONG).show();
                } else {
                    //Data atual do sistema
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

                    //Recebe o id do usuário logado
                    SharedPreferences sharedPreferences = getSharedPreferences("shared_preferences", Context.MODE_PRIVATE);
                    String idUsuario = sharedPreferences.getString("idUsuario_key", "");

                    registro.put("codUsuario", idUsuario);
                    registro.put("dataInicio", sdf.format(new Date()));
                    registro.put("descricao", editDescricao.getText().toString());
                    registro.put("situacao", "Em espera");
                    registro.put("validacao", true);

                    database.collection("registro")
                            .add(registro)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    storageRef = FirebaseStorage.getInstance().getReference(documentReference.getId());
                                    upload(documentReference.getId());
                                    Intent intent = new Intent(RegistrarOcorrenciaActivity.this, MapsActivity.class);
                                    startActivity(intent);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w("teste", "Error adding document", e);
                                }
                            });
                }
            }
        });

        imageButtonUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, PICK_IMAGE_REQUEST);
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void getLocation() {
        fusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                Location location = task.getResult();
                if (location != null) {
                    try {
                        Geocoder geocoder = new Geocoder(RegistrarOcorrenciaActivity.this, Locale.getDefault());
                        List<Address> addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        registro.clear();
                        registro.put("latitude",String.valueOf(addressList.get(0).getLatitude()));
                        registro.put("longitude",String.valueOf(addressList.get(0).getLongitude()));
                        local = true;
                    } catch (IOException e) {
                        Log.w("", "Erro de localização: " + e);
                    }
                }
            }
        });
    }

    private String extensaoArquivo(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void upload(String id) {
        if (mImageUri != null) {
            StorageReference storageReference = storageRef.child(id + "_antes." + extensaoArquivo(mImageUri));
            storageReference.putFile(mImageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(getApplicationContext(), "Registro enviado", Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(getApplicationContext(), "Arquivo não selecionado", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            mImageUri = data.getData();
            imageButtonUpload.setImageURI(mImageUri);
        }
        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                String result=data.getStringExtra("coord");
                Log.d("teste","LatLng: " + result);
                String coord[] = result.split(",");
                registro.clear();
                registro.put("latitude",coord[0]);
                registro.put("longitude",coord[1]);
                local = true;
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }
}