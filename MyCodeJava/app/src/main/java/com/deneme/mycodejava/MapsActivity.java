package com.deneme.mycodejava;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.deneme.mycodejava.databinding.ActivityMapsBinding;
import com.google.android.material.snackbar.Snackbar;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    LocationManager locationManager;
    LocationListener locationListener;
    ActivityResultLauncher<String> permissionLauncher;

    // Yeni: Kullanıcı konumunu tutmak için değişken
    private LatLng userLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // İzin isteği başlatıcıyı kaydediyoruz
        registerLauncher();

        // Harita yüklenmeden önce konum verilerini almak için izlemeye başlıyoruz
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Harita hazır olduğunda çalışacak fonksiyonu başlatıyoruz
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Konum bilgileri güncellendikçe işlemi tetikleyecek listener
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                // Kullanıcının yeni konumunu alıyoruz
                userLocation = new LatLng(location.getLatitude(), location.getLongitude());

                // Yeni konumu ekliyoruz veya güncelliyoruz
                updateUserLocationOnMap();

                // Haritada otomatik olarak yeni konuma odaklanıyoruz
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
            }
        };

        // İzin kontrolü yapıyoruz
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Eğer izin yoksa, izin istemek için gösterim yapılır
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Snackbar.make(binding.getRoot(), "Harita ve konum bilgisi için izin gereklidir", Snackbar.LENGTH_INDEFINITE)
                        .setAction("İzin Ver", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
                            }
                        }).show();
            } else {
                // Eğer izin daha önce istenmediyse, doğrudan izin istenir
                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            }
        } else {
            // İzin verildiğinde, konum bilgilerini alıyoruz
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }

        // Başlangıçta sabit bir marker ekliyoruz (Soykan Malikhanesi)
        LatLng sydney = new LatLng(40.8115327, 30.1223011);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Soykan Malikhanesi"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    private void updateUserLocationOnMap() {
        if (userLocation != null) {
            // Eğer kullanıcı konumu zaten haritada varsa, marker'ı güncelliyoruz
            mMap.clear(); // Haritadaki mevcut tüm markerları temizliyoruz
            mMap.addMarker(new MarkerOptions()
                    .position(userLocation)
                    .title("Sizin Konumunuz")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)) // Özel bir marker rengi
            );
        }
    }

    private void registerLauncher() {
        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                if (result) {
                    // İzin verildi, konum bilgisi almaya başlıyoruz
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                } else {
                    // İzin verilmedi, kullanıcıya hata mesajı gösteriyoruz
                    Snackbar.make(binding.getRoot(), "İzin verilmedi, harita kullanılamaz.", Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }
}
