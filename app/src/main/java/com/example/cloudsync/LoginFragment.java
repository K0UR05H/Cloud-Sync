package com.example.cloudsync;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.activity.result.ActivityResultRegistry;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.example.cloudsync.authorization.AuthState;
import com.example.cloudsync.authorization.GoogleAuthorizationClient;
import com.example.cloudsync.model.MainViewModel;

public class LoginFragment extends Fragment {

  private MainViewModel viewModel;
  private GoogleAuthorizationClient authClient;
  private Button signInButton;

  public LoginFragment() {
    // Required empty public constructor
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Context applicationContext = requireActivity().getApplicationContext();
    ActivityResultRegistry resultRegistry = requireActivity().getActivityResultRegistry();
    authClient = new GoogleAuthorizationClient(applicationContext, resultRegistry);

    getLifecycle().addObserver(authClient);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_login, container, false);

    signInButton = rootView.findViewById(R.id.sign_in_button);

    return rootView;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

    authClient.getAuthState().observe(getViewLifecycleOwner(), authState -> {
      CharSequence message = "Authorization Status: " + authState.name();
      Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show();

      signInButton.setEnabled(authState != AuthState.AUTHORIZING);

      if (authState == AuthState.AUTHORIZED) {
        viewModel.onAuthResult(authClient.getResult());

        NavController controller = Navigation.findNavController(view);
        controller.navigate(LoginFragmentDirections.actionLoginFragmentToHomeFragment());
      }
    });

    signInButton.setOnClickListener(v -> authClient.authorize());
  }
}