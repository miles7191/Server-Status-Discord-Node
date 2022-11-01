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

import java.awt.Color;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.t07m.application.Service;
import com.t07m.ssdn.handlers.StorageHandler;
import com.t07m.ssdn.providers.JDAProvider;
import com.t07m.ssdn.providers.OSHIProvider;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDA.Status;
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
						SSDNConfig conf = app.getConfig();
						EmbedBuilder eb = new EmbedBuilder()
								.setColor(new Color(Integer.parseInt(conf.getColorHex(), 16)))
								.setAuthor(conf.getServerName() + (conf.isReportIP() ? (" (" + app.getLocationMonitor().getIp() + ")") : ""), null, (conf.getIconURL().length()>0 ? conf.getIconURL() : null));
						if(conf.isReportCPU()) {
							eb.addField("Processor", OSHIProvider.getHardware().getProcessor().getProcessorIdentifier().getName(), false);
						}
						eb.addField("CPU 1m", app.getCpuMonitor().getUsage1M(2)+"%", true)
						.addField("CPU 5m", app.getCpuMonitor().getUsage5M(2)+"%", true)
						.addField("CPU 15m", app.getCpuMonitor().getUsage15M(2)+"%", true)
						.addField("Mem 1m", app.getRamMonitor().getUsage1M(2)+"%", true)
						.addField("Mem 5m", app.getRamMonitor().getUsage5M(2)+"%", true)
						.addField("Mem 15m", app.getRamMonitor().getUsage15M(2)+"%", true);
						if(conf.isReportSwap())
							eb.addField("Swap 1m", app.getSwapMonitor().getUsage1M(1)+"%", true)
							.addField("Swap 5m", app.getSwapMonitor().getUsage5M(2)+"°%", true)
							.addField("Swap 15m", app.getSwapMonitor().getUsage15M(2)+"°%", true);
						if(conf.isReportTemp())
							eb.addField("Temp 1m", app.getTempMonitor().getTemp1M(1)+"°C", true)
							.addField("Temp 5m", app.getTempMonitor().getTemp5M(1)+"°C", true)
							.addField("Temp 15m", app.getTempMonitor().getTemp15M(1)+"°C", true);
						if(conf.getMonitoredNetworkInterfaces().length > 0)
							for(String nif: conf.getMonitoredNetworkInterfaces()) {
								String name = conf.getNetworkInterfaceTransalations().getOrDefault(nif, nif);
								eb.addField(name+" 1m", "↑" + FormatUtils.formatBitsPerSecond(app.getNetworkMonitor().getSent1M(nif)) +" ↓" + FormatUtils.formatBitsPerSecond(app.getNetworkMonitor().getRecv1M(nif)), true)
								.addField(name+" 5m", "↑" + FormatUtils.formatBitsPerSecond(app.getNetworkMonitor().getSent5M(nif)) +" ↓" + FormatUtils.formatBitsPerSecond(app.getNetworkMonitor().getRecv5M(nif)), true)
								.addField(name+" 15m", "↑" + FormatUtils.formatBitsPerSecond(app.getNetworkMonitor().getSent15M(nif)) +" ↓" + FormatUtils.formatBitsPerSecond(app.getNetworkMonitor().getRecv15M(nif)), true);
							}
						if(conf.getMonitoredStorageMounts().length > 0)
							for(String mnt : conf.getMonitoredStorageMounts()) {
								String name = conf.getStorageMountTransalations().getOrDefault(mnt, mnt);
								eb.addField(name, FormatUtils.formatBytes(StorageHandler.getFreeSpace(mnt)), true);
							}
						if(conf.isReportUptime())
							eb.addField("Uptime", FormatUtils.formatUptime(OSHIProvider.getOperatingSystem().getSystemUptime()*1000), false);
						if(conf.isReportLocation())
							eb.addField("Location", app.getLocationMonitor().getCity() + ", " + app.getLocationMonitor().getRegion(), false);
						eb.setTimestamp(Instant.now());
						logger.debug("Queing Node Update.");
						channel.editMessageEmbedsById(messageID, eb.build()).queue(response -> {
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
