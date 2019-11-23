package com.liadk.android.pushit;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class UpdatePasswordFragment extends Fragment {
    private static final int REQUEST_LOGIN = 0;

    private static final String TAG = "UpdatePasswordFragment";

    private DatabaseManager mDatabaseManager;
    private FirebaseAuth mAuth;

    private EditText mCurrentPasswordEditText;
    private EditText mNewPasswordEditText;
    private EditText mConfirmPasswordEditText;
    private Button mUpdateButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        getActivity().setTitle(R.string.update_password);

        mDatabaseManager = DatabaseManager.get(getActivity());
        mAuth = FirebaseAuth.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_update_password, container, false);

        // Referencing Widgets
        mCurrentPasswordEditText = v.findViewById(R.id.currentPasswordEditText);
        mNewPasswordEditText = v.findViewById(R.id.newPasswordEditText);
        mConfirmPasswordEditText = v.findViewById(R.id.confirmPasswordEditText);
        mUpdateButton = v.findViewById(R.id.updatePasswordButton);

        configureView();

        return v;
    }

    private void configureView() {
        mUpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String currentPassword = mCurrentPasswordEditText.getText().toString();
                final String newPassword = mNewPasswordEditText.getText().toString();
                String confirmPassword = mConfirmPasswordEditText.getText().toString();

                if(!validate(newPassword, confirmPassword) || mAuth.getCurrentUser() == null) return;

                final ProgressDialog progressDialog = showProgressDialog(getString(R.string.update_password_progress_dialog));


                String email = (getActivity() != null) ? PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(SettingsFragment.KEY_EMAIL_PREFERENCE, null) : null;

                if(email == null) {
                    Toast.makeText(getActivity(), R.string.try_login, Toast.LENGTH_LONG).show();
                    progressDialog.dismiss();
                    return;
                }

                AuthCredential credentials = EmailAuthProvider.getCredential(email, currentPassword);

                mAuth.getCurrentUser().reauthenticate(credentials).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            mAuth.getCurrentUser().updatePassword(newPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    progressDialog.dismiss();

                                    if(task.isSuccessful()) {
                                        new EventsLogger(getActivity()).log("update_password_success");
                                        onUpdateSuccess();

                                        if(getActivity() != null)
                                            Toast.makeText(getActivity(), R.string.password_changed, Toast.LENGTH_SHORT).show();
                                    }

                                    else if(task.getException() != null) {
                                        onUpdateFailed(task.getException().getMessage());
                                    }
                                }
                            });
                        }

                        else if(task.getException() != null) {
                            progressDialog.dismiss();

                            if(task.getException() instanceof FirebaseAuthInvalidUserException)  // error caused by email issues, won't be solved unless user logs in again
                                Toast.makeText(getActivity(), R.string.try_login, Toast.LENGTH_LONG).show();
                            else
                                Toast.makeText(getActivity(), R.string.invalid_password, Toast.LENGTH_LONG).show();

                            onUpdateFailed(task.getException().getMessage());
                        }
                    }
                });
            }
        });
    }

    private void onUpdateSuccess() {
        Log.d(TAG, "updatePassword:success");
        NavUtils.navigateUpFromSameTask(getActivity());
    }

    private void onUpdateFailed(String errorMsg) {
        Log.d(TAG, "updatePassword:failure; " + errorMsg);
        new EventsLogger(getActivity()).log("update_password_failed", "error", errorMsg);
    }

    private ProgressDialog showProgressDialog(String string) {
        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(string);
        progressDialog.show();
        return progressDialog;
    }

    private boolean validate(String password, String confirmPassword) {
        boolean valid = true;

        if (password.isEmpty() || password.length() < 6 || password.length() > 16) {
            mNewPasswordEditText.setError(getString(R.string.enter_valid_password));
            mNewPasswordEditText.requestFocus();
            valid = false;
        }

        else {
            mNewPasswordEditText.setError(null);
        }


        if(!password.equals(confirmPassword)) {
            mConfirmPasswordEditText.setError(getString(R.string.passwords_dont_match));
            mConfirmPasswordEditText.requestFocus();
            valid = false;
        }

        else {
            mConfirmPasswordEditText.setError(null);
        }

        return valid;
    }
}
