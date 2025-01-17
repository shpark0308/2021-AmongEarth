package com.example.amongearth.record;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.amongearth.MainActivity;
import com.example.amongearth.R;
import com.example.amongearth.env.Utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ValueEventListener;
import com.gun0912.tedpermission.util.ObjectUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ResultWriteActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.result_write);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        result_image = findViewById(R.id.result_icon);
        result_text = findViewById(R.id.icon_text);
        origin_image = findViewById(R.id.origin_img);

        Intent intent = getIntent();
        String origin_path = intent.getExtras().getString("originPath");
        Integer sum_total = intent.getExtras().getInt("sum_total");

        Integer bad_score, normal_score, good_score;
        bad_score = 10; normal_score = 5; good_score = 3;

        Integer bad_case, normal_case, good_case;
        bad_case = 3; normal_case = 2; good_case = 3;

        if (sum_total > bad_score) {
            if(sum_total % bad_case == 0) {
                result_image.setImageResource(R.drawable.result_fever_earth);
                result_text.setText(getString(R.string.result_fever_earth));
            }
            else if(sum_total % bad_case == 1) {
                result_image.setImageResource(R.drawable.result_sad_earth);
                result_text.setText(getString(R.string.result_sad_earth));
            }
            else {
                result_image.setImageResource(R.drawable.result_polar_bear);
                result_text.setText(getString(R.string.result_polar_bear));
            }
        }
        else if (sum_total > normal_score) {
            if(sum_total % normal_case == 0) {
                result_image.setImageResource(R.drawable.result_healthy_earth);
                result_text.setText(getString(R.string.result_healthy_earth));
            }
            else {
                result_image.setImageResource(R.drawable.result_change_earth);
                result_text.setText(getString(R.string.result_change_earth));
            }
        }
        else {
            if(sum_total % good_case == 0) {
                result_image.setImageResource(R.drawable.result_save_lover);
                result_text.setText(getString(R.string.result_save_lover));
            }
            else if(sum_total % good_case == 1) {
                result_image.setImageResource(R.drawable.result_penguin);
                result_text.setText(getString(R.string.result_penguin));
            }
            else {
                result_image.setImageResource(R.drawable.result_orangutan);
                result_text.setText(getString(R.string.result_orangutan));
            }
        }

        this.sourceBitmap = BitmapFactory.decodeFile(origin_path);
        this.cropBitmap = Utils.processBitmap(sourceBitmap, TF_OD_API_INPUT_SIZE);
        origin_image.setImageBitmap(this.cropBitmap);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        
        mDatabase.child("user").child(userId).child("nickname").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                nickname = snapshot.getValue().toString();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        
        mDatabase.child("badge").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Object value = snapshot.child(userId).getValue();
                if (ObjectUtils.isEmpty(value)) {
                    addBadge(0, 0);
                    collected_photo = 0;
                    number_zero = 0;
                }
                else {
                    collected_photo = (int) (long) snapshot.child(userId).child("collected_photo").getValue();
                    number_zero = (int) (long) snapshot.child(userId).child("number_zero").getValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        write = findViewById(R.id.user_write);
        saveButton = findViewById(R.id.btn_save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent2 = new Intent(ResultWriteActivity.this, PopupActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("upload_file", origin_path);
                bundle.putString("nickname", nickname);
                bundle.putString("content", String.valueOf(write.getText()));
                bundle.putInt("collected_photo", collected_photo);
                bundle.putInt("number_zero", number_zero);
                intent2.putExtras(bundle);
                setResult(1, intent2);
                startActivity(intent2);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.toolbar, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                return true;
            }
            case R.id.BtnHome: {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public static final int TF_OD_API_INPUT_SIZE = 416;

    private Bitmap sourceBitmap;
    private Bitmap cropBitmap;

    private ImageView result_image, origin_image;
    private TextView result_text;
    private Button saveButton;
    private EditText write;

    private DatabaseReference mDatabase;
    String userId, nickname;
    Integer collected_photo, number_zero;

    @IgnoreExtraProperties
    public class BadgePost {
        public Integer collected_photo, number_zero;

        public BadgePost(){
        }

        public BadgePost(Integer collected_photo, Integer number_zero) {
            this.collected_photo = collected_photo;
            this.number_zero = number_zero;
        }

        @Exclude
        public Map<String, Object> toMap() {
            HashMap<String, Object> result = new HashMap<>();
            result.put("collected_photo", collected_photo);
            result.put("number_zero", number_zero);
            return result;
        }
    }

    public void addBadge(Integer collected_photo, Integer number_zero) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Map<String, Object> childUpdates = new HashMap<>();
        Map<String, Object> postValues = null;
        BadgePost post = new BadgePost(collected_photo, number_zero);
        postValues = post.toMap();
        childUpdates.put("/badge/" + userId, postValues);
        mDatabase.updateChildren(childUpdates);
    }

}
