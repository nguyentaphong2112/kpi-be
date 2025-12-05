package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.repositories.entity.ShedlockHistoriesEntity;
import vn.hbtplus.repositories.impl.ShedlockHistoriesRepository;
import vn.hbtplus.repositories.jpa.ShedlockHistoriesRepositoryJPA;
import vn.hbtplus.services.ShedlockHistoriesService;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class ShedlockHistoriesServiceImpl implements ShedlockHistoriesService {
    private final ShedlockHistoriesRepositoryJPA shedlockHistoriesRepositoryJPA;
    

    private final ShedlockHistoriesRepository shedlockHistoriesRepository;


    @Override
    @Transactional
    public Long saveHistory(String name) {
        ShedlockHistoriesEntity entity = new ShedlockHistoriesEntity();
        entity.setShedlockName(name);
        entity.setStartTime(new Date());
        entity.setStatus(ShedlockHistoriesEntity.STATUS.RUNNING);
        shedlockHistoriesRepositoryJPA.save(entity);
        return entity.getShedlockHistoryId();
    }

    @Override
    @Transactional
    public void updateEndTime(Long shedlockHistoryId, String status) {
        ShedlockHistoriesEntity entity = shedlockHistoriesRepositoryJPA.getById(shedlockHistoryId);
        entity.setEndTime(new Date());
        entity.setStatus(status);
        shedlockHistoriesRepositoryJPA.save(entity);
    }

    @Override
    public Date getLastRunSuccess(String name) {
        return shedlockHistoriesRepository.getLastRunSuccess(name);
    }
}
