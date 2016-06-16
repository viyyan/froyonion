package id.co.froyo.froyonion;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import id.co.froyo.froyonion.helper.AppController;
import id.co.froyo.froyonion.helper.CustomRequest;
import id.co.froyo.froyonion.helper.SessionManager;

public class RegisterActivity extends AppCompatActivity {
    private EditText regName, regEmail, regPassword;
    private Button regButton;
    private SessionManager mSessionManager;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        regName = (EditText) findViewById(R.id.regName);
        regEmail = (EditText) findViewById(R.id.regEmail);
        regPassword = (EditText) findViewById(R.id.regPassword);
        regButton = (Button) findViewById(R.id.regButton);
        mContext = getApplicationContext();
        mSessionManager = new SessionManager(mContext);

        regButton.setOnClickListener(new CustomOnClickListener());

        Bundle bundle = getIntent().getExtras();
        if(bundle != null && bundle.getString("logEmail") != null){
            regEmail.setText(bundle.getString("logEmail"));
        }
    }

    private class CustomOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            String url = "https://api.froyonion.com/user/create";
            String tag = "json_obj_login";
            if(!isEmpty(regName) && !isEmpty(regEmail) && !isEmpty(regPassword)) {
                CustomRequest jsonRequest = new CustomRequest(Request.Method.POST, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i("registerRes", response.toString());

                        try {
                            if(response.has("data")) {
                                Log.i("Success", "Registered");
                                Intent i = new Intent(RegisterActivity.this, LoginActivity.class);
                                i.putExtra("regEmail", regEmail.getText().toString());
                                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                mContext.startActivity(i);
                            } else if (response.has("message")) {
                                Toast.makeText(mContext, response.getString("message"), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(mContext, "Gagal", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if(error instanceof TimeoutError || error instanceof NoConnectionError) {
                            Toast.makeText(mContext, "Poor Network", Toast.LENGTH_SHORT).show();
                        } else if (error instanceof AuthFailureError) {
                            Toast.makeText(mContext, "Kamu Bukan Froyonion", Toast.LENGTH_SHORT).show();
                        } else if (error instanceof ServerError) {
                            Toast.makeText(mContext, "Server Error", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                {
                    @Override
                    protected Map<String, String> getParams(){
                        Map<String, String> params = new HashMap<>();
                        params.put("fullname", regName.getText().toString());
                        params.put("email", regEmail.getText().toString());
                        params.put("password", regPassword.getText().toString());
                        return params;
                    }
                };

//                Volley.newRequestQueue(mContext).add(stringRequest);
                AppController.getInstance().addToRequestQueue(jsonRequest, tag);

            } else {
                Log.i("error", "Fill all fields");

            }
        }
    }

    private boolean isEmpty(EditText eText) {
        return eText.getText().toString().trim().length() == 0;
    }
}
