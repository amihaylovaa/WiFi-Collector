package com.example.wi_ficollector.repository;

import android.content.Context;
import android.util.Log;


import com.example.wi_ficollector.wrapper.WifiLocation;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import static android.content.Context.MODE_APPEND;
import static com.example.wi_ficollector.utils.Constants.ENCODING;
import static com.example.wi_ficollector.utils.Constants.FILE_NAME;
import static com.example.wi_ficollector.utils.Constants.FILE_NOT_FOUND_EXCEPTION_MSG;
import static com.example.wi_ficollector.utils.Constants.FILE_NOT_FOUND_EXCEPTION_TAG;
import static com.example.wi_ficollector.utils.Constants.TRACK_POINT_TAG;
import static com.example.wi_ficollector.utils.Constants.TRACK_TAG;

public class WifiLocationInput implements InputOperation {

    private FileInputStream mFileInputStream;
    private Context mContext;
    private Executor mExecutor;
    XmlPullParser xpp;
    List<WifiLocation> mWifiLocations;

    public WifiLocationInput(Context mContext) {
        this.mContext = mContext;
        mWifiLocations = new ArrayList<>();
        openFileOutputStream();
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            xpp = factory.newPullParser();
        } catch (XmlPullParserException e) {
            ;
        }
    }

    @Override
    public void read() {
        try {
            xpp.setInput(mFileInputStream, ENCODING);
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tagName = xpp.getName();
                if (tagName != null) {
                    if (xpp.getEventType() == XmlPullParser.START_TAG && tagName.equals(TRACK_POINT_TAG)) {
                        String latitude = xpp.getAttributeValue(null, "lat");
                        String longitude = xpp.getAttributeValue(null, "lon");
                    }
                }
                eventType = xpp.next();
            }
        } catch (XmlPullParserException | IOException e) {
        }
    }

    public void openFileOutputStream() {
        try {
            mFileInputStream = mContext.openFileInput(FILE_NAME);
        } catch (FileNotFoundException exception) {
            Log.d(FILE_NOT_FOUND_EXCEPTION_TAG, FILE_NOT_FOUND_EXCEPTION_MSG);
        }
    }

}
