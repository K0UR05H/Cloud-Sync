package com.example.cloudsync;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.cloudsync.model.MainViewModel;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputLayout;

public class HomeFragment extends Fragment {

  private MainViewModel viewModel;
  private LinearProgressIndicator progressIndicator;
  private TextInputLayout firstNameField;
  private TextInputLayout lastNameField;
  private TextInputLayout phoneField;
  private Button addButton;
  private Button backupButton;
  private Button restoreButton;

  public HomeFragment() {
    // Required empty public constructor
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_home, container, false);

    progressIndicator = rootView.findViewById(R.id.progressIndicator);
    firstNameField = rootView.findViewById(R.id.firstNameTextField);
    lastNameField = rootView.findViewById(R.id.lastNameTextField);
    phoneField = rootView.findViewById(R.id.phoneTextField);
    addButton = rootView.findViewById(R.id.addButton);
    backupButton = rootView.findViewById(R.id.backupButton);
    restoreButton = rootView.findViewById(R.id.restoreButton);

    addButton.setOnClickListener(view -> saveForm());
    backupButton.setOnClickListener(view -> viewModel.backup());
    restoreButton.setOnClickListener(view -> viewModel.restore());

    viewModel.getIsLoading().observe(getViewLifecycleOwner(), this::showProgress);

    return rootView;
  }

  private void saveForm() {
    boolean hasErrors = false;

    String firstName = firstNameField.getEditText().getText().toString();
    String lastName = lastNameField.getEditText().getText().toString();
    String phone = phoneField.getEditText().getText().toString();

    if (firstName.isEmpty()) {
      firstNameField.setError(getString(R.string.field_required_error));
      hasErrors = true;
    } else {
      firstNameField.setError(null);
    }

    if (lastName.isEmpty()) {
      lastNameField.setError(getString(R.string.field_required_error));
      hasErrors = true;
    } else {
      lastNameField.setError(null);
    }

    if (phone.isEmpty()) {
      phoneField.setError(getString(R.string.field_required_error));
      hasErrors = true;
    } else {
      phoneField.setError(null);
    }

    if (!hasErrors) {
      viewModel.addUser(firstName, lastName, phone);
      clearForm();
    }
  }

  private void clearForm() {
    firstNameField.getEditText().setText(null);
    lastNameField.getEditText().setText(null);
    phoneField.getEditText().setText(null);
  }

  private void showProgress(boolean isProgressing) {
    progressIndicator.setVisibility(isProgressing ? View.VISIBLE : View.GONE);
    firstNameField.setEnabled(!isProgressing);
    lastNameField.setEnabled(!isProgressing);
    phoneField.setEnabled(!isProgressing);
    addButton.setEnabled(!isProgressing);
    backupButton.setEnabled(!isProgressing);
    restoreButton.setEnabled(!isProgressing);
  }
}