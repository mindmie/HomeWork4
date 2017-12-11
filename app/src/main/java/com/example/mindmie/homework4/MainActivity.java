package com.example.mindmie.homework4;

import android.app.usage.NetworkStats;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.annotation.Nullable;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import org.w3c.dom.Comment;
import org.w3c.dom.Text;

import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.jar.Attributes;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private TextView nameUser;
    private TextView emailUser;
    private Button btnLogout;

    String mCode;

    private ListView database_list_view1;
    private ArrayList<String> arrayList1 = new ArrayList<>();
    private ArrayAdapter<String> adapter1;

    private ListView database_list_view2;
    private ArrayList<String> arrayList2 = new ArrayList<>();
    private ArrayAdapter<String> adapter2;

    // firebase
    private GoogleApiClient googleApiClient;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;

    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nameUser = (TextView)findViewById(R.id.tv_name);
        emailUser = (TextView)findViewById(R.id.tv_email);
        btnLogout = (Button)findViewById(R.id.btn_logout);

        adapter1 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,arrayList1);
        database_list_view1 = (ListView)findViewById(R.id.tv_show_text1);
        database_list_view1.setAdapter(adapter1);
        database_list_view1.setVisibility(View.VISIBLE);

        adapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,arrayList2);
        database_list_view2 = (ListView)findViewById(R.id.tv_show_text2);
        database_list_view2.setAdapter(adapter2);
        database_list_view2.setVisibility(View.VISIBLE);

        //---------------------------------------------------------------- gg sign in here!!
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this,this)
                .addApi(Auth.GOOGLE_SIGN_IN_API,gso)
                .build();

        //---------------------------------------------------------------- firebase here!!
        firebaseAuth =FirebaseAuth.getInstance();
        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null){
                    setUserData(user);


                }else{
                    GoLogInScreen();
                }
            }
        };
    }

    //---------------------------------------------------------------- add text here!!
    public void addText(View view){

        database_list_view1.setVisibility(View.GONE);
        database_list_view2.setVisibility(View.GONE);

        EditText inputName = (EditText) findViewById(R.id.et_name);
        EditText inputText = (EditText) findViewById(R.id.et_text);


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        String stringName = inputName.getText().toString();
        String stringText = inputText.getText().toString();
        String stringCode = user.getUid().toString();

        if(stringName.length() == 0){
            stringName = "No name";
        }
        if(stringText.length() == 0){
            stringText = "There is nothing";
        }

        DatabaseReference mNameRef = mRootRef.child("Records").child(stringCode).child("Name");
        DatabaseReference mMessageRef = mRootRef.child("Records").child(stringCode).child("Text");


        DatabaseReference mCodeId = mRootRef.child("UserIDs").child(stringCode);
        mCodeId.setValue(stringCode + "");

        mNameRef.setValue(stringName + "");
        mMessageRef.setValue(stringText + "");
    }

    //---------------------------------------------------------------- set user here!!
    private void setUserData(FirebaseUser user) {

        nameUser.setText(user.getDisplayName());
        emailUser.setText(user.getEmail());
        mCode = user.getUid();
    }


    //---------------------------------------------------------------- log out here!!
    public void logOut(View view){
        firebaseAuth.signOut();
        Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                if(status.isSuccess()){
                    GoLogInScreen();
                }else{
                    Toast.makeText(getApplicationContext(),"Failed!! , please try again", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    //---------------------------------------------------------------- log in here!!
    private void GoLogInScreen() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(firebaseAuthListener);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(firebaseAuthListener!=null){
            firebaseAuth.removeAuthStateListener(firebaseAuthListener);
        }
    }

}
