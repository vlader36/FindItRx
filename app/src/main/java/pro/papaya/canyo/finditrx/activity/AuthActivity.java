package pro.papaya.canyo.finditrx.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import androidx.lifecycle.ViewModelProviders;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import pro.papaya.canyo.finditrx.R;
import pro.papaya.canyo.finditrx.firebase.FireBaseDataBaseHelper;
import pro.papaya.canyo.finditrx.firebase.FireBaseLoginManger;
import pro.papaya.canyo.finditrx.model.firebase.FireBaseResponseModel;
import pro.papaya.canyo.finditrx.utils.BaseTextWatcher;
import pro.papaya.canyo.finditrx.utils.Constants;
import pro.papaya.canyo.finditrx.utils.StringUtils;
import pro.papaya.canyo.finditrx.viewmodel.AuthViewModel;

public class AuthActivity extends BaseActivity {
  @BindView(R.id.auth_et_email_layout)
  TextInputLayout etEmailLayout;
  @BindView(R.id.auth_et_email)
  TextInputEditText etEmail;

  @BindView(R.id.auth_et_password_layout)
  TextInputLayout etPasswordLayout;
  @BindView(R.id.auth_et_password)
  TextInputEditText etPassword;

  @BindView(R.id.auth_et_repeat_password_layout)
  TextInputLayout etRepeatPasswordLayout;
  @BindView(R.id.auth_et_repeat_password)
  TextInputEditText etRepeatPassword;

  @BindView(R.id.auth_bth_login)
  Button btnLogin;

  @BindView(R.id.auth_bth_register)
  Button btnRegister;

  private AuthViewModel authViewModel;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_auth);
    ButterKnife.bind(this);
    setListeners();

    authViewModel = ViewModelProviders.of(this).get(AuthViewModel.class);
    renewAuth();
  }

  private void renewAuth() {
    authViewModel.renewAuth()
        .subscribe(new SingleObserver<Boolean>() {
          @Override
          public void onSubscribe(Disposable d) {
            logDebug("Renew auth started");
          }

          @Override
          public void onSuccess(Boolean aBoolean) {
            if (!aBoolean){
              logDebug("Can't renew session");
              return;
            }

            logDebug("Auth renewed successfully");
            navigateToMainActivity();
          }

          @Override
          public void onError(Throwable e) {
            logDebug("Cant renew auth with error: %s", e);
            showSnackBar(e.getLocalizedMessage());
          }
        });
  }

  private void navigateToMainActivity() {
    Intent navigationIntent = new Intent(AuthActivity.this, MainActivity.class);
    startActivity(navigationIntent);
    finish();
  }

  private void setListeners() {
    btnLogin.setOnClickListener(v -> {
      if (isActivityInRegistrationMode()) {
        setActivityMode(false);
      } else {
        setLoading(true);
        signIn();
      }
    });

    btnRegister.setOnClickListener(v -> {
      if (!isActivityInRegistrationMode()) {
        setActivityMode(true);
      } else {
        setLoading(true);
        signUp();
      }
    });

    etPassword.addTextChangedListener(new BaseTextWatcher() {
      @Override
      protected void onTextContentChanged(String s) {
        if (!s.isEmpty() && s.length() < Constants.MIN_PASSWORD_LENGTH) {
          etPasswordLayout.setError(getString(R.string.auth_password_length_error));
          setError(true);
        } else {
          etPasswordLayout.setErrorEnabled(false);
          setError(false);
        }
      }
    });

    etRepeatPassword.addTextChangedListener(new BaseTextWatcher() {
      @Override
      protected void onTextContentChanged(String s) {
        String password = etPassword.getText() != null
            ? etPassword.getText().toString()
            : Constants.EMPTY_STRING;

        if (!s.isEmpty() && !s.equals(password)) {
          etRepeatPasswordLayout.setError(getString(R.string.auth_repeat_password_error));
          setError(true);
        } else {
          etRepeatPasswordLayout.setErrorEnabled(false);
          setError(false);
        }
      }
    });

    etEmail.addTextChangedListener(new BaseTextWatcher() {
      @Override
      protected void onTextContentChanged(String s) {
        if (isEmailValid(getEmailTextString())) {
          etEmailLayout.setError(getString(R.string.auth_email_error));
          setError(true);
        } else {
          etEmailLayout.setErrorEnabled(false);
          setError(false);
        }
      }
    });
  }

  private void setError(boolean isErrorExist) {
    if (isActivityInRegistrationMode()) {
      setRegisterButtonState(!isErrorExist);
    } else {
      setAuthButtonState(!isErrorExist);
    }
  }

  private String getEmailTextString() {
    return etEmail.getText() == null
        ? Constants.EMPTY_STRING
        : etEmail.getText().toString();
  }

  private String getPasswordTextString() {
    return etPassword.getText() == null
        ? Constants.EMPTY_STRING
        : etPassword.getText().toString();
  }

  private String getRepPasswordTextString() {
    return etRepeatPassword.getText() == null
        ? Constants.EMPTY_STRING
        : etRepeatPassword.getText().toString();
  }

  private boolean isEmailValid(String email) {
    return !email.isEmpty() && !StringUtils.isEmailValid(email);
  }

  private boolean isRegistrationDataValid() {
    String email = getEmailTextString();
    String password = getPasswordTextString();
    String repeatPassword = getRepPasswordTextString();
    return isEmailValid(email)
        && !password.isEmpty()
        && password.length() >= Constants.MIN_PASSWORD_LENGTH
        && repeatPassword.equals(password);
  }

  private boolean isAuthDataValid() {
    String email = getEmailTextString();
    String password = getPasswordTextString();
    return isEmailValid(email)
        && !password.isEmpty()
        && password.length() >= Constants.MIN_PASSWORD_LENGTH;
  }

  private boolean isActivityInRegistrationMode() {
    return etRepeatPasswordLayout.getVisibility() == View.VISIBLE;
  }

  private void setActivityMode(boolean toRegistration) {
    String newLoginButtonText = getString(
        toRegistration ? R.string.auth_to_login : R.string.auth_login);
    String newRegisterButtonText = getString(
        toRegistration ? R.string.auth_register : R.string.auth_to_registration);
    int newRepeatPasswordLabelVisibility = toRegistration ? View.VISIBLE : View.GONE;

    btnRegister.setText(newRegisterButtonText);
    setRegisterButtonState(!toRegistration || isRegistrationDataValid());
    btnLogin.setText(newLoginButtonText);
    setAuthButtonState(toRegistration || isAuthDataValid());
    etRepeatPasswordLayout.setVisibility(newRepeatPasswordLabelVisibility);
  }

  private void setRegisterButtonState(boolean isEnabled) {
    btnRegister.setEnabled(isEnabled);
  }

  private void setAuthButtonState(boolean isEnabled) {
    btnLogin.setEnabled(isEnabled);
  }

  private void signIn() {
    authViewModel.signIn(getEmailTextString(), getPasswordTextString())
        .subscribe(new SingleObserver<FireBaseResponseModel>() {
          @Override
          public void onSubscribe(Disposable d) {
            logDebug("SignIn request started");
          }

          @Override
          public void onSuccess(FireBaseResponseModel fireBaseResponseModel) {
            setLoading(false);
            navigateToMainActivity();
            logDebug("Login success");
          }

          @Override
          public void onError(Throwable e) {
            setLoading(false);
            showSnackBar(e.getLocalizedMessage());
            logDebug("SignIn failed with error: %s", e.getMessage());
          }
        });
  }

  private void signUp() {
    authViewModel.signUp(getEmailTextString(), getPasswordTextString())
        .subscribe(new SingleObserver<FireBaseResponseModel>() {
          @Override
          public void onSubscribe(Disposable d) {
            logDebug("SignUp request started");
          }

          @Override
          public void onSuccess(FireBaseResponseModel fireBaseResponseModel) {
            createUserWrite();
          }

          @Override
          public void onError(Throwable e) {
            setLoading(false);
            showSnackBar(e.getLocalizedMessage());
            logDebug("Registration failed with message: %s", e.getMessage());
          }
        });
  }

  private void createUserWrite(){
    FireBaseDataBaseHelper.createUserWrite(
        FireBaseLoginManger.getInstance().getUserEmail(),
        FireBaseLoginManger.getInstance().getUserId()
    ).subscribe(new SingleObserver<Boolean>() {
      @Override
      public void onSubscribe(Disposable d) {

      }

      @Override
      public void onSuccess(Boolean aBoolean) {
        setLoading(false);
        navigateToMainActivity();
        logDebug("SignUp success");
      }

      @Override
      public void onError(Throwable e) {
        showSnackBar(e.getLocalizedMessage());
        logError(e);
      }
    });
  }
}
