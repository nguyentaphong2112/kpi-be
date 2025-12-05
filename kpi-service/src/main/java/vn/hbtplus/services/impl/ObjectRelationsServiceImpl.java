package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.repositories.impl.ObjectRelationsRepository;
import vn.hbtplus.services.ObjectRelationsService;
import vn.hbtplus.utils.Utils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ObjectRelationsServiceImpl implements ObjectRelationsService {
    private final ObjectRelationsRepository objectRelationsRepository;

    @Override
    @Transactional
    public void inactiveReferIdNotIn(Long objectId, List<Long> listReferObjectId, String tableName, String referTableName, String functionCode) {
        objectRelationsRepository.inactiveReferIdNotIn(objectId, listReferObjectId, tableName, referTableName, functionCode);
    }

    @Override
    @Transactional
    public void saveObjectRelations(Long objectId, List<Long> listReferObjectId, String tableName, String referTableName, String functionCode) {
        objectRelationsRepository.activeRelations(objectId, listReferObjectId, tableName, referTableName, functionCode);
        if (!Utils.isNullOrEmpty(listReferObjectId)) {
            objectRelationsRepository.insertRelations(objectId, listReferObjectId, tableName, referTableName, functionCode);
        }
    }
}
