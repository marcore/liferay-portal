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

package com.liferay.portlet.journal.action;

import com.liferay.portal.model.Layout;
import com.liferay.portal.security.auth.PrincipalException;
import com.liferay.portal.struts.PortletAction;
import com.liferay.portal.util.Constants;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portal.util.WebKeys;
import com.liferay.portlet.journal.DuplicateTemplateIdException;
import com.liferay.portlet.journal.NoSuchTemplateException;
import com.liferay.portlet.journal.RequiredTemplateException;
import com.liferay.portlet.journal.TemplateDescriptionException;
import com.liferay.portlet.journal.TemplateIdException;
import com.liferay.portlet.journal.TemplateNameException;
import com.liferay.portlet.journal.TemplateSmallImageNameException;
import com.liferay.portlet.journal.TemplateSmallImageSizeException;
import com.liferay.portlet.journal.TemplateXslException;
import com.liferay.portlet.journal.model.JournalTemplate;
import com.liferay.portlet.journal.model.impl.JournalTemplateImpl;
import com.liferay.portlet.journal.service.JournalTemplateServiceUtil;
import com.liferay.portlet.journal.util.JournalUtil;
import com.liferay.util.JS;
import com.liferay.util.ParamUtil;
import com.liferay.util.StringUtil;
import com.liferay.util.Validator;
import com.liferay.util.servlet.SessionErrors;
import com.liferay.util.servlet.UploadPortletRequest;

import java.io.File;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

/**
 * <a href="EditTemplateAction.java.html"><b><i>View Source</i></b></a>
 *
 * @author  Brian Wing Shun Chan
 *
 */
public class EditTemplateAction extends PortletAction {

	public void processAction(
			ActionMapping mapping, ActionForm form, PortletConfig config,
			ActionRequest req, ActionResponse res)
		throws Exception {

		String cmd = ParamUtil.getString(req, Constants.CMD);

		try {
			if (cmd.equals(Constants.ADD) || cmd.equals(Constants.UPDATE)) {
				updateTemplate(req);
			}
			else if (cmd.equals(Constants.DELETE)) {
				deleteTemplates(req);
			}

			sendRedirect(req, res);
		}
		catch (Exception e) {
			if (e instanceof NoSuchTemplateException ||
				e instanceof PrincipalException) {

				SessionErrors.add(req, e.getClass().getName());

				setForward(req, "portlet.journal.error");
			}
			else if (e instanceof DuplicateTemplateIdException ||
					 e instanceof RequiredTemplateException ||
					 e instanceof TemplateDescriptionException ||
					 e instanceof TemplateIdException ||
					 e instanceof TemplateNameException ||
					 e instanceof TemplateSmallImageNameException ||
					 e instanceof TemplateSmallImageSizeException ||
					 e instanceof TemplateXslException) {

				SessionErrors.add(req, e.getClass().getName());

				if (e instanceof RequiredTemplateException) {
					res.sendRedirect(ParamUtil.getString(req, "redirect"));
				}
			}
			else {
				throw e;
			}
		}
	}

	public ActionForward render(
			ActionMapping mapping, ActionForm form, PortletConfig config,
			RenderRequest req, RenderResponse res)
		throws Exception {

		try {
			String cmd = ParamUtil.getString(req, Constants.CMD);

			if (!cmd.equals(Constants.ADD)) {
				ActionUtil.getTemplate(req);
			}
		}
		catch (NoSuchTemplateException nsse) {

			// Let this slide because the user can manually input a template id
			// for a new template that does not yet exist.

		}
		catch (Exception e) {
			if (//e instanceof NoSuchTemplateException ||
				e instanceof PrincipalException) {

				SessionErrors.add(req, e.getClass().getName());

				return mapping.findForward("portlet.journal.error");
			}
			else {
				throw e;
			}
		}

		return mapping.findForward(
			getForward(req, "portlet.journal.edit_template"));
	}

	protected void deleteTemplates(ActionRequest req) throws Exception {
		String companyId = PortalUtil.getCompanyId(req);
		long groupId = ParamUtil.getLong(req, "groupId");

		String[] deleteTemplateIds = StringUtil.split(
			ParamUtil.getString(req, "deleteTemplateIds"));

		for (int i = 0; i < deleteTemplateIds.length; i++) {
			JournalTemplateServiceUtil.deleteTemplate(
				companyId, groupId, deleteTemplateIds[i]);

			JournalUtil.removeRecentTemplate(req, deleteTemplateIds[i]);
		}
	}

	protected void updateTemplate(ActionRequest req) throws Exception {
		UploadPortletRequest uploadReq =
			PortalUtil.getUploadPortletRequest(req);

		String cmd = ParamUtil.getString(uploadReq, Constants.CMD);

		Layout layout = (Layout)uploadReq.getAttribute(WebKeys.LAYOUT);

		long groupId = ParamUtil.getLong(req, "groupId");

		String templateId = ParamUtil.getString(uploadReq, "templateId");
		boolean autoTemplateId = ParamUtil.getBoolean(
			uploadReq, "autoTemplateId");

		String structureId = ParamUtil.getString(uploadReq, "structureId");
		String name = ParamUtil.getString(uploadReq, "name");
		String description = ParamUtil.getString(uploadReq, "description");

		String xsl = ParamUtil.getString(uploadReq, "xsl");
		String xslContent = JS.decodeURIComponent(
			ParamUtil.getString(uploadReq, "xslContent"));
		boolean formatXsl = ParamUtil.getBoolean(uploadReq, "formatXsl");

		if (Validator.isNull(xsl)) {
			xsl = xslContent;
		}

		String langType = ParamUtil.getString(
			uploadReq, "langType", JournalTemplateImpl.LANG_TYPE_XSL);

		boolean smallImage = ParamUtil.getBoolean(uploadReq, "smallImage");
		String smallImageURL = ParamUtil.getString(uploadReq, "smallImageURL");
		File smallFile = uploadReq.getFile("smallFile");

		String[] communityPermissions = req.getParameterValues(
			"communityPermissions");
		String[] guestPermissions = req.getParameterValues(
			"guestPermissions");

		JournalTemplate template = null;

		if (cmd.equals(Constants.ADD)) {

			// Add template

			template = JournalTemplateServiceUtil.addTemplate(
				templateId, autoTemplateId, layout.getPlid(), structureId, name,
				description, xsl, formatXsl, langType, smallImage,
				smallImageURL, smallFile, communityPermissions,
				guestPermissions);
		}
		else {

			// Update template

			template = JournalTemplateServiceUtil.updateTemplate(
				groupId, templateId, structureId, name, description, xsl,
				formatXsl, langType, smallImage, smallImageURL, smallFile);
		}

		// Recent templates

		JournalUtil.addRecentTemplate(req, template);
	}

}