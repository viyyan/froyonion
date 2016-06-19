package id.co.froyo.froyonion;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import id.co.froyo.froyonion.helper.AppController;
import id.co.froyo.froyonion.helper.CustomRequest;
import id.co.froyo.froyonion.helper.SessionManager;

public class LoginActivity extends AppCompatActivity {
    private EditText logEmail, logPassword;
    private Button logButton;
    private TextView msgLogin, regActivity;
    private Context mContext;
    private SessionManager sessionManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mContext = getApplicationContext();

        logEmail = (EditText) findViewById(R.id.logEmail);
        logPassword = (EditText) findViewById(R.id.logPassword);
        logButton =  (Button) findViewById(R.id.logButton);
        msgLogin = (TextView) findViewById(R.id.msgLogin);
        regActivity = (TextView) findViewById(R.id.regActiv);

        sessionManager = new SessionManager(mContext);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.getString("regEmail") != null){
            logEmail.setText(bundle.getString("regEmail"));
            msgLogin.setText("Sukses, Login dengan email anda");
        }

        regActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
                i.putExtra("logEmail", logEmail.getText().toString());
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(i);
            }
        });

        logButton.setOnClickListener(new LoginClickListener());
    }

    private class LoginClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if(!isEmpty(logEmail) && !isEmpty(logPassword)) {
                String url = "https://api.froyonion.com/session/create";
                String tag = "json_obj_req";
                CustomRequest jsonObjectRequest = new CustomRequest(Request.Method.POST, url, null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                    if(response.has("data")) {
                                        Log.i("response", response.toString());
                                        try {
                                            JSONObject data = response.getJSONObject("data");
                                            sessionManager.saveSession(data.getString("id"), data.getString("fullname"), data.getString("email"), data.getString("access_token"));
                                            Intent i = new Intent(LoginActivity.this, MainActivity.class);
                                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            mContext.startActivity(i);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                if(error instanceof TimeoutError || error instanceof NoConnectionError) {
                                    Toast.makeText(mContext, "Poor Network", Toast.LENGTH_SHORT).show();
                                } else if (error instanceof AuthFailureError) {
                                    Toast.makeText(mContext, "Email atau Password salah", Toast.LENGTH_SHORT).show();
                                } else if (error instanceof ServerError) {
                                    Toast.makeText(mContext, "Email atau Password salah", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }){

                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("email", logEmail.getText().toString());
                        params.put("password", logPassword.getText().toString());
                        return params;
                    }
                };
                AppController.getInstance().addToRequestQueue(jsonObjectRequest, tag);
            }
        }
    }

    private boolean isEmpty(EditText eText) {
        return eText.getText().toString().trim().length() == 0;
    }


}
