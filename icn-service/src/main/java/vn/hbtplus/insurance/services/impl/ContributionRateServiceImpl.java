package vn.hbtplus.insurance.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordConflictException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.BaseReportResponse;
import vn.hbtplus.insurance.models.request.ContributionRateRequest;
import vn.hbtplus.insurance.models.response.ContributionRateResponse;
import vn.hbtplus.insurance.repositories.entity.ContributionRateEntity;
import vn.hbtplus.insurance.repositories.impl.ContributionRateRepository;
import vn.hbtplus.insurance.repositories.jpa.ContributionRateRepositoryJPA;
import vn.hbtplus.insurance.services.ContributionRateService;
import vn.hbtplus.utils.Utils;

import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ContributionRateServiceImpl implements ContributionRateService {
    @Autowired
    private ContributionRateRepository contributionRateRepository;
    @Autowired
    private ContributionRateRepositoryJPA contributionRateRepositoryJPA;

    @Override
    public BaseDataTableDto search(ContributionRateRequest.SearchForm request) {
        return contributionRateRepository.search(request);
    }

    @Override
    public BaseReportResponse exportData(ContributionRateRequest.SearchForm request) {
        return null;
    }

    @Override
    @Transactional
    public Long saveData(ContributionRateRequest.SubmitForm request, Long id)
            throws BaseAppException {
        //validate bi trung qua trinh
        validateConflictProcess(request, id);

        ContributionRateEntity entity;
        if (id == null) {
            entity = new ContributionRateEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        } else {
            Optional<ContributionRateEntity> optional = contributionRateRepositoryJPA.findById(id);
            if (optional.isEmpty() || optional.get().isDeleted()) {
                throw new RecordNotExistsException(id, ContributionRateEntity.class);
            }
            entity = optional.get();

            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(request, entity);
        entity.setContributionRateId(id);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        contributionRateRepositoryJPA.save(entity);
        //update du lieu cu
        contributionRateRepositoryJPA.updatePreConfig(request.getEmpTypeCode(), request.getStartDate());

        return entity.getContributionRateId();
    }

    private void validateConflictProcess(ContributionRateRequest.SubmitForm request, Long id) throws RecordConflictException {
        ContributionRateEntity contributionRate = contributionRateRepository.getConflict(request, id);
        if (contributionRate != null) {
            throw new RecordConflictException("contribution-rate.error.conflict", Utils.formatDate(contributionRate.getStartDate()), Utils.formatDate(contributionRate.getEndDate()));
        }
    }

    @Override
    public Boolean deleteById(Long id) throws RecordNotExistsException {
        Optional<ContributionRateEntity> optional = contributionRateRepositoryJPA.findById(id);
        if (optional.isEmpty() || optional.get().isDeleted()) {
            throw new RecordNotExistsException(id, ContributionRateEntity.class);
        }
        ContributionRateEntity entity = optional.get();
        entity.setIsDeleted(BaseConstants.STATUS.DELETED);
        entity.setModifiedTime(new Date());
        entity.setModifiedBy(Utils.getUserNameLogin());
        contributionRateRepositoryJPA.save(entity);
        return true;
    }

    @Override
    public ContributionRateResponse getById(Long id) throws RecordNotExistsException {
        Optional<ContributionRateEntity> optional = contributionRateRepositoryJPA.findById(id);
        if (optional.isEmpty() || optional.get().isDeleted()) {
            throw new RecordNotExistsException(id, ContributionRateEntity.class);
        }
        ContributionRateResponse dto = new ContributionRateResponse();
        Utils.copyProperties(optional.get(), dto);
        return dto;
    }
}
