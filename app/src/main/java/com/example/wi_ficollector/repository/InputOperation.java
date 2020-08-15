package com.example.wi_ficollector.repository;


import org.json.JSONArray;
import org.json.JSONException;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public interface InputOperation {

    JSONArray read() throws XmlPullParserException, IOException, JSONException;
}
