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

package eu.dc4cities.controlsystem.modules.processcontroller;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.joda.time.DateTime;

import java.util.List;

/**
 * The input for the alerts feed API in Energis. Defines a set of alerts to delete and a set of alerts to save.
 * Fields names must conform to the Energis API for correct JSON serialization.
 */
public class EnergisAlertFeed {

	private String companyCode;
	private EnergisAlertFeedDelete delete;
	private List<EnergisAlert> save;
	
	@JsonCreator
	public EnergisAlertFeed(@JsonProperty("companyCode") String companyCode) {
		this.companyCode = companyCode;
	}
	
	/**
	 * Returns the code of the company for which alerts have to be updated.
	 * 
	 * @return the company code
	 */
	public String getCompanyCode() {
		return companyCode;
	}

	/**
	 * Returns details about alerts to delete from Energis.
	 * 
	 * @return the delete criteria
	 */
	public EnergisAlertFeedDelete getDelete() {
		return delete;
	}

	public void setDelete(EnergisAlertFeedDelete delete) {
		this.delete = delete;
	}

	/**
	 * Returns the list of alerts to save into Energis.
	 * 
	 * @return the new alert list
	 */
	public List<EnergisAlert> getSave() {
		return save;
	}

	public void setSave(List<EnergisAlert> save) {
		this.save = save;
	}

	public static class EnergisAlertFeedDelete {
		
		private String assetCode;
		private DateTime occurringSinceFrom;
		private DateTime occurringSinceTo;
		
		/**
		 * Returns the code of the asset for which alerts have to be deleted. Includes all descendant assets. E.g. if
		 * assetCode is "asset1", alerts for "asset1.child1" and "asset1.child2" will also be deleted.
		 * 
		 * @return the asset code
		 */
		public String getAssetCode() {
			return assetCode;
		}

		public void setAssetCode(String assetCode) {
			this.assetCode = assetCode;
		}
		
		/**
		 * Returns the start of the interval for which alerts are to be deleted, based on the occurringSince property.
		 * 
		 * @return the start of the delete interval
		 */
		@JsonFormat(shape = Shape.STRING, pattern = EnergisAlert.DATE_FORMAT)
		public DateTime getOccurringSinceFrom() {
			return occurringSinceFrom;
		}
		
		public void setOccurringSinceFrom(DateTime occurringSinceFrom) {
			this.occurringSinceFrom = occurringSinceFrom;
		}
		
		/**
		 * Returns the end of the interval for which alerts are to be deleted, based on the occurringSince property.
		 * 
		 * @return the end of the delete interval
		 */
		@JsonFormat(shape = Shape.STRING, pattern = EnergisAlert.DATE_FORMAT)
		public DateTime getOccurringSinceTo() {
			return occurringSinceTo;
		}
		
		public void setOccurringSinceTo(DateTime occurringSinceTo) {
			this.occurringSinceTo = occurringSinceTo;
		}
		
	}
	
}
