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
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class CreateAccountFragment extends Fragment {
    private static final String TAG = "LoginFragment";

    private DatabaseManager mDatabaseManager;
    private FirebaseAuth mAuth;

    private EditText mEmailEditText;
    private EditText mPasswordEditText;
    private EditText mConfirmPasswordEditText;
    private Switch mStatusSwitch;
    private LinearLayout mPageNameLayout;
    private EditText mPageNameEditText;
    private Button mSignUpButton;
    private TextView mLoginTextView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        getActivity().setTitle(R.string.sign_up);

        mDatabaseManager = DatabaseManager.get(getActivity());
        mAuth = FirebaseAuth.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_create_account, container, false);

        // Referencing Widgets
        mEmailEditText = (EditText) v.findViewById(R.id.emailEditText);
        mPasswordEditText = (EditText) v.findViewById(R.id.passwordEditText);
        mConfirmPasswordEditText = (EditText) v.findViewById(R.id.confirmPasswordEditText);
        mStatusSwitch = (Switch) v.findViewById(R.id.accountStatusSwitch);
        mPageNameLayout = (LinearLayout) v.findViewById(R.id.pageNameLayout);
        mPageNameEditText = (EditText) v.findViewById(R.id.pageNameEditText);
        mSignUpButton = (Button) v.findViewById(R.id.signUpButton);
        mLoginTextView = (TextView) v.findViewById(R.id.loginTextView);

        configureView(v);

        return v;
    }

    private void configureView(View v) {

        mStatusSwitch.setText(mStatusSwitch.isChecked() ? R.string.status_creator : R.string.status_follower);
        mStatusSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                int accountStatusStringId = checked ? R.string.status_creator : R.string.status_follower;
                mStatusSwitch.setText(accountStatusStringId);

                int pageNameLayoutVisibility = checked ? View.VISIBLE : View.GONE;
                mPageNameLayout.setVisibility(pageNameLayoutVisibility);
            }
        });

        mLoginTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                getActivity().finish();
                startActivity(intent);
            }
        });

        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email = mEmailEditText.getText().toString();
                String password = mPasswordEditText.getText().toString();
                String confirmPassword = mConfirmPasswordEditText.getText().toString();
                final boolean status = mStatusSwitch.isChecked();
                final String pageName = mPageNameEditText.getText().toString();

                if(!validate(email, password, confirmPassword, status, pageName)) return;

                final ProgressDialog progressDialog = showProgressDialog(getString(R.string.sign_up_progress_dialog));

                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();

                        if (task.isSuccessful()) {
                            String userId = mAuth.getCurrentUser().getUid();
                            String pageId = null;

                            if(status) {
                                Page newPage = new Page(pageName);
                                pageId = newPage.getId().toString();

                                mDatabaseManager.pushPageToDB(newPage);
                            }

                            PushItUser user = new PushItUser(email, status, pageId);
                            mDatabaseManager.pushUserToDB(user, userId);

                            onSignUpSuccess();
                        }

                        else if(task.getException() != null) {
                            String errorMsg = task.getException().getMessage();
                            onSignUpFailed(errorMsg);
                        }
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
    }

    private boolean validate(String email, String password, String confirmPassword, boolean status, String pageName) {
        boolean valid = true;

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mEmailEditText.setError("Enter a valid email address"); // TODO Make String Resources
            mEmailEditText.requestFocus();
            valid = false;
        }

        else {
            mEmailEditText.setError(null);
        }


        if (password.isEmpty() || password.length() < 6 || password.length() > 16) {
            mPasswordEditText.setError("Enter a 6-characters password");
            mPasswordEditText.requestFocus();
            valid = false;
        }

        else {
            mPasswordEditText.setError(null);
        }


        if(!password.equals(confirmPassword)) {
            mConfirmPasswordEditText.setError("Passwords don't match");
            mConfirmPasswordEditText.requestFocus();
            valid = false;
        }

        else {
            mConfirmPasswordEditText.setError(null);
        }


        if(status && pageName.isEmpty()) {
            mPageNameEditText.setError("Enter page name");
            mPageNameEditText.requestFocus();
            valid = false;
        }

        else {
            mPageNameEditText.setError(null);
        }


        return valid;
    }

    private void onSignUpSuccess() {
        Log.d(TAG, "signUpWithEmail:success");
        Toast.makeText(getActivity(), R.string.account_created, Toast.LENGTH_SHORT).show();
        NavUtils.navigateUpTo(getActivity(), new Intent(getActivity(), HomeActivity.class)); // launches HomeActivity
    }

    private void onSignUpFailed(String errorMsg) {
        Log.d(TAG, "signUpWithEmail:failure; " + errorMsg);
        Toast.makeText(getActivity(), errorMsg, Toast.LENGTH_SHORT).show();
    }
}
