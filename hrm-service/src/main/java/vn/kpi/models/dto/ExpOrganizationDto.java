package vn.kpi.models.dto;

import vn.kpi.utils.Utils;

import java.util.HashMap;
import java.util.Map;

public class ExpOrganizationDto {
    private int minLevel;
    private int currentLevel;

    private Map<Integer, Integer> mapIdx = new HashMap<>();

    public String getIdx() {
        if (currentLevel == minLevel) {
            return Utils.intToRoman(mapIdx.get(minLevel));
        } else {
            String result = String.valueOf(mapIdx.get(minLevel + 1));
            for (int i = minLevel + 2; i <= currentLevel; i++) {
                result += "." + mapIdx.get(i);
            }
            return result;
        }
    }

    public void addOrg(OrganizationDto exp) {
        if (minLevel == 0) {
            minLevel = exp.getPathLevel();
        }
        if (currentLevel > exp.getPathLevel()) {
            for (int i = currentLevel; i > exp.getPathLevel(); i--) {
                mapIdx.put(i, 0);
            }
        }
        mapIdx.put(exp.getPathLevel(), Utils.NVL(mapIdx.get(exp.getPathLevel())) + 1);
        currentLevel = exp.getPathLevel();
    }

}
