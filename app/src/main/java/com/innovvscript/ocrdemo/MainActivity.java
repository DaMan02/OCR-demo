package com.innovvscript.ocrdemo;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextDetector;

import java.util.List;

public class MainActivity extends BaseActivity {

    private Button mButton;
    private Button button_cam;
    private TextView tv;
    private SharedPreferences pref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);

        button_cam = findViewById(R.id.button_cam);
        mButton = findViewById(R.id.button_text);
        tv = findViewById(R.id.text);
        pref = PreferenceManager.getDefaultSharedPreferences(this);

        if(pref.contains("text")){
            String t = "Last saved text:\n\n" + pref.getString("text", "Not found!");
            tv.setText(t);
        }

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myBitmap != null) {
                    runTextRecognition();
                } else {
                    showToast("Choose an image");
                }

            }
        });
        button_cam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(getPickImageChooserIntent(), 200);
            }
        });

    }

    private void runTextRecognition() {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(myBitmap);
        FirebaseVisionTextDetector detector = FirebaseVision.getInstance()
                .getVisionTextDetector();
        mButton.setEnabled(false);
        detector.detectInImage(image)
                .addOnSuccessListener(
                        new OnSuccessListener<FirebaseVisionText>() {
                            @Override
                            public void onSuccess(FirebaseVisionText texts) {
                                mButton.setEnabled(true);
                                processTextRecognitionResult(texts);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Task failed with an exception
                                mButton.setEnabled(true);
                                e.printStackTrace();
                            }
                        });
    }

    private void processTextRecognitionResult(FirebaseVisionText texts) {

        StringBuilder t = new StringBuilder();

        List<FirebaseVisionText.Block> blocks = texts.getBlocks();
        if (blocks.size() == 0) {
            showToast("No text found");
            return;
        }

        for (int i = 0; i < blocks.size(); i++) {
            t.append(" ").append(blocks.get(i).getText());
        }
        String info = "DETECTED TEXT:\n" + t.toString();
        pref.edit().putString("text", t.toString()).apply();
        tv.setText(info);
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

}