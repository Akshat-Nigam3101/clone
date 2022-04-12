package com.v.weasone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.v.weasone.Login.Login;
import com.v.weasone.Login.Register;
import com.v.weasone.helper.TabsAccessorAdapter;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    TabLayout tabLayout;
    TabsAccessorAdapter tabsAccessorAdapter;
    ViewPager viewPager;
    Toolbar toolbar;
    FirebaseAuth auth;
    DatabaseReference dbref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        dbref = FirebaseDatabase.getInstance().getReference();


        tabLayout = findViewById(R.id.mainAppTabs);
        viewPager = findViewById(R.id.main_tabs_viewpager);
        toolbar = findViewById(R.id.main_app_toolbar);

        tabsAccessorAdapter = new TabsAccessorAdapter(getSupportFragmentManager());
        viewPager.setAdapter(tabsAccessorAdapter);
        setSupportActionBar(toolbar);

        tabLayout.setupWithViewPager(viewPager);
        Objects.requireNonNull(getSupportActionBar()).setTitle("WeAsOne");
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = auth.getCurrentUser();
        if(currentUser == null){
            auth.signOut();
            SendUserToLoginActivity();
        }
    }

    private void SendUserToLoginActivity() {
        Intent i = new Intent(MainActivity.this, Login.class);
        startActivity(i);
        MainActivity.this.finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId() == R.id.signoutMenuOption){
            auth.signOut();
            SendUserToLoginActivity();
        }
        else if(item.getItemId() == R.id.settingsMenuOption){
            SendUserToSettingsActivity();
        }
        return true;
    }

    private void SendUserToSettingsActivity() {
        Intent i = new Intent(MainActivity.this, UpdateProfile.class);
        startActivity(i);
    }
}