package pro.papaya.canyo.finditrx.fragment;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.fotoapparat.Fotoapparat;
import io.fotoapparat.configuration.UpdateConfiguration;
import io.fotoapparat.parameter.Flash;
import io.fotoapparat.parameter.ScaleType;
import io.fotoapparat.selector.FlashSelectorsKt;
import io.fotoapparat.selector.FocusModeSelectorsKt;
import io.fotoapparat.selector.LensPositionSelectorsKt;
import io.fotoapparat.selector.ResolutionSelectorsKt;
import io.fotoapparat.selector.SelectorsKt;
import io.fotoapparat.view.CameraView;
import kotlin.jvm.functions.Function1;
import pro.papaya.canyo.finditrx.R;
import pro.papaya.canyo.finditrx.listener.ExtendedEventListener;
import pro.papaya.canyo.finditrx.model.firebase.QuestModel;
import pro.papaya.canyo.finditrx.model.firebase.SettingsModel;
import pro.papaya.canyo.finditrx.model.view.FabMenuAction;
import pro.papaya.canyo.finditrx.view.FabItem;
import pro.papaya.canyo.finditrx.view.FabMenu;
import pro.papaya.canyo.finditrx.viewmodel.ActionViewModel;

public class ActionPageFragment extends BaseFragment implements FabMenu.FabMenuCallback {
  public static ActionPageFragment INSTANCE = null;

  public interface ActionPageCallback {
    void requestCameraPermissions();

    boolean isCameraPermissionsGranted();

    void snapshotTaken(List<QuestModel> takenSnapshotLabels);
  }

  @BindView(R.id.action_camera_view)
  CameraView cameraView;
  @BindView(R.id.action_menu)
  FabMenu menu;

  //TODO remove after testing
  @BindView(R.id.scan_result)
  LinearLayout scanResultContainer;
  @BindView(R.id.snapshot_btn)
  Button btnSnapshot;

  private Fotoapparat fotoapparat;
  private ActionPageCallback callback;
  private ActionViewModel actionViewModel;
  private SettingsModel settingsModel = SettingsModel.getStabSettings();

  public static ActionPageFragment getInstance(ActionPageCallback callback) {
    if (INSTANCE == null) {
      ActionPageFragment fragment = new ActionPageFragment();
      fragment.setCallback(callback);
      INSTANCE = fragment;
    } else if (callback != INSTANCE.callback) {
      INSTANCE.setCallback(callback);
    }

    return INSTANCE;
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater,
                           @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {

    View view = inflater.inflate(R.layout.main_fragment_action, container, false);
    return view;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    ButterKnife.bind(this, view);
    actionViewModel = ViewModelProviders.of(this).get(ActionViewModel.class);
    menu.setCallback(this);

    initFotoapparat();
    subscribeToViewModel();
    setListeners();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
  }

  @Override
  public void onPause() {
    super.onPause();
    if (callback.isCameraPermissionsGranted()) {
      fotoapparat.stop();
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    if (callback.isCameraPermissionsGranted()) {
      fotoapparat.start();
    }
  }

  @Override
  public void onFabItemClick(FabItem item) {
    if (item.getAction() == FabMenuAction.ACTION_AUTO_FLASH) {
      setFlashState(item.isEnabled(), true);
    }
  }

  public void setCallback(ActionPageCallback callback) {
    this.callback = callback;
  }

  public void refresh() {
    initFotoapparat();
  }

  private void subscribeToViewModel() {
    getSettings();
  }

  @SuppressLint("SetTextI18n")
  private void setListeners() {
    btnSnapshot.setOnClickListener(v -> {
      setLoading(true);
      fotoapparat.takePicture().toPendingResult().whenAvailable(photo -> {
        Bitmap bitmap = BitmapFactory.decodeByteArray(photo.encodedImage, 0, photo.encodedImage.length);

        actionViewModel.postImageTask(bitmap, photo.rotationDegrees)
            .addOnSuccessListener(firebaseVisionImageLabels -> {
              scanResultContainer.removeAllViews();

              for (FirebaseVisionImageLabel label : firebaseVisionImageLabels) {
                TextView labelContainer = new TextView(getContext());
                labelContainer.setText(label.getText() + " " + label.getConfidence());
                scanResultContainer.addView(labelContainer);
              }

              if (callback != null) {
                callback.snapshotTaken(QuestModel.fromCollection(firebaseVisionImageLabels));
              }

              setLoading(false);
            })
            .addOnFailureListener(e -> {
              setLoading(false);
              showSnackBar(e.getLocalizedMessage());
              logError(e);
            });

        return null;
      });
    });
  }

  private void getSettings() {
    actionViewModel.getSettingsReference()
        .addSnapshotListener(new ExtendedEventListener<DocumentSnapshot>() {
          @Override
          public void onSuccess(DocumentSnapshot documentSnapshot) {
            SettingsModel settingsModelRemote = documentSnapshot.toObject(SettingsModel.class);
            if (settingsModelRemote != null) {
              applySettings(settingsModelRemote);
              logDebug("Settings got: %s", settingsModel.toString());
            } else {
              actionViewModel.setStableSettings();
              applySettings(SettingsModel.getStabSettings());
              logDebug("Settings in null");
            }
          }

          @Override
          public void onError(FirebaseFirestoreException e) {
            showSnackBar(e.getLocalizedMessage());
            logError(e);
          }
        });
  }

  private void applySettings(SettingsModel settingsModel) {
    this.settingsModel = settingsModel;

    setFlashState(settingsModel.isFlashEnabled(), false);

    menu.applySettings(settingsModel);
  }

  private void setFlashState(boolean isEnabled, boolean updateRemote) {
    Function1<Iterable<? extends Flash>, Flash> newFlashState = isEnabled
        ? FlashSelectorsKt.autoFlash()
        : FlashSelectorsKt.off();

    UpdateConfiguration.builder()
        .flash(newFlashState)
        .build();

    if (updateRemote) {
      actionViewModel.setFlashState(settingsModel, isEnabled)
          .addOnSuccessListener(aVoid -> {
            logDebug("Settings flash updated");
          })
          .addOnFailureListener(e -> {
            showSnackBar(e.getLocalizedMessage());
            logDebug("Settings flash update error %s", e.getLocalizedMessage());
          });
    }
  }

  private void initFotoapparat() {
    if (getContext() != null && callback.isCameraPermissionsGranted()) {
      fotoapparat = Fotoapparat
          .with(getContext())
          .into(cameraView)
          .previewScaleType(ScaleType.CenterCrop)
          .photoResolution(ResolutionSelectorsKt.highestResolution())
          .lensPosition(LensPositionSelectorsKt.back())
          .focusMode(SelectorsKt.firstAvailable(
              FocusModeSelectorsKt.continuousFocusPicture(),
              FocusModeSelectorsKt.autoFocus(),
              FocusModeSelectorsKt.fixed()
          ))
          .build();

      fotoapparat.start();
    } else {
      callback.requestCameraPermissions();
    }
  }
}
