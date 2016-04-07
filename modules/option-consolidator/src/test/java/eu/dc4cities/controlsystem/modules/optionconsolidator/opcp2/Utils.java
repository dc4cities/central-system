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

package eu.dc4cities.controlsystem.modules.optionconsolidator.opcp2;

import eu.dc4cities.controlsystem.model.json.JsonUtils;

import java.util.Arrays;

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

    public static PowerSourceSlot[] makeSlots(int[] pow, int[] purity) {
        PowerSourceSlot[] slots = new PowerSourceSlot[pow.length];
        for (int t = 0; t < pow.length; t++) {
            slots[t] = new PowerSourceSlot(pow[t], purity[t], 0);
        }
        return slots;
    }

    public static PowerSourceSlot[] makeSlots(int[] pow, int[] purity, int[] prices) {
        PowerSourceSlot[] slots = new PowerSourceSlot[pow.length];
        for (int t = 0; t < pow.length; t++) {
            slots[t] = new PowerSourceSlot(pow[t], purity[t], prices[t]);
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

    public static PowerSource rev(PowerSource s) {
        PowerSource rev = new PowerSource(s.name(), new PowerSourceSlot[s.slots().length]).dcId(s.dcId());
        for (int i = s.slots().length - 1; i >= 0; i--) {
            PowerSourceSlot o = s.slots()[i];
            rev.slots()[s.slots().length - 1 - i] = o;
        }
        return rev;
    }

/*    @Test
    public void testSwitch() {
        int slots = 3;
        PowerSource grid = new PowerSource("grid",
                makeSlots(new int[]{1000, 1000, 10000},
                        makeProfile(slots, 0),
                        makeProfile(slots, 1600))).dcId("dc1"); //10-2 euro-cents
        PowerSource pv = new PowerSource("pv",
                makeSlots(new int[]{0, 150, 0},
                        makeProfile(slots, 100),
                        makeProfile(slots, 0))).dcId("dc1"); //euro-cents

        MyActivity a = new MyActivity("a").relocatibility(Relocability.NO);
        //2 working modes, name; performance; power
        WM s = WM("S", 100, 100);
        WM m = WM("M", 150, 150);
        a.add(new DatacenterPart("dc1").add(s).add(m));
        for (int i = 0; i < slots; i++) {
            InstantRevenue r = new InstantRevenue(i, 100, 5 * 1600 * 100);
            r.add(new Modifier(150, 1));
            a.add(r);
        }
        OPCP2 pb = new OPCP2(slots, Arrays.asList(grid, pv), Arrays.asList(a));
        Assert.assertEquals(ESat.TRUE, pb.solve());
        IntVar profit = pb.getProfit();
        System.out.println(pb.getSolver().toString());
        Assert.assertEquals(3200  + 2 * a.instantRevenues(0).basePrice() + a.instantRevenues(0).revenue(150), pb.val(profit));
    }*/

    public static <T> T loadJson(String name, Class<T> clazz) {
        return JsonUtils.loadResource(name + ".json", clazz);
    }
}
