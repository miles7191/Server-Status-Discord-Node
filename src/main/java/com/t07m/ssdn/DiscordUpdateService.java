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

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.t07m.application.Service;
import com.t07m.ssdn.providers.JDAProvider;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDA.Status;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public class DiscordUpdateService extends Service<ServerStatusDiscordNode>{

	private final ServerStatusDiscordNode app;
	private long lastUpdate = 0;
	private boolean pending = false;

	public DiscordUpdateService(ServerStatusDiscordNode app) {
		super(TimeUnit.SECONDS.toMillis(Math.min(app.getConfig().getUpdateFrequencySeconds(), 1)));
		this.app = app;
	}

	private static final Logger logger = LoggerFactory.getLogger(DiscordUpdateService.class);

	public void process() {
		if(!pending && lastUpdate < System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(app.getConfig().getUpdateFrequencySeconds())) {
			long channelID = app.getConfig().getChannelID();
			long messageID = app.getConfig().getMessageID();
			if(channelID != -1 && messageID != -1) {
				JDA jda = JDAProvider.get();
				if(jda != null && jda.getStatus() == Status.CONNECTED) {
					TextChannel channel = jda.getTextChannelById(channelID);
					if(channel != null) {
						pending = true;
						MessageEmbed embed = DiscordEmbedBuilder.buildMessage(app);						
						logger.debug("Queing Node Update.");
						channel.editMessageEmbedsById(messageID, embed).queue(response -> {
							pending = false;
							logger.info("Node Updated.");
						});
						lastUpdate = System.currentTimeMillis();
					}
				}
			}
		}

	}



}
