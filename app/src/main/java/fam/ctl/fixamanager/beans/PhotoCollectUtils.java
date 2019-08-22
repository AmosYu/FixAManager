package fam.ctl.fixamanager.beans;

import android.annotation.SuppressLint;

import java.util.HashMap;
////存储数据
// PhotoCollectUtils.getInstance().saveHashMapData(0, bytes);
//
////获取存储的数据
//private HashMap<Integer, byte[]> hashMap;
//        hashMap = PhotoCollectUtils.getInstance( ).getHashMapData( );
//        byte[] byteData = hashMap.get(0)；
public class PhotoCollectUtils {
    public static PhotoCollectUtils mPhotoCollectUtils;

    private HashMap<Integer, byte[]> hashMapData = null;

    public static PhotoCollectUtils getInstance() {
        if (mPhotoCollectUtils == null) {
            mPhotoCollectUtils = new PhotoCollectUtils();
        }
        return mPhotoCollectUtils;
    }

    @SuppressLint("UseSparseArrays")
    public PhotoCollectUtils() {

        hashMapData = new HashMap<Integer, byte[]>();
    }

    public void saveHashMapData(int num, byte[] dataList) {
        if (dataList != null) {
            hashMapData.put(num, dataList);
        }
    }

    public HashMap<Integer, byte[]> getHashMapData() {
        return hashMapData;
    }

    public void reSetHashMap() {
        if (hashMapData != null) {
            hashMapData.clear();
        }
    }

}
