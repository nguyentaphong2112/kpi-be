package vn.kpi.services;

import java.util.List;

public interface ObjectRelationsService {
    void inactiveReferIdNotIn(Long salaryRankId, List<Long> jobIds, String salaryRanks, String job, String ganChucDanhHuongLuong);

    void saveObjectRelations(Long salaryRankId, List<Long> jobIds, String salaryRanks, String job, String ganChucDanhHuongLuong);
}
