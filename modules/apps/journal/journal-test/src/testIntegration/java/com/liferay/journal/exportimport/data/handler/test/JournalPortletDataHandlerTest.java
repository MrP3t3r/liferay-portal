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

package com.liferay.journal.exportimport.data.handler.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.dynamic.data.mapping.model.DDMStructure;
import com.liferay.dynamic.data.mapping.model.DDMTemplate;
import com.liferay.dynamic.data.mapping.test.util.DDMStructureTestUtil;
import com.liferay.dynamic.data.mapping.test.util.DDMTemplateTestUtil;
import com.liferay.exportimport.kernel.lar.PortletDataHandler;
import com.liferay.journal.constants.JournalPortletKeys;
import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.model.JournalFolder;
import com.liferay.journal.service.JournalFolderLocalServiceUtil;
import com.liferay.journal.test.util.JournalTestUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.portlet.PortalPreferences;
import com.liferay.portal.kernel.portlet.PortletPreferencesFactoryUtil;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.service.GroupLocalServiceUtil;
import com.liferay.portal.kernel.service.PortalPreferencesLocalServiceUtil;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.PortletKeys;
import com.liferay.portal.lar.test.BasePortletDataHandlerTestCase;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.util.test.LayoutTestUtil;

import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Zsolt Berentey
 */
@RunWith(Arquillian.class)
public class JournalPortletDataHandlerTest
	extends BasePortletDataHandlerTestCase {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();

		CompanyThreadLocal.setCompanyId(TestPropsValues.getCompanyId());

		PortalPreferences portalPreferenceces =
			PortletPreferencesFactoryUtil.getPortalPreferences(
				TestPropsValues.getUserId(), true);

		_originalPortalPreferencesXML = PortletPreferencesFactoryUtil.toXML(
			portalPreferenceces);

		portalPreferenceces.setValue(
			"", "publishToLiveByDefaultEnabled", "true");
		portalPreferenceces.setValue(
			"", "versionHistoryByDefaultEnabled", "true");
		portalPreferenceces.setValue("", "articleCommentsEnabled", "true");
		portalPreferenceces.setValue(
			"", "expireAllArticleVersionsEnabled", "true");
		portalPreferenceces.setValue("", "folderIconCheckCountEnabled", "true");
		portalPreferenceces.setValue(
			"", "indexAllArticleVersionsEnabled", "true");
		portalPreferenceces.setValue(
			"", "databaseContentKeywordSearchEnabled", "true");
		portalPreferenceces.setValue("", "journalArticleStorageType", "json");
		portalPreferenceces.setValue(
			"", "journalArticlePageBreakToken", "@page_break@");

		PortalPreferencesLocalServiceUtil.updatePreferences(
			TestPropsValues.getCompanyId(),
			PortletKeys.PREFS_OWNER_TYPE_COMPANY,
			PortletPreferencesFactoryUtil.toXML(portalPreferenceces));
	}

	@After
	public void tearDown() throws Exception {
		PortalPreferencesLocalServiceUtil.updatePreferences(
			TestPropsValues.getCompanyId(),
			PortletKeys.PREFS_OWNER_TYPE_COMPANY,
			_originalPortalPreferencesXML);
	}

	@Test
	public void testDeleteAllFolders() throws Exception {
		Group group = GroupTestUtil.addGroup();

		JournalFolder parentFolder = JournalTestUtil.addFolder(
			group.getGroupId(), "parent");

		JournalFolder childFolder = JournalTestUtil.addFolder(
			group.getGroupId(), parentFolder.getFolderId(), "child");

		JournalFolderLocalServiceUtil.moveFolderToTrash(
			TestPropsValues.getUserId(), childFolder.getFolderId());

		JournalFolderLocalServiceUtil.moveFolderToTrash(
			TestPropsValues.getUserId(), parentFolder.getFolderId());

		JournalFolderLocalServiceUtil.deleteFolder(
			parentFolder.getFolderId(), false);

		GroupLocalServiceUtil.deleteGroup(group);

		List<JournalFolder> folders = JournalFolderLocalServiceUtil.getFolders(
			group.getGroupId());

		Assert.assertEquals(folders.toString(), 0, folders.size());
	}

	@Override
	protected void addParameters(Map<String, String[]> parameterMap) {
		String namespace = _journalPortletDataHandler.getNamespace();

		addBooleanParameter(parameterMap, namespace, "feeds", true);
		addBooleanParameter(parameterMap, namespace, "structures", true);
		addBooleanParameter(parameterMap, namespace, "web-content", true);
	}

	@Override
	protected void addStagedModels() throws Exception {
		Layout layout = LayoutTestUtil.addLayout(stagingGroup);

		JournalFolder folder = JournalTestUtil.addFolder(
			stagingGroup.getGroupId(), RandomTestUtil.randomString());

		JournalTestUtil.addArticle(
			stagingGroup.getGroupId(), folder.getFolderId(),
			RandomTestUtil.randomString(), RandomTestUtil.randomString());

		DDMStructure ddmStructure = DDMStructureTestUtil.addStructure(
			stagingGroup.getGroupId(), JournalArticle.class.getName());

		DDMTemplateTestUtil.addTemplate(
			stagingGroup.getGroupId(),
			PortalUtil.getClassNameId(DDMStructure.class),
			PortalUtil.getClassNameId(JournalArticle.class));

		DDMTemplate ddmTemplate = DDMTemplateTestUtil.addTemplate(
			stagingGroup.getGroupId(), ddmStructure.getStructureId(),
			PortalUtil.getClassNameId(JournalArticle.class));

		DDMTemplate rendererDDMTemplate = DDMTemplateTestUtil.addTemplate(
			stagingGroup.getGroupId(), ddmStructure.getStructureId(),
			PortalUtil.getClassNameId(JournalArticle.class));

		JournalTestUtil.addFeed(
			stagingGroup.getGroupId(), layout.getPlid(),
			RandomTestUtil.randomString(), ddmStructure.getStructureKey(),
			ddmTemplate.getTemplateKey(), rendererDDMTemplate.getTemplateKey());
	}

	@Override
	protected String getPortletId() {
		return JournalPortletKeys.JOURNAL;
	}

	@Inject(filter = "javax.portlet.name=" + JournalPortletKeys.JOURNAL)
	private PortletDataHandler _journalPortletDataHandler;

	private String _originalPortalPreferencesXML;

}