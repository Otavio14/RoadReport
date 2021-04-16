package com.otavio14.roadreport;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CadastroActivity extends AppCompatActivity {
    EditText editNome, editCpf, editTelefone, editSenha, editEmail;
    Button buttonCadastrar, buttonLogin;

    // Lista de dados a serem inseridos
    public Map<String, Object> usuario = new HashMap<>();

    //Acesso a intância do Cloud Firestore
    FirebaseFirestore database = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cadastro);

        //bloqueia o modo escuro
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        editNome = findViewById(R.id.editNome);
        editCpf = findViewById(R.id.editCpf);
        editTelefone = findViewById(R.id.editTelefone);
        editSenha = findViewById(R.id.editSenha);
        editEmail = findViewById(R.id.editEmail);
        buttonCadastrar = findViewById(R.id.buttonCadastrar);
        buttonLogin = findViewById(R.id.buttonLogin);

        buttonCadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(editCpf.getText().toString()) ||
                        TextUtils.isEmpty(editEmail.getText().toString()) ||
                        TextUtils.isEmpty(editNome.getText().toString()) ||
                        TextUtils.isEmpty(editSenha.getText().toString()) ||
                        TextUtils.isEmpty(editTelefone.getText().toString())) {
                    Toast.makeText(getApplicationContext(), "Preencha todos os campos", Toast.LENGTH_LONG).show();
                } else {
                    database.collection("usuario")
                            .whereEqualTo("email", editEmail.getText().toString())
                            .get()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    if (task.getResult().isEmpty()) {
                                        cadastrarDados();
                                    } else {
                                        Toast.makeText(getApplicationContext(), "Email já cadastrado", Toast.LENGTH_LONG).show();
                                        Intent intent = new Intent(CadastroActivity.this, LoginActivity.class);
                                        startActivity(intent);
                                    }
                                    //Lê os dados e id existentes (usar posteriormente para fazer login automático)
                                /*for (DocumentSnapshot document : task.getResult()) {
                                    Log.d("teste", document.getId() + " => " + document.getData());
                                }*/
                                } else {
                                    Log.d("teste", "Error getting documents: ", task.getException());
                                }
                            });
                }
            }
        });

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CadastroActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    //Desabilita o botão voltar
    @Override
    public void onBackPressed() {

    }

    /**
     * Insere os dados do cadastro no banco de dados
     */
    private void cadastrarDados() {
        usuario.put("administrador", false);
        usuario.put("cpf", editCpf.getText().toString());
        usuario.put("email", editEmail.getText().toString());
        usuario.put("nome", editNome.getText().toString());
        usuario.put("senha", editSenha.getText().toString());
        usuario.put("telefone", editTelefone.getText().toString());
        database.collection("usuario")
                .add(usuario)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Intent intent = new Intent(CadastroActivity.this, LoginActivity.class);
                        startActivity(intent);
                        Log.d("teste", "DocumentSnapshot added with ID: " + documentReference.getId());
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