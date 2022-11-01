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
package com.t07m.ssdn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FormatUtils {

	private static final Logger logger = LoggerFactory.getLogger(FormatUtils.class);

	private static final long SECOND = 1000;
	private static final long MINUTE = 60 * SECOND;
	private static final long HOUR = 60 * MINUTE;
	private static final long DAY = 24 * HOUR;
	private static final long MONTH = 30 * DAY;
	private static final long YEAR = 365 * DAY;

	public static String formatUptime(long uptime) {
		long start = uptime;
		StringBuilder buf = new StringBuilder();
		if (uptime > YEAR) {
			long years = (uptime - uptime % YEAR) / YEAR;
			buf.append(years);
			buf.append(" Year");
			if(years > 1)
				buf.append("s");
			uptime = uptime % YEAR;
		}
		if (uptime > MONTH) {
			long month = (uptime - uptime % MONTH) / MONTH;
			if (buf.length() > 0) {
				buf.append(" ");
			}
			buf.append(month);
			buf.append(" Month");
			if(month > 1)
				buf.append("s");
			uptime = uptime % MONTH;
		}
		if (uptime > DAY) {
			long days = (uptime - uptime % DAY) / DAY;
			if (buf.length() > 0) {
				buf.append(" ");
			}
			buf.append(days);
			buf.append(" Day");
			if(days > 1)
				buf.append("s");
			uptime = uptime % DAY;
		}
		if (uptime > HOUR) {
			long hours = (uptime - uptime % HOUR) / HOUR;
			if (buf.length() > 0) {
				buf.append(" ");
			}
			buf.append(hours);
			buf.append(" Hour");
			if(hours > 1)
				buf.append("s");
			uptime = uptime % HOUR;
		}
		if (start < YEAR && uptime > MINUTE) {
			long minutes = (uptime - uptime % MINUTE) / MINUTE;
			if (buf.length() > 0) {
				buf.append(" ");
			}
			buf.append(minutes);
			buf.append(" Minute");
			if(minutes > 1)
				buf.append("s");
			uptime = uptime % MINUTE;
		}
		if (start < MONTH && uptime > SECOND) {
			long seconds = (uptime - uptime % SECOND) / SECOND;
			if (buf.length() > 0) {
				buf.append(" ");
			}
			buf.append(seconds);
			buf.append(" Second");
			if(seconds > 1)
				buf.append("s");
			uptime = uptime % SECOND;
		}
		return buf.toString();
	}
	public static String formatBytesPerSecond(long Bps) {
		return formatBytes(Bps) + "ps";
	}

	public static String formatBitsPerSecond(long bps) {
		return formatBits(bps) + "ps";
	}

	public static String formatBytes(long B) {
		if(B > 1000000000000L) {
			return B /1000000000000L + "TB";
		}
		if(B > 1000000000L) {
			return B /1000000000L + "GB";
		}
		if(B > 1000000L) {
			return B/1000000L + "MB";
		}
		if(B > 1000L) {
			return B/1000L + "KB";
		}
		return B + "B";
	}

	public static String formatBits(long b) {
		if(b > 1000000000000L) {
			return b /1000000000000L + "Tb";
		}
		if(b > 1000000000L) {
			return b /1000000000L + "Gb";
		}
		if(b > 1000000L) {
			return b/1000000L + "Mb";
		}
		if(b > 1000L) {
			return b/1000L + "Kb";
		}
		return b + "b";
	}
}
