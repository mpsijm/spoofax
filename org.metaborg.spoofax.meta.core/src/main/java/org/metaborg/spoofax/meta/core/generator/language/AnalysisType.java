package org.metaborg.spoofax.meta.core.generator.language;

import java.util.Map;

import com.google.common.collect.Maps;

public enum AnalysisType {
    NaBL_TS("nabl_ts", "NaBL & TS"), NaBL2("nabl2", "NaBL2"), Stratego("stratego", "Stratego"), None("none", "None");


    public final String id;
    public final String name;


    AnalysisType(String id, String name) {
        this.id = id;
        this.name = name;
    }


    public static Map<String, AnalysisType> mapping() {
        final Map<String, AnalysisType> analysisTypes = Maps.newHashMap();
        for(AnalysisType analysisType : AnalysisType.values()) {
            analysisTypes.put(analysisType.name, analysisType);
        }
        return analysisTypes;
    }
}
