package co.onlini.beacome.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.yalantis.ucrop.UCrop;

import java.io.File;

import co.onlini.beacome.R;
import co.onlini.beacome.model.Vcard;
import co.onlini.beacome.util.FileUtil;
import co.onlini.beacome.util.InputDataValidation;
import co.onlini.beacome.util.TextWatcher;
import co.onlini.beacome.util.VCardUtil;

public class EditVcardActivity extends AppCompatActivity {

    public static final String EXTRA_RESULT_VCARD_UUID = "extra_result_vcard";

    private static final String EXTRA_DATA_VCARD = "extra_data_vcard";

    private static final int REQUEST_PICK_IMAGE = 0x1;

    private EditText mEtName;
    private EditText mEtEmail;
    private EditText mEtPhone;
    private ImageView mIvImage;
    private Vcard mVcard;

    private MenuItem.OnMenuItemClickListener mOnMenuItemClickListener = new MenuItem.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_save:
                default:
                    hideKeyboard();
                    returnData();
            }
            return false;
        }
    };

    public static Intent getIntent(Context context, Vcard vcard) {
        Intent intent = new Intent(context, EditVcardActivity.class);
        intent.putExtra(EXTRA_DATA_VCARD, vcard);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_vcard);
        //noinspection ConstantConditions
        findViewById(R.id.btn_choose_image).setOnClickListener(
                new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.btn_choose_image:
                        showImagePicker();
                        break;
                }
            }
        });
        mIvImage = (ImageView) findViewById(R.id.iv_image);
        mEtName = (EditText) findViewById(R.id.et_name);
        mEtEmail = (EditText) findViewById(R.id.et_email);
        mEtPhone = (EditText) findViewById(R.id.et_phone);
        mEtName.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                mVcard.setName(s.toString());
            }
        });
        mEtEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                mVcard.setEmail(s.toString());
            }
        });
        mEtPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                mVcard.setPhone(s.toString());
            }
        });
        initToolBar();
        Bundle extras = getIntent().getExtras();
        if (savedInstanceState != null) {
            mVcard = savedInstanceState.getParcelable(EXTRA_DATA_VCARD);
        } else if (extras != null && extras.containsKey(EXTRA_DATA_VCARD)) {
            mVcard = extras.getParcelable(EXTRA_DATA_VCARD);
        }
        if (mVcard != null) {
            mEtName.setText(mVcard.getName());
            mEtEmail.setText(mVcard.getEmail());
            mEtPhone.setText(mVcard.getPhone());
            applyImage(mVcard.getImageFile());
        } else {
            finish();
        }
    }

    private void initToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_clear_24dp);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(EXTRA_DATA_VCARD, mVcard);
    }

    @Override
    protected void onStop() {
        super.onStop();
        hideKeyboard();
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view == null) {
            view = new View(this);
        }
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_action_save, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_save).setOnMenuItemClickListener(
                new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_save:
                            default:
                                hideKeyboard();
                                returnData();
                        }
                        return true;
                    }
                });
        menu.findItem(R.id.action_save).setOnMenuItemClickListener(mOnMenuItemClickListener);
        return super.onPrepareOptionsMenu(menu);
    }

    public boolean onSupportNavigateUp() {
        hideKeyboard();
        onBackPressed();
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            UCrop.Options options = new UCrop.Options();
            options.setShowCropGrid(false);
            File tmpFile = FileUtil.getTempFile(this);
            if (tmpFile != null) {
                UCrop.of(data.getData(), Uri.fromFile(tmpFile))
                        .withAspectRatio(1, 1)
                        .withOptions(options)
                        .start(this);
            } else {
                Toast.makeText(this, R.string.toast_internal_error, Toast.LENGTH_LONG).show();
            }

        } else if (requestCode == UCrop.REQUEST_CROP && resultCode == Activity.RESULT_OK && data != null) {
            final Uri resultUri = UCrop.getOutput(data);
            importBitmap(resultUri);
        }
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
    }

    private boolean isDataValid() {
        boolean isValid = true;
        String name = mEtName.getText().toString();
        String email = mEtEmail.getText().toString();
        String phone = mEtPhone.getText().toString();
        if (TextUtils.isEmpty(name)) {
            mEtName.setError(getString(R.string.et_error_empty));
            isValid = false;
        }
        if (TextUtils.isEmpty(email)) {
            mEtEmail.setError(getString(R.string.et_error_empty));
            isValid = false;
        } else if (!InputDataValidation.isEmailValid(email)) {
            mEtEmail.setError(getString(R.string.et_error_invalid_email));
            isValid = false;
        }
        if (TextUtils.isEmpty(phone)) {
            mEtPhone.setError(getString(R.string.et_error_empty));
            isValid = false;
        } else if (!InputDataValidation.isPhoneValid(phone)) {
            mEtPhone.setError(getString(R.string.et_error_invalid_phone));
            isValid = false;
        }
        return isValid;
    }

    private void returnData() {
        if (!isDataValid()) {
            return;
        }
        Intent data = new Intent();
        Uri vcfFile = VCardUtil.getVCardVcfFile(this, mVcard.getName(), mVcard.getPhone(), mVcard.getEmail(), mVcard.getImageFile());
        Vcard vcard = new Vcard(mVcard.getUuid(), mVcard.getName(), mVcard.getEmail(), mVcard.getPhone(), vcfFile, mVcard.getImageFile(), mVcard.getTimestamp());
        data.putExtra(EXTRA_RESULT_VCARD_UUID, vcard);
        setResult(Activity.RESULT_OK, data);
        finish();
    }

    private void showImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.title_activity_pick_image)), REQUEST_PICK_IMAGE);
    }

    private void applyImage(Uri image) {
        Glide.with(this)
                .load(image)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(R.drawable.ic_userpic)
                .into(mIvImage);
    }

    private void importBitmap(Uri data) {
        mVcard.setImageFile(data);
        applyImage(data);
    }


}
