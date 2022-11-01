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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.t07m.application.Service;
import com.t07m.ssdn.FormatUtils;
import com.t07m.ssdn.ServerStatusDiscordNode;
import com.t07m.ssdn.providers.OSHIProvider;

import oshi.hardware.NetworkIF;

public class NetworkMonitor extends Service<ServerStatusDiscordNode>{

	private static final Logger logger = LoggerFactory.getLogger(NetworkMonitor.class);

	private final long MaxUsageAge = TimeUnit.MINUTES.toMillis(15);

	private Object usageLock = new Object();

	private HashMap<String, LinkedHashMap<Long, Long>> bytesRecv;
	private HashMap<String, LinkedHashMap<Long, Long>> bytesSent;
	private HashMap<String, Long> lastRecvs;
	private HashMap<String, Long> lastSents;
	private HashMap<String, Long> lastRecvsTime;
	private HashMap<String, Long> lastSentsTime;

	public NetworkMonitor() {
		super(TimeUnit.SECONDS.toMillis(3));
	}

	public void init() {
		logger.debug("Initializing NetworkMonitor");
		bytesRecv = new HashMap<String, LinkedHashMap<Long, Long>>();
		bytesSent = new HashMap<String, LinkedHashMap<Long, Long>>();
		lastRecvs = new HashMap<String, Long>();
		lastSents = new HashMap<String, Long>();
		lastRecvsTime = new HashMap<String, Long>();
		lastSentsTime = new HashMap<String, Long>();
	}

	public void process() {
		synchronized(usageLock) {
			for(NetworkIF nif : OSHIProvider.getHardware().getNetworkIFs(false)) {
				boolean newNif = false;
				LinkedHashMap<Long, Long> usagesRecv = bytesRecv.get(nif.getName());
				if(usagesRecv == null) {
					usagesRecv = new LinkedHashMap<Long, Long>();
					bytesRecv.put(nif.getName(), usagesRecv);
					newNif = true;
				}
				LinkedHashMap<Long, Long> usagesSent = bytesSent.get(nif.getName());
				if(usagesSent == null) {
					usagesSent = new LinkedHashMap<Long, Long>();
					bytesSent.put(nif.getName(), usagesSent);
					newNif = true;
				}
				if(newNif) {
					nif.updateAttributes();
					lastRecvs.put(nif.getName(), nif.getBytesRecv());
					lastRecvsTime.put(nif.getName(), System.currentTimeMillis());
					lastSents.put(nif.getName(), nif.getBytesSent());
					lastSentsTime.put(nif.getName(), System.currentTimeMillis());
					continue;
				}
				nif.updateAttributes();
				long newRecvs = nif.getBytesRecv();
				long newRecvsTime = System.currentTimeMillis();
				long newSents = nif.getBytesSent();
				long newSentsTime = System.currentTimeMillis();
				usagesRecv.put(System.currentTimeMillis(), (long) ((newRecvs - lastRecvs.get(nif.getName())) / ((newRecvsTime - lastRecvsTime.get(nif.getName()))/1000.0)));
				usagesSent.put(System.currentTimeMillis(), (long) ((newSents - lastSents.get(nif.getName())) / ((newSentsTime - lastSentsTime.get(nif.getName()))/1000.0)));
				lastRecvs.put(nif.getName(), newRecvs);
				lastRecvsTime.put(nif.getName(), newRecvsTime);
				lastSents.put(nif.getName(), newSents);
				lastSentsTime.put(nif.getName(), newSentsTime);
				for(Long l : usagesRecv.keySet().toArray(new Long[0])) {
					if(l > System.currentTimeMillis()-MaxUsageAge) {
						break;
					}
					usagesRecv.remove(l);
				}
				for(Long l : usagesSent.keySet().toArray(new Long[0])) {
					if(l > System.currentTimeMillis()-MaxUsageAge) {
						break;
					}
					usagesSent.remove(l);
				}
			}		
		}
	}

	public long getSent1M(String nif) {
		int samples = 0;
		long total = 0;
		synchronized(usageLock) {
			LinkedHashMap<Long, Long> usages = bytesSent.get(nif);
			if(usages != null) {
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
		}
		if(samples == 0)
			return 0;
		//Return in Bits per second
		return total/samples*8;
	}

	public long getRecv1M(String nif) {
		int samples = 0;
		long total = 0;
		synchronized(usageLock) {
			LinkedHashMap<Long, Long> usages = bytesRecv.get(nif);
			if(usages != null) {
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
		}
		if(samples == 0)
			return 0;
		//Return in Bits per second
		return total/samples*8;
	}

	public long getSent5M(String nif) {

		int samples = 0;long total = 0;
		synchronized(usageLock) {
			LinkedHashMap<Long, Long> usages = bytesSent.get(nif);
			if(usages != null) {
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
		}
		if(samples == 0)
			return 0;
		//Return in Bits per second
		return total/samples*8;
	}

	public long getRecv5M(String nif) {
		int samples = 0;
		long total = 0;
		synchronized(usageLock) {
			LinkedHashMap<Long, Long> usages = bytesRecv.get(nif);
			if(usages != null) {
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
		}
		if(samples == 0)
			return 0;
		//Return in Bits per second
		return total/samples*8;
	}

	public long getSent15M(String nif) {

		int samples = 0;long total = 0;
		synchronized(usageLock) {
			LinkedHashMap<Long, Long> usages = bytesSent.get(nif);
			if(usages != null) {
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
		}
		if(samples == 0)
			return 0;
		//Return in Bits per second
		return total/samples*8;
	}

	public long getRecv15M(String nif) {
		int samples = 0;
		long total = 0;
		synchronized(usageLock) {
			LinkedHashMap<Long, Long> usages = bytesRecv.get(nif);
			if(usages != null) {
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
		}
		if(samples == 0)
			return 0;
		//Return in Bits per second
		return total/samples*8;
	}

	public void cleanup() {
		synchronized(usageLock) {
			//usages.clear();
		}

	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Network Monitor[");
		for(String nif : bytesRecv.keySet().toArray(new String[0])) {
			sb.append("\n\r{ Name: ");
			sb.append(nif);
			sb.append(" s1m: ");
			sb.append(FormatUtils.formatBitsPerSecond(this.getSent1M(nif)));
			sb.append(" r1m: ");
			sb.append(FormatUtils.formatBitsPerSecond(this.getRecv1M(nif)));
			sb.append(" s5m: ");
			sb.append(FormatUtils.formatBitsPerSecond(this.getSent5M(nif)));
			sb.append(" r5m: ");
			sb.append(FormatUtils.formatBitsPerSecond(this.getRecv5M(nif)));
			sb.append(" s15m: ");
			sb.append(FormatUtils.formatBitsPerSecond(this.getSent15M(nif)));
			sb.append(" r15m: ");
			sb.append(FormatUtils.formatBitsPerSecond(this.getRecv15M(nif)));
			sb.append("}");
		}
		sb.append("]");
		return sb.toString();
	}

}
