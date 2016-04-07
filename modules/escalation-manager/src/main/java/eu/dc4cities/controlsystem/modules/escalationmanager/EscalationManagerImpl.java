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

package eu.dc4cities.controlsystem.modules.escalationmanager;

import eu.dc4cities.configuration.goal.Objective;
import eu.dc4cities.configuration.goal.Target;
import eu.dc4cities.controlsystem.model.TimeParameters;
import eu.dc4cities.controlsystem.model.TimeSlotBasedEntity;
import eu.dc4cities.controlsystem.model.TimeSlotPower;
import eu.dc4cities.controlsystem.model.datacenter.*;
import eu.dc4cities.controlsystem.model.easc.*;
import eu.dc4cities.controlsystem.model.erds.ErdsForecast;
import eu.dc4cities.controlsystem.model.erds.TimeSlotErdsForecast;
import eu.dc4cities.controlsystem.modules.EscalationManager;
import org.joda.time.DateTime;
import org.jscience.physics.amount.Amount;
import org.springframework.stereotype.Component;

import javax.measure.quantity.Dimensionless;
import javax.measure.unit.SI;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


@Component
public class EscalationManagerImpl implements EscalationManager 
{	
	public static String DAY_CURRENT = "current";
	public static String DAY_NEXT = "next";
	
	// ******************************************** //
	// Warning threshold, in percentage [0..100] 
	private int warnThreshold;
	
	// invocation time for the evaluation
	private DateTime dateNow;

	// Evaluation results, per time slot
	private List<StatusAlert> alerts;	
	private DataCenterStatus dataCenterStatus;
	
	// ******************************************** //
	
	// # FOR INTERNAL COMPUTATION # //
	private String evalDataCenterName;
	private TimeSlotBasedEntity evalTimeRange;
	private List<Objective> evalDataCenterObjectives;
	private List<ErdsForecast> evalErdsForecasts;
	private List<EascActivitySpecifications> evalEascActivitySpecifications;
	private DataCenterPower evalDataCenterPowerActual; 
	private List<EascServiceLevels> evalEascServiceLevels;
	private List<EascActivityPlan> evalEascActivityPlans;
 	
	private Objective evalPowerObjective;
	private String currentEvaluationPeriod;
	// ############################ //
	
	private TimeSlotBasedEntity invokTimeRange;
		
	public EscalationManagerImpl() 
	{
		super();
	}
	
	@Override
	public DataCenterStatus determineDcStatus(String dataCenterName,
			TimeParameters timeParameters,
			List<Objective> dataCenterObjectives,
			List<ErdsForecast> erdsForecasts,
			List<EascActivitySpecifications> eascActivitySpecifications,
			DataCenterPower dataCenterPowerActual, 
			List<EascServiceLevels> eascServiceLevels,
			List<EascActivityPlan> eascActivityPlans) 
	{
		this.alerts = new LinkedList<StatusAlert>();
		this.evalDataCenterName = dataCenterName;
		this.evalDataCenterObjectives = dataCenterObjectives;
		this.evalEascActivitySpecifications = eascActivitySpecifications;
		this.evalDataCenterPowerActual = dataCenterPowerActual;
		this.evalEascServiceLevels = eascServiceLevels;
		this.dateNow = timeParameters.getDateNow();
		TimeSlotBasedEntity timeRange = new TimeSlotBasedEntity(timeParameters);
		this.invokTimeRange = timeRange;
		
		// split data structures for current day and the day next
		// 
		// CURRENT DAY EVALUATION
		this.currentEvaluationPeriod = EscalationManagerImpl.DAY_CURRENT;
		splitTimeRange(timeRange, erdsForecasts, eascActivityPlans);
		evaluateSystemStatus();
		
		if((timeRange.getDateTo().getMillis() - timeRange.getDateFrom().plusDays(1).getMillis()) != 0)
		{
			// LONG TERM EVALUATION
			this.currentEvaluationPeriod = EscalationManagerImpl.DAY_NEXT;
			splitTimeRange(timeRange, erdsForecasts, eascActivityPlans);
			evaluateSystemStatus();
		}
			
		this.dataCenterStatus = new DataCenterStatus(dataCenterName);
		this.dataCenterStatus.setDateFrom(timeRange.getDateFrom());
		this.dataCenterStatus.setDateTo(timeRange.getDateTo());
		this.dataCenterStatus.setTimeSlotDuration(timeRange.getTimeSlotDuration());
		this.dataCenterStatus.setAlerts(this.alerts);
				
		return this.dataCenterStatus;
	}

		
	private void evaluateSystemStatus()
	{
		// construction of the cumulative data structure for the evaluation
		List<AggregateActivityValues> aggrValues = new LinkedList<AggregateActivityValues>();
		initialize(aggrValues);
		if(this.evalDataCenterPowerActual!=null) addPastPowerData(aggrValues);
		
		// for all EascActivityPlan
		for(int i=0 ; i<this.evalEascActivityPlans.size() ; i++)
		{			
			List<Activity> acts = this.evalEascActivityPlans.get(i).getActivities();
			// for all the Activity in the EascActPlan
			for(int j=0 ; j<acts.size() ; j++)
			{
				List<ActivityDataCenter> actDC = acts.get(j).getDataCenters();
				// for all the DC involved in the Activity
				for(int k=0 ; k<actDC.size() ; k++)
				{
					if(actDC.get(k).getDataCenterName().equalsIgnoreCase(this.evalDataCenterName))
					{
						List<Work> works = actDC.get(k).getWorks();
						addWorks(aggrValues, works, acts.get(j), this.evalEascActivityPlans.get(i));
					}
				}
			}
		}		
		evaluateTimeSlotStatus(aggrValues);
		
		/*
		if (this.currentEvaluationPeriod.equals(DAY_CURRENT))
		{
			calculateCompletedCumulativeBusinessStatus();
		}		
		calculateCumulativeBusinessStatus();
		*/
		
		computeCumulativeBusinessStatus();
	}

	private void initialize(List<AggregateActivityValues> aggrValues) 
	{
		int timeSlotNumber = this.evalTimeRange.getNumOfTimeSlots();
		DateTime currentTimeStamp = this.evalTimeRange.getDateFrom();
		for (int i=0 ; i<timeSlotNumber ; i++)
		{
			aggrValues.add(new AggregateActivityValues(currentTimeStamp));
			currentTimeStamp = currentTimeStamp.plus(this.evalTimeRange.getTimeSlotDuration().longValue(SI.MILLI(SI.SECOND)));
		}		
	}
	
	private void addPastPowerData(List<AggregateActivityValues> aggrValues) 
	{
		List<TimeSlotPower> tsp = this.evalDataCenterPowerActual.getPowerValues();
		for(int i=0 ; i<tsp.size() ; i++)
		{
			aggrValues.get(i).addWorkContribution(tsp.get(i).getPower(), null, null);
		}
	}
		

	private void evaluateTimeSlotStatus(List<AggregateActivityValues> aggrValues) 
	{
		for(int i=0 ; i<aggrValues.size() ; i++)
		{
			AggregateActivityValues currentEval = aggrValues.get(i);
			
			calculateRenPctStatus(currentEval, i);
			if(currentEval.getTimestamp().isBefore(this.dateNow))
				calculatePastInstantBusinessStatus(currentEval, i);
			else calculateFutureInstantBusinessStatus(currentEval, i);			
		}				
	}
	
	private void calculateRenPctStatus(AggregateActivityValues currentEval, int timeslot) 
	{
		List<PowerRenPercent> sortedErdsForecast = sort(currentEval.getTimestamp(), this.evalErdsForecasts);
		
		double totalPowCons = currentEval.getTotalPowerConsumption().doubleValue(SI.WATT);
		double remainingPower = totalPowCons;
		List<PowerRenPercent> usedErds = new LinkedList<PowerRenPercent>();
		
		for (int j=0 ; remainingPower>0.0 && j<sortedErdsForecast.size() ; j++)
		{
			PowerRenPercent currentErds = sortedErdsForecast.get(j);
			double remaining = remainingPower - currentErds.getPower().doubleValue(SI.WATT);
			if(remaining<0.0)
			{
				double newPower = currentErds.getPower().doubleValue(SI.WATT)+remaining;
				currentErds.setPower(Amount.valueOf(newPower, SI.WATT));
			}
			remainingPower = remainingPower - currentErds.getPower().doubleValue(SI.WATT);
			usedErds.add(currentErds);				
		}
		
		double totalRenPow = 0.0;		
		for(int j=0 ; j<usedErds.size() ; j++)
		{
			PowerRenPercent prp = usedErds.get(j);
			double currentRenPercent = prp.getPower().doubleValue(SI.WATT)*prp.getRenPercent().doubleValue(Dimensionless.UNIT);
			
			totalRenPow += currentRenPercent;	
			//weightedRenPercent += currentRenPercent*prp.getRenPercent().doubleValue(Dimensionless.UNIT);
		}
		
		double weightedRenPercent = (totalRenPow/totalPowCons)*100;
		//weightedRenPercent /= totalRenPow;
		//weightedRenPercent *= 100;
		
		// to calculate in dependence of the renPercent objective
		
		this.evalPowerObjective = null;
		
		for(int j=0 ; j<this.evalDataCenterObjectives.size() ; j++)
		{
			Objective o = this.evalDataCenterObjectives.get(j);
			if (o.getDataCenterId().equalsIgnoreCase(this.evalDataCenterName))
			{
				if (o.getId().equalsIgnoreCase("renewableObjective"))
					this.evalPowerObjective = o;					
			}				
		}
		
		if (this.evalPowerObjective==null) throw new RuntimeException("No renewableObjective found");
		
		String operator = this.evalPowerObjective.getTarget().getOperator();
		double targetValue = this.evalPowerObjective.getTarget().getValue();
				
		AlertSeverity status=null;;
		
		if (operator.equalsIgnoreCase(Target.GREATER_THAN))
		{
			if (weightedRenPercent>targetValue) status = null;
			else if (weightedRenPercent>(targetValue*this.warnThreshold/100)) status = AlertSeverity.WARNING;
			else status = AlertSeverity.ALARM;
		}
		else if (operator.equalsIgnoreCase(Target.GREATER_EQUALS))
		{
			if (weightedRenPercent>=targetValue) status = null;
			else if (weightedRenPercent>(targetValue*this.warnThreshold/100)) status = AlertSeverity.WARNING;
			else status = AlertSeverity.ALARM;
		}
		else if (operator.equalsIgnoreCase(Target.LESS_THAN))
		{
			if (weightedRenPercent<targetValue) status = null;
			else if (weightedRenPercent<(targetValue*this.warnThreshold/100)) status = AlertSeverity.WARNING;
			else status = AlertSeverity.ALARM;
		}
		else if (operator.equalsIgnoreCase(Target.LESS_EQUALS))
		{
			if (weightedRenPercent<=targetValue) status = null;
			else if (weightedRenPercent<(targetValue*this.warnThreshold/100)) status = AlertSeverity.WARNING;
			else status = AlertSeverity.ALARM;
		}
		else if (operator.equalsIgnoreCase(Target.EQUALS))
		{
			if (weightedRenPercent==targetValue) status = null;
			else if ((weightedRenPercent<(targetValue*this.warnThreshold/100)) || (weightedRenPercent>(targetValue*this.warnThreshold/100))) status = AlertSeverity.WARNING;
			else status = AlertSeverity.ALARM;
		}
					
		if(status != null)
		{
			StatusAlert alert = new StatusAlert();
			if(this.currentEvaluationPeriod.equalsIgnoreCase(EscalationManagerImpl.DAY_NEXT))
			{
				timeslot += this.evalTimeRange.getNumOfTimeSlots();
			}
			alert.setStartTimeSlot(timeslot);
			alert.setEndTimeSlot(timeslot+1);
			alert.setType(AlertType.RENPCT);
			alert.setSeverity(status);
			alert.setMessage("RenPct objective: "+(Math.round(targetValue*10.0)/10)+" - timeslot value: "+(Math.round(weightedRenPercent*10.0)/10));			
			this.alerts.add(alert);
		}
		
	}

	private void calculatePastInstantBusinessStatus(AggregateActivityValues currentEval, int timeslot) 
	{					
		for(int i=0 ; i<this.evalEascServiceLevels.size() ; i++)
		{
			EascServiceLevels eascSL = this.evalEascServiceLevels.get(i);
			String eascName = eascSL.getEascName();
			
			for(int j=0 ; j<eascSL.getActivityServiceLevels().size() ; j++)
			{
				ActivityServiceLevels actSL = eascSL.getActivityServiceLevels().get(j);
				String activityName = actSL.getActivityName();				
				ServiceLevelObjective slObjective = findServiceLevelObjective(eascName, activityName, currentEval.timestamp);
				if (slObjective==null) throw new RuntimeException("Error: Service Level Objective not found");
				
				if (slObjective.getInstantBusinessObjective()!=null)
				{				
					List<ServiceLevel> serviceLevels = actSL.getServiceLevels();
					for(int z=0 ; z<serviceLevels.size() ; z++)
					{
						ServiceLevel sl = serviceLevels.get(z);
						if((currentEval.timestamp.isEqual(sl.getDateFrom()) || currentEval.timestamp.isAfter(sl.getDateFrom())) &&  currentEval.timestamp.isBefore(sl.getDateTo()))
						{
							Amount<?> slAmount = sl.getInstantBusinessPerformance();
							
							if (slAmount!=null && slAmount.getEstimatedValue()!=0.0)
							{
								double slValue = slAmount.getEstimatedValue();
								Amount<?> slObjectiveAmount = slObjective.getInstantBusinessObjective();																						
								double slObjectiveValue = slObjectiveAmount.getEstimatedValue();
								
								if (!(slAmount.getUnit().toString()).equals((slObjectiveAmount.getUnit().toString())))
									throw new RuntimeException("Error: Service Level Amount not of the same measure unit");
																						
								if(slValue<slObjectiveValue)
								{
									StatusAlert alert = new StatusAlert();
									if(this.currentEvaluationPeriod.equalsIgnoreCase(EscalationManagerImpl.DAY_NEXT))
									{
										timeslot += this.evalTimeRange.getNumOfTimeSlots();
									}
									alert.setStartTimeSlot(timeslot);
									alert.setEndTimeSlot(timeslot+1);
									alert.setType(AlertType.BIZPERF);
									alert.setEascName(eascName);
									alert.setActivityName(activityName);
									if( slValue >= (slObjectiveValue*this.warnThreshold/100) )
									{
										alert.setSeverity(AlertSeverity.WARNING);
									}
									else alert.setSeverity(AlertSeverity.ALARM);
									String message = "Actual Performed Service Level: "+slValue+" "+slAmount.getUnit().toString() +
											", Instant Service Level Objective: "+slObjectiveValue+" "+slObjectiveAmount.getUnit().toString() +
											" (performance BELOW the Instant Service Level Objective)";
									alert.setMessage(message);
									
									this.alerts.add(alert);
								}
							}						
						}
					}
				}
			}
		}
	}

	private void calculateFutureInstantBusinessStatus(AggregateActivityValues currentEval, int timeslot) 
	{					
		for(int i=0 ; i<currentEval.getInvolvedEascActivities().size() ; i++)
		{
			EascAct eascAct = currentEval.getInvolvedEascActivities().get(i);
			String eascName = eascAct.getEascName();
			
			for(int j=0 ; j<eascAct.getInvolvedActivities().size() ; j++)
			{
				Activity activity = eascAct.getInvolvedActivities().get(j);
				String activityName = activity.getName();
				ServiceLevelObjective slObjective = findServiceLevelObjective(eascName, activityName, currentEval.timestamp);
				if (slObjective==null) throw new RuntimeException("Error: Service Level Objective not found");
				
				if (slObjective.getInstantBusinessObjective()!=null)
				{
					List<ServiceLevel> serviceLevels = activity.getServiceLevels();
					for(int z=0 ; z<serviceLevels.size() ; z++)
					{
						ServiceLevel sl = serviceLevels.get(z);
						if((currentEval.timestamp.isEqual(sl.getDateFrom()) || currentEval.timestamp.isAfter(sl.getDateFrom())) &&  currentEval.timestamp.isBefore(sl.getDateTo()))
						{
							Amount<?> slAmount = sl.getInstantBusinessPerformance();
							
							if (slAmount!=null && slAmount.getEstimatedValue()!=0.0)
							{
								double slValue = slAmount.getEstimatedValue();
								
								Amount<?> slObjectiveAmount = slObjective.getInstantBusinessObjective();
																						
								double slObjectiveValue = slObjectiveAmount.getEstimatedValue();
								
								if (!(slAmount.getUnit().toString()).equals((slObjectiveAmount.getUnit().toString())))
									throw new RuntimeException("Error: Service Level Amount not of the same measure unit");
																						
								if(slValue<slObjectiveValue)
								{
									StatusAlert alert = new StatusAlert();
									if(this.currentEvaluationPeriod.equalsIgnoreCase(EscalationManagerImpl.DAY_NEXT))
									{
										timeslot += this.evalTimeRange.getNumOfTimeSlots();
									}
									alert.setStartTimeSlot(timeslot);
									alert.setEndTimeSlot(timeslot+1);
									alert.setType(AlertType.BIZPERF);
									alert.setEascName(eascName);
									alert.setActivityName(activityName);
									if( slValue >= (slObjectiveValue*this.warnThreshold/100) )
									{
										alert.setSeverity(AlertSeverity.WARNING);
									}
									else alert.setSeverity(AlertSeverity.ALARM);
									String message = "Actual Performed Service Level: "+slValue+" "+slAmount.getUnit().toString() +
											", Instant Service Level Objective: "+slObjectiveValue+" "+slObjectiveAmount.getUnit().toString() +
											" (performance BELOW the Instant Service Level Objective)";
									alert.setMessage(message);
									
									this.alerts.add(alert);
								}
							}						
						}
					}
				}
			}
		}
	}
	
	
	//for cumulative activities yet completed
	//at the invoke time
	private void calculateCompletedCumulativeBusinessStatus()
	{
		for(int i=0 ; i<this.evalEascServiceLevels.size() ; i++)
		{
			String eascName = this.evalEascServiceLevels.get(i).getEascName();
			
			List<ActivityServiceLevels> lasl = this.evalEascServiceLevels.get(i).getActivityServiceLevels();
			for (int j=0 ; j<lasl.size() ; j++)
			{				
				String activityName = lasl.get(j).getActivityName();				
				ServiceLevelObjective slObjective = findServiceLevelObjective(eascName, activityName, this.evalTimeRange.getDateFrom());
				if (slObjective==null) throw new RuntimeException("Error: Service Level Objective not found");
				
				if (slObjective.getCumulativeBusinessObjective()!=null)
				{
					List<ServiceLevel> lsl = lasl.get(j).getServiceLevels();
					if (!lsl.isEmpty())
					{
						ServiceLevel sl = lsl.get(lsl.size()-1);
						if (sl!=null)
						{
							if(	sl.getDateTo().isBefore(this.dateNow) ||
								sl.getDateTo().isEqual(this.dateNow) )
							{
								Amount<?> slAmount = sl.getCumulativeBusinessPerformance();
								double slValue = slAmount.getEstimatedValue();
								Amount<?> slObjectiveAmount = slObjective.getCumulativeBusinessObjective();																				
								double slObjectiveValue = slObjectiveAmount.getEstimatedValue();
								
								if(slValue<slObjectiveValue)
								{
									StatusAlert alert = new StatusAlert();
									int startTimeSlot = 0;
									int endTimeSlot = this.evalTimeRange.getNumOfTimeSlots();
									alert.setStartTimeSlot(startTimeSlot);
									alert.setEndTimeSlot(endTimeSlot);
									alert.setType(AlertType.BIZPERF);
									alert.setEascName(eascName);
									alert.setActivityName(activityName);
									if( slValue >= (slObjectiveValue*this.warnThreshold/100) )
									{
										alert.setSeverity(AlertSeverity.WARNING);
									}
									else alert.setSeverity(AlertSeverity.ALARM);
									String message = "Total Performed work: "+slValue+" "+slAmount.getUnit().toString() +
											", Total Expected work: "+slObjectiveValue+" "+slObjectiveAmount.getUnit().toString() +
											" (performance BELOW the Cumulative Service Level Objective)";
									alert.setMessage(message);
									
									this.alerts.add(alert);
								}
							}								
						}
					}
					
				}
			}
		}
	}
		
	
	//for cumulative activities not yet completed
	//at the invoke time	
	private void calculateCumulativeBusinessStatus() 
	{
		// for all EascActivityPlan
		for(int i=0 ; i<this.evalEascActivityPlans.size() ; i++)
		{
			String eascName = this.evalEascActivityPlans.get(i).getEascName();			
			List<Activity> acts = this.evalEascActivityPlans.get(i).getActivities();
			// for all the Activities in the EascActPlan
			for(int j=0 ; j<acts.size() ; j++)
			{
				Activity activity = acts.get(j);
				List<ActivityDataCenter> actDC = activity.getDataCenters();
				boolean actInMyDC = false;
				// for all the DC involved in the Activity
				for(int k=0 ; !actInMyDC && k<actDC.size() ; k++)
				{					
					if(actDC.get(k).getDataCenterName().equalsIgnoreCase(this.evalDataCenterName))
					{
						actInMyDC = true;
					}
				}				
				if(actInMyDC)
				{
					List<ServiceLevel> serviceLevels = activity.getServiceLevels();
					for (int s=0 ; s<serviceLevels.size() ; s++)
					{
						ServiceLevel sl = activity.getServiceLevels().get(s);
						if(sl.getCumulativeBusinessPerformance()!=null && 
								(sl.getDateFrom().isEqual(this.evalTimeRange.getDateFrom()) && sl.getDateTo().isEqual(sl.getDateTo())))
						{
							Amount<?> slAmount = sl.getCumulativeBusinessPerformance();
							double slValue = slAmount.getEstimatedValue();
							ServiceLevelObjective slObjective = findServiceLevelObjective(eascName, activity.getName(), this.evalTimeRange.getDateFrom());
							if (slObjective==null) throw new RuntimeException("Error: Service Level Objective not found");						
							Amount<?> slObjectiveAmount = slObjective.getCumulativeBusinessObjective();																				
							double slObjectiveValue = slObjectiveAmount.getEstimatedValue();
							slValue += getPastWorkDone(eascName, activity.getName(), slObjective);							
							
							if (!(slAmount.getUnit().toString()).equals((slObjectiveAmount.getUnit().toString())))
								throw new RuntimeException("Error: Service Level Amount not of the same measure unit");
																					
							if(slValue<slObjectiveValue)
							{
								StatusAlert alert = new StatusAlert();
								int startTimeSlot = 0;
								int endTimeSlot = this.evalTimeRange.getNumOfTimeSlots();
								if(this.currentEvaluationPeriod.equalsIgnoreCase(EscalationManagerImpl.DAY_NEXT))
								{
									startTimeSlot += this.evalTimeRange.getNumOfTimeSlots();
									endTimeSlot += this.evalTimeRange.getNumOfTimeSlots();
								}
								alert.setStartTimeSlot(startTimeSlot);
								alert.setEndTimeSlot(endTimeSlot);
								alert.setType(AlertType.BIZPERF);
								alert.setEascName(eascName);
								alert.setActivityName(activity.getName());
								if( slValue >= (slObjectiveValue*this.warnThreshold/100) )
								{
									alert.setSeverity(AlertSeverity.WARNING);
								}
								else alert.setSeverity(AlertSeverity.ALARM);
								String message = "Total Performed work: "+slValue+" "+slAmount.getUnit().toString() +
										", Total Expected work: "+slObjectiveValue+" "+slObjectiveAmount.getUnit().toString() +
										" (performance BELOW the Cumulative Service Level Objective)";
								alert.setMessage(message);
								
								this.alerts.add(alert);
							}
							
						}
					}
				}
			}
		}				
	}
	
	private void computeCumulativeBusinessStatus()
	{
		for(int i=0 ; i<this.evalEascActivitySpecifications.size() ; i++)
		{
			String eascName = this.evalEascActivitySpecifications.get(i).getEascName();
			List<ActivitySpecification> las = this.evalEascActivitySpecifications.get(i).getActivitySpecifications();
			for(int j=0 ; j<las.size() ; j++)
			{
				ActivitySpecification as = las.get(j);
				List<DataCenterSpecification> actDC = as.getDataCenters();
				String activityName = as.getActivityName();				
				boolean actInMyDC = false;
				// for all the DC involved in the Activity
				for(int k=0 ; !actInMyDC && k<actDC.size() ; k++)
				{					
					if(actDC.get(k).getDataCenterName().equalsIgnoreCase(this.evalDataCenterName))
					{
						actInMyDC = true;
					}
				}
				if(actInMyDC)
				{
					List<ServiceLevelObjective> lslo = as.getServiceLevelObjectives();
					if(!lslo.isEmpty())
					{
						if(lslo.get(0).getCumulativeBusinessObjective()!=null)
						{
							double slValue = computeInPast(eascName, activityName) + computeInFuture(eascName, activityName);
							
							ServiceLevelObjective slo = null;
							boolean found = false;
							for (int z=0 ; !found && z<lslo.size() ; z++)
							{
								slo = lslo.get(z);					 
								if((this.evalTimeRange.getDateFrom().isEqual(slo.getDateFrom()) || this.evalTimeRange.getDateFrom().isAfter(slo.getDateFrom())) && 
										this.evalTimeRange.getDateFrom().isBefore(slo.getDateTo()))
								{
									found = true;
								}
							}
							if (slo==null) throw new RuntimeException("Error: Service Level Objective not found");						
							Amount<?> sloAmount = slo.getCumulativeBusinessObjective();																				
							double sloValue = sloAmount.getEstimatedValue();							
																											
							if(slValue<sloValue)
							{
								StatusAlert alert = new StatusAlert();
								int startTimeSlot = 0;
								int endTimeSlot = this.evalTimeRange.getNumOfTimeSlots();
								if(this.currentEvaluationPeriod.equalsIgnoreCase(EscalationManagerImpl.DAY_NEXT))
								{
									startTimeSlot += this.evalTimeRange.getNumOfTimeSlots();
									endTimeSlot += this.evalTimeRange.getNumOfTimeSlots();
								}
								alert.setStartTimeSlot(startTimeSlot);
								alert.setEndTimeSlot(endTimeSlot);
								alert.setType(AlertType.BIZPERF);
								alert.setEascName(eascName);
								alert.setActivityName(activityName);
								if( slValue >= (sloValue*this.warnThreshold/100) )
								{
									alert.setSeverity(AlertSeverity.WARNING);
								}
								else alert.setSeverity(AlertSeverity.ALARM);
								String message = "Total Performed work: "+slValue+" "+sloAmount.getUnit().toString() +
										", Total Expected work: "+sloValue+" "+sloAmount.getUnit().toString() +
										" (performance BELOW the Cumulative Service Level Objective)";
								alert.setMessage(message);
								
								this.alerts.add(alert);
							}						
						}
					}					
				}				
			}
		}
	}


	private double computeInPast(String eascName, String activityName) 
	{
		for(int i=0 ; i<this.evalEascServiceLevels.size() ; i++)
		{
			if(this.evalEascServiceLevels.get(i).getEascName().equalsIgnoreCase(eascName))
			{
				List<ActivityServiceLevels> lasl = this.evalEascServiceLevels.get(i).getActivityServiceLevels();
				for (int j=0 ; j<lasl.size() ; j++)
				{
					if(lasl.get(j).getActivityName().equalsIgnoreCase(activityName))
					{
						List<ServiceLevel> lsl = lasl.get(j).getServiceLevels();
						if (!lsl.isEmpty())
						{
							ServiceLevel sl = lsl.get(lsl.size()-1);
							if (sl!=null)
							{
								return sl.getCumulativeBusinessPerformance().getEstimatedValue();																
							}							
						}														
					}
				}
			}
		}
		return 0.0;
	}
	
	private double computeInFuture(String eascName, String activityName) 
	{
		for(int i=0 ; i<this.evalEascActivityPlans.size() ; i++)
		{
			if(this.evalEascActivityPlans.get(i).getEascName().equalsIgnoreCase(eascName))
			{
				List<Activity> acts = this.evalEascActivityPlans.get(i).getActivities();
				for(int j=0 ; j<acts.size() ; j++)
				{
					if(acts.get(j).getName().equalsIgnoreCase(activityName))
					{
						List<ServiceLevel> serviceLevels = acts.get(j).getServiceLevels();
						for (int s=0 ; s<serviceLevels.size() ; s++)
						{
							ServiceLevel sl = serviceLevels.get(s);
							if(sl.getCumulativeBusinessPerformance()!=null && 
									(sl.getDateFrom().isEqual(this.evalTimeRange.getDateFrom()) && sl.getDateTo().isEqual(sl.getDateTo())))
							{
								return sl.getCumulativeBusinessPerformance().getEstimatedValue();									
							}
						}
					}
				}
			}
		}
		return 0.0;
	}
	
	

	private ServiceLevelObjective findServiceLevelObjective(String eascName, String activityName, DateTime timestamp) 
	{
		for(int i=0 ; i<this.evalEascActivitySpecifications.size() ; i++)
		{
			if(this.evalEascActivitySpecifications.get(i).getEascName().equalsIgnoreCase(eascName))
			{
				List<ActivitySpecification> las = this.evalEascActivitySpecifications.get(i).getActivitySpecifications();
				for(int j=0 ; j<las.size() ; j++)
				{
					ActivitySpecification as = las.get(j);
					if (as.getActivityName().equalsIgnoreCase(activityName))
					{
						List<ServiceLevelObjective> lslo = as.getServiceLevelObjectives();
						for (int z=0 ; z<lslo.size() ; z++)
						{
							ServiceLevelObjective slo = lslo.get(z);
					
							// both for instant and cumulative SLO 
							if((timestamp.isEqual(slo.getDateFrom())||timestamp.isAfter(slo.getDateFrom())) && timestamp.isBefore(slo.getDateTo()))
							{
								return slo;
							}
						}
					}
				}
			}				
		}
		return null;
	}
	
	private double getPastWorkDone(String eascName, String activityName, ServiceLevelObjective slo) 
	{
		for(int i=0 ; i<this.evalEascServiceLevels.size() ; i++)
		{
			if(this.evalEascServiceLevels.get(i).getEascName().equalsIgnoreCase(eascName))
			{
				List<ActivityServiceLevels> lasl = this.evalEascServiceLevels.get(i).getActivityServiceLevels();
				for (int j=0 ; j<lasl.size() ; j++)
				{
					if(lasl.get(j).getActivityName().equalsIgnoreCase(activityName))
					{
						List<ServiceLevel> lsl = lasl.get(j).getServiceLevels();
						if (!lsl.isEmpty())
						{
							ServiceLevel sl = lsl.get(lsl.size()-1);
							if (sl!=null)
							{
								if(	(sl.getDateFrom().isAfter(slo.getDateFrom()) || sl.getDateFrom().isEqual(slo.getDateFrom()))
											&&
									(sl.getDateTo().isBefore(slo.getDateTo()) || sl.getDateTo().isEqual(slo.getDateTo()))	
								  )
								{
									return sl.getCumulativeBusinessPerformance().getEstimatedValue();
								}								
							}							
						}														
					}
				}
			}
		}
		return 0.0;
	}

	private List<PowerRenPercent> sort(DateTime dateTime, List<ErdsForecast> evalErdsForecasts) 
	{
		List<PowerRenPercent> sortedRenPercent = new LinkedList<PowerRenPercent>();
		
		for(int i=0 ; i<evalErdsForecasts.size() ; i++)
		{
			ErdsForecast erds = evalErdsForecasts.get(i);
			for(int j=0 ; j<erds.getTimeSlotForecasts().size() ; j++)
			{
				TimeSlotErdsForecast tsef = erds.getTimeSlotForecasts().get(j);
				DateTime erdsTime = erds.getDateFrom().plus(tsef.getTimeSlot()*erds.getTimeSlotDuration().longValue(SI.MILLI(SI.SECOND)));
				if(erdsTime.equals(dateTime))
				{
					PowerRenPercent prp = new PowerRenPercent(tsef.getPower(), tsef.getRenewablePercentage());
					sortedRenPercent.add(prp);
				}
			}
		}
		Collections.sort(sortedRenPercent, Collections.reverseOrder());
		return sortedRenPercent;
	}

	private void addWorks(List<AggregateActivityValues> aggrValues, List<Work> works,
			Activity activity, EascActivityPlan easc) 
	{
		for(int i=0 ; i<works.size() ; i++)
		{
			Work w = works.get(i);
			int numTimeSlot = w.getEndTimeSlot()-w.getStartTimeSlot();
			
			//DateTime eascStartTime = easc.getDateFrom();
			
			long timeSlotInMillis = this.evalTimeRange.getTimeSlotDuration().longValue(SI.MILLI(SI.SECOND));
			long startWorkTimeInMillis = this.evalTimeRange.getDateFrom().getMillis() + timeSlotInMillis*w.getStartTimeSlot();
			
			DateTime workStartTime = new DateTime(startWorkTimeInMillis);
			DateTime workEndTime = new DateTime(startWorkTimeInMillis+(timeSlotInMillis*numTimeSlot));
			
			TimeSlotBasedEntity workInTimeEntity = new TimeSlotBasedEntity();
			workInTimeEntity.setTimeSlotDuration(this.evalTimeRange.getTimeSlotDuration());
			workInTimeEntity.setDateFrom(workStartTime);
			workInTimeEntity.setDateTo(workEndTime);
			
			if(this.isTimeIntersection(workInTimeEntity, this.evalTimeRange))
			{
				int startIndex = -1;
				int endIndex = -1;
				
				if(workStartTime.isBefore(this.evalTimeRange.getDateFrom()))
				{
					// time intersection at the end
					startIndex = 0;
					long startDateInMillis = this.evalTimeRange.getDateFrom().getMillis();
					endIndex = (int)((workEndTime.getMillis()-startDateInMillis)/timeSlotInMillis);
				}
				else if (workEndTime.isAfter(this.evalTimeRange.getDateTo()))
				{
					// time intersection at the start
					endIndex = this.evalTimeRange.getNumOfTimeSlots();
					startIndex = (int)((startWorkTimeInMillis-this.evalTimeRange.getDateFrom().getMillis())/timeSlotInMillis);					
				}
				else
				{
					// full intersection
					startIndex = (int)((startWorkTimeInMillis-this.evalTimeRange.getDateFrom().getMillis())/timeSlotInMillis);
					endIndex = startIndex+numTimeSlot;
				}
				
				//DateTime startTime = this.evalTimeRange.getDateFrom().plus(startIndex*timeSlotInMillis);
				
				for(int j=startIndex ; j<endIndex ; j++)
				{
					AggregateActivityValues aav = aggrValues.get(j);
					aav.addWorkContribution(w.getPower(), easc.getEascName(), activity);
				}
			}			
		}
		
	}

	private void splitTimeRange(TimeSlotBasedEntity timeRange, 
			List<ErdsForecast> erdsForecasts, 
			List<EascActivityPlan> eascActivityPlans) 
	{
		this.evalErdsForecasts = new LinkedList<ErdsForecast>();
		this.evalEascActivityPlans = new LinkedList<EascActivityPlan>();
		
		this.evalTimeRange = new TimeSlotBasedEntity();
		this.evalTimeRange.setTimeSlotDuration(timeRange.getTimeSlotDuration());
		
		if(this.currentEvaluationPeriod.equalsIgnoreCase(EscalationManagerImpl.DAY_CURRENT))
		{			
			this.evalTimeRange.setDateFrom(timeRange.getDateFrom());
			this.evalTimeRange.setDateTo(this.evalTimeRange.getDateFrom().plusDays(1));
		}
		else if(this.currentEvaluationPeriod.equalsIgnoreCase(EscalationManagerImpl.DAY_NEXT))
		{
			this.evalTimeRange.setDateTo(timeRange.getDateTo());
			this.evalTimeRange.setDateFrom(timeRange.getDateTo().minusDays(1));
		}
		else throw new RuntimeException("Unknown time range to evaluate");
		
		// add only erdsForecasts in the evalTimeRange
		for(int i=0 ; i<erdsForecasts.size() ; i++)
		{	
			if(isTimeIntersection(erdsForecasts.get(i), evalTimeRange))
			{
				this.evalErdsForecasts.add(erdsForecasts.get(i));
			}
		}
			
		// add only eascActivityPlans in the evalTimeRange
		for(int i=0 ; i<eascActivityPlans.size() ; i++)
		{
			if(isTimeIntersection(eascActivityPlans.get(i), evalTimeRange))
			{
				this.evalEascActivityPlans.add(eascActivityPlans.get(i));
			}
		}
	}

	private boolean isTimeIntersection(TimeSlotBasedEntity target, TimeSlotBasedEntity reference)
	{
		if(target.getDateTo().isBefore(reference.getDateFrom()) || target.getDateFrom().isAfter(reference.getDateTo()))
			return false;
		return true;
	}

	/**
	 * @return the dAY_CURRENT
	 */
	public static String getDAY_CURRENT() {
		return DAY_CURRENT;
	}

	/**
	 * @param dAY_CURRENT the dAY_CURRENT to set
	 */
	public static void setDAY_CURRENT(String dAY_CURRENT) {
		DAY_CURRENT = dAY_CURRENT;
	}

	/**
	 * @return the dAY_NEXT
	 */
	public static String getDAY_NEXT() {
		return DAY_NEXT;
	}

	/**
	 * @param dAY_NEXT the dAY_NEXT to set
	 */
	public static void setDAY_NEXT(String dAY_NEXT) {
		DAY_NEXT = dAY_NEXT;
	}

	/**
	 * @return the warnThreshold
	 */
	public int getWarnThreshold() {
		return warnThreshold;
	}

	/**
	 * @param warnThreshold the warnThreshold to set
	 */
	public void setWarnThreshold(int warnThreshold) {
		this.warnThreshold = warnThreshold;
	}

	/**
	 * @return the alerts
	 */
	public List<StatusAlert> getAlerts() {
		return alerts;
	}

	/**
	 * @param alerts the alerts to set
	 */
	public void setAlerts(List<StatusAlert> alerts) {
		this.alerts = alerts;
	}

	/**
	 * @return the dataCenterStatus
	 */
	public DataCenterStatus getDataCenterStatus() {
		return dataCenterStatus;
	}

	/**
	 * @param dataCenterStatus the dataCenterStatus to set
	 */
	public void setDataCenterStatus(DataCenterStatus dataCenterStatus) {
		this.dataCenterStatus = dataCenterStatus;
	}

	/**
	 * @return the evalDataCenterName
	 */
	public String getEvalDataCenterName() {
		return evalDataCenterName;
	}

	/**
	 * @param evalDataCenterName the evalDataCenterName to set
	 */
	public void setEvalDataCenterName(String evalDataCenterName) {
		this.evalDataCenterName = evalDataCenterName;
	}

	/**
	 * @return the evalTimeRange
	 */
	public TimeSlotBasedEntity getEvalTimeRange() {
		return evalTimeRange;
	}

	/**
	 * @param evalTimeRange the evalTimeRange to set
	 */
	public void setEvalTimeRange(TimeSlotBasedEntity evalTimeRange) {
		this.evalTimeRange = evalTimeRange;
	}

	/**
	 * @return the evalDataCenterObjectives
	 */
	public List<Objective> getEvalDataCenterObjectives() {
		return evalDataCenterObjectives;
	}

	/**
	 * @param evalDataCenterObjectives the evalDataCenterObjectives to set
	 */
	public void setEvalDataCenterObjectives(List<Objective> evalDataCenterObjectives) {
		this.evalDataCenterObjectives = evalDataCenterObjectives;
	}

	/**
	 * @return the evalErdsForecasts
	 */
	public List<ErdsForecast> getEvalErdsForecasts() {
		return evalErdsForecasts;
	}

	/**
	 * @param evalErdsForecasts the evalErdsForecasts to set
	 */
	public void setEvalErdsForecasts(List<ErdsForecast> evalErdsForecasts) {
		this.evalErdsForecasts = evalErdsForecasts;
	}

	/**
	 * @return the evalEascActivitySpecifications
	 */
	public List<EascActivitySpecifications> getEvalEascActivitySpecifications() {
		return evalEascActivitySpecifications;
	}

	/**
	 * @param evalEascActivitySpecifications the evalEascActivitySpecifications to set
	 */
	public void setEvalEascActivitySpecifications(List<EascActivitySpecifications> evalEascActivitySpecifications) {
		this.evalEascActivitySpecifications = evalEascActivitySpecifications;
	}

	/**
	 * @return the evalDataCenterPowerActual
	 */
	public DataCenterPower getEvalDataCenterPowerActual() {
		return evalDataCenterPowerActual;
	}

	/**
	 * @param evalDataCenterPowerActual the evalDataCenterPowerActual to set
	 */
	public void setEvalDataCenterPowerActual(DataCenterPower evalDataCenterPowerActual) {
		this.evalDataCenterPowerActual = evalDataCenterPowerActual;
	}

	/**
	 * @return the evalEascServiceLevels
	 */
	public List<EascServiceLevels> getEvalEascServiceLevels() {
		return evalEascServiceLevels;
	}

	/**
	 * @param evalEascServiceLevels the evalEascServiceLevels to set
	 */
	public void setEvalEascServiceLevels(List<EascServiceLevels> evalEascServiceLevels) {
		this.evalEascServiceLevels = evalEascServiceLevels;
	}

	/**
	 * @return the evalEascActivityPlans
	 */
	public List<EascActivityPlan> getEvalEascActivityPlans() {
		return evalEascActivityPlans;
	}

	/**
	 * @param evalEascActivityPlans the evalEascActivityPlans to set
	 */
	public void setEvalEascActivityPlans(List<EascActivityPlan> evalEascActivityPlans) {
		this.evalEascActivityPlans = evalEascActivityPlans;
	}

	/**
	 * @return the evalPowerObjective
	 */
	public Objective getEvalPowerObjective() {
		return evalPowerObjective;
	}

	/**
	 * @param evalPowerObjective the evalPowerObjective to set
	 */
	public void setEvalPowerObjective(Objective evalPowerObjective) {
		this.evalPowerObjective = evalPowerObjective;
	}

	/**
	 * @return the currentEvaluatioPeriod
	 */
	public String getCurrentEvaluationPeriod() {
		return currentEvaluationPeriod;
	}

	/**
	 * @param currentEvaluatioPeriod the currentEvaluatioPeriod to set
	 */
	public void setCurrentEvaluationPeriod(String currentEvaluatioPeriod) {
		this.currentEvaluationPeriod = currentEvaluatioPeriod;
	}

	/**
	 * @return the invokTimeRange
	 */
	public TimeSlotBasedEntity getInvokTimeRange() {
		return invokTimeRange;
	}

	/**
	 * @param invokTimeRange the invokTimeRange to set
	 */
	public void setInvokTimeRange(TimeSlotBasedEntity invokTimeRange) {
		this.invokTimeRange = invokTimeRange;
	}
}