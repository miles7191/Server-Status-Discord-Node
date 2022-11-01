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

import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.t07m.application.Service;
import com.t07m.ssdn.ServerStatusDiscordNode;
import com.t07m.ssdn.providers.OSHIProvider;

import oshi.hardware.GlobalMemory;
import oshi.hardware.VirtualMemory;

public class RAMMonitor extends Service<ServerStatusDiscordNode>{

	private static final Logger logger = LoggerFactory.getLogger(RAMMonitor.class);

	private final long MaxUsageAge = TimeUnit.MINUTES.toMillis(15);

	private Object usageLock = new Object();

	private LinkedHashMap<Long, Float> usages;

	public RAMMonitor() {
		super(TimeUnit.SECONDS.toMillis(3));
	}

	public void init() {
		logger.debug("Initializing RAMMonitor");
		usages = new LinkedHashMap<Long, Float>();
	}

	public void process() {
		
		synchronized(usageLock) {
			GlobalMemory memory = OSHIProvider.getHardware().getMemory();
			double usage = 1-(memory.getAvailable()/(double) memory.getTotal());
			usages.put(System.currentTimeMillis(), (float) usage);
			for(Long l : usages.keySet().toArray(new Long[0])) {
				if(l > System.currentTimeMillis()-MaxUsageAge) {
					break;
				}
				usages.remove(l);
			}		
		}
	}

	private double format(double value, int decimals) {		
		return ((int)(value*100*Math.pow(10, decimals)))/Math.pow(10, decimals);
	}

	public double getUsage1M(int decimals) {
		int samples = 0;
		double total = 0;
		synchronized(usageLock) {
			Long[] keys = usages.keySet().toArray(new Long[0]);
			for(int i = keys.length-1; i > 0; i--) {
				Long l = keys[i];
				long cutoff = System.currentTimeMillis()-TimeUnit.MINUTES.toMillis(1);
				if(l < cutoff) {
					break;
				}
				samples++;
				total += usages.get(l);
			}
		}
		if(samples == 0 || total ==0)
			return 0;
		return format(total/samples, decimals);
	}

	public double getUsage5M(int decimals) {
		int samples = 0;
		double total = 0;
		synchronized(usageLock) {
			Long[] keys = usages.keySet().toArray(new Long[0]);
			for(int i = keys.length-1; i > 0; i--) {
				Long l = keys[i];
				long cutoff = System.currentTimeMillis()-TimeUnit.MINUTES.toMillis(5);
				if(l < cutoff) {
					break;
				}
				samples++;
				total += usages.get(l);
			}
		}
		if(samples == 0 || total ==0)
			return 0;
		return format(total/samples, decimals);
	}

	public double getUsage15M(int decimals) {
		int samples = 0;
		double total = 0;
		synchronized(usageLock) {
			Long[] keys = usages.keySet().toArray(new Long[0]);
			for(int i = keys.length-1; i > 0; i--) {
				Long l = keys[i];
				long cutoff = System.currentTimeMillis()-TimeUnit.MINUTES.toMillis(15);
				if(l < cutoff) {
					break;
				}
				samples++;
				total += usages.get(l);
			}
		}
		if(samples == 0 || total ==0)
			return 0;
		return format(total/samples, decimals);
	}

	public void cleanup() {
		synchronized(usageLock) {
			usages.clear();
		}
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("RAM Monitor[1m: ");
		sb.append(this.getUsage1M(2));
		sb.append("% 5m: ");
		sb.append(this.getUsage5M(2));
		sb.append("% 15m: ");
		sb.append(this.getUsage15M(2));
		sb.append("%]");
		return sb.toString();
	}

}
