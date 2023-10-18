package com.example.cloudsync;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.cloudsync.model.MainViewModel;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    MainViewModel viewModel = new ViewModelProvider(this).get(MainViewModel.class);
    viewModel.getMessage().observe(this, message -> {
      if (message != null) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        viewModel.onMessageDisplayed();
      }
    });
  }
}