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

package eu.dc4cities.connectors.ForecastENEAConnector.data;

/**
*
 *
*/

public class Data
{
	private String dataType;
	private long timestamp;
    private float ghi;
    
    public Data(long timestamp, float ghi)
    {
    	this.ghi = ghi;
    	this.timestamp = timestamp;
    }
    
    public String getDataType()
    {
    	return this.dataType;
    }
    
    public void setDataType(String dataType)
    {
    	this.dataType = dataType;
    }
    
    public float getGHI()
    {
    	return this.ghi;
    }
    
    public void setGHI(float ghi)
    {
    	this.ghi = ghi;
    }
    
    public long getTimestamp()
    {
    	return this.timestamp;
    }
    
    public void setTimestamp(long timestamp)
    {
    	this.timestamp = timestamp;
    }
}
