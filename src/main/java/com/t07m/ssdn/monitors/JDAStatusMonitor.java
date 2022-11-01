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

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.t07m.application.Service;
import com.t07m.ssdn.ServerStatusDiscordNode;
import com.t07m.ssdn.providers.JDAProvider;

import net.dv8tion.jda.api.JDA.Status;

public class JDAStatusMonitor extends Service<ServerStatusDiscordNode>{

	private final ServerStatusDiscordNode app;
	private boolean firstConnected = true;

	public JDAStatusMonitor(ServerStatusDiscordNode app) {
		super(TimeUnit.SECONDS.toMillis(1));
		this.app = app;
	}

	private static final Logger logger = LoggerFactory.getLogger(JDAStatusMonitor.class);

	private Status status;

	public void init() {
		if(JDAProvider.get() != null) {
			this.status = JDAProvider.get().getStatus();
		}
	}

	public void process() {
		if(JDAProvider.get() != null) {
			Status current = JDAProvider.get().getStatus();
			if(status != current) {
				logger.debug("JDA Stauts Changed from " + status + " to " + current + ".");
				status = current;
			}
			if(firstConnected && status == Status.CONNECTED) {
				firstConnected = false;
				logger.info("Bot Ready");
				if(app.getConfig().getMessageID() == -1L) {
					logger.info("Enter the command \"" 
							+ app.getConfig().getInitCommand().replace("<ServerName>", app.getConfig().getServerName()) 
							+ "\" to begin monitoring");
				}
			}
		}
	}

}
