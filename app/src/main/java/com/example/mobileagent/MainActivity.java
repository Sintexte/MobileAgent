package com.example.mobileagent;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    String host = "192.168.1.25";
    int port = 3001;

    EditText username ;
    EditText password;
    TextView info;
    Button connect;

    //add to backend this restrains for agent only [DONE]
    int userlen_max = 4;
    int passlen_max = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        username = (EditText) findViewById(R.id.edit_user);
        password = (EditText) findViewById(R.id.edit_pass);
        info     = (TextView) findViewById(R.id.textView2);
        connect  = (Button) findViewById(R.id.btn_connect);

        RequestQueue queue = Volley.newRequestQueue(this);
        String TAG = "[******] debug";
        Log.w(TAG, "onCreate: " );
        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String str_user = username.getText().toString();
                String str_pass = password.getText().toString();
                if(str_user.length() >= userlen_max && str_pass.length()>=passlen_max){
                    String url = "http://"+host+":"+port+"/api/login";
                    StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    info.setText("Connected !");
                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    if(error instanceof NetworkError || error instanceof ServerError){
                                        info.setText("NetworkError || ServerError");
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


                    queue.add(stringRequest);
                }else{
                    info.setText("Nom D'utulisateur longeur supérieur à "+userlen_max+"\nMot de passe longeur supérieur à "+passlen_max);
                }
            }
        });
    }
}