package com.example.rodrigo.multipleimagepicker.activity;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ViewSwitcher;

import com.example.rodrigo.multipleimagepicker.Action;
import com.example.rodrigo.multipleimagepicker.CustomGallery;
import com.example.rodrigo.multipleimagepicker.adapter.GalleryAdapter;
import com.example.rodrigo.multipleimagepicker.R;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * http://geekonjava.blogspot.com.br/2015/10/easy-multiple-image-pick-android.html
 */
public class MainActivity extends Activity {

	//--------------------------------------------------
	// Constants
	//--------------------------------------------------

	private static final int SINGLE_PICTURE_OPTION = 100;
	private static final int MULTIPLE_PICTURE_OPTION = 200;
	private static final int REQUEST_CAMERA_OPTION = 300;

	//--------------------------------------------------
	// Attributes
	//--------------------------------------------------

	private Handler mHandler;

	private GridView mGridView;
	private GalleryAdapter mGalleryAdapter;
	private ImageView mSinglePickImageView;

	private Button mGalleryPickButton;
	private Button mGalleryPickMultipleButton;
	private Button mTakePictureButton;

	private ViewSwitcher mViewSwitcher;
	private ImageLoader mImageLoader;
	private Uri mImageUri;

	//--------------------------------------------------
	// Activity Life Cycle
	//--------------------------------------------------

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);

		initImageLoader();
		init();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == SINGLE_PICTURE_OPTION && resultCode == RESULT_OK) {
			mGalleryAdapter.clear();
			mViewSwitcher.setDisplayedChild(1);
			String singlePath = data.getStringExtra("single_path");
			mImageLoader.displayImage("file://" + singlePath, mSinglePickImageView);
		} else if (requestCode == MULTIPLE_PICTURE_OPTION && resultCode == RESULT_OK) {
			String[] all_path = data.getStringArrayExtra("all_path");
			ArrayList<CustomGallery> dataT = new ArrayList<>();
			for (String string : all_path) {
				CustomGallery item = new CustomGallery();
				item.sdcardPath = string;
				dataT.add(item);
			}
			mViewSwitcher.setDisplayedChild(0);
			mGalleryAdapter.addAll(dataT);
		} else if (requestCode == REQUEST_CAMERA_OPTION && resultCode == RESULT_OK) {
//			onCaptureImageResult(data);
			// Bitmap bitmap = (Bitmap)data.getExtras().get("data");
			// mSinglePickImageView.setImageBitmap(bitmap);
			try {
				Bitmap thumbnail = MediaStore.Images.Media.getBitmap(getContentResolver(), mImageUri);
				mSinglePickImageView.setImageBitmap(thumbnail);
				String imageUrl = getRealPathFromUri(mImageUri);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	//--------------------------------------------------
	// Methods
	//--------------------------------------------------

	private void initImageLoader() {
		DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
			.cacheOnDisc().imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
			.bitmapConfig(Bitmap.Config.RGB_565).build();
		ImageLoaderConfiguration.Builder builder = new ImageLoaderConfiguration.Builder(this)
			.defaultDisplayImageOptions(defaultOptions).memoryCache(
			new WeakMemoryCache());

		ImageLoaderConfiguration config = builder.build();
		mImageLoader = ImageLoader.getInstance();
		mImageLoader.init(config);
	}

	private void init() {
		mHandler = new Handler();
		mGridView = (GridView)findViewById(R.id.id_activity_main__grid_view);
		mGridView.setFastScrollEnabled(true);

		mGalleryAdapter = new GalleryAdapter(getApplicationContext(), mImageLoader);
		mGalleryAdapter.setMultiplePick(false);
		mGridView.setAdapter(mGalleryAdapter);

		mViewSwitcher = (ViewSwitcher)findViewById(R.id.id_activity_main__view_switcher);
		mViewSwitcher.setDisplayedChild(1);

		mSinglePickImageView = (ImageView)findViewById(R.id.id_activity_main__single_pick_image_view);

		mGalleryPickButton = (Button)findViewById(R.id.id_activity_main__pick_button);
		mGalleryPickButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(Action.ACTION_PICK);
				startActivityForResult(intent, SINGLE_PICTURE_OPTION);
			}
		});

		mGalleryPickMultipleButton = (Button)findViewById(R.id.id_activity_main__multiple_pick_button);
		mGalleryPickMultipleButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(Action.ACTION_MULTIPLE_PICK);
				startActivityForResult(intent, MULTIPLE_PICTURE_OPTION);
			}
		});

		mTakePictureButton = (Button)findViewById(R.id.id_activity_main__take_picture_button);
		mTakePictureButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
//				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//				startActivityForResult(intent, REQUEST_CAMERA_OPTION);
				ContentValues values = new ContentValues();
				values.put(MediaStore.Images.Media.TITLE, "New Picture");
				values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
				mImageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
				startActivityForResult(intent, REQUEST_CAMERA_OPTION);
			}
		});
	}

	private void onCaptureImageResult(Intent data) {
		Bitmap thumbnail = (Bitmap)data.getExtras().get("data");
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

		File destination = new File(Environment.getExternalStorageDirectory(), System.currentTimeMillis() + ".jpg");
		FileOutputStream fileOutputStream;
		try {
			destination.createNewFile();
			fileOutputStream = new FileOutputStream(destination);
			fileOutputStream.write(bytes.toByteArray());
			fileOutputStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		mSinglePickImageView.setImageBitmap(thumbnail);
	}

	@SuppressWarnings("deprecation")
	private String getRealPathFromUri(Uri contentUri) {
		String[] projection = { MediaStore.Images.Media.DATA };
		Cursor cursor = managedQuery(contentUri, projection, null, null, null);
		int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(columnIndex);
	}
}