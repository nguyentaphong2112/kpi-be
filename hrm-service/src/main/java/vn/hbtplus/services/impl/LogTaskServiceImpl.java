package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.request.LogTaskRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.LogTasKResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.repositories.entity.JobsEntity;
import vn.hbtplus.repositories.entity.LogTaskEntity;
import vn.hbtplus.repositories.impl.LogTaskRepository;
import vn.hbtplus.repositories.jpa.LogTaskRepositoryJPA;
import vn.hbtplus.services.LogTaskService;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import javax.validation.Valid;
import java.util.*;

@Service
@RequiredArgsConstructor
public class LogTaskServiceImpl implements LogTaskService {

    private final LogTaskRepository logTaskRepository;
    private final LogTaskRepositoryJPA logTaskRepositoryJPA;

    @Override
    public TableResponseEntity<LogTasKResponse.SearchResult> searchData(LogTaskRequest.SearchForm dto) {
        return ResponseUtils.ok(logTaskRepository.searchData(dto));
    }

    @Override
    public BaseResponseEntity<Long> saveData(LogTaskRequest.@Valid SubmitForm dto, Long logTaskId) {
        LogTaskEntity entity;
        if (logTaskId != null && logTaskId > 0L) {
            entity = logTaskRepositoryJPA.getById(logTaskId);
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new LogTaskEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setLogDate(dto.getLogDate());
        entity.setTotalHouse(dto.getTotalHouse());
        entity.setProjectCode(dto.getProjectCode());
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        logTaskRepositoryJPA.save(entity);
        logTaskRepositoryJPA.flush();

        return ResponseUtils.ok(entity.getLogTaskId());
    }

    @Override
    public BaseResponseEntity<Long> deleteData(Long id) {
        Optional<LogTaskEntity> optional = logTaskRepositoryJPA.findById(id);
        if (optional.isEmpty() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, JobsEntity.class);
        }
        logTaskRepository.deActiveObject(LogTaskEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    public BaseResponseEntity<LogTasKResponse.DetailBean> getDataById(Long id) {
        Optional<LogTaskEntity> optional = logTaskRepositoryJPA.findById(id);
        if (optional.isEmpty() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, LogTaskEntity.class);
        }
        LogTasKResponse.DetailBean dto = new LogTasKResponse.DetailBean();
        Utils.copyProperties(optional.get(), dto);
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(LogTaskRequest.SearchForm dto) {
        return null;
    }
}
