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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {
    EditText editEmailLogin, editSenhaLogin;
    Button buttonLogar, buttonCadastro;

    public Boolean administradorCampo = false;
    public String nomeCampo = null, idUsuario = null;

    public FirebaseAuth mAuth;
    public SharedPreferences sharedPreferences;
    FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        //bloqueia o modo escuro
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        //Acesso a intância do Cloud Firestore
        database = FirebaseFirestore.getInstance();

        editEmailLogin = findViewById(R.id.editEmailLogin);
        editSenhaLogin = findViewById(R.id.editSenhaLogin);
        buttonLogar = findViewById(R.id.buttonLogar);
        buttonCadastro = findViewById(R.id.buttonCadastro);

        mAuth = FirebaseAuth.getInstance();

        //Inicia uma sessão de login
        sharedPreferences = getSharedPreferences("shared_preferences",Context.MODE_PRIVATE);

        //Verifica se existe uma conta logada e preenche o campo email
        if(sharedPreferences.contains("email_key")
                && sharedPreferences.contains("senha_key")
                && sharedPreferences.contains("administrador_key")
                && sharedPreferences.contains("nome_key")) {
            editEmailLogin.setText(sharedPreferences.getString("email_key",""));
        }

        buttonCadastro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, CadastroActivity.class);
                startActivity(intent);
            }
        });

        buttonLogar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(editEmailLogin.getText().toString()) || TextUtils.isEmpty(editSenhaLogin.getText().toString())) {
                    Toast.makeText(getApplicationContext(), "Preencha todos os campos", Toast.LENGTH_LONG).show();
                } else {
                    logar();
                }
            }
        });
    }

    /**
     * Realiza o login no Firebase
     */
    public void logar() {
        mAuth.signInWithEmailAndPassword(editEmailLogin.getText().toString(), editSenhaLogin.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            database.collection("usuario")
                                    .whereEqualTo("email", editEmailLogin.getText().toString())
                                    .whereEqualTo("senha", editSenhaLogin.getText().toString())
                                    .get()
                                    .addOnCompleteListener(task2 -> {
                                        if (task2.isSuccessful()) {
                                            //Recebe os valores do nivel do usuário e nome
                                            for (DocumentSnapshot document : task2.getResult()) {
                                                administradorCampo = document.getBoolean("administrador");
                                                nomeCampo = document.getString("nome");
                                                idUsuario = document.getId();
                                            }
                                            if (task2.getResult().isEmpty()) {
                                                Toast.makeText(getApplicationContext(), "Dados incorretos", Toast.LENGTH_LONG).show();
                                            } else {
                                                //Insere os dados da sessão
                                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                                editor.clear();
                                                editor.putString("email_key", editEmailLogin.getText().toString());
                                                editor.putString("senha_key", editSenhaLogin.getText().toString());
                                                editor.putBoolean("administrador_key",administradorCampo);
                                                editor.putString("nome_key", nomeCampo + " ");
                                                editor.putString("idUsuario_key", idUsuario);
                                                editor.apply();
                                            }
                                        } else {
                                            Log.d("erro", "Error getting documents: ", task2.getException());
                                        }
                                    });
                            Intent intent = new Intent(LoginActivity.this, MapsActivity.class);
                            startActivity(intent);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.d("teste", "signInWithEmail:failure", task.getException());
                            Toast.makeText(getApplicationContext(), "Dados incorretos", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    //Desabilita o botão voltar
    @Override
    public void onBackPressed() { }
}
