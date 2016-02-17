/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.syncope.client.console.widgets;

import com.pingunaut.wicket.chartjs.chart.impl.Line;
import com.pingunaut.wicket.chartjs.core.panel.LineChartPanel;
import com.pingunaut.wicket.chartjs.data.sets.LineDataSet;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.syncope.common.lib.info.SystemInfo;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;

public class LoadWidget extends AbstractWidget {

    private static final long serialVersionUID = -816175678514035085L;

    public LoadWidget(final String id, final SystemInfo systeminfo) {
        super(id);

        add(new Label("hostname", systeminfo.getHostname()));
        add(new Label("os", systeminfo.getOs()));
        add(new Label("jvm", systeminfo.getJvm()));

        List<String> labels = new ArrayList<>();

        List<Double> cpuValues = new ArrayList<>();
        List<Long> memValues = new ArrayList<>();

        for (SystemInfo.LoadInstant instant : systeminfo.getLoad()) {
            labels.add(DateFormatUtils.ISO_DATETIME_FORMAT.format(systeminfo.getStartTime() + instant.getUptime()));

            cpuValues.add(instant.getSystemLoadAverage() * 1000);
            memValues.add(instant.getTotalMemory());
        }

        Line line = new Line();
        line.getOptions().setPointDot(false);
        line.getOptions().setDatasetFill(false);
        line.getOptions().setResponsive(true);
        line.getOptions().setMaintainAspectRatio(true);
        line.getOptions().setShowScale(false);
        line.getOptions().setMultiTooltipTemplate("<%= datasetLabel %>");

        line.getData().setLabels(labels);

        List<LineDataSet> datasets = new ArrayList<>();
        LineDataSet cpuDataSet = new LineDataSet(cpuValues);
        cpuDataSet.setLabel("CPU");
        cpuDataSet.setPointColor("purple");
        cpuDataSet.setStrokeColor("purple");
        datasets.add(cpuDataSet);

        LineDataSet memDataSet = new LineDataSet(memValues);
        memDataSet.setLabel("MEM");
        memDataSet.setPointColor("grey");
        memDataSet.setStrokeColor("grey");
        datasets.add(memDataSet);
        line.getData().setDatasets(datasets);

        add(new LineChartPanel("chart", Model.of(line)));
    }

}
