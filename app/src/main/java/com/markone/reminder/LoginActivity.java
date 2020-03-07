package com.markone.reminder;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import static com.markone.reminder.Common.USER_FILE;
import static com.markone.reminder.Common.USER_ID;
import static com.markone.reminder.Common.USER_MAIL;
import static com.markone.reminder.Common.USER_NAME;
import static com.markone.reminder.Common.USER_URI;
import static com.markone.reminder.Common.getGoogleSignInClient;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private final int RC_SIGN_IN = 9999;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            startMainActivity();
        }
        setContentView(R.layout.activity_login);
        findViewById(R.id.bt_sign_in).setOnClickListener(this);
        super.onCreate(savedInstanceState);
    }

    private void startMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bt_sign_in) {
            Intent signInIntent = getGoogleSignInClient(this, getString(R.string.default_web_client_id)).getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null) {
                firebaseAuthWithGoogle(account);
            } else {
                Common.viewToast(this, "Unable to sign-in");
            }
        } catch (ApiException e) {
            Log.w("Reminder-TAG", "signInResult:failed code=" + e.getStatusCode());
            Common.viewToast(this, "Unable to sign-in");
        }
    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                getSharedPreferences(USER_FILE, MODE_PRIVATE).edit()
                                        .putString(USER_NAME, user.getDisplayName())
                                        .putString(USER_MAIL, user.getEmail())
                                        .putString(USER_URI, user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "")
                                        .putString(USER_ID, acct.getId()).apply();
                                startMainActivity();
                            } else {
                                Common.viewToast(getApplicationContext(), "Unable to sign-in");
                            }
                        }
                    }
                });
    }
}
