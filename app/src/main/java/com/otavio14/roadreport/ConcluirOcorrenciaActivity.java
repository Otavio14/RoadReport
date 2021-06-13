package com.otavio14.roadreport;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ConcluirOcorrenciaActivity extends AppCompatActivity {
    EditText editResponsavel, editConcluirDescricao;
    Button buttonConcluir;
    ImageButton imageButtonConcluir;

    //Acesso a intância do Cloud Firestore
    FirebaseFirestore database = FirebaseFirestore.getInstance();

    //Acesso a intância do Cloud Storage
    FirebaseStorage storage = FirebaseStorage.getInstance();

    // Create a Cloud Storage reference from the app
    StorageReference storageRef = storage.getReference();

    //Variáveis para o upload da imagem
    private static final int PICK_IMAGE_REQUEST = 2;
    private Uri mImageUri;

    // Lista de dados a serem inseridos
    public Map<String, Object> concluir = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.concluir_ocorrencia);
        //bloqueia o modo escuro
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        editResponsavel = findViewById(R.id.editResponsavel);
        editConcluirDescricao = findViewById(R.id.editConcluirDescricao);
        buttonConcluir = findViewById(R.id.buttonConcluir);
        imageButtonConcluir = findViewById(R.id.imageButtonConcluir);

        buttonConcluir.setOnClickListener(v -> {
            if (TextUtils.isEmpty(editResponsavel.getText().toString()) ||
                    TextUtils.isEmpty(editConcluirDescricao.getText().toString()) ||
                    mImageUri == null) {
                Toast.makeText(getApplicationContext(), "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            } else {
                //Data atual do sistema
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

                concluir.put("nomeResponsavel", editResponsavel.getText().toString());
                concluir.put("descricaoConclusao", editConcluirDescricao.getText().toString());
                concluir.put("situacao", "Concluido");
                concluir.put("dataFim", sdf.format(new Date()));
                storageRef = FirebaseStorage.getInstance().getReference(getIntent().getStringExtra("ID_OCORRENCIA"));
                database.collection("registro").document(getIntent().getStringExtra("ID_OCORRENCIA")).update(concluir);
                upload(getIntent().getStringExtra("ID_OCORRENCIA"));
                Intent intent = new Intent(getApplicationContext(), OcorrenciasActivity.class);
                startActivity(intent);
            }
        });

        imageButtonConcluir.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });
    }

    /**
     * Coleta a extensão da imagem escolhida
     *
     * @param uri - código da imagem selecionada
     * @return - retorna a extensão da imagem
     */
    private String extensaoArquivo(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    /**
     * Realiza o upload da imagem no Firebase
     *
     * @param id - ID da ocorrência
     */
    private void upload(String id) {
        if (mImageUri != null) {
            StorageReference storageReference = storageRef.child(id + "_depois." + extensaoArquivo(mImageUri));
            storageReference.putFile(mImageUri)
                    .addOnSuccessListener(taskSnapshot -> Toast.makeText(getApplicationContext(), "Registro enviado", Toast.LENGTH_LONG).show())
                    .addOnFailureListener(e -> Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(getApplicationContext(), "Arquivo não selecionado", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            mImageUri = data.getData();
            imageButtonConcluir.setImageURI(mImageUri);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
