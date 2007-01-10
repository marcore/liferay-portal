/**
 * Copyright (c) 2000-2006 Liferay, Inc. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.liferay.portlet;

import com.liferay.portal.language.LanguageException;
import com.liferay.portal.language.LanguageUtil;
import com.liferay.portal.util.WebKeys;

import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * <a href="StrutsResourceBundle.java.html"><b><i>View Source</i></b></a>
 *
 * @author  Brian Wing Shun Chan
 *
 */
public class StrutsResourceBundle extends ResourceBundle {

	public static ResourceBundle getBundle(
		String portletName, String companyId, Locale locale) {

		return new StrutsResourceBundle(portletName, companyId, locale);
	}

	public StrutsResourceBundle(String portletName, String companyId,
								Locale locale) {

		_portletName = portletName;
		_companyId = companyId;
		_locale = locale;
	}

	public Enumeration getKeys() {
		return null;
	}

	public Locale getLocale() {
		return _locale;
	}

	protected Object handleGetObject(String key) {
		Object obj = null;

		if (key != null &&
			(key.equals(WebKeys.JAVAX_PORTLET_TITLE) ||
			 key.equals(WebKeys.JAVAX_PORTLET_SHORT_TITLE) ||
			 key.equals(WebKeys.JAVAX_PORTLET_KEYWORDS))) {

			key += "." + _portletName;
		}

		try {
			obj = LanguageUtil.get(_companyId, _locale, key);
		}
		catch (LanguageException le) {
		}

		return obj;
	}

	private String _portletName;
	private String _companyId;
	private Locale _locale;

}