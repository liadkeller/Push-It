package com.liadk.android.pushit;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginFragment extends Fragment {

    private static final String TAG = "LoginFragment";

    private FirebaseAuth mAuth;

    private EditText mEmailEditText;
    private EditText mPasswordEditText;
    private Button mSignInButton;
    private TextView mSignUpTextView;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        getActivity().setTitle(R.string.login);

        mAuth = FirebaseAuth.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_login, container, false);

        // Referencing Widgets
        mEmailEditText = (EditText) v.findViewById(R.id.emailEditText);
        mPasswordEditText = (EditText) v.findViewById(R.id.passwordEditText);
        mSignInButton = (Button) v.findViewById(R.id.signUpButton);
        mSignUpTextView = (TextView) v.findViewById(R.id.loginTextView);

        configureView(v);

        return v;
    }

    private void configureView(View v) {
        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = mEmailEditText.getText().toString();
                String password = mPasswordEditText.getText().toString();

                if(!validate(email, password)) return;

                final ProgressDialog progressDialog = showProgressDialog(getString(R.string.login_progress_dialog));

                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();

                        if (task.isSuccessful()) onLoginSuccess();
                        else onLoginFailed();
                    }
                });
            }

            private ProgressDialog showProgressDialog(String string) {
                final ProgressDialog progressDialog = new ProgressDialog(getActivity());
                progressDialog.setIndeterminate(true);
                progressDialog.setMessage(string);
                progressDialog.show();
                return progressDialog;
            }
        });

        mSignUpTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CreateAccountActivity.class);
                getActivity().finish();
                startActivity(intent);
            }
        });
    }

    private boolean validate(String email, String password) {
        boolean valid = true;

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mEmailEditText.setError(getString(R.string.enter_valid_email));
            mEmailEditText.requestFocus();
            valid = false;
        } else {
            mEmailEditText.setError(null);
        }

        if (password.isEmpty() || password.length() < 6 || password.length() > 16) {
            mPasswordEditText.setError(getString(R.string.enter_valid_password));
            mPasswordEditText.requestFocus();
            valid = false;
        } else {
            mPasswordEditText.setError(null);
        }

        return valid;
    }


    private void onLoginSuccess() {
        Log.d(TAG, "signInWithEmail:success");
        Toast.makeText(getActivity(), R.string.logged_in, Toast.LENGTH_SHORT).show();
        NavUtils.navigateUpTo(getActivity(), new Intent(getActivity(), HomeActivity.class)); // launches HomeActivity
    }

    private void onLoginFailed() {
        Log.d(TAG, "signInWithEmail:failure");
        Toast.makeText(getActivity(), R.string.wrong_credentials,
                Toast.LENGTH_SHORT).show();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getActivity().finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
