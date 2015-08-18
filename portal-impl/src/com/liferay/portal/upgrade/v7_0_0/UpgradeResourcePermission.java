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

package com.liferay.portal.upgrade.v7_0_0;

import com.liferay.portal.kernel.dao.jdbc.DataAccess;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.StringBundler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @author Sampsa Sohlman
 */
public class UpgradeResourcePermission extends UpgradeProcess {

	@Override
	protected void doUpgrade() throws Exception {
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			con = DataAccess.getUpgradeOptimizedConnection();

			ps = con.prepareStatement(
				"SELECT resourcePermissionId, primKey, primKeyId, actionIds, " +
					"viewPermission FROM ResourcePermission");

			rs = ps.executeQuery();

			while (rs.next()) {
				long actionIds = rs.getLong("actionIds");
				long primKeyId = rs.getLong("primKeyId");
				long resourcePermissionId = rs.getLong("resourcePermissionId");
				boolean viewPermission = rs.getBoolean("viewPermission");

				long newPrimKeyId = GetterUtil.getLong(rs.getString("primKey"));
				boolean newViewPermission = (actionIds % 2 == 1);

				if ((newViewPermission != viewPermission) ||
					(primKeyId != newPrimKeyId)) {

					StringBundler sb = new StringBundler();

					sb.append("update ResourcePermission set primKeyId = ");
					sb.append(newPrimKeyId);
					sb.append(", viewPermission = ");

					if (newViewPermission) {
						sb.append("[$TRUE$]");
					}
					else {
						sb.append("[$FALSE$]");
					}

					sb.append(" where resourcePermissionId = ");
					sb.append(resourcePermissionId);

					runSQL(sb.toString());
				}
			}
		}
		finally {
			DataAccess.cleanUp(con, ps, rs);
		}
	}

}