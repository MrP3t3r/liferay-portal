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

package com.liferay.change.tracking.service.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.change.tracking.CTEngineManager;
import com.liferay.change.tracking.CTManager;
import com.liferay.change.tracking.configuration.CTConfiguration;
import com.liferay.change.tracking.configuration.CTConfigurationRegistrar;
import com.liferay.change.tracking.configuration.builder.CTConfigurationBuilder;
import com.liferay.change.tracking.model.CTCollection;
import com.liferay.change.tracking.model.CTEntry;
import com.liferay.change.tracking.service.CTEntryLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.ClassName;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.ClassNameLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.service.test.ServiceTestUtil;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import java.util.List;
import java.util.Optional;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Daniel Kocsis
 */
@RunWith(Arquillian.class)
public class CTManagerTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Before
	public void setUp() throws Exception {
		ServiceTestUtil.setUser(TestPropsValues.getUser());

		_user = UserTestUtil.addUser();

		_ctEngineManager.enableChangeTracking(
			TestPropsValues.getCompanyId(), TestPropsValues.getUserId());

		_testVersionClassClassName = _classNameLocalService.addClassName(
			Object.class.getName());

		_ctConfiguration = _ctConfigurationBuilder.setEntityClasses(
			Object.class, Object.class
		).setResourceEntityByResourceEntityIdFunction(
			id -> new Object()
		).setEntityIdsFromResourceEntityFunctions(
			testResource -> _TEST_RESOURCE_CLASS_ENTITY_ID,
			testResource -> _TEST_VERSION_CLASS_ENTITY_ID
		).setVersionEntityByVersionEntityIdFunction(
			id -> new Object()
		).setEntityIdsFromVersionEntityFunctions(
			testVersion -> _TEST_RESOURCE_CLASS_ENTITY_ID,
			testVersion -> _TEST_VERSION_CLASS_ENTITY_ID
		).setVersionEntityStatusInfo(
			new Integer[] {WorkflowConstants.STATUS_APPROVED},
			testVersion -> WorkflowConstants.STATUS_APPROVED
		).build();

		_ctConfigurationRegistrar.register(_ctConfiguration);
	}

	@After
	public void tearDown() throws Exception {
		if (_ctConfiguration != null) {
			_ctConfigurationRegistrar.unregister(_ctConfiguration);
		}

		_ctEngineManager.disableChangeTracking(TestPropsValues.getCompanyId());
	}

	@Test
	public void testGetLatestModelChangeCTEntryOptional() throws Exception {
		Optional<CTCollection> ctCollectionOptional =
			_ctEngineManager.getActiveCTCollectionOptional(_user.getUserId());

		Assert.assertTrue(ctCollectionOptional.isPresent());

		long ctCollectionId = ctCollectionOptional.map(
			CTCollection::getCtCollectionId
		).get();

		_ctEntryLocalService.addCTEntry(
			_user.getUserId(), _testVersionClassClassName.getClassNameId(),
			RandomTestUtil.nextLong(), _TEST_RESOURCE_CLASS_ENTITY_ID,
			ctCollectionId, new ServiceContext());

		CTEntry ctEntry = _ctEntryLocalService.addCTEntry(
			_user.getUserId(), _testVersionClassClassName.getClassNameId(),
			RandomTestUtil.nextLong(), _TEST_RESOURCE_CLASS_ENTITY_ID,
			ctCollectionId, new ServiceContext());

		Optional<CTEntry> ctEntryOptional =
			_ctManager.getLatestModelChangeCTEntryOptional(
				_user.getUserId(), _TEST_RESOURCE_CLASS_ENTITY_ID);

		Assert.assertTrue(
			"Change tracking entry must exist", ctEntryOptional.isPresent());
		Assert.assertEquals(ctEntry, ctEntryOptional.get());
	}

	@Test
	public void testGetLatestModelChangeCTEntryOptionalWhenChangeTrackingIsDisabled()
		throws Exception {

		Optional<CTCollection> ctCollectionOptional =
			_ctEngineManager.getActiveCTCollectionOptional(_user.getUserId());

		Assert.assertTrue(ctCollectionOptional.isPresent());

		long ctCollectionId = ctCollectionOptional.map(
			CTCollection::getCtCollectionId
		).get();

		_ctEntryLocalService.addCTEntry(
			_user.getUserId(), _testVersionClassClassName.getClassNameId(),
			RandomTestUtil.nextLong(), _TEST_RESOURCE_CLASS_ENTITY_ID,
			ctCollectionId, new ServiceContext());

		_ctEngineManager.disableChangeTracking(TestPropsValues.getCompanyId());

		Optional<CTEntry> ctEntryOptional =
			_ctManager.getLatestModelChangeCTEntryOptional(
				_user.getUserId(), _TEST_RESOURCE_CLASS_ENTITY_ID);

		Assert.assertFalse(
			"Change tracking entry must not exist",
			ctEntryOptional.isPresent());
	}

	@Test
	public void testGetModelChangeCTEntries() throws Exception {
		List<CTEntry> ctEntries = _ctManager.getModelChangeCTEntries(
			_user.getUserId(), _TEST_RESOURCE_CLASS_ENTITY_ID);

		Assert.assertTrue("List must be empty", ListUtil.isEmpty(ctEntries));

		Optional<CTCollection> ctCollectionOptional =
			_ctEngineManager.getActiveCTCollectionOptional(_user.getUserId());

		Assert.assertTrue(ctCollectionOptional.isPresent());

		long ctCollectionId = ctCollectionOptional.map(
			CTCollection::getCtCollectionId
		).get();

		_ctEntryLocalService.addCTEntry(
			_user.getUserId(), _testVersionClassClassName.getClassNameId(),
			RandomTestUtil.nextLong(), _TEST_RESOURCE_CLASS_ENTITY_ID,
			ctCollectionId, new ServiceContext());
		_ctEntryLocalService.addCTEntry(
			_user.getUserId(), _testVersionClassClassName.getClassNameId(),
			RandomTestUtil.nextLong(), _TEST_RESOURCE_CLASS_ENTITY_ID,
			ctCollectionId, new ServiceContext());

		ctEntries = _ctManager.getModelChangeCTEntries(
			_user.getUserId(), _TEST_RESOURCE_CLASS_ENTITY_ID);

		Assert.assertFalse(
			"List must not be empty", ListUtil.isEmpty(ctEntries));
		Assert.assertEquals(
			"There must be 2 change tracking entries", 2, ctEntries.size());
	}

	@Test
	public void testGetModelChangeCTEntriesWhenChangeTrackingIsDisabled()
		throws Exception {

		Optional<CTCollection> ctCollectionOptional =
			_ctEngineManager.getActiveCTCollectionOptional(_user.getUserId());

		Assert.assertTrue(ctCollectionOptional.isPresent());

		long ctCollectionId = ctCollectionOptional.map(
			CTCollection::getCtCollectionId
		).get();

		_ctEntryLocalService.addCTEntry(
			_user.getUserId(), _testVersionClassClassName.getClassNameId(),
			RandomTestUtil.nextLong(), _TEST_RESOURCE_CLASS_ENTITY_ID,
			ctCollectionId, new ServiceContext());

		_ctEngineManager.disableChangeTracking(TestPropsValues.getCompanyId());

		List<CTEntry> ctEntries = _ctManager.getModelChangeCTEntries(
			_user.getUserId(), _TEST_RESOURCE_CLASS_ENTITY_ID);

		Assert.assertTrue("List must be empty", ListUtil.isEmpty(ctEntries));
	}

	@Test
	public void testGetModelChangeCTEntryOptional() throws Exception {
		Optional<CTCollection> ctCollectionOptional =
			_ctEngineManager.getActiveCTCollectionOptional(_user.getUserId());

		Assert.assertTrue(ctCollectionOptional.isPresent());

		long ctCollectionId = ctCollectionOptional.map(
			CTCollection::getCtCollectionId
		).get();

		CTEntry originalCTEntry = _ctEntryLocalService.addCTEntry(
			_user.getUserId(), _testVersionClassClassName.getClassNameId(),
			_TEST_VERSION_CLASS_ENTITY_ID, _TEST_RESOURCE_CLASS_ENTITY_ID,
			ctCollectionId, new ServiceContext());

		Optional<CTEntry> ctEntryOptional =
			_ctManager.getModelChangeCTEntryOptional(
				_user.getUserId(), _testVersionClassClassName.getClassNameId(),
				_TEST_VERSION_CLASS_ENTITY_ID);

		Assert.assertTrue(
			"Change tracking entry must exist", ctEntryOptional.isPresent());
		Assert.assertEquals(originalCTEntry, ctEntryOptional.get());
	}

	@Test
	public void testGetModelChangeCTEntryOptionalWhenChangeTrackingIsDisabled()
		throws PortalException {

		Optional<CTCollection> ctCollectionOptional =
			_ctEngineManager.getActiveCTCollectionOptional(_user.getUserId());

		Assert.assertTrue(ctCollectionOptional.isPresent());

		long ctCollectionId = ctCollectionOptional.map(
			CTCollection::getCtCollectionId
		).get();

		_ctEntryLocalService.addCTEntry(
			_user.getUserId(), _testVersionClassClassName.getClassNameId(),
			_TEST_VERSION_CLASS_ENTITY_ID, _TEST_RESOURCE_CLASS_ENTITY_ID,
			ctCollectionId, new ServiceContext());

		_ctEngineManager.disableChangeTracking(TestPropsValues.getCompanyId());

		Optional<CTEntry> ctEntryOptional =
			_ctManager.getModelChangeCTEntryOptional(
				_user.getUserId(), _testVersionClassClassName.getClassNameId(),
				_TEST_VERSION_CLASS_ENTITY_ID);

		Assert.assertFalse(
			"Change tracking entry must not exist",
			ctEntryOptional.isPresent());
	}

	@Test
	public void testRegisterModelChange() {
		Optional<CTEntry> ctEntryOptional = _ctManager.registerModelChange(
			_user.getUserId(), _testVersionClassClassName.getClassNameId(),
			_TEST_VERSION_CLASS_ENTITY_ID, _TEST_RESOURCE_CLASS_ENTITY_ID);

		Assert.assertTrue(ctEntryOptional.isPresent());

		CTEntry ctEntry = ctEntryOptional.get();

		Assert.assertEquals(
			_testVersionClassClassName.getClassNameId(),
			ctEntry.getClassNameId());
		Assert.assertEquals(
			_TEST_VERSION_CLASS_ENTITY_ID, ctEntry.getClassPK());
		Assert.assertEquals(
			_TEST_RESOURCE_CLASS_ENTITY_ID, ctEntry.getResourcePrimKey());
	}

	@Test
	public void testRegisterModelChangeWhenChangeTrackingIsDisabled()
		throws Exception {

		_ctEngineManager.disableChangeTracking(TestPropsValues.getCompanyId());

		Optional<CTEntry> ctEntryOptional = _ctManager.registerModelChange(
			_user.getUserId(), _testVersionClassClassName.getClassNameId(),
			_TEST_VERSION_CLASS_ENTITY_ID, _TEST_RESOURCE_CLASS_ENTITY_ID);

		Assert.assertFalse("Optional is present", ctEntryOptional.isPresent());
	}

	private static final long _TEST_RESOURCE_CLASS_ENTITY_ID = 1L;

	private static final long _TEST_VERSION_CLASS_ENTITY_ID = 1L;

	@Inject
	private ClassNameLocalService _classNameLocalService;

	private CTConfiguration _ctConfiguration;

	@Inject
	private CTConfigurationBuilder<Object, Object> _ctConfigurationBuilder;

	@Inject
	private CTConfigurationRegistrar _ctConfigurationRegistrar;

	@Inject
	private CTEngineManager _ctEngineManager;

	@Inject
	private CTEntryLocalService _ctEntryLocalService;

	@Inject
	private CTManager _ctManager;

	@DeleteAfterTestRun
	private ClassName _testVersionClassClassName;

	@DeleteAfterTestRun
	private User _user;

}