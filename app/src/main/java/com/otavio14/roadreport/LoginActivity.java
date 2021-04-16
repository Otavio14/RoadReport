package com.otavio14.roadreport;

import android.content.Intent;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Text;

public class LoginActivity extends AppCompatActivity {
    EditText editEmailLogin, editSenhaLogin;
    Button buttonLogar, buttonCadastro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        //bloqueia o modo escuro
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        //Acesso a intância do Cloud Firestore
        FirebaseFirestore database = FirebaseFirestore.getInstance();

        editEmailLogin = findViewById(R.id.editEmailLogin);
        editSenhaLogin = findViewById(R.id.editSenhaLogin);
        buttonLogar = findViewById(R.id.buttonLogar);
        buttonCadastro = findViewById(R.id.buttonCadastro);

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
                    database.collection("usuario")
                            .whereEqualTo("email", editEmailLogin.getText().toString())
                            .whereEqualTo("senha", editSenhaLogin.getText().toString())
                            .get()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    if (task.getResult().isEmpty()) {
                                        Toast.makeText(getApplicationContext(), "Dados incorretos", Toast.LENGTH_LONG).show();
                                    } else {
                                        Intent intent = new Intent(LoginActivity.this, MapsActivity.class);
                                        startActivity(intent);
                                    }

                                /*for (DocumentSnapshot document : task.getResult()) {
                                    Log.d(TAG, document.getId() + " => " + document.getData());
                                }*/
                                } else {
                                    Log.d("teste", "Error getting documents: ", task.getException());
                                }
                            });
                }
            }
        });
    }

    //Desabilita o botão voltar
    @Override
    public void onBackPressed() {

    }
}
