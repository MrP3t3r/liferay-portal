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

package com.liferay.calendar.util.comparator;

import com.liferay.calendar.model.CalendarResource;
import com.liferay.portal.kernel.util.OrderByComparator;

/**
 * @author Eduardo Lundgren
 * @author Fabio Pezzutto
 */
public class CalendarResourceNameComparator
	extends OrderByComparator<CalendarResource> {

	public static final String ORDER_BY_ASC =
		"CalendarResource.name, CalendarResource.code ASC";

	public static final String ORDER_BY_DESC =
		"CalendarResource.name, CalendarResource.code DESC";

	public static final String[] ORDER_BY_FIELDS = {"name", "code"};

	public CalendarResourceNameComparator() {
		this(false);
	}

	public CalendarResourceNameComparator(boolean ascending) {
		_ascending = ascending;
	}

	@Override
	public int compare(
		CalendarResource calendarResource1,
		CalendarResource calendarResource2) {

		String name1 = calendarResource1.getName();
		String name2 = calendarResource2.getName();

		int value = name1.compareTo(name2);

		if (value == 0) {
			String code1 = calendarResource1.getCode();
			String code2 = calendarResource2.getCode();

			value = code1.compareTo(code2);
		}

		if (_ascending) {
			return value;
		}

		return -value;
	}

	@Override
	public String getOrderBy() {
		if (_ascending) {
			return ORDER_BY_ASC;
		}

		return ORDER_BY_DESC;
	}

	@Override
	public String[] getOrderByFields() {
		return ORDER_BY_FIELDS;
	}

	@Override
	public boolean isAscending() {
		return _ascending;
	}

	private final boolean _ascending;

}