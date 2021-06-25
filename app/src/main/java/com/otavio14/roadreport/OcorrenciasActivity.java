package com.otavio14.roadreport;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class OcorrenciasActivity extends AppCompatActivity {
    FirebaseFirestore database;

    // Create a Cloud Storage reference from the app
    StorageReference storageRef = FirebaseStorage.getInstance().getReference();
    StorageReference pathReference;

    Query queryUsuario, queryOutros;

    //Variáveis para o estado dos botões de filtro
    int estadoFiltroData = 0, estadoFiltroStatus = 0, estadoFiltroOrdem = 0;

    TextView botaoExpandir, filtroData, filtroStatus, filtroOrdem;
    ConstraintLayout hiddenView;
    CardView cardView;

    boolean admin;
    String idUsuario;
    ArrayList<Boolean> ocorrenciaUsuario = new ArrayList<>();
    ArrayList<Boolean> ocorrenciaAvaliada = new ArrayList<>();

    RecyclerView recyclerView;
    MyAdapter myAdapter;

    ArrayList<String> idOcorrencia = new ArrayList<>();
    ArrayList<String> nomeBairro = new ArrayList<>();
    ArrayList<String> textoStatus = new ArrayList<>();
    ArrayList<Integer> iconeStatus = new ArrayList<>(Arrays.asList(R.drawable.ic_status_espera, R.drawable.ic_status_andamento, R.drawable.ic_status_concluido, R.drawable.ic_status_concluido, R.drawable.ic_status_concluido));
    ArrayList<String> dataInicio = new ArrayList<>();
    ArrayList<String> dataFim = new ArrayList<>();
    ArrayList<String> fotoAntes = new ArrayList<>();
    ArrayList<String> fotoDepois = new ArrayList<>();
    ArrayList<String> descricao = new ArrayList<>();
    ArrayList<String> nomeResponsavel = new ArrayList<>();
    ArrayList<String> expandirPosicao = new ArrayList<>();

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
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
        Drawable ic_status_padrao = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_status_padrao, null);
        Drawable ic_status_espera = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_status_espera, null);
        Drawable ic_status_andamento = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_status_andamento, null);
        Drawable ic_status_concluido = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_status_concluido, null);


        recyclerView = findViewById(R.id.recyclerView);

        //Verifica se o usuário atual é admin
        SharedPreferences sharedPreferences = getSharedPreferences("shared_preferences", Context.MODE_PRIVATE);
        admin = sharedPreferences.getBoolean("administrador_key", admin);
        idUsuario = sharedPreferences.getString("idUsuario_key", "");

        //Queries para a pesquisa das ocorrências
        queryOutros = database.collection("registro").orderBy("dataInicio", Query.Direction.ASCENDING);
        queryUsuario = database.collection("registro").whereEqualTo("codUsuario", idUsuario).orderBy("dataInicio", Query.Direction.ASCENDING);

        registrosUsuario();
        registrosGeral(admin, idUsuario);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        myAdapter = new MyAdapter(this, nomeBairro, textoStatus, iconeStatus, dataInicio, dataFim,
                fotoAntes, fotoDepois, descricao, nomeResponsavel, idOcorrencia,
                ocorrenciaUsuario, admin, ocorrenciaAvaliada, expandirPosicao);
        recyclerView.setAdapter(myAdapter);

        botaoExpandir.setOnClickListener(v -> {
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

                filtroData.setOnClickListener(v1 -> {
                    switch (estadoFiltroData) {
                        case 0:
                            //Mudança de estado inicial para crescente
                            queryOutros = database.collection("registro").orderBy("dataInicio", Query.Direction.DESCENDING);
                            queryUsuario = database.collection("registro").whereEqualTo("codUsuario", idUsuario).orderBy("dataInicio", Query.Direction.DESCENDING);
                            registrosUsuario();
                            registrosGeral(admin, idUsuario);
                            estadoFiltroData = 1;
                            estadoFiltroStatus = 0;
                            estadoFiltroOrdem = 0;
                            filtroData.setCompoundDrawablesWithIntrinsicBounds(null, null, ic_recolher, null);
                            filtroStatus.setCompoundDrawablesWithIntrinsicBounds(null, null, ic_status_padrao, null);
                            filtroOrdem.setCompoundDrawablesWithIntrinsicBounds(null, null, ic_seta_up_down, null);
                            break;
                        case 1:
                            //Mudança de estado crescente para decrescente
                            queryOutros = database.collection("registro").orderBy("dataInicio", Query.Direction.ASCENDING);
                            queryUsuario = database.collection("registro").whereEqualTo("codUsuario", idUsuario).orderBy("dataInicio", Query.Direction.ASCENDING);
                            registrosUsuario();
                            registrosGeral(admin, idUsuario);
                            estadoFiltroData = 2;
                            estadoFiltroStatus = 0;
                            estadoFiltroOrdem = 0;
                            filtroData.setCompoundDrawablesWithIntrinsicBounds(null, null, ic_expandir, null);
                            filtroStatus.setCompoundDrawablesWithIntrinsicBounds(null, null, ic_status_padrao, null);
                            filtroOrdem.setCompoundDrawablesWithIntrinsicBounds(null, null, ic_seta_up_down, null);
                            break;
                        case 2:
                            //Mudança de estado descrescente para inicial
                            queryOutros = database.collection("registro").orderBy("dataInicio", Query.Direction.ASCENDING);
                            queryUsuario = database.collection("registro").whereEqualTo("codUsuario", idUsuario).orderBy("dataInicio", Query.Direction.ASCENDING);
                            registrosUsuario();
                            registrosGeral(admin, idUsuario);
                            estadoFiltroData = 0;
                            estadoFiltroStatus = 0;
                            estadoFiltroOrdem = 0;
                            filtroData.setCompoundDrawablesWithIntrinsicBounds(null, null, ic_seta_up_down, null);
                            filtroStatus.setCompoundDrawablesWithIntrinsicBounds(null, null, ic_status_padrao, null);
                            filtroOrdem.setCompoundDrawablesWithIntrinsicBounds(null, null, ic_seta_up_down, null);
                            break;
                    }
                });

                filtroStatus.setOnClickListener(v12 -> {
                    switch (estadoFiltroStatus) {
                        case 0:
                            //Mudança de estado inicial para em espera
                            queryOutros = database.collection("registro").whereEqualTo("situacao", "Em espera").orderBy("dataInicio", Query.Direction.DESCENDING);
                            queryUsuario = database.collection("registro").whereEqualTo("codUsuario", idUsuario).whereEqualTo("situacao", "Em espera").orderBy("dataInicio", Query.Direction.DESCENDING);
                            registrosUsuario();
                            registrosGeral(admin, idUsuario);
                            estadoFiltroStatus = 1;
                            estadoFiltroData = 0;
                            estadoFiltroOrdem = 0;
                            filtroStatus.setCompoundDrawablesWithIntrinsicBounds(null, null, ic_status_espera, null);
                            filtroData.setCompoundDrawablesWithIntrinsicBounds(null, null, ic_seta_up_down, null);
                            filtroOrdem.setCompoundDrawablesWithIntrinsicBounds(null, null, ic_seta_up_down, null);
                            break;
                        case 1:
                            //Mudança de estado em espera para em andamento
                            queryOutros = database.collection("registro").whereEqualTo("situacao", "Em andamento").orderBy("bairro", Query.Direction.ASCENDING);
                            queryUsuario = database.collection("registro").whereEqualTo("codUsuario", idUsuario).whereEqualTo("situacao", "Em andamento").orderBy("dataInicio", Query.Direction.ASCENDING);
                            registrosUsuario();
                            registrosGeral(admin, idUsuario);
                            estadoFiltroStatus = 2;
                            estadoFiltroData = 0;
                            estadoFiltroOrdem = 0;
                            filtroStatus.setCompoundDrawablesWithIntrinsicBounds(null, null, ic_status_andamento, null);
                            filtroData.setCompoundDrawablesWithIntrinsicBounds(null, null, ic_seta_up_down, null);
                            filtroOrdem.setCompoundDrawablesWithIntrinsicBounds(null, null, ic_seta_up_down, null);
                            break;
                        case 2:
                            //Mudança de estado em andamento para concluido
                            queryOutros = database.collection("registro").whereEqualTo("situacao", "Concluido").orderBy("dataInicio", Query.Direction.ASCENDING);
                            queryUsuario = database.collection("registro").whereEqualTo("codUsuario", idUsuario).whereEqualTo("situacao", "Concluido").orderBy("dataInicio", Query.Direction.ASCENDING);
                            registrosUsuario();
                            registrosGeral(admin, idUsuario);
                            estadoFiltroStatus = 3;
                            estadoFiltroData = 0;
                            estadoFiltroOrdem = 0;
                            filtroStatus.setCompoundDrawablesWithIntrinsicBounds(null, null, ic_status_concluido, null);
                            filtroData.setCompoundDrawablesWithIntrinsicBounds(null, null, ic_seta_up_down, null);
                            filtroOrdem.setCompoundDrawablesWithIntrinsicBounds(null, null, ic_seta_up_down, null);
                            break;
                        case 3:
                            //Mudança de estado concluido para inicial
                            queryOutros = database.collection("registro").orderBy("dataInicio", Query.Direction.ASCENDING);
                            queryUsuario = database.collection("registro").whereEqualTo("codUsuario", idUsuario).orderBy("dataInicio", Query.Direction.ASCENDING);
                            registrosUsuario();
                            registrosGeral(admin, idUsuario);
                            estadoFiltroStatus = 0;
                            estadoFiltroData = 0;
                            estadoFiltroOrdem = 0;
                            filtroStatus.setCompoundDrawablesWithIntrinsicBounds(null, null, ic_status_padrao, null);
                            filtroData.setCompoundDrawablesWithIntrinsicBounds(null, null, ic_seta_up_down, null);
                            filtroOrdem.setCompoundDrawablesWithIntrinsicBounds(null, null, ic_seta_up_down, null);
                            break;
                    }
                });

                filtroOrdem.setOnClickListener(v13 -> {
                    switch (estadoFiltroOrdem) {
                        case 0:
                            //Mudança de estado inicial para crescente
                            queryOutros = database.collection("registro").orderBy("bairro", Query.Direction.ASCENDING);
                            queryUsuario = database.collection("registro").whereEqualTo("codUsuario", idUsuario).orderBy("bairro", Query.Direction.DESCENDING);
                            registrosUsuario();
                            registrosGeral(admin, idUsuario);
                            estadoFiltroOrdem = 1;
                            estadoFiltroStatus = 0;
                            estadoFiltroData = 0;
                            filtroOrdem.setCompoundDrawablesWithIntrinsicBounds(null, null, ic_recolher, null);
                            filtroStatus.setCompoundDrawablesWithIntrinsicBounds(null, null, ic_status_padrao, null);
                            filtroData.setCompoundDrawablesWithIntrinsicBounds(null, null, ic_seta_up_down, null);
                            break;
                        case 1:
                            //Mudança de estado crescente para decrescente
                            queryOutros = database.collection("registro").orderBy("bairro", Query.Direction.DESCENDING);
                            queryUsuario = database.collection("registro").whereEqualTo("codUsuario", idUsuario).orderBy("bairro", Query.Direction.ASCENDING);
                            registrosUsuario();
                            registrosGeral(admin, idUsuario);
                            estadoFiltroOrdem = 2;
                            estadoFiltroStatus = 0;
                            estadoFiltroData = 0;
                            filtroOrdem.setCompoundDrawablesWithIntrinsicBounds(null, null, ic_expandir, null);
                            filtroStatus.setCompoundDrawablesWithIntrinsicBounds(null, null, ic_status_padrao, null);
                            filtroData.setCompoundDrawablesWithIntrinsicBounds(null, null, ic_seta_up_down, null);
                            break;
                        case 2:
                            //Mudança de estado descrescente para inicial
                            queryOutros = database.collection("registro").orderBy("dataInicio", Query.Direction.ASCENDING);
                            queryUsuario = database.collection("registro").whereEqualTo("codUsuario", idUsuario).orderBy("dataInicio", Query.Direction.ASCENDING);
                            registrosUsuario();
                            registrosGeral(admin, idUsuario);
                            estadoFiltroOrdem = 0;
                            estadoFiltroStatus = 0;
                            estadoFiltroData = 0;
                            filtroOrdem.setCompoundDrawablesWithIntrinsicBounds(null, null, ic_seta_up_down, null);
                            filtroStatus.setCompoundDrawablesWithIntrinsicBounds(null, null, ic_status_padrao, null);
                            filtroData.setCompoundDrawablesWithIntrinsicBounds(null, null, ic_seta_up_down, null);
                            break;
                    }
                });
            }
        });
    }

    private void focarPosicao() {
        if (getIntent().getStringExtra("ID_OCORRENCIA") != null && idOcorrencia.contains(getIntent().getStringExtra("ID_OCORRENCIA"))) {
            recyclerView.scrollToPosition(idOcorrencia.indexOf(getIntent().getStringExtra("ID_OCORRENCIA")));
        }
    }

    private void registrosUsuario() {
        clearArrays();
        queryUsuario.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    if (document.getBoolean("validacao")) {
                        if (document.getBoolean("avaliado")) {
                            ocorrenciaAvaliada.add(true);
                        } else {
                            ocorrenciaAvaliada.add(false);
                        }
                        ocorrenciaUsuario.add(true);
                        idOcorrencia.add(document.getId());
                        nomeBairro.add(document.getString("bairro"));
                        textoStatus.add(document.getString("situacao"));
                        dataInicio.add(document.getString("dataInicio"));
                        dataFim.add(document.getString("dataFim"));
                        descricao.add(document.getString("descricao"));
                        nomeResponsavel.add(document.getString("nomeResponsavel"));
                        pathReference = storageRef.child(document.getId() + "/" + document.getId() + "_antes.jpg");
                        pathReference.getDownloadUrl().addOnSuccessListener(uri -> {
                            fotoAntes.add(String.valueOf(uri));
                            myAdapter.notifyDataSetChanged();
                        });
                        pathReference = storageRef.child(document.getId() + "/" + document.getId() + "_depois.jpg");
                        pathReference.getDownloadUrl().addOnSuccessListener(uri1 -> {
                            fotoDepois.add(String.valueOf(uri1));
                            myAdapter.notifyDataSetChanged();
                        }).addOnFailureListener(uri2 -> {
                            fotoDepois.add(null);
                            myAdapter.notifyDataSetChanged();
                        });
                    }
                }
                myAdapter.notifyDataSetChanged();
            } else {
                Log.d("erro", "Error getting documents.", task.getException());
            }
        });
    }

    private void clearArrays() {
        idOcorrencia.clear();
        nomeBairro.clear();
        textoStatus.clear();
        dataInicio.clear();
        dataFim.clear();
        fotoAntes.clear();
        fotoDepois.clear();
        descricao.clear();
        nomeResponsavel.clear();
        ocorrenciaAvaliada.clear();
        ocorrenciaUsuario.clear();
    }

    private void registrosGeral(Boolean admin, String idUsuario) {
        queryOutros.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    if (!Objects.requireNonNull(document.getString("codUsuario")).equals(idUsuario) && document.getBoolean("validacao")) {
                        if (admin || !Objects.requireNonNull(document.getString("situacao")).equals("Em espera")) {
                            ocorrenciaUsuario.add(false);
                            ocorrenciaAvaliada.add(false);
                            idOcorrencia.add(document.getId());
                            nomeBairro.add(document.getString("bairro"));
                            textoStatus.add(document.getString("situacao"));
                            dataInicio.add(document.getString("dataInicio"));
                            dataFim.add(document.getString("dataFim"));
                            if (admin) {
                                descricao.add(document.getString("descricao"));
                                nomeResponsavel.add(document.getString("nomeResponsavel"));
                            } else {
                                descricao.add(null);
                                nomeResponsavel.add(null);
                            }
                            pathReference = storageRef.child(document.getId() + "/" + document.getId() + "_antes.jpg");
                            pathReference.getDownloadUrl().addOnSuccessListener(uri -> {
                                fotoAntes.add(String.valueOf(uri));
                                myAdapter.notifyDataSetChanged();
                            });
                            pathReference = storageRef.child(document.getId() + "/" + document.getId() + "_depois.jpg");
                            pathReference.getDownloadUrl().addOnSuccessListener(uri1 -> {
                                fotoDepois.add(String.valueOf(uri1));
                                myAdapter.notifyDataSetChanged();
                            }).addOnFailureListener(uri2 -> {
                                fotoDepois.add(null);
                                myAdapter.notifyDataSetChanged();
                            });
                        }
                    }
                }
                focarPosicao();
                myAdapter.notifyDataSetChanged();
            } else {
                Log.d("erro", "Error getting documents.", task.getException());
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (getIntent().getStringExtra("ID_OCORRENCIA") != null) {
            expandirPosicao.add(getIntent().getStringExtra("ID_OCORRENCIA"));
        }
    }
}
