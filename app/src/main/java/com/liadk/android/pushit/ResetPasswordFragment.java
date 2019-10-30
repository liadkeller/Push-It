package com.liadk.android.pushit;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class ResetPasswordFragment extends Fragment {
    private static final String TAG = "ResetPasswordFragment";

    private DatabaseManager mDatabaseManager;
    private FirebaseAuth mAuth;

    private EditText mEmailEditText;
    private Button mResetButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        getActivity().setTitle(R.string.reset_password);

        mDatabaseManager = DatabaseManager.get(getActivity());
        mAuth = FirebaseAuth.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_reset_password, container, false);

        // Referencing Widgets
        mEmailEditText = v.findViewById(R.id.emailEditText);
        mResetButton = v.findViewById(R.id.resetPasswordButton);

        configureView();

        return v;
    }

    private void configureView() {
        mResetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email = mEmailEditText.getText().toString();

                if(!validate(email)) return;

                final ProgressDialog progressDialog = showProgressDialog(getString(R.string.reset_password_progress_dialog));

                mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressDialog.dismiss();

                        if(task.isSuccessful()) {
                            new EventsLogger(getActivity()).log("reset_password_success", "email", email);
                            onResetSuccess();
                        }

                        else if(task.getException() != null) {
                            onResetFailed(task.getException(), email);

                        }
                    }
                });
            }
        });
    }

    private void onResetSuccess() {
        Log.d(TAG, "resetPassword:success");
        Toast.makeText(getActivity(), R.string.password_reset, Toast.LENGTH_LONG).show();
        NavUtils.navigateUpFromSameTask(getActivity());
    }

    private void onResetFailed(Exception exception, String email) {
        String errorMsg = exception.getMessage();

        if(exception instanceof FirebaseAuthInvalidUserException)
            Toast.makeText(getActivity(), R.string.no_email_address, Toast.LENGTH_LONG).show();
        else
            Toast.makeText(getActivity(), errorMsg, Toast.LENGTH_LONG).show();

        Log.d(TAG, "resetPassword:failure; " + errorMsg);
        new EventsLogger(getActivity()).log("reset_password_failed", "email", email, "error", errorMsg);
    }

    private ProgressDialog showProgressDialog(String string) {
        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(string);
        progressDialog.show();
        return progressDialog;
    }

    private boolean validate(String email) {
        boolean valid = true;

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mEmailEditText.setError(getString(R.string.enter_valid_email));
            mEmailEditText.requestFocus();
            valid = false;
        }

        else {
            mEmailEditText.setError(null);
        }

        return valid;
    }
}
