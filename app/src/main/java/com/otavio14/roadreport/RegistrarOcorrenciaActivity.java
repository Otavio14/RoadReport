package com.otavio14.roadreport;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class RegistrarOcorrenciaActivity extends AppCompatActivity {
    EditText editRua, editBairro, editReferencia, editDescricao;
    Button buttonRegistrar;
    ImageButton imageButtonUpload;

    // Lista de dados a serem inseridos
    public Map<String, Object> usuario = new HashMap<>();

    //Acesso a intância do Cloud Firestore
    FirebaseFirestore database = FirebaseFirestore.getInstance();

    //Acesso a intância do Cloud Storage
    FirebaseStorage storage = FirebaseStorage.getInstance();

    // Create a Cloud Storage reference from the app
    StorageReference storageRef = storage.getReference();

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri mImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registrar_ocorrencia);

        //bloqueia o modo escuro
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        editRua = findViewById(R.id.editRua);
        editBairro = findViewById(R.id.editBairro);
        editReferencia = findViewById(R.id.editReferencia);
        editDescricao = findViewById(R.id.editDescricao);
        buttonRegistrar = findViewById(R.id.buttonRegistrar);
        imageButtonUpload = findViewById(R.id.imageButtonUpload);

        buttonRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(editRua.getText().toString())
                        || TextUtils.isEmpty(editBairro.getText().toString())
                        || TextUtils.isEmpty(editReferencia.getText().toString())
                        || TextUtils.isEmpty(editDescricao.getText().toString())) {
                    Toast.makeText(getApplicationContext(), "Preencha todos os campos", Toast.LENGTH_LONG).show();
                } else {
                    //Data atual do sistema
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

                    //Recebe o id do usuário logado
                    SharedPreferences sharedPreferences = getSharedPreferences("shared_preferences", Context.MODE_PRIVATE);
                    String idUsuario = sharedPreferences.getString("idUsuario_key","");

                    usuario.put("codUsuario",idUsuario);
                    usuario.put("dataInicio",sdf.format(new Date()));
                    usuario.put("rua", editRua.getText().toString());
                    usuario.put("bairro", editBairro.getText().toString());
                    usuario.put("referencia", editReferencia.getText().toString());
                    usuario.put("descricao", editDescricao.getText().toString());
                    usuario.put("situacao", "Em espera");
                    usuario.put("validacao", true);

                    database.collection("registro")
                            .add(usuario)
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
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            mImageUri = data.getData();
        }
    }
}