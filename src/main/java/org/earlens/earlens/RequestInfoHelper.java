package org.earlens.earlens;

import android.location.Address;
import android.location.Location;

import com.hound.core.model.sdk.HoundRequestInfo;

/**
 * Helpers with setting request info attributes.
 */
public class RequestInfoHelper {


    public static void setLocation(final HoundRequestInfo requestInfo, final Location location) {
        if (location != null) {
            requestInfo.setLatitude(location.getLatitude());
            requestInfo.setLongitude(location.getLongitude());
            requestInfo.setPositionHorizontalAccuracy((double) location.getAccuracy());
        }
    }

    public static void setAddress(final HoundRequestInfo requestInfo, final Address address) {
        if (address != null) {
            requestInfo.setStreet(address.getThoroughfare());
            requestInfo.setState(address.getAdminArea());
            requestInfo.setCity(address.getLocality());
            requestInfo.setCountry(address.getCountryCode());
        }
    }
}