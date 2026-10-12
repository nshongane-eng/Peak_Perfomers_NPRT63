package com.peakperformers.smartloadshedding.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.peakperformers.smartloadshedding.R;
import com.peakperformers.smartloadshedding.fragments.CommunityFragment;
import com.peakperformers.smartloadshedding.fragments.GrantsFragment;
import com.peakperformers.smartloadshedding.fragments.HomeFragment;
import com.peakperformers.smartloadshedding.fragments.LossesFragment;
import com.peakperformers.smartloadshedding.fragments.ScheduleFragment;
import com.peakperformers.smartloadshedding.utils.NotificationHelper;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NotificationHelper.createNotificationChannel(this);

        drawerLayout = findViewById(R.id.drawerLayout);
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        NavigationView navView = findViewById(R.id.navView);

        if (savedInstanceState == null) {
            swapFragment(new HomeFragment());
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selected;
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                selected = new HomeFragment();
            } else if (id == R.id.nav_schedule) {
                selected = new ScheduleFragment();
            } else if (id == R.id.nav_losses) {
                selected = new LossesFragment();
            } else if (id == R.id.nav_grants) {
                selected = new GrantsFragment();
            } else if (id == R.id.nav_community) {
                selected = new CommunityFragment();
            } else {
                selected = new HomeFragment();
            }

            swapFragment(selected);
            return true;
        });

        navView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            drawerLayout.closeDrawer(GravityCompat.START);

            if (id == R.id.nav_profile) {
                startActivity(new Intent(MainActivity.this, BusinessProfileActivity.class));
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            } else if (id == R.id.nav_logout) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
            return true;
        });
    }

    public void openDrawer() {
        if (drawerLayout != null) {
            drawerLayout.openDrawer(GravityCompat.START);
        }
    }

    private void swapFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}
