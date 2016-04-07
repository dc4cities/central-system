/*
 * Copyright 2016 The DC4Cities author.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.dc4cities.controlsystem.modules.optionconsolidator.opcp;

import eu.dc4cities.controlsystem.model.easc.EascActivityPlan;

import java.util.ArrayList;
import java.util.List;

/**
 * Statistics about a solving process.
 *
 *
 */
public class Statistics {

    private String status = "";

    private long start = -1;

    private long end = -1;

    private List<EascActivityPlan> solution;

    private List<Score> scores;

    /**
     * New statistics.
     */
    public Statistics() {
        scores = new ArrayList<>();
    }

    public Statistics start() {
        start = System.currentTimeMillis();
        return this;
    }

    public Statistics noSolution() {
        status = "ko";
        end = System.currentTimeMillis();
        return this;
    }

    public Statistics newSolution(Number n) {
        scores.add(new Score(n, System.currentTimeMillis() - start));
        return this;
    }

    public Statistics retainedPlan(List<EascActivityPlan> p) {
        solution = p;
        return this;
    }

    public List<EascActivityPlan> retainedPlan() {
        return solution;
    }
    /**
     * Signal the computation termination.
     *
     * @param timeout {@code true} if the solver hit the timeout
     * @return {@code this}
     */
    public Statistics finished(boolean timeout) {
        status = timeout ? "to" : "ok";
        end = System.currentTimeMillis();
        return this;
    }


    public Statistics timeout() {
        status = "to";
        end = System.currentTimeMillis();
        return this;
    }

    public Statistics terminated() {
        status = "ok";
        end = System.currentTimeMillis();
        return this;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < scores.size(); i++) {
            b.append(" ").append(scores.get(i));
        }
        b.append(" ").append(end - start).append("@").append(status);
        return b.toString();
    }

    public List<Score> scores() {
        return scores;
    }

    static class Score {
        public Number value;
        public long timestamp;

        public Score(Number v, long ts) {
            value = v;
            this.timestamp = ts;
        }

        @Override
        public String toString() {
            return value + " " + timestamp;
        }
    }
}
