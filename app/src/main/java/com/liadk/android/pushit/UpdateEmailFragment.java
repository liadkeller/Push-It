package com.liadk.android.pushit;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v7.preference.PreferenceManager;
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
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class UpdateEmailFragment extends Fragment {
    private static final int REQUEST_LOGIN = 0;

    private static final String TAG = "UpdateEmailFragment";

    private DatabaseManager mDatabaseManager;
    private FirebaseAuth mAuth;

    private EditText mCurrentEmailEditText;
    private EditText mNewEmailEditText;
    private Button mUpdateButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        getActivity().setTitle(R.string.update_email);

        mDatabaseManager = DatabaseManager.get(getActivity());
        mAuth = FirebaseAuth.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_update_email, container, false);

        // Referencing Widgets
        mCurrentEmailEditText = v.findViewById(R.id.currentEmailEditText);
        mNewEmailEditText = v.findViewById(R.id.newEmailEditText);
        mUpdateButton = v.findViewById(R.id.updateEmailButton);

        configureView();

        return v;
    }

    private void configureView() {
        final String currentEmail = (getActivity() != null) ? PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(AccountSettingsFragment.KEY_EMAIL_PREFERENCE, "") : null;
        mCurrentEmailEditText.setText(currentEmail);

        mUpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String newEmail = mNewEmailEditText.getText().toString();

                if(!validate(newEmail) || mAuth.getCurrentUser() == null) return;

                final ProgressDialog progressDialog = showProgressDialog(getString(R.string.update_email_progress_dialog));

                mAuth.getCurrentUser().updateEmail(newEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mDatabaseManager.addUsersSingleEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(mAuth.getCurrentUser() == null) return;

                                    String userId = mAuth.getCurrentUser().getUid();
                                    PushItUser user = dataSnapshot.child(userId).getValue(PushItUser.class);

                                    if(user == null) return;

                                    final String prevEmail = user.getEmail();
                                    user.setEmail(newEmail);

                                    PreferenceManager.getDefaultSharedPreferences(getActivity())
                                            .edit()
                                            .putString(AccountSettingsFragment.KEY_EMAIL_PREFERENCE, newEmail)
                                            .commit();

                                    mDatabaseManager.setUserEmail(user, userId, new OnCompleteListener() {
                                        @Override
                                        public void onComplete(@NonNull Task task) {
                                            progressDialog.dismiss();

                                            if(getActivity() != null)
                                                Toast.makeText(getActivity(), getString(R.string.email_changed, newEmail), Toast.LENGTH_LONG).show();

                                            new EventsLogger(getActivity()).log("update_email_success", "new_email", newEmail, "prev_email", prevEmail);
                                            onUpdateSuccess();
                                        }
                                    });
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {}
                            });
                        }

                        else
                            progressDialog.dismiss();

                        if(task.getException() != null) {
                            if(task.getException() instanceof FirebaseAuthRecentLoginRequiredException) {
                                Intent intent = new Intent(getActivity(), LoginActivity.class);
                                startActivityForResult(intent, REQUEST_LOGIN);
                                Toast.makeText(getActivity(), R.string.login_to_continue, Toast.LENGTH_LONG).show();
                            }

                            else {
                                String errorMsg = task.getException().getMessage();
                                new EventsLogger(getActivity()).log("update_email_failed", "new_email", newEmail, "current_email", currentEmail, "error", errorMsg);
                                onUpdateFailed(errorMsg);
                            }
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == Activity.RESULT_OK) {
            if(requestCode == REQUEST_LOGIN) {
                String previousUserId = data.getExtras().getString(LoginFragment.EXTRA_PREV_UID);
                String currentUserId = data.getExtras().getString(LoginFragment.EXTRA_CUR_UID);

                if(previousUserId == null || currentUserId == null) return;

                if(!previousUserId.equals(currentUserId)) {

                    if(getActivity() != null) {
                        String email = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(AccountSettingsFragment.KEY_EMAIL_PREFERENCE, "");
                        Toast.makeText(getActivity(), getResources().getString(R.string.email_not_changed, email), Toast.LENGTH_LONG).show();
                    }
                    NavUtils.navigateUpFromSameTask(getActivity());
                }
            }
        }
    }

    private void onUpdateSuccess() {
        Log.d(TAG, "updateEmail:success");
        NavUtils.navigateUpFromSameTask(getActivity());
    }

    private void onUpdateFailed(String errorMsg) {
        Log.d(TAG, "updateEmail:failure; " + errorMsg);
        Toast.makeText(getActivity(), errorMsg, Toast.LENGTH_LONG).show();
    }

    private ProgressDialog showProgressDialog(String string) {
        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(string);
        progressDialog.show();
        return progressDialog;
    }

    private boolean validate(String email) {
        boolean valid = !(email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches());

        if (!valid) {
            mNewEmailEditText.setError(getString(R.string.enter_valid_email));
            mNewEmailEditText.requestFocus();
        }

        else {
            mNewEmailEditText.setError(null);
        }

        return valid;
    }
}
