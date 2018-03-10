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

package com.liferay.jenkins.results.parser.vm.provisioner.amazon;

/**
 * @author Kiyoshi Lee
 */
public class AuroraAmazonVMProvisionerFactory {

	public static AuroraAmazonVMProvisioner newAuroraAmazonVMProvisioner(
		String awsAccessKeyId, String awsSecretAccessKey, String dbInstanceId) {

		return new MySQLAuroraAmazonVMProvisioner(
			awsAccessKeyId, awsSecretAccessKey, dbInstanceId);
	}

	public static AuroraAmazonVMProvisioner newAuroraAmazonVMProvisioner(
		String awsAccessKeyId, String awsSecretAccessKey, String dbClusterId,
		String dbEngine, String dbInstanceClass, String dbInstanceId) {

		if (dbEngine.equals("aurora")) {
			return new MySQLAuroraAmazonVMProvisioner(
				awsAccessKeyId, awsSecretAccessKey, dbClusterId,
				dbInstanceClass, dbInstanceId);
		}
		else if (dbEngine.contains("aurora-postgresql")) {
			return new PostgreSQLAuroraAmazonVMProvisioner(
				awsAccessKeyId, awsSecretAccessKey, dbClusterId,
				dbInstanceClass, dbInstanceId);
		}

		throw new RuntimeException("Invalid database engine " + dbEngine);
	}

}