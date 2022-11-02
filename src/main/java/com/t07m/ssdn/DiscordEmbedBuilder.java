package com.t07m.ssdn;
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

import java.awt.Color;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.t07m.ssdn.handlers.StorageHandler;
import com.t07m.ssdn.providers.OSHIProvider;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class DiscordEmbedBuilder {

	private static final Logger logger = LoggerFactory.getLogger(DiscordEmbedBuilder.class);

	public static MessageEmbed buildMessage(ServerStatusDiscordNode app) {
		SSDNConfig conf = app.getConfig();
		EmbedBuilder eb = new EmbedBuilder()
				.setColor(new Color(Integer.parseInt(conf.getColorHex(), 16)))
				.setAuthor(conf.getServerName() + (conf.isReportIP() ? (" (" + app.getLocationMonitor().getIp() + ")") : ""), null, (conf.getIconURL().length()>0 ? conf.getIconURL() : null));
		if(conf.isReportCPU()) {
			eb.addField("Processor", OSHIProvider.getHardware().getProcessor().getProcessorIdentifier().getName(), false);
		}
		eb.addField("CPU Usage:", FormatUtils.formatRow(12,
				app.getCpuMonitor().getUsage1M(2)+"%",
				app.getCpuMonitor().getUsage5M(2)+"%",
				app.getCpuMonitor().getUsage15M(2)+"%"), false);
		eb.addField("Memory Usage:", FormatUtils.formatRow(12,
				app.getRamMonitor().getUsage1M(2)+"%",
				app.getRamMonitor().getUsage5M(2)+"%",
				app.getRamMonitor().getUsage15M(2)+"%"), false);
		if(conf.isReportSwap())
			eb.addField("Swap Usage:", FormatUtils.formatRow(12,
					app.getSwapMonitor().getUsage1M(2)+"%",
					app.getSwapMonitor().getUsage5M(2)+"%",
					app.getSwapMonitor().getUsage15M(2)+"%"), false);
		if(conf.isReportTemp())
			eb.addField("CPU Temperatrue:", FormatUtils.formatRow(12,
					app.getTempMonitor().getTemp1M(1)+"°C",
					app.getTempMonitor().getTemp1M(1)+"°C",
					app.getTempMonitor().getTemp1M(1)+"°C"), false);
		if(conf.getMonitoredNetworkInterfaces().length > 0)
			for(String nif: conf.getMonitoredNetworkInterfaces()) {
				String name = conf.getNetworkInterfaceTransalations().getOrDefault(nif, nif);
				eb.addField(name + " Activity:", FormatUtils.formatRow(12,
				("↑" + FormatUtils.formatBitsPerSecond(app.getNetworkMonitor().getSent1M(nif)) +"↓" + FormatUtils.formatBitsPerSecond(app.getNetworkMonitor().getRecv1M(nif))),
				("↑" + FormatUtils.formatBitsPerSecond(app.getNetworkMonitor().getSent5M(nif)) +"↓" + FormatUtils.formatBitsPerSecond(app.getNetworkMonitor().getRecv5M(nif))),
				("↑" + FormatUtils.formatBitsPerSecond(app.getNetworkMonitor().getSent15M(nif)) +"↓" + FormatUtils.formatBitsPerSecond(app.getNetworkMonitor().getRecv15M(nif)))), false);
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
		return eb.build();
	}

}
