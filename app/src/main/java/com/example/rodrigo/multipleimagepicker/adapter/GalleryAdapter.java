package com.example.rodrigo.multipleimagepicker.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.example.rodrigo.multipleimagepicker.CustomGallery;
import com.example.rodrigo.multipleimagepicker.R;
import com.example.rodrigo.multipleimagepicker.activity.CustomGalleryActivity;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;

import java.util.ArrayList;

public class GalleryAdapter extends BaseAdapter {

	//--------------------------------------------------
	// Constants
	//--------------------------------------------------

	public static final Integer LIMIT = 10;

	//--------------------------------------------------
	// Attributes
	//--------------------------------------------------

	private Activity mContext;
	private LayoutInflater mLayoutInflater;
	private ArrayList<CustomGallery> mData = new ArrayList<>();
	private ImageLoader mImageLoader;

	private Boolean mIsActionMultiplePick;

	//--------------------------------------------------
	// Constructor
	//--------------------------------------------------

	public GalleryAdapter(Activity context, ImageLoader imageLoader) {
		mLayoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mContext = context;
		mImageLoader = imageLoader;
//		clearCache();
	}

	//--------------------------------------------------
	// Adapter Methods
	//--------------------------------------------------

	@Override
	public int getCount() {
		return mData.size();
	}

	@Override
	public CustomGallery getItem(int position) {
		return mData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		if (convertView == null) {
			convertView = mLayoutInflater.inflate(R.layout.adapter_gallery, null);
			holder = new ViewHolder();

			// Set layout.
			holder.mDisplayImageView = (ImageView) convertView.findViewById(R.id.id_adapter_gallery__display_image_view);
			holder.mSelectImageView = (ImageView) convertView.findViewById(R.id.id_adapter_gallery__select_image_view);

			// Set behavior.
			if (mIsActionMultiplePick) {
				holder.mSelectImageView.setVisibility(View.VISIBLE);
			} else {
				holder.mSelectImageView.setVisibility(View.GONE);
			}
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder)convertView.getTag();
		}

		holder.mDisplayImageView.setTag(position);
		try {
			mImageLoader.displayImage("file://" + mData.get(position).sdcardPath, holder.mDisplayImageView, new SimpleImageLoadingListener() {
				@Override
				public void onLoadingStarted(String imageUri, View view) {
					holder.mDisplayImageView.setImageResource(R.drawable.no_media);
					super.onLoadingStarted(imageUri, view);
				}
			});
			if (mIsActionMultiplePick) {
				holder.mSelectImageView.setSelected(mData.get(position).isSelected);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return convertView;
	}

	//--------------------------------------------------
	// Methods
	//--------------------------------------------------

	public void setMultiplePick(boolean isMultiplePick) {
		mIsActionMultiplePick = isMultiplePick;
	}

	public void selectAll(boolean selection) {
		for (int i = 0; i < mData.size(); i++) {
			mData.get(i).isSelected = selection;
		}
		notifyDataSetChanged();
	}

	public boolean isAllSelected() {
		boolean isAllSelected = true;
		for (int i = 0; i < mData.size(); i++) {
			if (!mData.get(i).isSelected) {
				isAllSelected = false;
				break;
			}
		}
		return isAllSelected;
	}

	public boolean isAnySelected() {
		boolean isAnySelected = false;
		for (int i = 0; i < mData.size(); i++) {
			if (mData.get(i).isSelected) {
				isAnySelected = true;
				break;
			}
		}
		return isAnySelected;
	}

	public ArrayList<CustomGallery> getSelected() {
		ArrayList<CustomGallery> dataT = new ArrayList<>();
		for (int i = 0; i < mData.size(); i++) {
			if (mData.get(i).isSelected) {
				dataT.add(mData.get(i));
			}
		}
		return dataT;
	}

	public void addAll(ArrayList<CustomGallery> files) {
		try {
			mData.clear();
			mData.addAll(files);
		} catch (Exception e) {
			e.printStackTrace();
		}
		notifyDataSetChanged();
	}

	public void changeSelection(View view, int position, Integer counter) {
		// Updates selection.
		if (mData.get(position).isSelected) {
			mData.get(position).isSelected = false;
			counter--;
		} else {
			if (counter < LIMIT) {
				counter++;
				mData.get(position).isSelected = true;
			}
		}
		((ViewHolder)view.getTag()).mSelectImageView.setSelected(mData.get(position).isSelected);

		// Checks counter.
		if (counter <= LIMIT) {
			((ViewHolder)view.getTag()).mSelectImageView.setBackgroundResource(R.drawable.on_focus_checkbox);
		} else {
			((ViewHolder)view.getTag()).mSelectImageView.setBackgroundResource(R.drawable.circle_drawable);
		}

		// Updates counter.
		CustomGalleryActivity activity = (CustomGalleryActivity)mContext;
		activity.updateCounter(counter);
	}

	//--------------------------------------------------
	// View Holder
	//--------------------------------------------------

	public class ViewHolder {
		ImageView mDisplayImageView;
		ImageView mSelectImageView;
	}

	public void clearCache() {
		mImageLoader.clearDiscCache();
		mImageLoader.clearMemoryCache();
	}

	public void clear() {
		mData.clear();
		notifyDataSetChanged();
	}
}