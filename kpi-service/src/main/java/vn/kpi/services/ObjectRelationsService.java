package vn.kpi.services;

import java.util.List;

public interface ObjectRelationsService {
    void inactiveReferIdNotIn(Long objectId, List<Long> listReferObjectId, String tableName, String referTableName, String functionCode);

    void saveObjectRelations(Long objectId, List<Long> listReferObjectId, String tableName, String referTableName, String functionCode);
}
