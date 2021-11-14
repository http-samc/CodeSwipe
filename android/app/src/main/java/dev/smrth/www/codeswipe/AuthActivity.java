package dev.smrth.www.codeswipe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AdditionalUserInfo;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.OAuthCredential;
import com.google.firebase.auth.OAuthProvider;

import java.util.ArrayList;
import java.util.List;

public class AuthActivity extends AppCompatActivity {

    Button btnLogin;
    FirebaseAuth mAuth;
    FirebaseUser fUser;

    static String token;
    static String username;

    static final String tokenKey = "GITHUB_OAUTH_TOKEN";
    static final String usernameKey = "GITHUB_USERNAME";
    static final String historyKey = "HISTORY";

    static final String PREFERENCES = "CodeSwipePreferences";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        btnLogin = findViewById(R.id.authBtn);
        mAuth = FirebaseAuth.getInstance();
        fUser = mAuth.getCurrentUser();

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OAuthProvider.Builder provider = OAuthProvider.newBuilder("github.com");

                List<String> scopes =
                        new ArrayList<String>() {
                            {
                                add("user");
                                add("gist");
                            }
                        };
                provider.setScopes(scopes);


                Task<AuthResult> pendingResultTask = mAuth.getPendingAuthResult();
                if (pendingResultTask != null) {
                    // There's something already here! Finish the sign-in for your user.
                    pendingResultTask
                            .addOnSuccessListener(
                                    new OnSuccessListener<AuthResult>() {
                                        @Override
                                        public void onSuccess(AuthResult authResult) {
                                            OAuthCredential cred = (OAuthCredential) authResult.getCredential();
                                            AuthActivity.token = cred.getAccessToken();

                                            AdditionalUserInfo info = authResult.getAdditionalUserInfo();
                                            AuthActivity.username = info.getUsername();
                                        }
                                    })
                            .addOnFailureListener(
                                    new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(AuthActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                } else {
                    mAuth
                            .startActivityForSignInWithProvider(/* activity= */ AuthActivity.this, provider.build())
                            .addOnSuccessListener(
                                    new OnSuccessListener<AuthResult>() {
                                        @Override
                                        public void onSuccess(AuthResult authResult) {
                                            OAuthCredential cred = (OAuthCredential) authResult.getCredential();
                                            AuthActivity.token = cred.getAccessToken();

                                            AdditionalUserInfo info = authResult.getAdditionalUserInfo();
                                            AuthActivity.username = info.getUsername();

                                            openNextActivity();
                                        }
                                    })
                            .addOnFailureListener(
                                    new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(AuthActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });

                }

            }
        });

    }

    // After we get Auth, write username and token to SharedPrefs and open home activity
    private void openNextActivity() {
        Intent intent = new Intent(AuthActivity.this,HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);

        SharedPreferences sp = getSharedPreferences(
                AuthActivity.PREFERENCES,
                Context.MODE_PRIVATE
        );

        SharedPreferences.Editor editor = sp.edit();
        editor.putString(this.tokenKey, this.token);
        editor.putString(this.usernameKey, this.username);
        editor.apply();

        startActivity(intent);
        finish();
    }
}