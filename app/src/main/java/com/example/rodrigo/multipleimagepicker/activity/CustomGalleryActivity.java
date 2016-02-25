package com.example.rodrigo.multipleimagepicker.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;

import com.example.rodrigo.multipleimagepicker.Action;
import com.example.rodrigo.multipleimagepicker.CustomGallery;
import com.example.rodrigo.multipleimagepicker.adapter.GalleryAdapter;
import com.example.rodrigo.multipleimagepicker.R;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.PauseOnScrollListener;
import com.nostra13.universalimageloader.utils.StorageUtils;

public class CustomGalleryActivity extends Activity {

	//--------------------------------------------------
	// Attributes
	//--------------------------------------------------

	private GridView mGridView;
	private Handler mHandler;
	private GalleryAdapter mGalleryAdapter;

	private ImageView mNoMediaImageView;
	private Button mGalleryOkButton;

	private String mAction;
	private ImageLoader mImageLoader;

	//--------------------------------------------------
	// Activity Life Cycle
	//--------------------------------------------------

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_custom_gallery);

		mAction = getIntent().getAction();
		if (mAction == null) {
			finish();
		}
		setImageLoader();
		setLayout();
	}

	//--------------------------------------------------
	// Methods
	//--------------------------------------------------

	private void setImageLoader() {
		try {
			String CACHE_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.temp_tmp";
			new File(CACHE_DIR).mkdirs();
			File cacheDir = StorageUtils.getOwnCacheDirectory(getBaseContext(),CACHE_DIR);

			DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
				.cacheOnDisc(true).imageScaleType(ImageScaleType.EXACTLY)
				.bitmapConfig(Bitmap.Config.RGB_565).build();
			ImageLoaderConfiguration.Builder builder = new ImageLoaderConfiguration.Builder(getBaseContext())
				.defaultDisplayImageOptions(defaultOptions)
				.discCache(new UnlimitedDiscCache(cacheDir))
				.memoryCache(new WeakMemoryCache());

			ImageLoaderConfiguration config = builder.build();
			mImageLoader = ImageLoader.getInstance();
			mImageLoader.init(config);
		} catch (Exception e) {}
	}

	private void setLayout() {
		mHandler = new Handler();

		mGridView = (GridView) findViewById(R.id.id_activity_custom_gallery__grid_view);
		mGridView.setFastScrollEnabled(true);

		mGalleryAdapter = new GalleryAdapter(getApplicationContext(), mImageLoader);
		PauseOnScrollListener listener = new PauseOnScrollListener(mImageLoader, true, true);
		mGridView.setOnScrollListener(listener);

		if (mAction.equalsIgnoreCase(Action.ACTION_MULTIPLE_PICK)) {
			findViewById(R.id.id_activity_custom_gallery__bottom_container_linear_layout).setVisibility(View.VISIBLE);
			mGridView.setOnItemClickListener(mItemMulClickListener);
			mGalleryAdapter.setMultiplePick(true);
		} else if (mAction.equalsIgnoreCase(Action.ACTION_PICK)) {
			findViewById(R.id.id_activity_custom_gallery__bottom_container_linear_layout).setVisibility(View.GONE);
			mGridView.setOnItemClickListener(mItemSingleClickListener);
			mGalleryAdapter.setMultiplePick(false);
		}
		mGridView.setAdapter(mGalleryAdapter);
		mNoMediaImageView = (ImageView) findViewById(R.id.id_activity_custom_gallery__no_media_image_view);

		mGalleryOkButton = (Button)findViewById(R.id.id_activity_custom_gallery__ok_button);
		mGalleryOkButton.setOnClickListener(mOkClickListener);
		new Thread() {
			@Override
			public void run() {
				Looper.prepare();
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						mGalleryAdapter.addAll(getGalleryPhotos());
						checkImageStatus();
					}
				});
				Looper.loop();
			}
		}.start();
	}

	private void checkImageStatus() {
		if (mGalleryAdapter.isEmpty()) {
			mNoMediaImageView.setVisibility(View.VISIBLE);
		} else {
			mNoMediaImageView.setVisibility(View.GONE);
		}
	}

	private ArrayList<CustomGallery> getGalleryPhotos() {
		ArrayList<CustomGallery> galleryList = new ArrayList<>();

		try {
			final String[] columns = { MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID };
			final String orderBy = MediaStore.Images.Media._ID;

			Cursor imagecursor = managedQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null, null, orderBy);
			if (imagecursor != null && imagecursor.getCount() > 0) {
				while (imagecursor.moveToNext()) {
					CustomGallery item = new CustomGallery();
					int dataColumnIndex = imagecursor.getColumnIndex(MediaStore.Images.Media.DATA);
					item.sdcardPath = imagecursor.getString(dataColumnIndex);
					galleryList.add(item);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Show newest photo at beginning of the list.
		Collections.reverse(galleryList);
		return galleryList;
	}

	//--------------------------------------------------
	// Listeners
	//--------------------------------------------------

	private View.OnClickListener mOkClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			ArrayList<CustomGallery> selected = mGalleryAdapter.getSelected();
			String[] allPath = new String[selected.size()];
			for (int i = 0; i < allPath.length; i++) {
				allPath[i] = selected.get(i).sdcardPath;
			}

			Intent data = new Intent().putExtra("all_path", allPath);
			setResult(RESULT_OK, data);
			finish();
		}
	};

	private AdapterView.OnItemClickListener mItemMulClickListener = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> l, View v, int position, long id) {
			mGalleryAdapter.changeSelection(v, position);
		}
	};

	private AdapterView.OnItemClickListener mItemSingleClickListener = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> l, View v, int position, long id) {
			CustomGallery item = mGalleryAdapter.getItem(position);
			Intent data = new Intent().putExtra("single_path", item.sdcardPath);
			setResult(RESULT_OK, data);
			finish();
		}
	};
}