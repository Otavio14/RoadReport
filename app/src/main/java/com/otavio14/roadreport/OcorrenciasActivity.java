package com.otavio14.roadreport;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.transition.AutoTransition;
import android.transition.TransitionManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OcorrenciasActivity extends AppCompatActivity {
    FirebaseFirestore database;
    Query queryUsuario, queryOutros;

    //Variáveis para o estado dos botões de filtro
    int estadoFiltroData = 0, estadoFiltroStatus = 0, estadoFiltroOrdem = 0;

    TextView botaoExpandir, filtroData, filtroStatus, filtroOrdem;
    ConstraintLayout hiddenView;
    CardView cardView;

    RecyclerView recyclerView;

    ArrayList<String> nomeRua = new ArrayList<>(Arrays.asList("Nome Rua 1", "Nome Rua 2", "Nome Rua 3"));
    ArrayList<String> nomeBairro = new ArrayList<>(Arrays.asList("Nome Bairro 1", "Nome Bairro 2", "Nome Bairro 3"));
    ArrayList<String> textoStatus = new ArrayList<>(Arrays.asList("Em espera", "Em andamento", "Concluído"));
    ArrayList<Integer> iconeStatus = new ArrayList<>(Arrays.asList(R.drawable.ic_status_espera, R.drawable.ic_status_andamento, R.drawable.ic_status_concluido));
    ArrayList<String> dataInicio = new ArrayList<>(Arrays.asList("Data Início 1", "Data Início 2", "Data Início 3"));
    ArrayList<String> dataFim = new ArrayList<>(Arrays.asList("Data Fim 1", "Data Fim 2", "Data Fim 3"));
    ArrayList<Integer> fotoAntes = new ArrayList<>(Arrays.asList(R.drawable.ic_status_espera, R.drawable.ic_status_andamento, R.drawable.ic_status_concluido));
    ArrayList<Integer> fotoDepois = new ArrayList<>(Arrays.asList(R.drawable.ic_status_espera, R.drawable.ic_status_andamento, R.drawable.ic_status_concluido));
    ArrayList<String> descricao = new ArrayList<>(Arrays.asList("Descrição 1", "Descrição 2", "Descrição 3"));
    ArrayList<String> nomeResponsavel = new ArrayList<>(Arrays.asList("Nome Responsável 1", "Nome Responsável 2", "Nome Responsável 3"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ocorrencias);

        //bloqueia o modo escuro
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        //Acesso a intância do Cloud Firestore
        database = FirebaseFirestore.getInstance();

        cardView = findViewById(R.id.base_cardview);
        botaoExpandir = findViewById(R.id.filtros);
        filtroData = findViewById(R.id.filtroData);
        filtroStatus = findViewById(R.id.filtroStatus);
        filtroOrdem = findViewById(R.id.filtroOrdem);
        hiddenView = findViewById(R.id.hidden_view);
        Drawable ic_expandir = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_expand_more, null);
        Drawable ic_recolher = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_expand_less, null);
        Drawable ic_seta_up_down = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_seta_up_down, null);


        recyclerView = findViewById(R.id.recyclerView);

        SharedPreferences sharedPreferences = getSharedPreferences("shared_preferences", Context.MODE_PRIVATE);
        Boolean admin = false;
        admin = sharedPreferences.getBoolean("administrador_key", admin);
        String idUsuario = sharedPreferences.getString("idUsuario_key", "");

        queryOutros = database.collection("registro").orderBy("dataInicio", Query.Direction.ASCENDING);
        queryUsuario = database.collection("registro").whereEqualTo("codUsuario", idUsuario).orderBy("dataInicio", Query.Direction.ASCENDING);

        registrosUsuario(admin, idUsuario);
        registrosGeral(admin, idUsuario);

        MyAdapter myAdapter = new MyAdapter(this, nomeRua, nomeBairro, textoStatus, iconeStatus, dataInicio, dataFim, fotoAntes, fotoDepois, descricao, nomeResponsavel);
        recyclerView.setAdapter(myAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        botaoExpandir.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                // If the CardView is already expanded, set its visibility
                //  to gone and change the expand less icon to expand more.
                if (hiddenView.getVisibility() == View.VISIBLE) {

                    // The transition of the hiddenView is carried out
                    //  by the TransitionManager class.
                    // Here we use an object of the AutoTransition
                    // Class to create a default transition.
                    TransitionManager.beginDelayedTransition(cardView,
                            new AutoTransition());
                    hiddenView.setVisibility(View.GONE);
                    botaoExpandir.setCompoundDrawablesWithIntrinsicBounds(null, null, ic_expandir, null);
                }

                // If the CardView is not expanded, set its visibility
                // to visible and change the expand more icon to expand less.
                else {

                    TransitionManager.beginDelayedTransition(cardView,
                            new AutoTransition());
                    hiddenView.setVisibility(View.VISIBLE);
                    botaoExpandir.setCompoundDrawablesWithIntrinsicBounds(null, null, ic_recolher, null);

                    filtroData.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            switch (estadoFiltroData) {
                                case 0:

                                    //Mudança de estado inicial para crescente
                                    estadoFiltroData = 1;
                                    filtroData.setCompoundDrawablesWithIntrinsicBounds(null, null, ic_recolher, null);
                                    filtroStatus.setCompoundDrawablesWithIntrinsicBounds(null, null, ic_seta_up_down, null);
                                    filtroOrdem.setCompoundDrawablesWithIntrinsicBounds(null, null, ic_seta_up_down, null);
                                    break;
                                case 1:

                                    //Mudança de estado crescente para decrescente

                                    estadoFiltroData = 2;
                                    filtroData.setCompoundDrawablesWithIntrinsicBounds(null, null, ic_expandir, null);
                                    filtroStatus.setCompoundDrawablesWithIntrinsicBounds(null, null, ic_seta_up_down, null);
                                    filtroOrdem.setCompoundDrawablesWithIntrinsicBounds(null, null, ic_seta_up_down, null);
                                    break;
                                case 2:

                                    //Mudança de estado descrescente para inicial

                                    estadoFiltroData = 0;
                                    filtroData.setCompoundDrawablesWithIntrinsicBounds(null, null, ic_seta_up_down, null);
                                    filtroStatus.setCompoundDrawablesWithIntrinsicBounds(null, null, ic_seta_up_down, null);
                                    filtroOrdem.setCompoundDrawablesWithIntrinsicBounds(null, null, ic_seta_up_down, null);
                                    break;
                            }
                        }
                    });

                    filtroStatus.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            switch (estadoFiltroStatus) {
                                case 0: //Mudança de estado inicial para em espera

                                    break;
                                case 1: //Mudança de estado em espera para em andamento

                                    break;
                                case 2: //Mudança de estado em andamento para concluido

                                    break;
                                case 3: //Mudança de estado concluido para inicial

                                    break;
                            }
                        }
                    });

                    filtroOrdem.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            switch (estadoFiltroOrdem) {
                                case 0: //Mudança de estado inicial para crescente

                                    break;
                                case 1: //Mudança de estado crescente para decrescente

                                    break;
                                case 2: //Mudança de estado descrescente para inicial

                                    break;
                            }
                        }
                    });
                }
            }
        });
    }

    private void registrosUsuario(Boolean admin, String idUsuario) {
        queryUsuario.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Log.d("teste",""+document.getString("dataInicio"));
                    }
                } else {
                    Log.d("erro", "Error getting documents.", task.getException());
                }

            }
        });
    }

    private void registrosGeral(Boolean admin, String idUsuario) {
        queryOutros.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Log.d("teste",""+document.getString("dataInicio"));
                    }
                } else {
                    Log.d("erro", "Error getting documents.", task.getException());
                }

            }
        });
    }
}
