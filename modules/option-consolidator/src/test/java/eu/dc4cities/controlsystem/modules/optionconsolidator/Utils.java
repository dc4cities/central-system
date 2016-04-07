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

package eu.dc4cities.controlsystem.modules.optionconsolidator;

import eu.dc4cities.controlsystem.modules.optionconsolidator.opcp.OldPowerSourceSlot;
import eu.dc4cities.controlsystem.modules.optionconsolidator.opcp.WorkingModeSlot;
import eu.dc4cities.controlsystem.modules.optionconsolidator.opcp2.DatacenterPart;
import eu.dc4cities.controlsystem.modules.optionconsolidator.opcp2.WM;
import org.jscience.physics.amount.Amount;

import java.util.Arrays;

import static javax.measure.unit.SI.WATT;

/**
 *
 */
public class Utils {

    public static int[] makeProfile(int nbSlots, int c) {
        int[] profile = new int[nbSlots];
        for (int t = 0; t < nbSlots; t++) {
            profile[t] = c;
        }
        return profile;
    }

    public static int[] makeLinearProfile(int nbSlots, int from, int step) {
        int[] profile = new int[nbSlots];
        int f = from;
        for (int t = 0; t < nbSlots; t++) {
            profile[t] = f;
            f += step;
        }
        return profile;
    }

    public static WorkingModeSlot[] makeOptionSlots(int[] powerDemand, int[] penalties) {
        WorkingModeSlot[] slots = new WorkingModeSlot[powerDemand.length];
        for (int t = 0; t < powerDemand.length; t++) {
            slots[t] = new WorkingModeSlot(powerDemand[t], penalties[t]);
        }
        return slots;
    }

    public static OldPowerSourceSlot[] makeSlots(int[] pow, int[] purity) {
        OldPowerSourceSlot[] slots = new OldPowerSourceSlot[pow.length];
        for (int t = 0; t < pow.length; t++) {
            slots[t] = new OldPowerSourceSlot(Amount.valueOf(pow[t], WATT), purity[t], 0);
        }
        return slots;
    }

    public static OldPowerSourceSlot[] makeSlots(int[] pow, int[] purity, int[] prices) {
        OldPowerSourceSlot[] slots = new OldPowerSourceSlot[pow.length];
        for (int t = 0; t < pow.length; t++) {
            slots[t] = new OldPowerSourceSlot(Amount.valueOf(pow[t], WATT), purity[t], prices[t]);
        }
        return slots;
    }

    public static int[] slots(int nb, int dur) {
        int[] s = new int[nb];
        Arrays.fill(s, dur);
        return s;
    }

    public static DatacenterPart newDCPart(String n) {
        return new DatacenterPart(n);
    }

    public static WM WM(String id, int p) {
        return WM(id, 0, p);
    }

    public static WM WM(String id) {
        return new WM("", id);
    }


    public static WM WM(String id, int perf, int power) {
        return new WM("", id).addPerfLevel(perf, power);
    }
}
