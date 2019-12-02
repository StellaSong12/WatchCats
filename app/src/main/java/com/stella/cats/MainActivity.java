package com.stella.cats;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.stella.cats.databinding.ActivityMainBinding;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import android.os.Environment;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding mBinding;

    private static final int REQUEST_CODE_PERMISSION_READ = 0x00;

    private int picWidth = 0;
    private int picHeight = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        setContentView(mBinding.getRoot());

        getPicture();

//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        // 浮点保存图片
        DragFloatActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_CODE_PERMISSION_READ);
                } else {
                    drawBitmap();
                }
            }
        });

        mBinding.contentMain.layoutAddText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBinding.contentMain.dragView.addDragView(R.layout.drag_text_view, 100, 400, 580, 560, false, false);
            }
        });

        mBinding.contentMain.layoutFindCat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPicture();
            }
        });

        mBinding.contentMain.layoutTextColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBinding.contentMain.llTextColor.getVisibility() == View.VISIBLE) {
                    mBinding.contentMain.llTextColor.setVisibility(View.GONE);
                } else {
                    mBinding.contentMain.llTextColor.setVisibility(View.VISIBLE);
                }
                mBinding.contentMain.llBackgroundColor.setVisibility(View.GONE);
            }
        });
        mBinding.contentMain.layoutTextBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBinding.contentMain.llTextColor.setVisibility(View.GONE);
                if (mBinding.contentMain.llBackgroundColor.getVisibility() == View.VISIBLE) {
                    mBinding.contentMain.llBackgroundColor.setVisibility(View.GONE);
                } else {
                    mBinding.contentMain.llBackgroundColor.setVisibility(View.VISIBLE);
                }
            }
        });

        mBinding.contentMain.colorPickerText.setOnColorChangeListener(new SlideColorPicker.OnColorChangeListener() {
            @Override
            public void onColorChange(int selectedColor) {
                int id = mBinding.contentMain.dragView.getChooseViewId();
                mBinding.contentMain.dragView.setViewTextColor(id, selectedColor);
            }
        });

        mBinding.contentMain.colorPickerBackground.setOnColorChangeListener(new SlideColorPicker.OnColorChangeListener() {
            @Override
            public void onColorChange(int selectedColor) {
                int id = mBinding.contentMain.dragView.getChooseViewId();
                mBinding.contentMain.dragView.setViewBackgroundColor(id, selectedColor);
            }
        });

        mBinding.contentMain.isTransparency.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int id = mBinding.contentMain.dragView.getChooseViewId();
                if (isChecked) {
                    mBinding.contentMain.dragView.setViewBackgroundColor(id, Color.argb(0, 0, 0, 0));
                } else {
                    mBinding.contentMain.dragView.setViewBackgroundColor(id, mBinding.contentMain.colorPickerBackground.getColor());
                }
            }
        });

        mBinding.contentMain.crisperding.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int id = mBinding.contentMain.dragView.getChooseViewId();
                if (checkedId == R.id.no_crisperding) {
                    mBinding.contentMain.dragView.setViewTextCrisperding(id, Color.argb(0, 0, 0, 0));
                } else if (checkedId == R.id.black_crisperding) {
                    mBinding.contentMain.dragView.setViewTextCrisperding(id, getResources().getColor(R.color.black));
                } else if (checkedId == R.id.white_crisperding) {
                    mBinding.contentMain.dragView.setViewTextCrisperding(id, getResources().getColor(R.color.white));
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSION_READ) {
            if (grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                drawBitmap();
            }
        }
    }

    private void getPicture() {
        Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(ObservableEmitter<Object> emitter) throws Exception {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url("https://api.thecatapi.com/v1/images/search")
                        .build();
                emitter.onNext(client.newCall(request).execute());
                emitter.onComplete();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) throws Exception {
                        if (o instanceof Response && ((Response) o).body() != null) {
                            JSONArray jsonArray = new JSONArray(((Response) o).body().string());
                            JSONObject jsonObject = new JSONObject(jsonArray.get(0).toString());
                            Glide.with(MainActivity.this).
                                    load(jsonObject.get("url")).
                                    into(mBinding.contentMain.dragView.mBackgroundIv);
                            picHeight = jsonObject.getInt("height");
                            picWidth = jsonObject.getInt("width");
                        }
                    }
                });
    }

    private void drawBitmap() {
        View decorView = mBinding.contentMain.dragView;
        Bitmap mBitmap = Bitmap.createBitmap(decorView.getWidth(), decorView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas mCanvas = new Canvas(mBitmap);
        decorView.draw(mCanvas);

        // 截取图片范围
        int w = mBitmap.getWidth();
        int h = mBitmap.getHeight();
        Bitmap bitmap;
        if (picHeight * w > picWidth * h) {
            int trueWidth = picWidth * h / picHeight;
            bitmap = Bitmap.createBitmap(mBitmap, (w - trueWidth) / 2, 0, trueWidth, h);
        } else {
            int trueHeight = picHeight * w / picWidth;
            bitmap = Bitmap.createBitmap(mBitmap, 0, (h - trueHeight) / 2, w, trueHeight);
        }

        // 保存绘图为本地图片
        mCanvas.save();
        mCanvas.restore();

        // 保存到sdcard根目录下，share_pic文件夹下
        File file = new File(Environment.getExternalStorageDirectory().getPath() + "/" + System.currentTimeMillis() + ".png");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 50, fos);
            Toast.makeText(this, "保存成功: " + file.getName(), Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            if (fos != null) {
                fos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
