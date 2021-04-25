package com.otavio14.roadreport;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

    public FirebaseAuth mAuth;

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

        //Instance da conta
        mAuth = FirebaseAuth.getInstance();

        buttonCadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Verifica se os campos estão preenchidos
                if (TextUtils.isEmpty(editCpf.getText().toString()) ||
                        TextUtils.isEmpty(editEmail.getText().toString()) ||
                        TextUtils.isEmpty(editNome.getText().toString()) ||
                        TextUtils.isEmpty(editSenha.getText().toString()) ||
                        TextUtils.isEmpty(editTelefone.getText().toString())) {
                    Toast.makeText(getApplicationContext(), "Preencha todos os campos", Toast.LENGTH_LONG).show();
                } else {
                    cadastrarConta();
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

    /**
     * Cadastra a conta na autenticação do Firebase
     */
    private void cadastrarConta() {
        mAuth.createUserWithEmailAndPassword(editEmail.getText().toString(), editSenha.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("teste", "createUserWithEmail:success");
                            cadastrarDados();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.d("teste", "createUserWithEmail:failure", task.getException());
                            Toast.makeText(getApplicationContext(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
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
                        //Inicia uma sessão de login
                        SharedPreferences sharedPreferences = getSharedPreferences("shared_preferences", Context.MODE_PRIVATE);
                        //Insere os dados da sessão
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("email_key", editEmail.getText().toString());
                        editor.putString("senha_key", editSenha.getText().toString());
                        editor.putBoolean("administrador_key",false);
                        editor.putString("nome_key", editNome.getText().toString() + " ");
                        editor.putString("idUsuario_key", documentReference.getId());
                        editor.apply();
                        Intent intent = new Intent(CadastroActivity.this, MapsActivity.class);
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