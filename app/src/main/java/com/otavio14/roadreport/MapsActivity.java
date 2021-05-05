package com.otavio14.roadreport;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    ExtendedFloatingActionButton efabSair, efabVerOcorrencias, efabRelatarOcorrencia, efabNomeUsuario;
    FloatingActionButton fabMenuFechado;
    boolean menuStatus = false;

    FirebaseFirestore database;
    SharedPreferences sharedPreferences;
    Boolean admin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Inicia uma sessão de login
        sharedPreferences = getSharedPreferences("shared_preferences", Context.MODE_PRIVATE);

        //Acesso a intância do Cloud Firestore
        database = FirebaseFirestore.getInstance();

        //bloqueia o modo escuro
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        fabMenuFechado = findViewById(R.id.menu_fechado);
        efabVerOcorrencias = findViewById(R.id.ver_ocorrencias);
        efabSair = findViewById(R.id.sair);
        efabRelatarOcorrencia = findViewById(R.id.relatar_ocorrencia);
        efabNomeUsuario = findViewById(R.id.nome_usuario);

        //Insere o nome do usuário no menu
        sharedPreferences.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                String[] nome = sharedPreferences.getString("nome_key", "").split(" ");
                efabNomeUsuario.setText(nome[0]);
                admin = sharedPreferences.getBoolean("administrador_key",false);
            }
        });

        fabMenuFechado.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (menuStatus) {
                    //Oculta o menu
                    fabMenuFechado.setImageResource(R.drawable.ic_menu_fechado);
                    fabMenuFechado.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.orange)));
                    efabSair.hide();
                    efabRelatarOcorrencia.hide();
                    efabVerOcorrencias.hide();
                    efabNomeUsuario.hide();
                    menuStatus = false;
                } else {
                    //Exibe o menu
                    fabMenuFechado.setImageResource(R.drawable.ic_menu_aberto);
                    fabMenuFechado.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.gray)));
                    efabSair.show();
                    efabRelatarOcorrencia.show();
                    efabVerOcorrencias.show();
                    efabNomeUsuario.show();
                    menuStatus = true;
                    //Direciona para a tela de ocorrências
                    efabVerOcorrencias.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(MapsActivity.this, OcorrenciasActivity.class);
                            startActivity(intent);
                        }
                    });
                    //Sair da conta
                    efabSair.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //Finaliza a sessão
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.clear();
                            editor.apply();
                            FirebaseAuth.getInstance().signOut();
                            Intent intent = new Intent(MapsActivity.this, LoginActivity.class);
                            startActivity(intent);
                        }
                    });
                    //Direciona para a tela de relatar ocorrências
                    efabRelatarOcorrencia.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(MapsActivity.this, RegistrarOcorrenciaActivity.class);
                            startActivity(intent);
                        }
                    });
                }
            }
        });
    }

    //Desabilita o botão voltar
    @Override
    public void onBackPressed() {

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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        LatLng localInicial = new LatLng(-23.097395584050947, -47.22833023185295);
        //mMap.addMarker(new MarkerOptions().position(localInicial)).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(localInicial,12f));
        mapsMarker();
    }

    private void mapsMarker() {
        database.collection("registro").whereEqualTo("validacao",true).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {
                        if(admin == true && document.getString("situacao").equals("Em espera")) {
                            LatLng latLng1 = new LatLng(Double.parseDouble(document.getString("latitude")), Double.parseDouble(document.getString("longitude")));
                            mMap.addMarker(new MarkerOptions().position(latLng1)).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                        }
                        if (document.getString("situacao").equals("Em andamento")) {
                            LatLng latLng2 = new LatLng(Double.parseDouble(document.getString("latitude")), Double.parseDouble(document.getString("longitude")));
                            mMap.addMarker(new MarkerOptions().position(latLng2)).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                        }
                        if (document.getString("situacao").equals("Concluido")) {
                            LatLng latLng3 = new LatLng(Double.parseDouble(document.getString("latitude")), Double.parseDouble(document.getString("longitude")));
                            mMap.addMarker(new MarkerOptions().position(latLng3)).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                        }
                    }
                } else {
                    Log.d("erro", "Error getting documents: ", task.getException());
                }
            }
        });
    }
}