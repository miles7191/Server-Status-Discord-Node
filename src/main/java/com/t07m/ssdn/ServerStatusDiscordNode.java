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

import com.github.zafarkhaja.semver.Version;
import com.t07m.application.Application;
import com.t07m.ssdn.commands.MonitorPrintCommand;
import com.t07m.ssdn.monitors.CPUMonitor;
import com.t07m.ssdn.monitors.JDAStatusMonitor;
import com.t07m.ssdn.monitors.LocationMonitor;
import com.t07m.ssdn.monitors.NetworkMonitor;
import com.t07m.ssdn.monitors.RAMMonitor;
import com.t07m.ssdn.monitors.SwapMonitor;
import com.t07m.ssdn.monitors.TempMonitor;
import com.t07m.ssdn.providers.JDAProvider;

import lombok.Getter;
import net.cubespace.Yamler.Config.InvalidConfigurationException;

public class ServerStatusDiscordNode extends Application{

	private static final Logger logger = LoggerFactory.getLogger(ServerStatusDiscordNode.class);

	public static final Version VERSION = Version.valueOf("1.0.1");

	public static void main(String[] args) {
		boolean gui = true;
		if(args.length > 0) {
			for(String arg : args) {
				if(arg.equalsIgnoreCase("-nogui")) {
					gui = false;
				}
			}
		}
		new ServerStatusDiscordNode(gui).start();
	}

	private @Getter SSDNConfig config;

	private @Getter CPUMonitor cpuMonitor;
	private @Getter RAMMonitor ramMonitor;
	private @Getter SwapMonitor swapMonitor;
	private @Getter TempMonitor tempMonitor;
	private @Getter LocationMonitor locationMonitor;
	private @Getter NetworkMonitor networkMonitor;
	private @Getter JDAStatusMonitor jdaStatusMonitor;
	private @Getter DiscordUpdateService discordUpdateService;

	public ServerStatusDiscordNode(boolean gui) {
		super(gui, "Server Status Discord Node - " + VERSION.toString());
	}

	public void init() {
		this.config = new SSDNConfig();
		try {
			this.config.init();
			this.config.save();
			if(!this.config.isConfigured()) {
				logger.info("Configuration file is not configured.");
				logger.info("Complete the config file and relaunch this application.");
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e1) {}
				System.exit(-1);
			}				
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
			System.err.println("Unable to load configuration file!");
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e1) {}
			System.exit(-1);
		}
		logger.info("Launching Application.");
		JDAProvider.initialize(this);
		this.cpuMonitor = new CPUMonitor();
		this.ramMonitor = new RAMMonitor();
		this.swapMonitor = new SwapMonitor();
		this.tempMonitor = new TempMonitor();
		this.locationMonitor = new LocationMonitor();
		this.networkMonitor = new NetworkMonitor();
		this.jdaStatusMonitor = new JDAStatusMonitor(this);
		this.discordUpdateService = new DiscordUpdateService(this);
		this.registerService(cpuMonitor);
		this.registerService(ramMonitor);
		this.registerService(swapMonitor);
		this.registerService(tempMonitor);
		this.registerService(locationMonitor);
		this.registerService(networkMonitor);
		this.registerService(jdaStatusMonitor);
		this.registerService(discordUpdateService);
		this.getConsole().registerCommands(
				new MonitorPrintCommand(this));
	}
}
