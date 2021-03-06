package com.otavio14.roadreport;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    TextView textLocalizacao, textCardBairro, textCardStatus, textCardDataInicio, textCardDataFim;
    Button buttonCardVerMais;
    CardView cardMapa;
    private GoogleMap mMap;
    ExtendedFloatingActionButton efabSair, efabVerOcorrencias, efabRelatarOcorrencia, efabNomeUsuario;
    FloatingActionButton fabMenuFechado, fabCardFechar;
    boolean menuStatus = false;

    FirebaseFirestore database;
    SharedPreferences sharedPreferences;
    Boolean admin = false;
    String idUsuario;

    //Vari??vel para acesso a localiza????o atual
    FusedLocationProviderClient fusedLocationClient;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Inicia uma sess??o de login
        sharedPreferences = getSharedPreferences("shared_preferences", Context.MODE_PRIVATE);

        //Acesso a int??ncia do Cloud Firestore
        database = FirebaseFirestore.getInstance();

        //bloqueia o modo escuro
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
        }

        fabMenuFechado = findViewById(R.id.menu_fechado);
        efabVerOcorrencias = findViewById(R.id.ver_ocorrencias);
        efabSair = findViewById(R.id.sair);
        efabRelatarOcorrencia = findViewById(R.id.relatar_ocorrencia);
        efabNomeUsuario = findViewById(R.id.nome_usuario);
        textLocalizacao = findViewById(R.id.textLocalizacao);
        textCardBairro = findViewById(R.id.textCardBairro);
        textCardStatus = findViewById(R.id.textCardStatus);
        textCardDataInicio = findViewById(R.id.textCardDataInicio);
        textCardDataFim = findViewById(R.id.textCardDataFim);
        buttonCardVerMais = findViewById(R.id.buttonCardVerMais);
        fabCardFechar = findViewById(R.id.fabCardFechar);
        cardMapa = findViewById(R.id.cardMapa);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        //Insere o nome do usu??rio no menu
        sharedPreferences.registerOnSharedPreferenceChangeListener((sharedPreferences, key) -> {
            String[] nome = sharedPreferences.getString("nome_key", "").split(" ");
            efabNomeUsuario.setText(nome[0]);
            admin = sharedPreferences.getBoolean("administrador_key", false);
            idUsuario = sharedPreferences.getString("idUsuario_key", "");
        });

        fusedLocationClient.getLastLocation().addOnCompleteListener(task -> {
            Location location = task.getResult();
            if (location != null) {
                try {
                    Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                    List<Address> addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    String[] endereco = addressList.get(0).getAddressLine(0).split("-");
                    String[] subEndereco = endereco[1].split(",");
                    String[] estado = endereco[2].split(",");
                    textLocalizacao.setText(subEndereco[0].trim() + "\n" + subEndereco[1].trim() + " - " + estado[0].trim());
                } catch (IOException e) {
                    Log.w("", "Erro de localiza????o: " + e);
                }
            }
        });

        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                try {
                    Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                    List<Address> addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    String[] endereco = addressList.get(0).getAddressLine(0).split("-");
                    String[] subEndereco = endereco[1].split(",");
                    textLocalizacao.setText(subEndereco[0].trim() + "\n" + addressList.get(0).getLocality() + " - " + addressList.get(0).getCountryCode());
                } catch (IOException e) {
                    Log.w("", "Erro de localiza????o: " + e);
                }
            }
        };

        fabMenuFechado.setOnClickListener(v -> {
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
                cardMapa.setVisibility(View.GONE);
                fabMenuFechado.setImageResource(R.drawable.ic_menu_aberto);
                fabMenuFechado.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.gray)));
                efabSair.show();
                efabRelatarOcorrencia.show();
                efabVerOcorrencias.show();
                efabNomeUsuario.show();
                menuStatus = true;
                //Direciona para a tela de ocorr??ncias
                efabVerOcorrencias.setOnClickListener(v1 -> {
                    Intent intent = new Intent(MapsActivity.this, OcorrenciasActivity.class);
                    startActivity(intent);
                });
                //Sair da conta
                efabSair.setOnClickListener(v12 -> {
                    //Finaliza a sess??o
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.clear();
                    editor.apply();
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(MapsActivity.this, LoginActivity.class);
                    startActivity(intent);
                });
                //Direciona para a tela de relatar ocorr??ncias
                efabRelatarOcorrencia.setOnClickListener(v13 -> {
                    Intent intent = new Intent(MapsActivity.this, RegistrarOcorrenciaActivity.class);
                    startActivity(intent);
                });
            }
        });

        fabCardFechar.setOnClickListener(v -> cardMapa.setVisibility(View.GONE));
    }

    //Desabilita o bot??o voltar
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
    @SuppressLint("SetTextI18n")
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);
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

        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.style_json));

            if (!success) {
                Log.d("erro", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.d("erro", "Can't find style. Error: ", e);
        }

        mMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        LatLng localInicial = new LatLng(-23.097395584050947, -47.22833023185295);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(localInicial, 12f));
        mapsMarker();

        if (getIntent().getStringExtra("ID_OCORRENCIA") != null) {
            fabMenuFechado.setImageResource(R.drawable.ic_menu_fechado);
            fabMenuFechado.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.orange)));
            efabSair.hide();
            efabRelatarOcorrencia.hide();
            efabVerOcorrencias.hide();
            efabNomeUsuario.hide();
            menuStatus = false;
            cardMapa.setVisibility(View.VISIBLE);
            database.collection("registro").document(getIntent()
                    .getStringExtra("ID_OCORRENCIA"))
                    .get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    LatLng local = new LatLng(Double.parseDouble(Objects.requireNonNull(document.getString("latitude"))), Double.parseDouble(Objects.requireNonNull(document.getString("longitude"))));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(local, 18f));
                    textCardBairro.setText(document.getString("bairro"));
                    textCardStatus.setText("Status: " + document.getString("situacao"));
                    textCardDataInicio.setText(document.getString("dataInicio"));
                    if (document.getString("dataFim") != null) {
                        textCardDataFim.setText(document.getString("dataFim"));
                        textCardDataFim.setVisibility(View.VISIBLE);
                    }
                } else {
                    Log.d("erro", "Error getting documents.", task.getException());
                }
            });
        }


        mMap.setOnMarkerClickListener(marker -> {
            fabMenuFechado.setImageResource(R.drawable.ic_menu_fechado);
            fabMenuFechado.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.orange)));
            efabSair.hide();
            efabRelatarOcorrencia.hide();
            efabVerOcorrencias.hide();
            efabNomeUsuario.hide();
            menuStatus = false;
            cardMapa.setVisibility(View.VISIBLE);
            database.collection("registro").document(Objects.requireNonNull(marker.getTag()).toString())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            textCardBairro.setText(document.getString("bairro"));
                            textCardStatus.setText("Status: " + document.getString("situacao"));
                            textCardDataInicio.setText(document.getString("dataInicio"));
                            if (document.getString("dataFim") != null) {
                                textCardDataFim.setText(document.getString("dataFim"));
                                textCardDataFim.setVisibility(View.VISIBLE);
                            } else {
                                textCardDataFim.setVisibility(View.GONE);
                            }
                        } else {
                            Log.d("erro", "Error getting documents.", task.getException());
                        }
                    });
            buttonCardVerMais.setOnClickListener(v -> {
                Intent intent = new Intent(getApplicationContext(), OcorrenciasActivity.class);
                intent.putExtra("ID_OCORRENCIA", marker.getTag().toString());
                startActivity(intent);
            });
            return false;
        });
    }

    /**
     * Adiciona os marcadores conforme o status da ocorr??ncia
     */
    private void mapsMarker() {
        database.collection("registro")
                .whereEqualTo("validacao", true)
                .get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (DocumentSnapshot document : task.getResult()) {
                    if ((document.getString("codUsuario").equals(idUsuario) || admin) && Objects.requireNonNull(document.getString("situacao")).equals("Em espera")) {
                        LatLng latLng1 = new LatLng(Double.parseDouble(Objects.requireNonNull(document.getString("latitude"))), Double.parseDouble(Objects.requireNonNull(document.getString("longitude"))));
                        Marker marker1 = mMap.addMarker(new MarkerOptions().position(latLng1));
                        Objects.requireNonNull(marker1).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                        marker1.setTag(document.getId());
                    }
                    if (Objects.requireNonNull(document.getString("situacao")).equals("Em andamento")) {
                        LatLng latLng2 = new LatLng(Double.parseDouble(Objects.requireNonNull(document.getString("latitude"))), Double.parseDouble(Objects.requireNonNull(document.getString("longitude"))));
                        Marker marker2 = mMap.addMarker(new MarkerOptions().position(latLng2));
                        Objects.requireNonNull(marker2).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                        marker2.setTag(document.getId());
                    }
                    if (Objects.requireNonNull(document.getString("situacao")).equals("Concluido")) {
                        LatLng latLng3 = new LatLng(Double.parseDouble(Objects.requireNonNull(document.getString("latitude"))), Double.parseDouble(Objects.requireNonNull(document.getString("longitude"))));
                        Marker marker3 = mMap.addMarker(new MarkerOptions().position(latLng3));
                        Objects.requireNonNull(marker3).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                        marker3.setTag(document.getId());
                    }
                }
            } else {
                Log.d("erro", "Error getting documents: ", task.getException());
            }
        });
    }
}