package com.otavio14.roadreport;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OcorrenciasActivity extends AppCompatActivity {

    TextView botaoExpandir;
    ConstraintLayout hiddenView;
    CardView cardView;

    RecyclerView recyclerView;

    ArrayList<String> nomeRua = new ArrayList<>(Arrays.asList("Nome Rua 1", "Nome Rua 2", "Nome Rua 3"));
    ArrayList<String> nomeBairro = new ArrayList<>(Arrays.asList("Nome Bairro 1", "Nome Bairro 2", "Nome Bairro 3"));
    ArrayList<String> referencia = new ArrayList<>(Arrays.asList("Referência 1", "Referência 2", "Referência  3"));
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

        cardView = findViewById(R.id.base_cardview);
        botaoExpandir = findViewById(R.id.filtros);
        hiddenView = findViewById(R.id.hidden_view);
        Drawable ic_expandir = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_expand_more, null);
        Drawable ic_recolher = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_expand_less, null);

        recyclerView = findViewById(R.id.recyclerView);

        MyAdapter myAdapter = new MyAdapter(this,nomeRua,nomeBairro,referencia,textoStatus,iconeStatus,dataInicio,dataFim,fotoAntes,fotoDepois,descricao,nomeResponsavel);
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
                }
            }
        });
    }
}
