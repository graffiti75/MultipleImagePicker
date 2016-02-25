package com.example.rodrigo.multipleimagepicker.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
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

import java.util.ArrayList;

/**
 * http://geekonjava.blogspot.com.br/2015/10/easy-multiple-image-pick-android.html
 */
public class MainActivity extends Activity {

	//--------------------------------------------------
	// Attributes
	//--------------------------------------------------

	private GridView mGridView;
	private Handler mHandler;
	private GalleryAdapter mGalleryAdapter;

	private ImageView mSinglePickImageView;
	private Button mGalleryPickButton;
	private Button mGalleryPickMultipleButton;

	private ViewSwitcher mViewSwitcher;
	private ImageLoader mImageLoader;

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
		if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
			mGalleryAdapter.clear();
			mViewSwitcher.setDisplayedChild(1);
			String singlePath = data.getStringExtra("single_path");
			mImageLoader.displayImage("file://" + singlePath, mSinglePickImageView);
		} else if (requestCode == 200 && resultCode == Activity.RESULT_OK) {
			String[] all_path = data.getStringArrayExtra("all_path");
			ArrayList<CustomGallery> dataT = new ArrayList<>();
			for (String string : all_path) {
				CustomGallery item = new CustomGallery();
				item.sdcardPath = string;
				dataT.add(item);
			}
			mViewSwitcher.setDisplayedChild(0);
			mGalleryAdapter.addAll(dataT);
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

		mViewSwitcher = (ViewSwitcher) findViewById(R.id.id_activity_main__view_switcher);
		mViewSwitcher.setDisplayedChild(1);

		mSinglePickImageView = (ImageView) findViewById(R.id.id_activity_main__single_pick_image_view);

		mGalleryPickButton = (Button) findViewById(R.id.id_activity_main__pick_button);
		mGalleryPickButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(Action.ACTION_PICK);
				startActivityForResult(intent, 100);
			}
		});

		mGalleryPickMultipleButton = (Button) findViewById(R.id.id_activity_main__multiple_pick_button);
		mGalleryPickMultipleButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(Action.ACTION_MULTIPLE_PICK);
				startActivityForResult(intent, 200);
			}
		});
	}
}