package com.tangentlu.whereisputian;

import com.amap.api.maps2d.model.LatLng;
import com.amap.api.services.core.LatLonPoint;

/**
 * Created by Administrator on 2016/5/5 0005.
 */
public class AMapUtil {
    /**
     * 把LatLonPoint对象转化为LatLon对象
     * @param latLonPoint
     * @return
     */
    public static LatLng convertToLatLng(LatLonPoint latLonPoint) {
        return new LatLng(latLonPoint.getLatitude(), latLonPoint.getLongitude());
    }
}
