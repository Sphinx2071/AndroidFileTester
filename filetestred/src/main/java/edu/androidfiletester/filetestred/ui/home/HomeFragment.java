package edu.androidfiletester.filetestred.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import edu.androidfiletester.filetestred.R;
import edu.androidfiletester.filetestred.databinding.FragmentHomeBinding;
import edu.androidfiletester.sharedfileopslib.appspecific.PrivateFileNavigator;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textHome;
        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        return root;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        binding.btnAppSpecificStorage.setOnClickListener(v ->{
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_navigation_home_to_appSpecificFragment);
        });

        binding.btnSafStorage.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_navigation_home_to_safFragment);
        });

        binding.btnDbStorage.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "DB not implemented yet", Toast.LENGTH_LONG).show();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}