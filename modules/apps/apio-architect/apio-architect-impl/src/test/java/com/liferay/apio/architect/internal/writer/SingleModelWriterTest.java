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

package com.liferay.apio.architect.internal.writer;

import static com.spotify.hamcrest.optional.OptionalMatchers.emptyOptional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.liferay.apio.architect.internal.single.model.SingleModelImpl;

import java.util.Optional;
import java.util.stream.Stream;

import org.junit.Test;

/**
 * @author Alejandro Hernández
 */
public class SingleModelWriterTest {

	@Test
	public void testWriterReturnsEmptyIfNoRepresentorOrPathIsFound() {
		SingleModelWriter<String> singleModelWriter = SingleModelWriter.create(
			builder -> builder.singleModel(
				new SingleModelImpl<>("Apio", "")
			).modelMessageMapper(
				() -> "mediaType"
			).pathFunction(
				(resourceName, identifier) -> Optional.empty()
			).resourceNameFunction(
				__ -> Optional.empty()
			).representorFunction(
				__ -> Optional.empty()
			).requestInfo(
				null
			).singleModelFunction(
				(o, aClass) -> Optional.empty()
			).actionSemanticsFunction(
				__ -> Stream.empty()
			).build());

		Optional<String> optional = singleModelWriter.write();

		assertThat(optional, is(emptyOptional()));
	}

}