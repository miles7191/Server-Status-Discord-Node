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
package com.t07m.ssdn.listeners;

import java.awt.Color;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.t07m.ssdn.ServerStatusDiscordNode;

import lombok.RequiredArgsConstructor;
import net.cubespace.Yamler.Config.InvalidConfigurationException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.internal.utils.PermissionUtil;

@RequiredArgsConstructor
public class StatusCommandListener extends ListenerAdapter{

	private static final Logger logger = LoggerFactory.getLogger(StatusCommandListener.class);

	private final ServerStatusDiscordNode app;
	
	public void onMessageReceived(MessageReceivedEvent e) {
		if(e.getAuthor().isBot() || e.getChannelType() == ChannelType.PRIVATE)
			return;
		Message msg = e.getMessage();
		if(msg.getContentRaw().equalsIgnoreCase(app.getConfig().getInitCommand().replace("<ServerName>", app.getConfig().getServerName())) && PermissionUtil.checkPermission(e.getMember(), Permission.ADMINISTRATOR)) {
			e.getChannel().sendMessageEmbeds(new EmbedBuilder().setTitle("Placeholder").build())
			.queue(response -> {
				if(app.getConfig().getMessageID() != -1) {
					logger.info("New Static Node requested. Deleting old Satic Node.");
					e.getJDA().getTextChannelById(app.getConfig().getChannelID()).deleteMessageById(app.getConfig().getMessageID()).queue();
				}else {
					logger.info("New Static Node requested.");
				}
				app.getConfig().setChannelID(response.getChannel().getIdLong());
				app.getConfig().setMessageID(response.getIdLong());
				try {
					app.getConfig().save();
				} catch (InvalidConfigurationException e1) {
					logger.error(e.getMessage().toString());
				}
			});
		}
	}

}
