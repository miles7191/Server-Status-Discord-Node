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
package com.t07m.ssdn.monitors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.t07m.application.Service;
import com.t07m.ssdn.ServerStatusDiscordNode;

import lombok.Getter;

public class LocationMonitor extends Service<ServerStatusDiscordNode>{

	public LocationMonitor() {
		super(TimeUnit.MINUTES.toMillis(1));
	}

	private static final Logger logger = LoggerFactory.getLogger(LocationMonitor.class);
	
	private final String url = "https://ipapi.co/json/";
	private final long MaxDataAge = TimeUnit.MINUTES.toMillis(30);
	
	private @Getter String ip = "";
	private @Getter String city = "";
	private @Getter String region = "";
	private long lastPoll = 0;
	
	public void init() {
		logger.debug("Initializing LocationMonitor");
	}

	public void process() {
		if(lastPoll < System.currentTimeMillis() - MaxDataAge) {
			JsonNode json = readRecordsFromUrl(url);
			if(json != null) {
				JsonNode ipjn, cityjn, regionjn;
				ipjn = json.get("ip");
				cityjn = json.get("city");
				regionjn = json.get("region");
				if(ipjn != null && cityjn != null && regionjn != null) {
					ip = ipjn.asText();
					city = cityjn.asText();
					region = regionjn.asText();
					lastPoll = System.currentTimeMillis();
				}
			}
		}
	}
	
	private String readAll(Reader rd) {
		StringBuilder sb = new StringBuilder();
		int cp;
		try {
			while ((cp = rd.read()) != -1) {
				sb.append((char) cp);
			}
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		return sb.toString();
	}

	public JsonNode readRecordsFromUrl(String url) {
		try (InputStream is = new URL(url).openStream()){
			BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
			String jsonText = readAll(rd);
			ObjectMapper mapper = new ObjectMapper();
			JsonNode json = mapper.readTree(jsonText);
			return json;
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		return null;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Location Monitor[IP: ");
		sb.append(this.getIp());
		sb.append(" City: ");
		sb.append(this.getCity());
		sb.append(" Region: ");
		sb.append(this.getRegion());
		sb.append("]");
		return sb.toString();
	}
}
