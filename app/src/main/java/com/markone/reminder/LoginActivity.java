package com.markone.reminder;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import static com.markone.reminder.Common.USER_FILE;
import static com.markone.reminder.Common.USER_ID;
import static com.markone.reminder.Common.USER_MAIL;
import static com.markone.reminder.Common.USER_NAME;
import static com.markone.reminder.Common.getGoogleSignInClient;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private final int RC_SIGN_IN = 9999;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

        if (account != null) {
            startMainActivity();
        }
        setContentView(R.layout.activity_login);
        findViewById(R.id.bt_sign_in).setOnClickListener(this);
        super.onCreate(savedInstanceState);
    }

    private void startMainActivity() {
        Intent i = new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bt_sign_in) {
            Intent signInIntent = getGoogleSignInClient(this).getSignInIntent();
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

//            Uri personPhoto = account.getPhotoUrl();
            //Todo save user photo

            assert account != null;
            getSharedPreferences(USER_FILE, MODE_PRIVATE).edit()
                    .putString(USER_NAME, account.getGivenName())
                    .putString(USER_MAIL, account.getEmail())
                    .putString(USER_ID, account.getId()).apply();

            // Signed in successfully, show authenticated UI.
            startMainActivity();
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("Reminder-TAG", "signInResult:failed code=" + e.getStatusCode());
            Common.viewToast(this, "Unable to sign-in");
        }
    }
}
