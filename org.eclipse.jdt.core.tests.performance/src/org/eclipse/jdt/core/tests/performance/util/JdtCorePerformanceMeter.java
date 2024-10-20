/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.performance.util;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.test.internal.performance.OSPerformanceMeter;
import org.eclipse.test.internal.performance.data.DataPoint;
import org.eclipse.test.internal.performance.data.Dim;
import org.eclipse.test.internal.performance.data.Sample;
import org.eclipse.test.internal.performance.eval.StatisticsSession;

public class JdtCorePerformanceMeter extends OSPerformanceMeter {

    public static Map CPU_TIMES = null, ELAPSED_TIMES = null;
	
	public static final class Statistics {
		public long count;
		public long sum;
		public double average;
		public double stddev;
		
		public Statistics(StatisticsSession s, Dim dimension) {
			count = s.getCount(dimension);
			average = s.getAverage(dimension);
			sum = s.getSum(dimension);
			stddev = s.getStddev(dimension);
		}
		
		public String toString() {
			return "n="+count+", s="+sum+", av="+average+", dev="+stddev;
		}
	}
	
	public JdtCorePerformanceMeter(String scenarioId) {
		super(scenarioId);
		CPU_TIMES = new HashMap();
		ELAPSED_TIMES = new HashMap();
    }

	/*
	 * @see org.eclipse.test.performance.PerformanceMeter#commit()
	 */
	public void commit() {
	    Sample sample= getSample();
	    if (sample != null) {
			 storeCpuTime(sample);
		}
	}

	private void storeCpuTime(Sample sample) {
		DataPoint[] dataPoints= sample.getDataPoints();
		System.out.println("Scenario '" + getReadableName()+ "':"); //$NON-NLS-1$ //$NON-NLS-2$
		if (dataPoints.length > 0) {
			StatisticsSession s= new StatisticsSession(dataPoints);
			Dim[] dimensions= dataPoints[0].getDimensions();
			if (dimensions.length > 0) {
				for (int i= 0; i < dimensions.length; i++) {
				    Dim dimension= dimensions[i];
					if (dimension.getName().equals("CPU Time")) {
						Statistics stat = new Statistics(s, dimension);
					    CPU_TIMES.put(getReadableName(), stat);
						System.out.println("	- CPU Time: "+stat.toString());
					} else if (dimension.getName().startsWith("Elapsed")) {
						Statistics stat = new Statistics(s, dimension);
					    ELAPSED_TIMES.put(getReadableName(), stat);
						System.out.println("	- Elapsed process: "+stat.toString());
					}
				}
			}
		}
	}

	public String getReadableName() {
		String name = getScenarioName();
		return name.substring(name.lastIndexOf('.')+1, name.length()-2);
	}

	public String getShortName() {
		String name = getReadableName();
		return name.substring(name.lastIndexOf('#')+5/*1+"test".length()*/, name.length());
	}

}
