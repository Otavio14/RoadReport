package com.otavio14.roadreport;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    Context context;
    ArrayList<String> nomeBairro;
    ArrayList<String> textoStatus;
    ArrayList<String> opcoesStatus = new ArrayList<>(Arrays.asList("Invalidar", "Em espera", "Em andamento", "Concluído"));
    ArrayList<Integer> iconeStatus;
    ArrayList<String> dataInicio;
    ArrayList<String> dataFim;
    ArrayList<String> fotoAntes;
    ArrayList<String> fotoDepois;
    ArrayList<String> descricao;
    ArrayList<String> nomeResponsavel;
    ArrayList<String> idOcorrencia;
    ArrayList<Boolean> ocorrenciaUsuario;
    ArrayList<Boolean> ocorrenciaAvaliada;
    ArrayList<String> expandirPosicao;
    boolean admin;
    FirebaseFirestore database = FirebaseFirestore.getInstance();

    public MyAdapter(Context ct, ArrayList<String> p_nomeBairro,
                     ArrayList<String> p_textStatus, ArrayList<Integer> p_iconeStatus, ArrayList<String> p_dataInicio,
                     ArrayList<String> p_dataFim, ArrayList<String> p_fotoAntes,
                     ArrayList<String> p_fotoDepois, ArrayList<String> p_descricao,
                     ArrayList<String> p_nomeResponsavel, ArrayList<String> p_idOcorrencia, ArrayList<Boolean> p_ocorrenciaUsuario,
                     boolean p_admin, ArrayList<Boolean> p_ocorrenciaAvaliada, ArrayList<String> p_expandirPosicao) {
        context = ct;
        nomeBairro = p_nomeBairro;
        textoStatus = p_textStatus;
        iconeStatus = p_iconeStatus;
        dataInicio = p_dataInicio;
        dataFim = p_dataFim;
        fotoAntes = p_fotoAntes;
        fotoDepois = p_fotoDepois;
        descricao = p_descricao;
        nomeResponsavel = p_nomeResponsavel;
        idOcorrencia = p_idOcorrencia;
        ocorrenciaUsuario = p_ocorrenciaUsuario;
        admin = p_admin;
        ocorrenciaAvaliada = p_ocorrenciaAvaliada;
        expandirPosicao = p_expandirPosicao;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.my_row, parent, false);
        return new MyViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.nomeBairro.setText(nomeBairro.get(position));
        holder.dataInicioValor.setText(dataInicio.get(position));
        if (dataFim.get(position) != null) {
            holder.dataFimValor.setText(dataFim.get(position));
        } else {
            holder.dataFimValor.setText("Em breve");
        }
        holder.descricao.setKeyListener(null);
        if (descricao.get(position) != null) {
            holder.descricao.setText(descricao.get(position));
        } else {
            holder.descricao.setText("Descrição");
        }
        if (nomeResponsavel.get(position) != null) {
            holder.nomeResponsavel.setText(nomeResponsavel.get(position));
        } else {
            holder.nomeResponsavel.setText("Nome do Responsável");
        }

        if (position < fotoAntes.size()) {
            Glide.with(context).load(fotoAntes.get(position)).into(holder.fotoAntes);
        }

        if (position < fotoDepois.size()) {
            Glide.with(context).load(fotoDepois.get(position)).into(holder.fotoDepois);
        }

        holder.spinnerStatus.setOnItemSelectedListener(null);
        //Spinner do status
        ArrayAdapter<String> adapterStatus = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, opcoesStatus);
        adapterStatus.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.spinnerStatus.setAdapter(adapterStatus);
        switch (textoStatus.get(position)) {
            case "Em espera":
                holder.spinnerStatus.setSelection(1);
                break;
            case "Em andamento":
                holder.spinnerStatus.setSelection(2);
                break;
            case "Concluido":
                holder.spinnerStatus.setSelection(3);
                break;
        }

        if (ocorrenciaUsuario.get(position) && !ocorrenciaAvaliada.get(position) && textoStatus.get(position).equals("Concluido")) {
            holder.buttonAvaliar.setVisibility(View.VISIBLE);
        } else {
            holder.buttonAvaliar.setVisibility(View.GONE);
        }

        holder.spinnerStatus.setEnabled(false);
        if (admin) {
            holder.spinnerStatus.setEnabled(true);
            holder.descricao.setVisibility(View.VISIBLE);
            holder.nomeResponsavel.setVisibility(View.VISIBLE);
        }

        if (ocorrenciaUsuario.get(position)) {
            holder.descricao.setVisibility(View.VISIBLE);
            holder.nomeResponsavel.setVisibility(View.VISIBLE);
        }

        holder.spinnerStatus.post(() -> holder.spinnerStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int p_position, long id) {
                switch (p_position) {
                    case 0:
                        if (!textoStatus.get(position).equals("Concluido")) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setCancelable(true);
                            builder.setTitle("Confirmar invalidação");
                            builder.setMessage("Deseja invalidar a ocorrência para uma futura avaliação?");
                            builder.setPositiveButton("Sim",
                                    (dialog, which) -> database.collection("registro").document(idOcorrencia.get(position)).update("validacao", false));
                            builder.setNegativeButton("Não", (dialog, which) -> notifyDataSetChanged());
                            AlertDialog dialog = builder.create();
                            dialog.show();
                        } else {
                            notifyDataSetChanged();
                            Toast.makeText(context, "Seleção incorreta", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case 1:
                        if (!textoStatus.get(position).equals("Concluido") && !textoStatus.get(position).equals("Em espera")) {
                            database.collection("registro").document(idOcorrencia.get(position)).update("situacao", "Em espera");
                        } else {
                            Toast.makeText(context, "Seleção incorreta", Toast.LENGTH_SHORT).show();
                            notifyDataSetChanged();
                        }
                        break;
                    case 2:
                        if (!textoStatus.get(position).equals("Concluido") && !textoStatus.get(position).equals("Em andamento")) {
                            database.collection("registro").document(idOcorrencia.get(position)).update("situacao", "Em andamento");
                        } else {
                            notifyDataSetChanged();
                            Toast.makeText(context, "Seleção incorreta", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case 3:
                        if (!textoStatus.get(position).equals("Concluido")) {
                            Intent intent = new Intent(context, ConcluirOcorrenciaActivity.class);
                            intent.putExtra("ID_OCORRENCIA", idOcorrencia.get(position));
                            context.startActivity(intent);
                        } else {
                            notifyDataSetChanged();
                            Toast.makeText(context, "Seleção incorreta", Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        }));

        holder.buttonAvaliar.setOnClickListener(v -> {
            Intent intent = new Intent(context, AvaliarActivity.class);
            intent.putExtra("ID_OCORRENCIA", idOcorrencia.get(position));
            context.startActivity(intent);
        });

        if (expandirPosicao.size() > 0) {
            if (expandirPosicao.get(0).equals(idOcorrencia.get(position))) {
                holder.hiddenViewOcorrencias.setVisibility(View.VISIBLE);
                holder.buttonExpandirOcorrencias.setBackgroundResource(R.drawable.ic_expand_less);
            } else {
                holder.hiddenViewOcorrencias.setVisibility(View.GONE);
                holder.buttonExpandirOcorrencias.setBackgroundResource(R.drawable.ic_expand_more);
            }
        }

        holder.textVerNoMapa.setOnClickListener(v -> {
            Intent intent = new Intent(context, MapsActivity.class);
            intent.putExtra("ID_OCORRENCIA", idOcorrencia.get(position));
            context.startActivity(intent);
        });

        holder.buttonExpandirOcorrencias.setOnClickListener(v -> {
            // If the CardView is already expanded, set its visibility
            //  to gone and change the expand less icon to expand more.
            if (holder.hiddenViewOcorrencias.getVisibility() == View.VISIBLE) {

                // The transition of the hiddenView is carried out
                //  by the TransitionManager class.
                // Here we use an object of the AutoTransition
                // Class to create a default transition.
                TransitionManager.beginDelayedTransition(holder.cardView,
                        new AutoTransition());
                holder.hiddenViewOcorrencias.setVisibility(View.GONE);
                holder.buttonExpandirOcorrencias.setBackgroundResource(R.drawable.ic_expand_more);
            }

            // If the CardView is not expanded, set its visibility
            // to visible and change the expand more icon to expand less.
            else {

                TransitionManager.beginDelayedTransition(holder.cardView,
                        new AutoTransition());
                holder.hiddenViewOcorrencias.setVisibility(View.VISIBLE);
                holder.buttonExpandirOcorrencias.setBackgroundResource(R.drawable.ic_expand_less);
            }
        });
    }

    @Override
    public int getItemCount() {
        return nomeBairro.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView nomeBairro, dataInicioValor, dataFimValor, descricao, nomeResponsavel, textVerNoMapa;
        ImageView fotoAntes, fotoDepois;
        CardView cardView;
        Button buttonExpandirOcorrencias, buttonAvaliar;
        ConstraintLayout hiddenViewOcorrencias;
        Spinner spinnerStatus;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            nomeBairro = itemView.findViewById(R.id.textNomeBairro);
            dataInicioValor = itemView.findViewById(R.id.textDataInicioValor);
            dataFimValor = itemView.findViewById(R.id.textDataFimValor);
            fotoAntes = itemView.findViewById(R.id.textFotoAntes);
            fotoDepois = itemView.findViewById(R.id.textFotoDepois);
            descricao = itemView.findViewById(R.id.textDescricao);
            nomeResponsavel = itemView.findViewById(R.id.textResponsavel);
            spinnerStatus = itemView.findViewById(R.id.spinnerStatus);
            buttonAvaliar = itemView.findViewById(R.id.buttonAvaliar);

            cardView = itemView.findViewById(R.id.base_cardview_ocorrencias);
            buttonExpandirOcorrencias = itemView.findViewById(R.id.buttonExpandirOcorrencias);
            hiddenViewOcorrencias = itemView.findViewById(R.id.hidden_view_ocorrencias);
            textVerNoMapa = itemView.findViewById(R.id.textVerNoMapa);
        }
    }
}
