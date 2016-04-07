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

package eu.dc4cities.energis.client.builder;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class ExecuteBuilderTest {

    @Test(expected = NullPointerException.class)
    public void shouldRejectMetricNameNull() {
        ExecuteBuilder.getInstance().setMetricName(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldRejectMetricNameEmpty() {
        ExecuteBuilder.getInstance().setMetricName("");
    }

    @Test(expected = NullPointerException.class)
    public void shouldRejectCompanyCodeNull() {
        ExecuteBuilder.getInstance().setCompanyCode(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldRejectCompanyCodeEmpty() {
        ExecuteBuilder.getInstance().setCompanyCode("");
    }

    @Test(expected = NullPointerException.class)
    public void shouldRejectAssetCodeNull() {
        ExecuteBuilder.getInstance().setAssetCode(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldRejectAssetCodeEmpty() {
        ExecuteBuilder.getInstance().setAssetCode("");
    }

    @Test(expected = NullPointerException.class)
    public void shouldRejectInputNull() {
        ExecuteBuilder.getInstance().addInput(null);
    }
    
    @Test
    public void shouldBuildQueryWithAbsoluteTimes() throws IOException {
        String json = Resources.toString(Resources.getResource("execute_formula_with_inputs.json"), Charsets.UTF_8);

        ExecuteBuilder builder = ExecuteBuilder.getInstance();
        builder.setMetricName("metric1")
                .setCompanyCode("company1")
                .setAssetCode("asset1")
                .addInput(new FormulaInput("x1", Arrays.asList(10.0, 11.0, 12.0)))
                .addInput(new FormulaInput("x2", Arrays.asList(20.0, 21.0, 22.0)));
        assertThat(builder.build(), equalTo(json));
    }

}
