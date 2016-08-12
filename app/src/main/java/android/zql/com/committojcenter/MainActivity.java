package android.zql.com.committojcenter;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.zql.com.jad.JAD;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        JAD.sayHello(this);
    }
}
