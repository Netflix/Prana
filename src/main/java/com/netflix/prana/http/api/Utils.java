package com.netflix.prana.http.api;

import java.util.List;
import java.util.Map;

public class Utils {

    public static String forQueryParam(Map<String, List<String>> queryParams, String paramName) {
        List<String> values = queryParams.get(paramName);
        if (values != null) {
            return values.get(0);
        }
        return null;
    }
}
