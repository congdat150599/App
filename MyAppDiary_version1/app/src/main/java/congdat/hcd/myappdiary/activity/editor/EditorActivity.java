package congdat.hcd.myappdiary.activity.editor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.thebluealliance.spectrum.SpectrumPalette;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import congdat.hcd.myappdiary.Api.ApiClient;
import congdat.hcd.myappdiary.Api.ApiInterface;
import congdat.hcd.myappdiary.R;
import congdat.hcd.myappdiary.activity.main.HomeActivity;
import congdat.hcd.myappdiary.model.Note;
import retrofit2.Call;
import retrofit2.Callback;

public class EditorActivity extends AppCompatActivity implements EditorView{

    private static final String TAG = HomeActivity.class.getSimpleName();
    EditText et_title, et_note, mdate;
    ImageView mPicture;
    ProgressDialog progressDialog;
    SpectrumPalette palette;


    Calendar myCalendar = Calendar.getInstance();

    EditorPresenter presenter;
    EditorActivity editorActivity;

    ApiInterface apiInterface;

    int color, id;
    String title, note, date, picture;
    Menu actionMenu;

    private Bitmap bitmap;

    private static String URL_UPLOAD = "http://172.17.30.214/Android/DiaryApp/upload.php";
    String getId;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        et_title = (EditText) findViewById(R.id.title);
        et_note = (EditText) findViewById(R.id.note);
        palette = findViewById(R.id.palette);
        mdate = findViewById(R.id.date);
        mPicture = findViewById(R.id.picture);

        palette.setOnColorSelectedListener(
                clr -> color = clr
        );

        mdate.setFocusableInTouchMode(false);
        mdate.setFocusable(false);
        mdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(EditorActivity.this, ngay, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });


//      Progress Dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");

        presenter = new EditorPresenter(this);

        Intent intent = getIntent();
        id = intent.getIntExtra("id", 0);
        title = intent.getStringExtra("title");
        note = intent.getStringExtra("note");
        color = intent.getIntExtra("color", 0);
        date = intent.getStringExtra("date");
        picture = intent.getStringExtra("picture");

        setDataFromIntentExtra();

    }

    private void setDataFromIntentExtra(){
        if (id != 0){

            getSupportActionBar().setTitle("Update Note");
            readMode();

            et_title.setText(title);
            et_note.setText(note);
            palette.setSelectedColor(color);
            mdate.setText(date);

            RequestOptions requestOptions = new RequestOptions();
            requestOptions.skipMemoryCache(true);
            requestOptions.diskCacheStrategy(DiskCacheStrategy.NONE);
            requestOptions.placeholder(R.drawable.logo);
            requestOptions.error(R.drawable.logo);

            Glide.with(EditorActivity.this)
                    .load(picture)
                    .apply(requestOptions)
                    .into(mPicture);


        }else {
            //     Default color Setup
            palette.setSelectedColor(getResources().getColor(R.color.white));
            color = getResources().getColor(R.color.white);
            editMode();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_editor, menu);
        actionMenu = menu;

        if (id != 0){
            actionMenu.findItem(R.id.edit).setVisible(true);
            actionMenu.findItem(R.id.delete).setVisible(true);
            actionMenu.findItem(R.id.save).setVisible(false);
            actionMenu.findItem(R.id.attach_image).setVisible(false);
            actionMenu.findItem(R.id.update).setVisible(false);

        }else {
            actionMenu.findItem(R.id.edit).setVisible(false);
            actionMenu.findItem(R.id.delete).setVisible(false);
            actionMenu.findItem(R.id.save).setVisible(true);
            actionMenu.findItem(R.id.attach_image).setVisible(true);
            actionMenu.findItem(R.id.update).setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        String title = et_title.getText().toString().trim();
        String note = et_note.getText().toString().trim();
        int color = this.color;

        switch (item.getItemId()){
            case R.id.save:
                //save
                if (title.isEmpty()){
                    et_title.setError("Please enter a title");
                }else if(note.isEmpty()){
                    et_note.setError("please enter a note");
                }else {
                    presenter.saveNote(title, note, color, date, picture);
                }
                return true;

            case R.id.edit:
                editMode();
                actionMenu.findItem(R.id.edit).setVisible(false);
                actionMenu.findItem(R.id.delete).setVisible(false);
                actionMenu.findItem(R.id.save).setVisible(false);
                actionMenu.findItem(R.id.update).setVisible(true);
                actionMenu.findItem(R.id.attach_image).setVisible(true);

                return true;
            case R.id.update:
                //update
                if (title.isEmpty()){
                    et_title.setError("Please enter a title");
                }else if(note.isEmpty()){
                    et_note.setError("please enter a note");
                }else {
                    presenter.updateNote(id, title, note, color, date, picture);
                }
                return true;
            case R.id.delete:

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                alertDialog.setTitle("Confirm!");
                alertDialog.setMessage("Are you sure?");
                alertDialog.setNegativeButton("Yes", (dialogInterface, i) ->{
                    dialogInterface.dismiss();
                    presenter.deleteNote(id, picture);
                });
                alertDialog.setPositiveButton("Cancel",
                        (dialogInterface, i) -> dialogInterface.dismiss());

                alertDialog.show();

                return true;
            case R.id.attach_image:
                chooseFile();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    DatePickerDialog.OnDateSetListener ngay = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            // TODO Auto-generated method stub
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            setDate();
        }

    };
    private void setDate() {
        String myFormat = "dd MMMM yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        mdate.setText(sdf.format(myCalendar.getTime()));
    }

    public String getStringImage(Bitmap bitmap){

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);

        byte[] imageByteArray = byteArrayOutputStream.toByteArray();
        String encodedImage = Base64.encodeToString(imageByteArray, Base64.DEFAULT);

        return encodedImage;
    }

    private void chooseFile(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode ==RESULT_OK && data != null && data.getData() != null){
//
            Uri filePath = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                mPicture.setImageBitmap(bitmap);

            } catch (IOException e) {
                e.printStackTrace();
            }
            UploadPicture(getId, getStringImage(bitmap));

        }
    }


    @Override
    public void showProgress() {
        progressDialog.show();
    }

    @Override
    public void hideProgress() {
        progressDialog.hide();
    }

    @Override
    public void onRequestSuccess(String message) {
        Toast.makeText(EditorActivity.this,
                message,
                Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();//back to main activity

    }

    @Override
    public void onRequestError(String message) {
        Toast.makeText(EditorActivity.this,
                message,
                Toast.LENGTH_SHORT).show();
        finish();//back to main activity

    }


    private void editMode() {
        et_title.setFocusableInTouchMode(true);
        et_note.setFocusableInTouchMode(true);
        palette.setEnabled(true);
        mdate.setEnabled(true);
    }

    private void readMode() {
        et_title.setFocusableInTouchMode(false);
        et_note.setFocusableInTouchMode(false);
        et_title.setFocusable(false);
        et_note.setFocusable(false);
        palette.setEnabled(false);
        mdate.setEnabled(false);
    }



    private void UploadPicture(final String id, final String photo){

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading...");
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_UPLOAD,
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        Log.i(TAG, response.toString());
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String success = jsonObject.getString("success");

                            if (success.equals("1")){
                                Toast.makeText(EditorActivity.this, "Success!", Toast.LENGTH_SHORT).show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            progressDialog.dismiss();
                            Toast.makeText(EditorActivity
                                    .this, "Try Again!"+e.toString(), Toast.LENGTH_SHORT).show();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        Toast.makeText(EditorActivity.this, "Try Again!"+error.toString(), Toast.LENGTH_SHORT).show();

                    }
                })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("id", id);
                params.put("photo", photo);

                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

    }

    ///post data

//    private void saveNote(final String key) {
//
//        final ProgressDialog progressDialog = new ProgressDialog(this);
//        progressDialog.setMessage("Saving...");
//        progressDialog.show();
//
//        readMode();
//
//        String title = et_title.getText().toString().trim();
//        String note = et_note.getText().toString().trim();
//        String date = mdate.getText().toString().trim();
//        String picture = null;
//        if (bitmap == null) {
//            picture = "";
//        } else {
//            picture = getStringImage(bitmap);
//        }
//
//        apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
//
//        Call<Note> call = apiInterface.saveNote(key, title, note, color, date, picture);
//
//        call.enqueue(new Callback<Note>() {
//            @Override
//            public void onResponse(Call<Note> call, retrofit2.Response<Note> response) {
//
//                progressDialog.dismiss();
//
//                Log.i(EditorActivity.class.getSimpleName(), response.toString());
//
//                String value = response.body().getValue();
//                String message = response.body().getMessage();
//
//                if (value.equals("1")){
//                    finish();
//                } else {
//                    Toast.makeText(EditorActivity.this, message, Toast.LENGTH_SHORT).show();
//                }
//
//            }
//
//            @Override
//            public void onFailure(Call<Note> call, Throwable t) {
//                progressDialog.dismiss();
//                Toast.makeText(EditorActivity.this, t.getMessage().toString(), Toast.LENGTH_SHORT).show();
//            }
//        });
//
//    }
//
//    private void updateNote(final String key, final int id) {
//
//        final ProgressDialog progressDialog = new ProgressDialog(this);
//        progressDialog.setMessage("Updating...");
//        progressDialog.show();
//
//        readMode();
//
//        String title = et_title.getText().toString().trim();
//        String note = et_note.getText().toString().trim();
//        String date = mdate.getText().toString().trim();
//        String picture = null;
//        if (bitmap == null) {
//            picture = "";
//        } else {
//            picture = getStringImage(bitmap);
//        }
//
//        apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
//
//        Call<Note> call = apiInterface.updateNote(key, id, title, note, color , date, picture);
//
//        call.enqueue(new Callback<Note>() {
//            @Override
//            public void onResponse(Call<Note> call, retrofit2.Response<Note> response) {
//
//                progressDialog.dismiss();
//
//                Log.i(EditorActivity.class.getSimpleName(), response.toString());
//
//                String value = response.body().getValue();
//                String message = response.body().getMessage();
//
//                if (value.equals("1")){
//                    Toast.makeText(EditorActivity.this, message, Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(EditorActivity.this, message, Toast.LENGTH_SHORT).show();
//                }
//
//            }
//
//            @Override
//            public void onFailure(Call<Note> call, Throwable t) {
//                progressDialog.dismiss();
//                Toast.makeText(EditorActivity.this, t.getMessage().toString(), Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    private void delete(final String key, final int id, final String pic) {
//
//        final ProgressDialog progressDialog = new ProgressDialog(this);
//        progressDialog.setMessage("Deleting...");
//        progressDialog.show();
//
//        readMode();
//
//        apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
//
//        Call<Note> call = apiInterface.deleteNote(key, id, picture);
//
//        call.enqueue(new Callback<Note>() {
//            @Override
//            public void onResponse(Call<Note> call, retrofit2.Response<Note> response) {
//
//                progressDialog.dismiss();
//
//                Log.i(EditorActivity.class.getSimpleName(), response.toString());
//
//                String value = response.body().getValue();
//                String message = response.body().getMessage();
//
//                if (value.equals("1")){
//                    Toast.makeText(EditorActivity.this, message, Toast.LENGTH_SHORT).show();
//                    finish();
//                } else {
//                    Toast.makeText(EditorActivity.this, message, Toast.LENGTH_SHORT).show();
//                }
//
//            }
//
//            @Override
//            public void onFailure(Call<Note> call, Throwable t) {
//                progressDialog.dismiss();
//                Toast.makeText(EditorActivity.this, t.getMessage().toString(), Toast.LENGTH_SHORT).show();
//            }
//        });
//
//    }

}
