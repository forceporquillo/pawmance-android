package dev.apes.pawmance.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewbinding.ViewBinding;

public abstract class BaseFragment<T extends ViewBinding> extends Fragment {

  private final int layoutId;

  public BaseFragment(@LayoutRes int layoutId) {
    this.layoutId = layoutId;
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    return inflater.inflate(layoutId, container, false);
  }

  protected void navigate(@IdRes final int destinationId) {
    NavHostFragment.findNavController(this)
        .navigate(destinationId);
  }
}