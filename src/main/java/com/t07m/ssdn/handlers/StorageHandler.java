/*
 * Copyright (C) 2022 Matthew Rosato
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
package com.t07m.ssdn.handlers;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.t07m.ssdn.providers.OSHIProvider;

import oshi.software.os.OSFileStore;

public class StorageHandler {

	private static final Logger logger = LoggerFactory.getLogger(StorageHandler.class);

	public static String[] getMountPoints() {
		ArrayList<String> mountpoints = new ArrayList<String>();
		for(OSFileStore os : OSHIProvider.getOperatingSystem().getFileSystem().getFileStores()) {
			if(os.getMount() != null)
				mountpoints.add(os.getMount());
		}
		return mountpoints.toArray(new String[mountpoints.size()]);
	}

	public static long getFreeSpace(String mountPoint) {
		OSFileStore fs = getFileStore(mountPoint);
		if(fs != null) {
			return fs.getFreeSpace();
		}
		return -1;
	}

	public static long getTotalSpace(String mountPoint) {
		OSFileStore fs = getFileStore(mountPoint);
		if(fs != null) {
			return fs.getTotalSpace();
		}
		return -1;
	}

	private static OSFileStore getFileStore(String mountPoint) {
		for(OSFileStore os : OSHIProvider.getOperatingSystem().getFileSystem().getFileStores()) {
			if(os.getMount() != null && os.getMount().equals(mountPoint))
				return os;				
		}
		return null;
	}
}
