/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sileht.quickgg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * Provides access to the dictionary database.
 */
public class SuggestionProvider extends ContentProvider {
	String TAG = "SeeksSuggestionProvider";

	public static String AUTHORITY = "net.sileht.quickgg.SuggestionProvider";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/query");

	public static final String KEY_TITLE = SearchManager.SUGGEST_COLUMN_TEXT_1;
	public static final String KEY_DESCRIPTION = SearchManager.SUGGEST_COLUMN_TEXT_2;
	public static final String KEY_QUERY = SearchManager.SUGGEST_COLUMN_QUERY;

	public static final String mKey = "	AIzaSyBjAXL61Oj6_XF5saDiXVLZxp2lSuzmBhY";

	@Override
	public boolean onCreate() {
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {

		String query = uri.getLastPathSegment();

		if (query == null || query.equals("")
				|| query.equals("search_suggest_query"))
			return null;

		Log.v(TAG, "Request '" + query + "' for '" + uri + "'");

		try {
			return getCursorForQuery(query);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	public Cursor getCursorForQuery(String query) throws MalformedURLException,
			IOException {

		String queryurl = "http://ajax.googleapis.com/ajax/services/search/web?v=1.0&rsz=8&q="
				+ URLEncoder.encode(query);

		Log.v(TAG, "Query:" + queryurl);

		String json = null;

		while (json == null) {
			HttpURLConnection connection = null;
			connection = (HttpURLConnection) (new URL(queryurl))
					.openConnection();

			try {
				connection.setDoOutput(true);
				connection.setChunkedStreamingMode(0);
				connection.setInstanceFollowRedirects(true);

				connection.connect();

				InputStream in = connection.getInputStream();

				BufferedReader r = new BufferedReader(new InputStreamReader(in));
				StringBuilder builder = new StringBuilder();

				String line;
				while ((line = r.readLine()) != null) {
					builder.append(line);
				}

				json = builder.toString();
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			} finally {
				connection.disconnect();
			}
		}

		JSONArray snippets;
		JSONObject object;
		Log.v(TAG, "JSON: " + json);

		try {
			object = (JSONObject) new JSONTokener(json).nextValue();
			snippets = object.getJSONObject("responseData").getJSONArray(
					"results");
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}

		Log.v(TAG, "Snippets found: " + snippets.length());

		String title;
		String content;
		String url;
		JSONObject snippet;

		MatrixCursor matrix = new MatrixCursor(new String[] { BaseColumns._ID,
				KEY_TITLE, KEY_DESCRIPTION, KEY_QUERY });

		for (int i = 0; i < snippets.length(); i++) {
			try {
				snippet = snippets.getJSONObject(i);
				title = snippet.getString("titleNoFormatting");
				content = snippet.getString("content");
				url = snippet.getString("url");
				matrix.newRow().add(i).add(title).add(content).add(url);

			} catch (JSONException e) {
				e.printStackTrace();
				continue;
			}
		}

		return matrix;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

}