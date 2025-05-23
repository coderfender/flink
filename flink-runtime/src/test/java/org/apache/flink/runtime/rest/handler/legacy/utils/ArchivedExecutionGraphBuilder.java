/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.runtime.rest.handler.legacy.utils;

import org.apache.flink.api.common.ArchivedExecutionConfig;
import org.apache.flink.api.common.JobID;
import org.apache.flink.api.common.JobStatus;
import org.apache.flink.runtime.accumulators.StringifiedAccumulatorResult;
import org.apache.flink.runtime.checkpoint.CheckpointStatsSnapshot;
import org.apache.flink.runtime.executiongraph.ArchivedExecutionGraph;
import org.apache.flink.runtime.executiongraph.ArchivedExecutionJobVertex;
import org.apache.flink.runtime.executiongraph.ErrorInfo;
import org.apache.flink.runtime.jobgraph.JobType;
import org.apache.flink.runtime.jobgraph.JobVertexID;
import org.apache.flink.runtime.rest.messages.JobPlanInfo;
import org.apache.flink.util.OptionalFailure;
import org.apache.flink.util.Preconditions;
import org.apache.flink.util.SerializedValue;
import org.apache.flink.util.TernaryBoolean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

/** Utility class for constructing an ArchivedExecutionGraph. */
public class ArchivedExecutionGraphBuilder {

    private static final Random RANDOM = new Random();

    private JobID jobID;
    private String jobName;
    private Map<JobVertexID, ArchivedExecutionJobVertex> tasks;
    private List<ArchivedExecutionJobVertex> verticesInCreationOrder;
    private long[] stateTimestamps;
    private JobStatus state;
    private ErrorInfo failureCause;
    private JobPlanInfo.Plan plan;
    private StringifiedAccumulatorResult[] archivedUserAccumulators;
    private ArchivedExecutionConfig archivedExecutionConfig;
    private boolean isStoppable;
    private Map<String, SerializedValue<OptionalFailure<Object>>> serializedUserAccumulators;
    private CheckpointStatsSnapshot checkpointStatsSnapshot;
    private String streamGraphJson;
    private int pendingOperatorCounts = 0;

    public ArchivedExecutionGraphBuilder setJobID(JobID jobID) {
        this.jobID = jobID;
        return this;
    }

    public ArchivedExecutionGraphBuilder setJobName(String jobName) {
        this.jobName = jobName;
        return this;
    }

    public ArchivedExecutionGraphBuilder setTasks(
            Map<JobVertexID, ArchivedExecutionJobVertex> tasks) {
        this.tasks = tasks;
        return this;
    }

    public ArchivedExecutionGraphBuilder setVerticesInCreationOrder(
            List<ArchivedExecutionJobVertex> verticesInCreationOrder) {
        this.verticesInCreationOrder = verticesInCreationOrder;
        return this;
    }

    public ArchivedExecutionGraphBuilder setStateTimestamps(long[] stateTimestamps) {
        Preconditions.checkArgument(stateTimestamps.length == JobStatus.values().length);
        this.stateTimestamps = stateTimestamps;
        return this;
    }

    public ArchivedExecutionGraphBuilder setState(JobStatus state) {
        this.state = state;
        return this;
    }

    public ArchivedExecutionGraphBuilder setFailureCause(ErrorInfo failureCause) {
        this.failureCause = failureCause;
        return this;
    }

    public ArchivedExecutionGraphBuilder setPlan(JobPlanInfo.Plan plan) {
        this.plan = plan;
        return this;
    }

    public ArchivedExecutionGraphBuilder setStreamGraphJson(String streamGraphJson) {
        this.streamGraphJson = streamGraphJson;
        return this;
    }

    public ArchivedExecutionGraphBuilder setArchivedUserAccumulators(
            StringifiedAccumulatorResult[] archivedUserAccumulators) {
        this.archivedUserAccumulators = archivedUserAccumulators;
        return this;
    }

    public ArchivedExecutionGraphBuilder setArchivedExecutionConfig(
            ArchivedExecutionConfig archivedExecutionConfig) {
        this.archivedExecutionConfig = archivedExecutionConfig;
        return this;
    }

    public ArchivedExecutionGraphBuilder setStoppable(boolean stoppable) {
        isStoppable = stoppable;
        return this;
    }

    public ArchivedExecutionGraphBuilder setSerializedUserAccumulators(
            Map<String, SerializedValue<OptionalFailure<Object>>> serializedUserAccumulators) {
        this.serializedUserAccumulators = serializedUserAccumulators;
        return this;
    }

    public ArchivedExecutionGraphBuilder setCheckpointStatsSnapshot(
            CheckpointStatsSnapshot checkpointStatsSnapshot) {
        this.checkpointStatsSnapshot = checkpointStatsSnapshot;
        return this;
    }

    public ArchivedExecutionGraphBuilder setPendingOperatorCounts(int pendingOperatorCounts) {
        this.pendingOperatorCounts = pendingOperatorCounts;
        return this;
    }

    public ArchivedExecutionGraph build() {
        JobID jobID = this.jobID != null ? this.jobID : new JobID();
        String jobName = this.jobName != null ? this.jobName : "job_" + RANDOM.nextInt();

        if (tasks == null) {
            tasks = Collections.emptyMap();
        }

        return new ArchivedExecutionGraph(
                jobID,
                jobName,
                tasks,
                verticesInCreationOrder != null
                        ? verticesInCreationOrder
                        : new ArrayList<>(tasks.values()),
                stateTimestamps != null ? stateTimestamps : new long[JobStatus.values().length],
                state != null ? state : JobStatus.FINISHED,
                JobType.STREAMING,
                failureCause,
                plan != null
                        ? plan
                        : new JobPlanInfo.Plan(jobID.toString(), jobName, "", new ArrayList<>()),
                archivedUserAccumulators != null
                        ? archivedUserAccumulators
                        : new StringifiedAccumulatorResult[0],
                serializedUserAccumulators != null
                        ? serializedUserAccumulators
                        : Collections.emptyMap(),
                archivedExecutionConfig != null
                        ? archivedExecutionConfig
                        : new ArchivedExecutionConfigBuilder().build(),
                isStoppable,
                null,
                checkpointStatsSnapshot,
                "stateBackendName",
                "checkpointStorageName",
                TernaryBoolean.UNDEFINED,
                "changelogStorageName",
                streamGraphJson,
                pendingOperatorCounts);
    }
}
