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

import java.io.File;
import java.util.HashMap;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.t07m.ssdn.handlers.NetworkHandler;
import com.t07m.ssdn.handlers.StorageHandler;

import lombok.Getter;
import lombok.Setter;
import net.cubespace.Yamler.Config.Comment;
import net.cubespace.Yamler.Config.YamlConfig;

public class SSDNConfig extends YamlConfig{

	private static final Logger logger = LoggerFactory.getLogger(SSDNConfig.class);

	public SSDNConfig() {
		CONFIG_HEADER = new String[]{"Server Status Discord Node Configuration Data"};
		CONFIG_FILE = new File("config.yml");
	}
	
	private @Getter @Setter String DiscordToken = "";
	private @Getter @Setter String IconURL = "https://raw.githubusercontent.com/miles7191/Server-Status-Discord-Node/main/default_icon.png";
	private @Getter @Setter String ServerName = "";
	private @Getter @Setter String InitCommand = "!status_<ServerName>";
	private @Getter @Setter int UpdateFrequencySeconds = 60;
	private @Getter @Setter String ColorHex = "3CAAFF";

	private @Getter @Setter boolean ReportIP = true;
	private @Getter @Setter boolean ReportCPU = true;
	private @Getter @Setter boolean ReportSwap = true;
	private @Getter @Setter boolean ReportTemp = true;
	private @Getter @Setter String[] MonitoredNetworkInterfaces = NetworkHandler.getInterfaceNames();
	private @Getter @Setter HashMap<String, String> NetworkInterfaceTransalations = new HashMap<String,String>(){{
		for(String s : MonitoredNetworkInterfaces) {
			put(s, s);
		}
	}};
	private @Getter @Setter String[] MonitoredStorageMounts = StorageHandler.getMountPoints();
	private @Getter @Setter HashMap<String, String> StorageMountTransalations = new HashMap<String,String>(){{
		for(String s : MonitoredStorageMounts) {
			put(s, s);
		}
	}};
	private @Getter @Setter boolean ReportUptime = true;
	private @Getter @Setter boolean ReportLocation = true;
	@Comment("Set to true once configuration is complete.")
	private @Getter @Setter boolean Configured = false;
	
	@Comment("Do not modify these values unless you know what you are doing.")
	private @Getter @Setter long ChannelID = -1L;
	private @Getter @Setter long MessageID = -1L;
}
