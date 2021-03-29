package com.otavio14.roadreport;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    Context context;
    ArrayList<String> nomeRua = new ArrayList<>();
    ArrayList<String> nomeBairro = new ArrayList<>();
    ArrayList<String> referencia = new ArrayList<>();
    ArrayList<String> textoStatus = new ArrayList<>();
    ArrayList<Integer> iconeStatus = new ArrayList<>();
    ArrayList<String> dataInicio = new ArrayList<>();
    ArrayList<String> dataFim = new ArrayList<>();
    ArrayList<Integer> fotoAntes = new ArrayList<>();
    ArrayList<Integer> fotoDepois = new ArrayList<>();
    ArrayList<String> descricao = new ArrayList<>();
    ArrayList<String> nomeResponsavel = new ArrayList<>();

    public MyAdapter(Context ct, ArrayList<String> p_nomeRua, ArrayList<String> p_nomeBairro,
                     ArrayList<String> p_referencia, ArrayList<String> p_textStatus,
                     ArrayList<Integer> p_iconeStatus, ArrayList<String> p_dataInicio,
                     ArrayList<String> p_dataFim, ArrayList<Integer> p_fotoAntes,
                     ArrayList<Integer> p_fotoDepois, ArrayList<String> p_descricao,
                     ArrayList<String> p_nomeResponsavel) {
        context = ct;
        nomeRua = p_nomeRua;
        nomeBairro = p_nomeBairro;
        referencia = p_referencia;
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
        holder.referencia.setText(referencia.get(position));
        holder.dataInicioValor.setText(dataInicio.get(position));
        holder.dataFimValor.setText(dataFim.get(position));
        holder.descricao.setText(descricao.get(position));
        holder.nomeResponsavel.setText(nomeResponsavel.get(position));
        holder.fotoAntes.setImageResource(fotoAntes.get(position));
        holder.fotoDepois.setImageResource(fotoDepois.get(position));
    }

    @Override
    public int getItemCount() {

        return nomeBairro.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView nomeRua, nomeBairro, referencia, dataInicio, dataFim, dataInicioValor, dataFimValor, descricao, nomeResponsavel;
        ImageView fotoAntes, fotoDepois;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            nomeRua = itemView.findViewById(R.id.textNomeRua);
            nomeBairro = itemView.findViewById(R.id.textNomeBairro);
            referencia = itemView.findViewById(R.id.textReferencia);
            dataInicio = itemView.findViewById(R.id.textDataInicio);
            dataFim = itemView.findViewById(R.id.textDataFim);
            dataInicioValor = itemView.findViewById(R.id.textDataInicioValor);
            dataFimValor = itemView.findViewById(R.id.textDataFimValor);
            fotoAntes = itemView.findViewById(R.id.textFotoAntes);
            fotoDepois = itemView.findViewById(R.id.textFotoDepois);
            descricao = itemView.findViewById(R.id.textDescricao);
            nomeResponsavel = itemView.findViewById(R.id.textResponsavel);
        }
    }
}
