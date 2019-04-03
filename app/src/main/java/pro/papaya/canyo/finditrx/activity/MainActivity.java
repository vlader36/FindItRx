package pro.papaya.canyo.finditrx.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observer;
import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import pro.papaya.canyo.finditrx.R;
import pro.papaya.canyo.finditrx.adapter.MainPageAdapter;
import pro.papaya.canyo.finditrx.dialog.CameraUnavailableDialog;
import pro.papaya.canyo.finditrx.firebase.FireBaseItemsManager;
import pro.papaya.canyo.finditrx.fragment.ActionPageFragment;
import pro.papaya.canyo.finditrx.fragment.QuestsFragment;
import pro.papaya.canyo.finditrx.model.firebase.QuestModel;
import pro.papaya.canyo.finditrx.model.firebase.UserQuestsModel;
import pro.papaya.canyo.finditrx.model.view.MainViewPagerModel;
import pro.papaya.canyo.finditrx.viewmodel.MainViewModel;
import timber.log.Timber;

public class MainActivity extends BaseActivity implements
    ActionPageFragment.ActionPageCallback,
    QuestsFragment.QuestFragmentCallback {
  private final static int CAMERA_PERMISSION_CODE = 1000;

  @BindView(R.id.main_view_pager)
  ViewPager mainViewPager;
  @BindView(R.id.main_tab_navigator)
  TabLayout tabLayout;

  private MainViewModel mainViewModel;
  private MainPageAdapter adapter;
  private List<QuestModel> itemsCollection = new ArrayList<>();

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_main);
    mainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
    ButterKnife.bind(this);
    initViews();
    setListeners();
    subscribeToViewModel();
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      adapter.refreshActionFragment();
    } else {
      new CameraUnavailableDialog(this).show();
    }
  }

  @Override
  public void requestCameraPermissions() {
    ActivityCompat.requestPermissions(MainActivity.this,
        new String[]{Manifest.permission.CAMERA},
        CAMERA_PERMISSION_CODE);
  }

  @Override
  public boolean isCameraPermissionsGranted() {
    return ContextCompat.checkSelfPermission(
        MainActivity.this,
        Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED;
  }

  @Override
  public void snapshotTaken(List<QuestModel> takenSnapshotLabels) {
    mainViewModel.updateLabelsRemote(itemsCollection, takenSnapshotLabels);
  }

  private void subscribeToViewModel() {
    mainViewModel.getAllLabels()
        .subscribe(new Observer<List<QuestModel>>() {
          @Override
          public void onSubscribe(Disposable d) {
          }

          @Override
          public void onNext(List<QuestModel> questModels) {
            itemsCollection = questModels;
            logDebug("Items collection get: %s", questModels);
          }

          @Override
          public void onError(Throwable e) {
            showSnackBar(e.getLocalizedMessage());
            logError(e);
          }

          @Override
          public void onComplete() {
          }
        });
  }

  private void initViews() {
    adapter = new MainPageAdapter(
        getSupportFragmentManager(),
        this,
        this
    );
    mainViewPager.setAdapter(adapter);
    selectPage(MainViewPagerModel.CAMERA_PAGE.ordinal(), false);
    selectTab(MainViewPagerModel.CAMERA_PAGE.ordinal());
  }

  private void selectTab(int idx) {
    TabLayout.Tab tab = tabLayout.getTabAt(idx);
    if (tab != null) {
      tab.select();
    }
  }

  private void selectPage(int idx, boolean smoothScroll) {
    mainViewPager.setCurrentItem(idx, smoothScroll);
  }

  private void setListeners() {
    mainViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
      @Override
      public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
      }

      @Override
      public void onPageSelected(int position) {
        TabLayout.Tab tab = tabLayout.getTabAt(position);

        if (tab != null) {
          tab.select();
        }
      }

      @Override
      public void onPageScrollStateChanged(int state) {
      }
    });

    tabLayout.addOnTabSelectedListener(new TabLayout.BaseOnTabSelectedListener() {
      @Override
      public void onTabSelected(TabLayout.Tab tab) {
        selectPage(tab.getPosition(), true);
      }

      @Override
      public void onTabUnselected(TabLayout.Tab tab) {

      }

      @Override
      public void onTabReselected(TabLayout.Tab tab) {
      }
    });
  }

  @Override
  public void requestQuests(List<UserQuestsModel> userQuests, int questsCount) {
    mainViewModel.requestQuests(itemsCollection, userQuests, questsCount)
        .subscribe(new SingleObserver<Boolean>() {
          @Override
          public void onSubscribe(Disposable d) {

          }

          @Override
          public void onSuccess(Boolean aBoolean) {
            logDebug("Create task success: %s", aBoolean);
          }

          @Override
          public void onError(Throwable e) {
            showSnackBar(e.getLocalizedMessage());
            logError(e);
          }
        });
  }

  @Override
  public void requestRandomQuests(int questCount) {
    FireBaseItemsManager.requestRandomUserQuests(itemsCollection, questCount)
        .subscribe(new SingleObserver<Boolean>() {
          @Override
          public void onSubscribe(Disposable d) {

          }

          @Override
          public void onSuccess(Boolean aBoolean) {
            logDebug("Quests count requested %s times successfully", questCount);
          }

          @Override
          public void onError(Throwable e) {
            logError(e);
            showSnackBar(e.getLocalizedMessage());
          }
        });
  }
}
