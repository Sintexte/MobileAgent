package com.example.mobileagent;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    public static final int port = 443;
    public static final String host = "https://armyforce.azurewebsites.net:"+port+"/api";
    public static final String SHARED_PREFS = "user";
    public static final int TRACKINGINTERVALMS = 20000;

    private EditText username ;
    private EditText password;
    private TextView info;
    private Button connect;

    //add to backend this restrains for agent only [DONE]
    private int userlen_max = 4;
    private int passlen_max = 4;

    private SharedPreferences sharedpreferences;
    private String token;
    private int role;

    boolean connected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextInputLayout username_layout = findViewById(R.id.edit_user);
        TextInputLayout password_layout = findViewById(R.id.edit_pass);

        username = (EditText) username_layout.getEditText();
        password = (EditText) password_layout.getEditText();
        info     = (TextView) findViewById(R.id.textView2);
        connect  = (Button) findViewById(R.id.btn_connect);
        sharedpreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        connected = sharedpreferences.getBoolean("connected", false);
        token = sharedpreferences.getString("token", null);

        RequestQueue queue = Volley.newRequestQueue(this);
        String TAG = "[******] debug";

        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String str_user = username.getText().toString();
                String str_pass = password.getText().toString();
                info.setText("Vérification ...");

                if(str_user.length() >= userlen_max && str_pass.length()>=passlen_max){
                    String url = host+"/login";
                    StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    try {
                                        JSONObject json = new JSONObject(response);
                                        JSONObject json_data = json.getJSONObject("data");
                                        String token = json_data.getString("token");
                                        String username = json_data.getString("username");
                                        String email = json_data.getString("email");

                                        if(json_data.getInt("id_role") == 1){
                                            SharedPreferences.Editor editor = sharedpreferences.edit();
                                            editor.putBoolean("connected", true);
                                            editor.putString("token",token);
                                            editor.putString("username",username);
                                            editor.putString("email",email);
                                            editor.commit();
                                            Log.d(TAG, token);
                                            Toast.makeText(MainActivity.this, "Connected {token:"+token+"}", Toast.LENGTH_SHORT).show();
                                            info.setText("Connected");
                                            Intent panel = new Intent(MainActivity.this, PanelActivity.class);
                                            startActivity(panel);
                                            finish();
                                        }else{
                                            info.setText("");
                                            Toast.makeText(MainActivity.this, "Privileges: Administrators cannot use the app", Toast.LENGTH_LONG).show();
                                        }
                                    } catch (JSONException e) {
                                        info.setText("Response Error json");
                                        Log.e(TAG, e.toString());
                                    }

                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    if(error instanceof NetworkError || error instanceof ServerError || error instanceof TimeoutError){
                                        info.setText("NetworkError || ServerError || Timeout");
                                    }else{
                                        info.setText("Mauvais  Utulisateur ou Mot de Passe");
                                    }
                                    Log.d(TAG, "[*] FAILS");
                                    Log.e(TAG, error.toString() );
                                }
                    }){
                        @Nullable
                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {
                            Map<String,String> params = new HashMap<String,String>();
                            params.put("username",str_user);
                            params.put("password",str_pass);

                            return params;
                        }

                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            Map<String,String> params = new HashMap<String, String>();
                            params.put("Content-Type","application/x-www-form-urlencoded");
                            return params;
                        }
                    };

                    stringRequest.setRetryPolicy(new DefaultRetryPolicy(5000,2,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                    queue.add(stringRequest);
                }else{
                    info.setText("Nom D'utulisateur longeur supérieur à "+userlen_max+"\nMot de passe longeur supérieur à "+passlen_max);
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(connected && token!=null){
            Toast.makeText(MainActivity.this, "Already Connected with token :"+token, Toast.LENGTH_LONG).show();
            Intent panel = new Intent(MainActivity.this, PanelActivity.class);
            startActivity(panel);
            finish();
        }
    }
}