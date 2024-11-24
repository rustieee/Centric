package com.finals.centric;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.finals.centric.databinding.ActivityMain2Binding;

import java.util.HashMap;
import java.util.Map;
import android.view.View;


public class MainActivity2 extends AppCompatActivity {

    private ActivityMain2Binding binding;
    private HashMap<Integer, Fragment> fragmentMapping = new HashMap<>();
    private int currentMenuItemId = R.id.home;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMain2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        updateBackStackListener();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeFragments();
        replaceFragment(fragmentMapping.get(currentMenuItemId)); // Start with homeFragment fragment

        binding.navbar.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (currentMenuItemId != itemId) { // Only replace if it's a different item
                replaceFragment(fragmentMapping.get(itemId));
                setActiveMenuItem(itemId); // Update the active menu item
            }
            return true;
        });

        // Handle back button presses
        OnBackPressedDispatcher onBackPressedDispatcher = getOnBackPressedDispatcher();
        onBackPressedDispatcher.addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStack(); // Pop fragment from back stack
                } else {
                    finish(); // Exit the activity when there are no fragments left
                }
            }
        });

        binding.profilebase.setOnClickListener(v -> {
            setButtonAnimation(v, () -> {
                int profileMenuId = R.id.profile;
                setActiveMenuItem(profileMenuId); // Set the active menu item to profile
                replaceFragment(fragmentMapping.get(profileMenuId)); // Switch to profileFragment
            });
        });


    }

    private void updateBackStackListener() {
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            int backStackCount = getSupportFragmentManager().getBackStackEntryCount();
            if (backStackCount > 0) {
                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.mainframe);
                if (currentFragment != null && currentFragment instanceof bookinginfoFragment) {
                    setActiveMenuItem(R.id.booking);
                } else if (currentFragment != null && currentFragment instanceof homeFragment) {
                    setActiveMenuItem(R.id.home);
                }
            } else {
                // Set the homeFragment fragment when no fragments are in the back stack
                replaceFragment(fragmentMapping.get(R.id.home));
                setActiveMenuItem(R.id.home);
                finish(); // Exit the activity when there are no fragments left
            }
        });
    }

    private void initializeFragments() {
        fragmentMapping.put(R.id.home, new homeFragment());
        fragmentMapping.put(R.id.booking, new bookingFragment());
        fragmentMapping.put(R.id.payment, new paymentFragment());
        fragmentMapping.put(R.id.profile, new profileFragment());
    }

    private void replaceFragment(Fragment fragment) {
        if (fragment instanceof bookinginfoFragment || fragment instanceof bookingFragment || fragment instanceof bookingformFragment) {
            setActiveMenuItem(R.id.booking); // Set active menu to booking
        } else if (fragment instanceof paymentFragment || fragment instanceof paymentBillFragment || fragment instanceof paymentGcashFragment || fragment instanceof paymentNoticeFragment) {
            setActiveMenuItem(R.id.payment); // Set active menu to payment
        } else if (fragment instanceof profileFragment || fragment instanceof profileEditFragment) {
            setActiveMenuItem(R.id.profile); // Set active menu to profile
        } else {
            setActiveMenuItem(R.id.home); // Default to home for other fragments
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.mainframe, fragment)
                .addToBackStack(null)
                .commit();
    }

    public void setActiveMenuItem(int menuItemId) {
        currentMenuItemId = menuItemId;
        MenuItem activeItem = binding.navbar.getMenu().findItem(currentMenuItemId);

        // Check if the active menu is profile
        if (menuItemId == R.id.profile) {
            binding.profilebase.setVisibility(View.GONE); // Hide profilebase
        } else {
            binding.profilebase.setVisibility(View.VISIBLE); // Show profilebase
        }

        if (activeItem != null) {
            activeItem.setChecked(true);
        }
    }

    private void setButtonAnimation(View button, Runnable onClickAction) {
        button.animate()
                .scaleX(0f)  // Shrink to 0
                .scaleY(0f)
                .setDuration(100)  // Shrink duration
                .withEndAction(() -> {
                    // Perform the fragment change action after shrinking is done
                    onClickAction.run();

                    // Scale the button back to 1 after the fragment change
                    button.animate()
                            .scaleX(1f)  // Scale back to normal
                            .scaleY(1f)
                            .setDuration(100)  // Duration for scaling back
                            .start();
                })
                .start();
    }

}







