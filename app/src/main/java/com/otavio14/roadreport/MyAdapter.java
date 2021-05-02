package com.otavio14.roadreport;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    Context context;
    ArrayList<String> nomeRua = new ArrayList<>();
    ArrayList<String> nomeBairro = new ArrayList<>();
    ArrayList<String> textoStatus = new ArrayList<>();
    ArrayList<Integer> iconeStatus = new ArrayList<>();
    ArrayList<String> dataInicio = new ArrayList<>();
    ArrayList<String> dataFim = new ArrayList<>();
    ArrayList<Integer> fotoAntes = new ArrayList<>();
    ArrayList<Integer> fotoDepois = new ArrayList<>();
    ArrayList<String> descricao = new ArrayList<>();
    ArrayList<String> nomeResponsavel = new ArrayList<>();

    public MyAdapter(Context ct, ArrayList<String> p_nomeRua, ArrayList<String> p_nomeBairro,
                     ArrayList<String> p_textStatus, ArrayList<Integer> p_iconeStatus, ArrayList<String> p_dataInicio,
                     ArrayList<String> p_dataFim, ArrayList<Integer> p_fotoAntes,
                     ArrayList<Integer> p_fotoDepois, ArrayList<String> p_descricao,
                     ArrayList<String> p_nomeResponsavel) {
        context = ct;
        nomeRua = p_nomeRua;
        nomeBairro = p_nomeBairro;
        textoStatus = p_textStatus;
        iconeStatus = p_iconeStatus;
        dataInicio = p_dataInicio;
        dataFim = p_dataFim;
        fotoAntes = p_fotoAntes;
        fotoDepois = p_fotoDepois;
        descricao = p_descricao;
        nomeResponsavel = p_nomeResponsavel;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.my_row, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.nomeRua.setText(nomeRua.get(position));
        holder.nomeBairro.setText(nomeBairro.get(position));
        holder.dataInicioValor.setText(dataInicio.get(position));
        holder.dataFimValor.setText(dataFim.get(position));
        holder.descricao.setText(descricao.get(position));
        holder.nomeResponsavel.setText(nomeResponsavel.get(position));
        holder.fotoAntes.setImageResource(fotoAntes.get(position));
        holder.fotoDepois.setImageResource(fotoDepois.get(position));

        //Spinner do status
        ArrayAdapter<String> adapterStatus = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, textoStatus);
        adapterStatus.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.spinnerStatus.setAdapter(adapterStatus);

        holder.buttonExpandirOcorrencias.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
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
            }
        });
    }

    @Override
    public int getItemCount() {
        return nomeBairro.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView nomeRua, nomeBairro, dataInicio, dataFim, dataInicioValor, dataFimValor, descricao, nomeResponsavel;
        ImageView fotoAntes, fotoDepois;
        CardView cardView;
        Button buttonExpandirOcorrencias;
        ConstraintLayout hiddenViewOcorrencias;
        Spinner spinnerStatus;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            nomeRua = itemView.findViewById(R.id.textNomeRua);
            nomeBairro = itemView.findViewById(R.id.textNomeBairro);
            dataInicio = itemView.findViewById(R.id.textDataInicio);
            dataFim = itemView.findViewById(R.id.textDataFim);
            dataInicioValor = itemView.findViewById(R.id.textDataInicioValor);
            dataFimValor = itemView.findViewById(R.id.textDataFimValor);
            fotoAntes = itemView.findViewById(R.id.textFotoAntes);
            fotoDepois = itemView.findViewById(R.id.textFotoDepois);
            descricao = itemView.findViewById(R.id.textDescricao);
            nomeResponsavel = itemView.findViewById(R.id.textResponsavel);
            spinnerStatus = itemView.findViewById(R.id.spinnerStatus);

            cardView = itemView.findViewById(R.id.base_cardview_ocorrencias);
            buttonExpandirOcorrencias = itemView.findViewById(R.id.buttonExpandirOcorrencias);
            hiddenViewOcorrencias = itemView.findViewById(R.id.hidden_view_ocorrencias);

        }
    }
}
