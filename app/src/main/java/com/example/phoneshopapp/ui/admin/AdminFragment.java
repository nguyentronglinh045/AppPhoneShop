package com.example.phoneshopapp.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.example.phoneshopapp.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

/**
 * Admin Fragment with tabs for Products and Orders management
 */
public class AdminFragment extends Fragment {
  private TabLayout tabLayout;
  private ViewPager2 viewPager;

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View root = inflater.inflate(R.layout.fragment_admin, container, false);

    tabLayout = root.findViewById(R.id.tabLayout);
    viewPager = root.findViewById(R.id.viewPager);

    // Setup ViewPager2 with adapter
    AdminPagerAdapter pagerAdapter = new AdminPagerAdapter(getActivity());
    viewPager.setAdapter(pagerAdapter);

    // Link TabLayout with ViewPager2
    new TabLayoutMediator(tabLayout, viewPager,
        (tab, position) -> {
          switch (position) {
            case 0:
              tab.setText("Products");
              break;
            case 1:
              tab.setText("Orders");
              break;
          }
        }).attach();

    return root;
  }

  /**
   * Adapter for ViewPager2 to manage Products and Orders tabs
   */
  private static class AdminPagerAdapter extends FragmentStateAdapter {
    public AdminPagerAdapter(FragmentActivity fragmentActivity) {
      super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
      switch (position) {
        case 0:
          return new ProductsTabFragment();
        case 1:
          return new OrdersTabFragment();
        default:
          return new ProductsTabFragment();
      }
    }

    @Override
    public int getItemCount() {
      return 2; // Products and Orders
    }
  }
}
