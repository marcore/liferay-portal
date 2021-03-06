<%--
/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */
--%>

<%@ include file="/init.jsp" %>

<%
String redirect = ParamUtil.getString(request, "redirect");

String backURL = ParamUtil.getString(request, "backURL");

JournalArticle article = ActionUtil.getArticle(request);

long classNameId = BeanParamUtil.getLong(article, request, "classNameId");

String title = StringPool.BLANK;

if (classNameId > JournalArticleConstants.CLASSNAME_ID_DEFAULT) {
	title = LanguageUtil.get(request, "structure-default-values");
}
else if ((article != null) && !article.isNew()) {
	title = article.getTitle(locale);
}
else {
	title = LanguageUtil.get(request, "new-web-content");
}

portletDisplay.setShowBackIcon(true);
portletDisplay.setURLBack((article == null) ? redirect : backURL);

renderResponse.setTitle(title);
%>