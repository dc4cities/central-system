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

package eu.dc4cities.controlsystem.model.metrics;

/**
 * Enum containing all assets available for dc4cities at the date of it's creation 9/10/2014.
 *
 *
 *
 * Created by Adrian on 9/10/2014.
 */
public enum AssetCatalog {

    DC4C("dc4c", AssetType.COMPANY),
    HP_MILAN("hp_milan", AssetType.SITE),
    OPENSTACK01("OpenStack01", AssetType.SERVER),
    OPENSTACK02("OpenStack02", AssetType.SERVER),
    OPENSTACK03("OpenStack03", AssetType.SERVER),
    OPENSTACK04("OpenStack04", AssetType.SERVER),
    OPENSTACK05("OpenStack05", AssetType.SERVER),
    OPENSTACK06("OpenStack06", AssetType.SERVER),
    OPENSTACK07("OpenStack07", AssetType.SERVER),
    OPENSTACK08("OpenStack08", AssetType.SERVER),
    CSUC_BARCELONA("csuc_barcelona", AssetType.SITE),
    CLUSTER00("cluster00", AssetType.SERVER),
    CLUSTER01("cluster01", AssetType.SERVER),
    VT1("VT1", AssetType.WM),
    VT2("VT2", AssetType.WM),
    VT3("VT3", AssetType.WM),
    VT4("VT4", AssetType.WM),
    VT5("VT5", AssetType.WM),
    SIM_DC("sim_dc", AssetType.SITE),
    SIM_WM1("sim_wm1", AssetType.WM),
    CN_TRENTO("cn_trento", AssetType.SITE),
    IMI_BARCELONA("imi_barcelona", AssetType.SITE),
    WM1("WM1", AssetType.WM),
    WM4("WM4", AssetType.WM),
    WM7("WM7", AssetType.WM),
    WM10("WM10", AssetType.WM),
    WM13("WM13", AssetType.WM),
    WM14("WM14", AssetType.WM),
    VTWM1("VTWM1", AssetType.WM),
    VTWM2("VTWM2", AssetType.WM),
    VTWM3("VTWM3", AssetType.WM),
    VTWM4("VTWM4", AssetType.WM),
    VTWM5("VTWM5", AssetType.WM),
    WM1SM("WM1SM", AssetType.WM),
    WM1SF("WM1SF", AssetType.WM),
    WM1SF1SM("WM1SF1SM", AssetType.WM),
    WM2SF("WM2SF", AssetType.WM),
    WM2SF1SM("WM2SF1SM", AssetType.WM),
    WM3SF("WM3SF", AssetType.WM),
    WMA0("WMA0", AssetType.WM),
    WMA1("WMA1", AssetType.WM),
    WMA2("WMA2", AssetType.WM),
    WM0("WM0", AssetType.WM),
    VTWM0("VTWM0", AssetType.WM),

    NODE18_CTRL("node18-ctrl", AssetType.SERVER),
    NODE19_CMPT("node19-cmpt", AssetType.SERVER),
    NODE20_CMPT("node20-cmpt", AssetType.SERVER),
    SWITCH("switch", AssetType.SERVER),
    VM_DB("vm-db", AssetType.VM),
    VM_WEB("vm-web", AssetType.VM),
    CLUSTER02("cluster02", AssetType.SERVER),
    CLUSTER03("cluster03", AssetType.SERVER);







    public static enum AssetType {
        COMPANY,
        SERVER,
        SITE,
        WM,
        VM
    }


    private String code;
    private AssetType assetType;


    /**
     * @param code - code of the asset
     * @param assetType - type of the asset
     */
    AssetCatalog(String code, AssetType assetType) {
        this.code = code;
        this.assetType = assetType;
    }

    public String getCode() {
        return code;
    }

    public AssetType getAssetType() {
        return assetType;
    }


    @Override
    public String toString() {
        return "Asset{" +
                "code='" + code + '\'' +
                ", assetType=" + assetType +
                '}';
    }
}
