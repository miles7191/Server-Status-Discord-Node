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
package com.t07m.ssdn.providers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.t07m.ssdn.ServerStatusDiscordNode;
import com.t07m.ssdn.listeners.StatusCommandListener;

import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class JDAProvider {

	private static final Logger logger = LoggerFactory.getLogger(JDAProvider.class);

	private static JDA jda;
	
	public static void initialize(ServerStatusDiscordNode app) {
		if(jda != null)
			return;
		jda = JDABuilder
				.create(app.getConfig().getDiscordToken(),
						GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
				.addEventListeners(new StatusCommandListener(app)).setAutoReconnect(true).build();
	}
	
	public static JDA get() {
		return jda;
	}
}
