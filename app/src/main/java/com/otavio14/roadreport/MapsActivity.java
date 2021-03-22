package com.otavio14.roadreport;

import androidx.fragment.app.FragmentActivity;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    ExtendedFloatingActionButton efabSair, efabVerOcorrencias, efabRelatarOcorrencia, efabNomeUsuario;
    FloatingActionButton fabMenuFechado;
    boolean menuStatus = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        fabMenuFechado = (FloatingActionButton) findViewById(R.id.menu_fechado);
        efabVerOcorrencias = (ExtendedFloatingActionButton) findViewById(R.id.ver_ocorrencias);
        efabSair = (ExtendedFloatingActionButton) findViewById(R.id.sair);
        efabRelatarOcorrencia = (ExtendedFloatingActionButton) findViewById(R.id.relatar_ocorrencia);
        efabNomeUsuario = (ExtendedFloatingActionButton) findViewById(R.id.nome_usuario);

        fabMenuFechado.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(menuStatus) {
                    fabMenuFechado.setImageResource(R.drawable.ic_menu_fechado);
                    fabMenuFechado.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.orange)));
                    efabSair.hide();
                    efabRelatarOcorrencia.hide();
                    efabVerOcorrencias.hide();
                    efabNomeUsuario.hide();
                    menuStatus = false;
                }
                else {
                    fabMenuFechado.setImageResource(R.drawable.ic_menu_aberto);
                    fabMenuFechado.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.gray)));
                    efabSair.show();
                    efabRelatarOcorrencia.show();
                    efabVerOcorrencias.show();
                    efabNomeUsuario.show();
                    menuStatus = true;
                }
            }
        });
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng fiec = new LatLng(-23.097395584050947, -47.22833023185295);
        mMap.addMarker(new MarkerOptions().position(fiec).title("Marker em FIEC"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(fiec));
    }
}