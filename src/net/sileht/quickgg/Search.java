/*
 SeeksWidget is the legal property of mehdi abaakouk <theli48@gmail.com>
 Copyright (c) 2010 Mehdi Abaakouk

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 3 of the License

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 */

package net.sileht.quickgg;

import java.net.URLEncoder;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

public class Search extends Activity {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();

		String query = intent.getStringExtra(SearchManager.QUERY);
		String queryAction = intent.getAction();

		if (Intent.ACTION_SEARCH.equals(queryAction)) {
			if (!query.startsWith("http")) {
				query = "https://www.google.com/q=" + URLEncoder.encode(query);
			}
			Uri uri = Uri.parse(query);
			startActivity(new Intent(Intent.ACTION_VIEW, uri));
			finish();
		} else {
			onSearchRequested();
			SearchManager sm = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
			sm.setOnDismissListener(new SearchManager.OnDismissListener() {
				@Override
				public void onDismiss() {
					finish();
				}
			});
		}
	}
}
