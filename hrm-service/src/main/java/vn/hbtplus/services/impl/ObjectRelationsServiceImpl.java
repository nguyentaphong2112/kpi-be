package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.repositories.impl.ObjectRelationsRepository;
import vn.hbtplus.services.ObjectRelationsService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ObjectRelationsServiceImpl implements ObjectRelationsService {
    private final ObjectRelationsRepository objectRelationsRepository;
    @Override
    @Transactional
    public void inactiveReferIdNotIn(Long objectId, List referObjectIds, String tableName, String referTableName, String functionCode) {
        objectRelationsRepository.inactiveReferIdNotIn(objectId, referObjectIds, tableName, referTableName, functionCode);
    }

    @Override
    @Transactional
    public void saveObjectRelations(Long objectId, List referObjectIds, String tableName, String referTableName, String functionCode) {
        objectRelationsRepository.activeRelations(objectId, referObjectIds, tableName, referTableName, functionCode);
        objectRelationsRepository.insertRelations(objectId, referObjectIds, tableName, referTableName, functionCode);


    }
}
