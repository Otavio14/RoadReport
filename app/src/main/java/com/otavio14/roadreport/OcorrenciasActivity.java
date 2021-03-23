package com.otavio14.roadreport;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;

import android.transition.AutoTransition;
import android.transition.TransitionManager;

public class OcorrenciasActivity extends AppCompatActivity {

    TextView botaoExpandir;
    ConstraintLayout hiddenView;
    CardView cardView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ocorrencias);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        cardView = findViewById(R.id.base_cardview);
        botaoExpandir = findViewById(R.id.filtros);
        hiddenView = findViewById(R.id.hidden_view);
        Drawable ic_expandir = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_expand_more, null);
        Drawable ic_recolher = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_expand_less, null);

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
