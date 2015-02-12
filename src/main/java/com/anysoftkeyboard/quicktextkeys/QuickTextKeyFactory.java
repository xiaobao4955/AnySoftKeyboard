/*
 * Copyright (c) 2015 Menny Even-Danan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.anysoftkeyboard.quicktextkeys;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.AttributeSet;

import com.anysoftkeyboard.addons.AddOnsFactory;
import com.menny.android.anysoftkeyboard.R;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

public class QuickTextKeyFactory extends AddOnsFactory<QuickTextKey> {

	private static final QuickTextKeyFactory msInstance;

	static {
		msInstance = new QuickTextKeyFactory();
	}

	private static final String XML_POPUP_KEYBOARD_RES_ID_ATTRIBUTE = "popupKeyboard";
	private static final String XML_POPUP_LIST_TEXT_RES_ID_ATTRIBUTE = "popupListText";
	private static final String XML_POPUP_LIST_OUTPUT_RES_ID_ATTRIBUTE = "popupListOutput";
	private static final String XML_POPUP_LIST_ICONS_RES_ID_ATTRIBUTE = "popupListIcons";
	private static final String XML_ICON_RES_ID_ATTRIBUTE = "keyIcon";
	private static final String XML_KEY_LABEL_RES_ID_ATTRIBUTE = "keyLabel";
	private static final String XML_KEY_OUTPUT_TEXT_RES_ID_ATTRIBUTE = "keyOutputText";
	private static final String XML_ICON_PREVIEW_RES_ID_ATTRIBUTE = "iconPreview";

	private QuickTextKeyFactory() {
		super("ASK_QKF", "com.anysoftkeyboard.plugin.QUICK_TEXT_KEY",
				"com.anysoftkeyboard.plugindata.quicktextkeys",
				"QuickTextKeys", "QuickTextKey", R.xml.quick_text_keys, true);
	}

	public static QuickTextKey getCurrentQuickTextKey(Context context) {
		return getAllEnabledQuickKeys(context).get(0);
	}

	public static ArrayList<QuickTextKey> getAllAvailableQuickKeys(Context applicationContext) {
		return msInstance.getAllAddOns(applicationContext);
	}

	public static ArrayList<QuickTextKey> getAllEnabledQuickKeys(Context applicationContext) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			return getAllEnabledQuickKeysOrdered(applicationContext);
		} else {
			ArrayList<QuickTextKey> allKeys = getAllAvailableQuickKeys(applicationContext);
			QuickTextKey activeKey = getEnabledQuickKeyForLegacy(applicationContext);
			allKeys.remove(activeKey);
			allKeys.add(0, activeKey);
			return allKeys;
		}
	}

	private static QuickTextKey getEnabledQuickKeyForLegacy(Context applicationContext) {
		ArrayList<QuickTextKey> quickTextKeys = msInstance.getAllAddOns(applicationContext);

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
		String settingKey = applicationContext.getString(R.string.settings_key_active_quick_text_key);

		String activeQuickKey = sharedPreferences.getString(settingKey, quickTextKeys.get(0).getId());

		for (QuickTextKey key : quickTextKeys) {
			if (key.getId().equals(activeQuickKey)) return key;
		}

		return quickTextKeys.get(0);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private static ArrayList<QuickTextKey> getAllEnabledQuickKeysOrdered(Context applicationContext) {
		ArrayList<QuickTextKey> quickTextKeys = msInstance.getAllAddOns(applicationContext);

		//now, reading the ordered array of active keys
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
		String settingKey = applicationContext.getString(R.string.settings_key_ordered_active_quick_text_keys);

		Set<String> defaultOrder = new LinkedHashSet<>(quickTextKeys.size());
		for (QuickTextKey key : quickTextKeys) defaultOrder.add(key.getId());

		Set<String> quickKeyIdsOrder = sharedPreferences.getStringSet(settingKey, defaultOrder);

		ArrayList<QuickTextKey> orderedQuickTextKeys = new ArrayList<>(quickKeyIdsOrder.size());
		for (String keyId : quickKeyIdsOrder) {
			Iterator<QuickTextKey> iterator = quickTextKeys.iterator();
			while (iterator.hasNext()) {
				QuickTextKey nextQuickKey = iterator.next();
				if (nextQuickKey.getId().equals(keyId)) {
					orderedQuickTextKeys.add(nextQuickKey);
					iterator.remove();
					break;
				}
			}
		}

		//forcing at least one key
		if (orderedQuickTextKeys.size() == 0) orderedQuickTextKeys.add(quickTextKeys.get(0));

		return orderedQuickTextKeys;
	}

	@Override
	protected QuickTextKey createConcreteAddOn(Context askContext, Context context, String prefId, int nameResId, String description, int sortIndex, AttributeSet attrs) {
		final int popupKeyboardResId = attrs.getAttributeResourceValue(null,
				XML_POPUP_KEYBOARD_RES_ID_ATTRIBUTE, -1);
		final int popupListTextResId = attrs.getAttributeResourceValue(null,
				XML_POPUP_LIST_TEXT_RES_ID_ATTRIBUTE, -1);
		final int popupListOutputResId = attrs.getAttributeResourceValue(null,
				XML_POPUP_LIST_OUTPUT_RES_ID_ATTRIBUTE, -1);
		final int popupListIconsResId = attrs.getAttributeResourceValue(null,
				XML_POPUP_LIST_ICONS_RES_ID_ATTRIBUTE, -1);
		final int iconResId = attrs.getAttributeResourceValue(null,
				XML_ICON_RES_ID_ATTRIBUTE, -1); // Maybe should make a default
		// icon
		final int keyLabelResId = attrs.getAttributeResourceValue(null,
				XML_KEY_LABEL_RES_ID_ATTRIBUTE, -1);
		final int keyOutputTextResId = attrs.getAttributeResourceValue(null,
				XML_KEY_OUTPUT_TEXT_RES_ID_ATTRIBUTE, -1);
		final int keyIconPreviewResId = attrs.getAttributeResourceValue(null,
				XML_ICON_PREVIEW_RES_ID_ATTRIBUTE, -1);

		if (((popupKeyboardResId == -1) && ((popupListTextResId == -1) || (popupListOutputResId == -1)))
				|| ((iconResId == -1) && (keyLabelResId == -1))
				|| (keyOutputTextResId == -1)) {
			String detailMessage = String
					.format("Missing details for creating QuickTextKey! prefId %s\n"
									+ "popupKeyboardResId: %d, popupListTextResId: %d, popupListOutputResId: %d, (iconResId: %d, keyLabelResId: %d), keyOutputTextResId: %d",
							prefId, popupKeyboardResId, popupListTextResId,
							popupListOutputResId, iconResId, keyLabelResId,
							keyOutputTextResId);

			throw new RuntimeException(detailMessage);
		}
		return new QuickTextKey(askContext, context, prefId, nameResId, popupKeyboardResId,
				popupListTextResId, popupListOutputResId, popupListIconsResId,
				iconResId, keyLabelResId, keyOutputTextResId,
				keyIconPreviewResId, description, sortIndex);
	}
}
