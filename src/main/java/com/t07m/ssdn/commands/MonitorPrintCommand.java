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
package com.t07m.ssdn.commands;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.t07m.console.Command;
import com.t07m.console.Console;
import com.t07m.ssdn.ServerStatusDiscordNode;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

public class MonitorPrintCommand extends Command{

	private static final Logger logger = LoggerFactory.getLogger(MonitorPrintCommand.class);

	private ServerStatusDiscordNode ssdn;

	public MonitorPrintCommand(ServerStatusDiscordNode ssdn) {
		super("Monitor Print");
		this.ssdn = ssdn;
		OptionParser op = new OptionParser();
		String[] monitorOptions = {"m",
		"monitor"};
		op.acceptsAll(Arrays.asList(monitorOptions), "Monitor Name").withRequiredArg().ofType(String.class);	
		setOptionParser(op);
	}

	@Override
	public void process(OptionSet optionSet, Console console) {
		if(optionSet.has("monitor")) {
			switch((String) optionSet.valueOf("monitor")) {
			case "cpu":
				logger.info(ssdn.getCpuMonitor().toString());
				break;
			case "ram":
				logger.info(ssdn.getRamMonitor().toString());
				break;
			case "temp":
				logger.info(ssdn.getTempMonitor().toString());
				break;
			case "network":
				logger.info(ssdn.getNetworkMonitor().toString());
				break;
			case "location":
				logger.info(ssdn.getLocationMonitor().toString());
				break;
			}
			return;
		}
		logger.info(ssdn.getCpuMonitor().toString());
		logger.info(ssdn.getRamMonitor().toString());
		logger.info(ssdn.getTempMonitor().toString());
		logger.info(ssdn.getNetworkMonitor().toString());
		logger.info(ssdn.getLocationMonitor().toString());
	}
}
