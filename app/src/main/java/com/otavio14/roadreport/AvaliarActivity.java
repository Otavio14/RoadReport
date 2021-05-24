package com.otavio14.roadreport;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AvaliarActivity extends AppCompatActivity {
    RatingBar ratingOcorrencia, ratingTempo, ratingResultado;
    EditText editAvaliarDescricao;
    Button buttonAvaliar;

    FirebaseFirestore database = FirebaseFirestore.getInstance();
    Map<String, Object> avaliacao = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avaliar);
        //Bloqueia o modo escuro
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        ratingOcorrencia = findViewById(R.id.ratingOcorrencia);
        ratingTempo = findViewById(R.id.ratingTempo);
        ratingResultado = findViewById(R.id.ratingResultado);
        editAvaliarDescricao = findViewById(R.id.editAvaliarDescricao);
        buttonAvaliar = findViewById(R.id.buttonAvaliar);

        buttonAvaliar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ratingResultado.getRating() > 0 && ratingTempo.getRating() > 0 && ratingOcorrencia.getRating() > 0) {
                    avaliacao.put("notaOcorrencia", String.valueOf((int) ratingOcorrencia.getRating()));
                    avaliacao.put("notaTempo", String.valueOf((int) ratingTempo.getRating()));
                    avaliacao.put("notaResultado", String.valueOf((int) ratingResultado.getRating()));
                    avaliacao.put("codRegistro", getIntent().getStringExtra("ID_OCORRENCIA"));
                    if (!TextUtils.isEmpty(editAvaliarDescricao.getText().toString())) {
                        avaliacao.put("notaDescricao", editAvaliarDescricao.getText().toString());
                    }
                }
                registrar();
            }
        });
    }

    private void registrar() {
        database.collection("avaliacao").add(avaliacao).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Toast.makeText(getApplicationContext(), "Ocorrência Avaliada", Toast.LENGTH_SHORT).show();
                database.collection("registro").document(getIntent().getStringExtra("ID_OCORRENCIA")).update("avaliado",true);
                Intent intent = new Intent(getApplicationContext(), OcorrenciasActivity.class);
                startActivity(intent);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("erro", "Error adding document", e);
            }
        });
    }
}