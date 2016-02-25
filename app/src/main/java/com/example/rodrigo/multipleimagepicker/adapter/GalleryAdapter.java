package com.example.rodrigo.multipleimagepicker.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.example.rodrigo.multipleimagepicker.CustomGallery;
import com.example.rodrigo.multipleimagepicker.R;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;

import java.util.ArrayList;

public class GalleryAdapter extends BaseAdapter {

	//--------------------------------------------------
	// Attributes
	//--------------------------------------------------

	private Context mContext;
	private LayoutInflater mLayoutInflater;
	private ArrayList<CustomGallery> mData = new ArrayList<>();
	private ImageLoader mImageLoader;
	private Boolean mIsActionMultiplePick;

	//--------------------------------------------------
	// Constructor
	//--------------------------------------------------

	public GalleryAdapter(Context context, ImageLoader imageLoader) {
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
			holder.mImageView = (ImageView) convertView.findViewById(R.id.id_adapter_gallery__image_view);
			holder.mMultiSelectedImageView = (ImageView) convertView.findViewById(R.id.id_adapter_gallery__multi_selected_image_view);
			if (mIsActionMultiplePick) {
				holder.mMultiSelectedImageView.setVisibility(View.VISIBLE);
			} else {
				holder.mMultiSelectedImageView.setVisibility(View.GONE);
			}
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.mImageView.setTag(position);
		try {
			mImageLoader.displayImage("file://" + mData.get(position).sdcardPath, holder.mImageView, new SimpleImageLoadingListener() {
				@Override
				public void onLoadingStarted(String imageUri, View view) {
					holder.mImageView.setImageResource(R.drawable.no_media);
					super.onLoadingStarted(imageUri, view);
				}
			});
			if (mIsActionMultiplePick) {
				holder.mMultiSelectedImageView.setSelected(mData.get(position).isSelected);
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

	public void changeSelection(View view, int position) {
		if (mData.get(position).isSelected) {
			mData.get(position).isSelected = false;
		} else {
			mData.get(position).isSelected = true;
		}
		((ViewHolder)view.getTag()).mMultiSelectedImageView.setSelected(mData.get(position).isSelected);
	}

	//--------------------------------------------------
	// View Holder
	//--------------------------------------------------

	public class ViewHolder {
		ImageView mImageView;
		ImageView mMultiSelectedImageView;
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