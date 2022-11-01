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

import oshi.hardware.Sensors;

public class TempMonitor extends Service<ServerStatusDiscordNode>{

	private static final Logger logger = LoggerFactory.getLogger(TempMonitor.class);

	private final long MaxTempAge = TimeUnit.MINUTES.toMillis(15);

	private Object tempsLock = new Object();

	private LinkedHashMap<Long, Float> temps;

	public TempMonitor() {
		super(TimeUnit.SECONDS.toMillis(1));
	}

	public void init() {
		logger.debug("Initializing TempMonitor");
		temps = new LinkedHashMap<Long, Float>();
	}

	public void process() {
		Sensors sensors = OSHIProvider.getHardware().getSensors();
		synchronized(tempsLock) {
			double temp = sensors.getCpuTemperature();
			temps.put(System.currentTimeMillis(), (float) temp);
			for(Long l : temps.keySet().toArray(new Long[0])) {
				if(l > System.currentTimeMillis()-MaxTempAge) {
					break;
				}
				temps.remove(l);
			}		
		}
	}

	private double format(double value, int decimals) {		
		return ((int)(value*Math.pow(10, decimals)))/Math.pow(10, decimals);
	}

	public double getTemp1M(int decimals) {
		int samples = 0;
		double total = 0;
		synchronized(tempsLock) {
			Long[] keys = temps.keySet().toArray(new Long[0]);
			for(int i = keys.length-1; i > 0; i--) {
				Long l = keys[i];
				long cutoff = System.currentTimeMillis()-TimeUnit.MINUTES.toMillis(1);
				if(l < cutoff) {
					break;
				}
				samples++;
				total += temps.get(l);
			}
		}
		if(samples == 0 || total ==0)
			return 0;
		return format(total/samples, decimals);
	}

	public double getTemp5M(int decimals) {
		int samples = 0;
		double total = 0;
		synchronized(tempsLock) {
			Long[] keys = temps.keySet().toArray(new Long[0]);
			for(int i = keys.length-1; i > 0; i--) {
				Long l = keys[i];
				long cutoff = System.currentTimeMillis()-TimeUnit.MINUTES.toMillis(5);
				if(l < cutoff) {
					break;
				}
				samples++;
				total += temps.get(l);
			}
		}
		if(samples == 0 || total ==0)
			return 0;
		return format(total/samples, decimals);
	}

	public double getTemp15M(int decimals) {
		int samples = 0;
		double total = 0;
		synchronized(tempsLock) {
			Long[] keys = temps.keySet().toArray(new Long[0]);
			for(int i = keys.length-1; i > 0; i--) {
				Long l = keys[i];
				long cutoff = System.currentTimeMillis()-TimeUnit.MINUTES.toMillis(15);
				if(l < cutoff) {
					break;
				}
				samples++;
				total += temps.get(l);
			}
		}
		if(samples == 0 || total ==0)
			return 0;
		return format(total/samples, decimals);
	}

	public void cleanup() {
		synchronized(tempsLock) {
			temps.clear();
		}
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Temp Monitor[1m: ");
		sb.append(this.getTemp1M(2));
		sb.append("C 5m: ");
		sb.append(this.getTemp5M(2));
		sb.append("C 15m: ");
		sb.append(this.getTemp15M(2));
		sb.append("C]");
		return sb.toString();
	}

}
