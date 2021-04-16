package com.otavio14.roadreport;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import org.w3c.dom.Text;

public class RegistrarOcorrenciaActivity extends AppCompatActivity {
    EditText editRua, editBairro, editReferencia, editDescricao;
    Button buttonRegistrar;

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

        buttonRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(editRua.getText().toString())
                        || TextUtils.isEmpty(editBairro.getText().toString())
                        || TextUtils.isEmpty(editReferencia.getText().toString())
                        || TextUtils.isEmpty(editDescricao.getText().toString())) {
                    Toast.makeText(getApplicationContext(), "Preencha todos os campos", Toast.LENGTH_LONG).show();
                } else {

                }
            }
        });
    }
}