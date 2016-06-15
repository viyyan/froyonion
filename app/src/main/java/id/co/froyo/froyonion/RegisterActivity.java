package id.co.froyo.froyonion;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import id.co.froyo.froyonion.helper.SessionManager;

public class RegisterActivity extends AppCompatActivity {
    private EditText regName, regEmail, regPassword;
    private Button regButton;
    private SessionManager mSessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        regName = (EditText) findViewById(R.id.regName);
        regEmail = (EditText) findViewById(R.id.regEmail);
        regPassword = (EditText) findViewById(R.id.regPassword);
        regButton = (Button) findViewById(R.id.regButton);

        mSessionManager = new SessionManager(getApplicationContext());

        regButton.setOnClickListener(new CustomOnClickListener());

    }

    private class CustomOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            String url = "https://api.froyonion.com/user/create";

            if(!isEmpty(regName) && !isEmpty(regEmail) && !isEmpty(regPassword)) {
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i("register Res", response.toString());
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("error", error.toString());

                    }
                })
                {
                    @Override
                    protected Map<String, String> getParams(){
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("fullname", regName.getText().toString());
                        params.put("email", regEmail.getText().toString());
                        params.put("password", regPassword.getText().toString());
                        return params;
                    }
                };
            } else {
                Log.i("error", "Fill all fields");

            }
        }
    }

    private boolean isEmpty(EditText eText) {
        return eText.getText().toString().trim().length() == 0;
    }
}
