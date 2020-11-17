package movil.itq.excel26;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.FileAsyncHttpResponseHandler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.read.biff.BiffException;

public class MainActivity extends AppCompatActivity {

    Button btnCalcular;
    RadioButton mensual, anual;
    TextInputEditText txtCalcular;
    TextView txtISR;
    Workbook workbook;
    List<String> limInf = new ArrayList<>(), limSup = new ArrayList<>(), cuota = new ArrayList<>(),
            percent = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AsyncHttpClient client = new AsyncHttpClient();

        btnCalcular = findViewById(R.id.btnCalcular);
        mensual = findViewById(R.id.radMen);
        anual = findViewById(R.id.radAn);
        txtCalcular = findViewById(R.id.txtCalcular);
        txtISR = findViewById(R.id.txtISR);

        mensual.setOnClickListener(v -> CMensual());
        anual.setOnClickListener(v -> CAnual());

        String url = "https://github.com/buhowo/ISR-Calc/blob/main/isr.xls?raw=true";

        //Asignamos una acción al boton de calcular
        btnCalcular.setOnClickListener(v -> client.get(url, new FileAsyncHttpResponseHandler(getApplicationContext()) {
            //En caso de fallo, se hara saber al usuario que las descargas fallaron
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {
                Toast.makeText(MainActivity.this, "Fallo la descarga.", Toast.LENGTH_SHORT).show();
            }

            //En caso de exito, se avisa al usuario y se continua el proceso
            @SuppressLint("SetTextI18n")
            @Override
            public void onSuccess(int statusCode, Header[] headers, File file) {
                Toast.makeText(MainActivity.this, "Tablas actualizadas!", Toast.LENGTH_SHORT).show();

                WorkbookSettings ws = new WorkbookSettings();
                ws.setGCDisabled(true);
                Double sueldo = Double.valueOf(String.valueOf(txtCalcular.getText()));

                if (file != null){
                    try {
                        workbook = Workbook.getWorkbook(file);
                        Sheet sheet;
                        if (mensual.isChecked()){
                            sheet = workbook.getSheet(0);
                            //Se inicia i = 1 para que se omitan los titulos de cada columna
                            for(int i = 1; i < sheet.getRows(); i++){
                                //Llenamos los arreglos con sus valores
                                Cell[] row = sheet.getRow(i);
                                limInf.add(row[0].getContents());
                                limSup.add(row[1].getContents());
                                cuota.add(row[2].getContents());
                                percent.add(row[3].getContents());
                            }
                        }
                        else if (anual.isChecked()){
                            sheet = workbook.getSheet(1);
                            //Se inicia i = 1 para que se omitan los titulos de cada columna
                            for(int i = 1; i < sheet.getRows(); i++){
                                //Llenamos los arreglos con sus valores
                                Cell[] row = sheet.getRow(i);
                                limInf.add(row[0].getContents());
                                limSup.add(row[1].getContents());
                                cuota.add(row[2].getContents());
                                percent.add(row[3].getContents());
                            }
                        }
                        else {
                            Toast.makeText(MainActivity.this, "Selecciona un periodo.",Toast.LENGTH_SHORT).show();
                        }
                        txtISR.setText("$ " + calcular(sueldo));
                    } catch (IOException | BiffException e) {
                        e.printStackTrace();
                    }
                }
            }

            private Double calcular(Double sueldo) {
                double ISR, inf, tasa, cuot;

                for (int i = 0; i < limInf.size(); i++){
                    if (sueldo >= Double.parseDouble(limInf.get(i)) && sueldo <= Double.parseDouble(limSup.get(i))){
                        inf = Double.parseDouble(limInf.get(i));
                        tasa = Double.parseDouble(percent.get(i));
                        cuot = Double.parseDouble(cuota.get(i));

                        ISR = (((sueldo - inf)*tasa)/100)+cuot;
                        return ((double)Math.round(ISR * 100d) / 100d);
                    }
                }
                return null;
            }
        }));
    }

    private void CMensual() {
        anual.setChecked(false);
        mensual.setChecked(true);
    }

    public void CAnual(){
        mensual.setChecked(false);
        anual.setChecked(true);
    }

}