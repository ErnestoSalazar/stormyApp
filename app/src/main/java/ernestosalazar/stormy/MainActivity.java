package ernestosalazar.stormy;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = MainActivity.class.getSimpleName();

    private CurrentWeather mCurrentWeather;

    //creamos las variables con la libreria ButterKnide
    @BindView(R.id.timeLabel) TextView mTimeLabel;
    @BindView(R.id.temperatureLabel) TextView mTemperatureLabel;
    @BindView(R.id.humidityValue) TextView mHumidityValue;
    @BindView(R.id.precipValue) TextView mPrecipValue;
    @BindView(R.id.summaryLabel) TextView mSummaryLabel;
    @BindView(R.id.iconImageView) ImageView mIconImageView;
    @BindView(R.id.refreshImageView) ImageView mRefreshImageView;
    @BindView(R.id.progressBar) ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // enlazamos las variables creadas con .bind(this)
        ButterKnife.bind(this);

        mProgressBar.setVisibility(View.INVISIBLE);

        final double latitude = 1;
        final double longitude = 1;

        mRefreshImageView.setOnClickListener(new View.OnClickListener() { // cuando la imagen de refresh sea clickeada
            @Override
            public void onClick(View v) {
                getForecast(latitude,longitude);
            }
        });

        getForecast(latitude,longitude);

        Log.d(TAG, "Main UI code is running");
    }

    private void getForecast( double latitude, double longitude) {
        String apiKey = "";

        String forecastUrl = "https://api.darksky.net/forecast/"+apiKey+"/"+latitude+","+longitude;

        if(isNetworkAvailable()) {
            toggleRefresh();
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(forecastUrl)
                    .build();

            Call call = client.newCall(request);
            call.enqueue(new Callback() { // manera asyncrona de hacer nuestra peticion http gracias a que corre en un thread spearado
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toggleRefresh();
                        }
                    });
                    alertUserAboutError();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toggleRefresh();
                        }
                    });
                    try {
                        String jsonData = response.body().string();

                        Log.v(TAG,jsonData);
                        if (response.isSuccessful()) {
                            mCurrentWeather = getCurrentDetails(jsonData);
                            runOnUiThread(new Runnable() { // para poder actualizar informacion en la UI
                                                          //debemos avisar a nuestro hilo principal que hay una respuesta
                                                          // del hilo que hace la peticion al web service

                                @Override
                                public void run() {// codigo que corre en el hilo principal (encargado de la interfaz grafica)
                                    updateDisplay();
                                }
                            });
                        } else {
                            alertUserAboutError();
                        }
                    }
                    catch (IOException e) {
                        Log.e(TAG, "Exception caught: ", e);
                    }
                    catch (JSONException e){
                        Log.e(TAG, "Exception caught: ", e);
                    }
                }
            });
        }
        else{
            Toast.makeText(this, R.string.network_unavailable_message,Toast.LENGTH_LONG).show();
        }
    }

    private void toggleRefresh() {
        if(mProgressBar.getVisibility() == View.INVISIBLE){
            mProgressBar.setVisibility(View.VISIBLE);
            mRefreshImageView.setVisibility(View.INVISIBLE);
        }
        else{
            mProgressBar.setVisibility(View.INVISIBLE);
            mRefreshImageView.setVisibility(View.VISIBLE);
        }
    }

    private void updateDisplay() {
        mTemperatureLabel.setText(String.valueOf(mCurrentWeather.getmTemperature()));
        mTimeLabel.setText("At "+mCurrentWeather.getFormattedTime()+" it will be");
        mHumidityValue.setText(String.valueOf(mCurrentWeather.getmHumidity()));
        mPrecipValue.setText(mCurrentWeather.getmPrecipChance()+"%");
        mSummaryLabel.setText(mCurrentWeather.getmSummary());

        Drawable drawable = getResources().getDrawable(mCurrentWeather.getIconId());
        mIconImageView.setImageDrawable(drawable);
    }

    private CurrentWeather getCurrentDetails(String jsonData) throws JSONException{ // lanzamos la excepcion para poderl utilizarla donde se llama el metodo si es necesario
        // JSONObject puede guardar cualquier objeto representado en JSON
        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");
        Log.i(TAG,"FROM JSON: "+timezone);

        // forecast es un json con otro objeto dentro llamado "currently"
        JSONObject currently = forecast.getJSONObject("currently"); // accedemos a ese objeto

        CurrentWeather currentWeather = new CurrentWeather();
        currentWeather.setmHumidity(currently.getDouble("humidity"));
        currentWeather.setmTime(currently.getLong("time"));
        currentWeather.setmIcon(currently.getString("icon"));
        currentWeather.setmPrecipChance(currently.getDouble("precipProbability"));
        currentWeather.setmSummary(currently.getString("summary"));
        currentWeather.setmTemperature(currently.getDouble("temperature"));
        currentWeather.setmTimeZone(timezone);

        Log.d(TAG,currentWeather.getFormattedTime());
        return currentWeather;
    }

    // utilizamos este metodo para determinar si existe o no conexión a internet
    private boolean isNetworkAvailable() {
        // getSystemService() retorna un objeto generico así que lo casteamos a ConnectivityManager
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE); // getSystemService() es un metodo de la ActivityClass
                                                         // el prametro que le ingresamos es un string con el nombre del servicio que queremos

        NetworkInfo networkInfo = manager.getActiveNetworkInfo(); // con networkInfo podemos analizar la info de la red
        boolean isAvailable = false;

        if(networkInfo != null){ // primeramente checamos si la red esta presente
            if(networkInfo.isConnected()){ // luego si hay conexion a internet
                isAvailable = true;
            }
        }

        return isAvailable;
    }

    private void alertUserAboutError() {
        AlertDialogFragment dialog = new AlertDialogFragment();
        dialog.show(getFragmentManager(),"erro_dialog");
    }
}
