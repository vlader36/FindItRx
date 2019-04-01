package pro.papaya.canyo.finditrx.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.Nullable;
import pro.papaya.canyo.finditrx.R;
import timber.log.Timber;

public class FabItem extends LinearLayout {
  public interface FabItemCallback {
    void onFabClick(FabItem item);
  }

  private View root;
  private FloatingActionButton fab;
  private TextView title;
  private boolean isSwitcher;
  private FabItemCallback callback;

  public FabItem(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init(attrs);
  }

  public FabItem(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(attrs);
  }

  public boolean isSwitcher() {
    return isSwitcher;
  }

  public void setHeaderVisibility(boolean isVisible) {
    title.setVisibility(isVisible ? View.VISIBLE : View.GONE);
  }

  public void setItemVisibility(boolean isVisible) {
    root.setVisibility(isVisible ? View.VISIBLE : View.GONE);
  }

  public void setCallback(FabItemCallback callback) {
    this.callback = callback;
  }

  private void init(@Nullable AttributeSet attributeSet) {
    root = LayoutInflater.from(getContext()).inflate(R.layout.view_floating_item, this);

    fab = root.findViewById(R.id.floating_item_btn);
    title = root.findViewById(R.id.floating_item_title);

    if (attributeSet != null) {
      unpackAttributes(attributeSet);
    }

    fab.setOnClickListener(view -> {
      if (callback != null) {
        callback.onFabClick(this);
      }
    });
    title.setOnClickListener(view -> {
      if (callback != null) {
        callback.onFabClick(this);
      }
    });
  }

  private void unpackAttributes(AttributeSet attributeSet) {
    TypedArray typedArray = getContext().obtainStyledAttributes(attributeSet, R.styleable.FabItem);
    fab.setImageResource(typedArray.getResourceId(
        R.styleable.FabItem_android_src,
        R.drawable.ic_settings)
    );
    title.setText(typedArray.getText(R.styleable.FabItem_android_text));
    isSwitcher = typedArray.getBoolean(R.styleable.FabItem_switcher, false);
    typedArray.recycle();
  }
}
