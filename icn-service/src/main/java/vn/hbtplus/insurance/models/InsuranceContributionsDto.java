package vn.hbtplus.insurance.models;

import lombok.Data;
import vn.hbtplus.insurance.repositories.entity.ContributionRateEntity;
import vn.hbtplus.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Data
public class InsuranceContributionsDto {
    private Long employeeId;

    private Long jobId;
    private Date periodDate;
    private String jobName;
    private Date startDate;
    private Date endDate;

    private Long orgId;
    private Long debitOrgId;

    private Long contractSalary = 0l;

    private Long reserveSalary = 0l;

    private Long posAllowanceSalary = 0l;

    private Long senioritySalary = 0l;

    private Long posSenioritySalary = 0l;

    private Long totalSalary = 0l;

    private Long perSocialAmount = 0l;

    private Long unitSocialAmount = 0l;

    private Long perMedicalAmount = 0l;

    private Long unitMedicalAmount = 0l;

    private Long perUnempAmount = 0l;

    private Long unitUnempAmount = 0l;

    private Long unitUnionAmount = 0l;
    private Long totalAmount = 0l;

    private Long baseUnionAmount = 0l;

    private Long superiorUnionAmount = 0l;

    private Long modUnionAmount = 0l;
    private Long retirementSocialAmount = 0l;
    private Long sicknessSocialAmount = 0l;
    private Long accidentSocialAmount = 0l;

    private String empTypeCode;
    private String labourType;
    private String tableType;

    private String status;

    private String reason;

    private String note;
    private Double insuranceFactor;
    private Double insuranceBaseSalary;
    private Double reserveFactor;
    private Double allowanceFactor;
    private Double seniorityPercent;
    private Double posSeniorityPercent;
    private Double insuranceTimekeeping;
    private Double leaveTimekeeping;
    private Double maternityTimekeeping;
    private Double leaveReason;
    private String insuranceRegion; //vung trich nop bao hiem
    private String insuranceAgency; //noi tham gia BHXH
    private String type; //noi tham gia BHXH
    private boolean isMaternity = false;
    private InsuranceContributionsDto soDaThuDto;
    private InsuranceContributionsDto soPhaiThuDto;

    public String getLyDoTruyThuTruyLinh() {
        if (soDaThuDto == null || soDaThuDto.getTotalAmount().equals(0d)) {
            return "Chưa thực hiện thu trong tháng";
        }
        if (soPhaiThuDto == null || soPhaiThuDto.getTotalAmount().equals(0d)) {
            return "Nhân viên không đóng BHXH";
        }

        StringBuilder builder = new StringBuilder();
        if (!Utils.NVL(this.insuranceFactor).equals(0d)) {
            builder.append("; Thay đổi hệ số lương");
        }
        if (!Utils.NVL(this.allowanceFactor).equals(0d)) {
            builder.append("; Thay đổi hệ số phụ cấp chức vụ");
        }
        if (!Utils.NVL(this.reserveFactor).equals(0d)) {
            builder.append("; Thay đổi hệ số chênh lệch bảo lưu");
        }
        if (!Utils.NVL(this.seniorityPercent).equals(0d)) {
            builder.append("; Thay đổi % thâm niên vượt khung");
        }
        if (!Utils.NVL(this.posSeniorityPercent).equals(0d)) {
            builder.append("; Thay đổi % thâm niên nghề");
        }
        if (builder.isEmpty()) {
            return "Lý do khác";
        }
        return builder.toString().substring(2);
    }


    public void setContributionAmount(ContributionRateEntity rateDto, ContributionParameterDto divisionRatioDto) {
        if (rateDto != null && this.totalSalary > 0) {
            this.unitSocialAmount = Math.round(Utils.NVL(rateDto.getUnitSocialPercent()) * this.totalSalary / 100);
            this.perSocialAmount = Math.round(Utils.NVL(rateDto.getPerSocialPercent()) * this.totalSalary / 100);
            this.unitMedicalAmount = Math.round(Utils.NVL(rateDto.getUnitMedicalPercent()) * this.totalSalary / 100);
            this.perMedicalAmount = Math.round(Utils.NVL(rateDto.getPerMedicalPercent()) * this.totalSalary / 100);
            this.unitUnempAmount = Math.round(Utils.NVL(rateDto.getUnitUnempPercent()) * this.totalSalary / 100);
            this.perUnempAmount = Math.round(Utils.NVL(rateDto.getPerUnempPercent()) * this.totalSalary / 100);
            this.unitUnionAmount = Math.round(Utils.NVL(rateDto.getUnitUnionPercent()) * this.totalSalary / 100);
            this.totalAmount = this.unitSocialAmount + this.perSocialAmount
                               + this.unitMedicalAmount + this.perMedicalAmount
                               + this.unitUnempAmount + this.perUnempAmount;
            this.baseUnionAmount = Math.round(Utils.NVL(divisionRatioDto.getBaseUnionPercent()) * this.unitUnionAmount / 100);
            this.superiorUnionAmount = Math.round(Utils.NVL(divisionRatioDto.getSuperiorUnionPercent()) * this.unitUnionAmount / 100);
            this.modUnionAmount = this.unitUnionAmount - this.baseUnionAmount - this.superiorUnionAmount;
            this.retirementSocialAmount = Math.round(Utils.NVL(divisionRatioDto.getRetirementSocialPercent()) * this.unitSocialAmount / 100);
            this.sicknessSocialAmount = Math.round(Utils.NVL(divisionRatioDto.getSicknessSocialPercent()) * this.unitSocialAmount / 100);
            this.accidentSocialAmount = this.unitSocialAmount - this.retirementSocialAmount - this.sicknessSocialAmount;
        }
    }

    public void setTimekeeping(List<TimekeepingDto> timekeepingDtos, ContributionParameterDto parameterDto) {
        if (timekeepingDtos != null) {
            timekeepingDtos.stream().forEach(item -> {
                if (parameterDto.getIdCongThaiSans().contains(item.getWorkdayTypeId())) {
                    this.maternityTimekeeping = Utils.NVL(this.maternityTimekeeping) + Utils.NVL(item.getNumOfDays());
                }
                if (parameterDto.getIdCongTrichNops().contains(item.getWorkdayTypeId())) {
                    this.insuranceTimekeeping = Utils.NVL(this.insuranceTimekeeping) + Utils.NVL(item.getNumOfDays());
                }
            });
        }
    }


    public void retroactive() {
        this.insuranceFactor = 0 - Utils.NVL(this.insuranceFactor);
        this.insuranceBaseSalary = 0 - Utils.NVL(this.insuranceBaseSalary);
        this.reserveFactor = 0 - Utils.NVL(this.reserveFactor);
        this.allowanceFactor = 0 - Utils.NVL(this.allowanceFactor);
        this.seniorityPercent = 0 - Utils.NVL(this.seniorityPercent);
        this.posSeniorityPercent = 0 - Utils.NVL(this.posSeniorityPercent);
        this.contractSalary = 0 - Utils.NVL(this.contractSalary);
        this.reserveSalary = 0 - Utils.NVL(this.reserveSalary);
        this.posAllowanceSalary = 0 - Utils.NVL(this.posAllowanceSalary);
        this.senioritySalary = 0 - Utils.NVL(this.senioritySalary);
        this.posSenioritySalary = 0 - Utils.NVL(this.posSenioritySalary);
        this.totalSalary = 0 - Utils.NVL(this.totalSalary);
        this.perSocialAmount = 0 - Utils.NVL(this.perSocialAmount);
        this.unitSocialAmount = 0 - Utils.NVL(this.unitSocialAmount);
        this.perMedicalAmount = 0 - Utils.NVL(this.perMedicalAmount);
        this.unitMedicalAmount = 0 - Utils.NVL(this.unitMedicalAmount);
        this.perUnempAmount = 0 - Utils.NVL(this.perUnempAmount);
        this.unitUnempAmount = 0 - Utils.NVL(this.unitUnempAmount);
        this.unitUnionAmount = 0 - Utils.NVL(this.unitUnionAmount);
        this.totalAmount = 0 - Utils.NVL(this.totalAmount);
        this.baseUnionAmount = 0 - Utils.NVL(this.baseUnionAmount);
        this.superiorUnionAmount = 0 - Utils.NVL(this.superiorUnionAmount);
        this.modUnionAmount = 0 - Utils.NVL(this.modUnionAmount);
        this.retirementSocialAmount = 0 - Utils.NVL(this.retirementSocialAmount);
        this.sicknessSocialAmount = 0 - Utils.NVL(this.sicknessSocialAmount);
        this.accidentSocialAmount = 0 - Utils.NVL(this.accidentSocialAmount);

    }

    public void retroactive(InsuranceContributionsDto beforeDto) {
        this.insuranceFactor = Utils.NVL(this.insuranceFactor) - Utils.NVL(beforeDto.insuranceFactor);
        this.insuranceBaseSalary = Utils.NVL(this.insuranceBaseSalary) - Utils.NVL(beforeDto.insuranceBaseSalary);
        this.reserveFactor = Utils.NVL(this.reserveFactor) - Utils.NVL(beforeDto.reserveFactor);
        this.allowanceFactor = Utils.NVL(this.allowanceFactor) - Utils.NVL(beforeDto.allowanceFactor);
        this.seniorityPercent = Utils.NVL(this.seniorityPercent) - Utils.NVL(beforeDto.seniorityPercent);
        this.posSeniorityPercent = Utils.NVL(this.posSeniorityPercent) - Utils.NVL(beforeDto.posSeniorityPercent);
        this.contractSalary = Utils.NVL(this.contractSalary) - Utils.NVL(beforeDto.contractSalary);
        this.reserveSalary = Utils.NVL(this.reserveSalary) - Utils.NVL(beforeDto.reserveSalary);
        this.posAllowanceSalary = Utils.NVL(this.posAllowanceSalary) - Utils.NVL(beforeDto.posAllowanceSalary);
        this.senioritySalary = Utils.NVL(this.senioritySalary) - Utils.NVL(beforeDto.senioritySalary);
        this.posSenioritySalary = Utils.NVL(this.posSenioritySalary) - Utils.NVL(beforeDto.posSenioritySalary);
        this.totalSalary = Utils.NVL(this.totalSalary) - Utils.NVL(beforeDto.totalSalary);
        this.perSocialAmount = Utils.NVL(this.perSocialAmount) - Utils.NVL(beforeDto.perSocialAmount);
        this.unitSocialAmount = Utils.NVL(this.unitSocialAmount) - Utils.NVL(beforeDto.unitSocialAmount);
        this.perMedicalAmount = Utils.NVL(this.perMedicalAmount) - Utils.NVL(beforeDto.perMedicalAmount);
        this.unitMedicalAmount = Utils.NVL(this.unitMedicalAmount) - Utils.NVL(beforeDto.unitMedicalAmount);
        this.perUnempAmount = Utils.NVL(this.perUnempAmount) - Utils.NVL(beforeDto.perUnempAmount);
        this.unitUnempAmount = Utils.NVL(this.unitUnempAmount) - Utils.NVL(beforeDto.unitUnempAmount);
        this.unitUnionAmount = Utils.NVL(this.unitUnionAmount) - Utils.NVL(beforeDto.unitUnionAmount);
        this.totalAmount = Utils.NVL(this.totalAmount) - Utils.NVL(beforeDto.totalAmount);
        this.baseUnionAmount = Utils.NVL(this.baseUnionAmount) - Utils.NVL(beforeDto.baseUnionAmount);
        this.superiorUnionAmount = Utils.NVL(this.superiorUnionAmount) - Utils.NVL(beforeDto.superiorUnionAmount);
        this.modUnionAmount = Utils.NVL(this.modUnionAmount) - Utils.NVL(beforeDto.modUnionAmount);
        this.retirementSocialAmount = Utils.NVL(this.retirementSocialAmount) - Utils.NVL(beforeDto.retirementSocialAmount);
        this.sicknessSocialAmount = Utils.NVL(this.sicknessSocialAmount) - Utils.NVL(beforeDto.sicknessSocialAmount);
        this.accidentSocialAmount = Utils.NVL(this.accidentSocialAmount) - Utils.NVL(beforeDto.accidentSocialAmount);
    }

    public List<String> getPositionGroups() {
        if (Utils.isNullOrEmpty(labourType)) {
            return new ArrayList<>();
        } else {
            return Arrays.asList(labourType.replace(" ", "").split(","));
        }
    }

    public void addAmount(InsuranceContributionsDto item, int size) {
        this.perSocialAmount = Utils.NVL(this.perSocialAmount) + Utils.NVL(item.perSocialAmount) / size;
        this.unitSocialAmount = Utils.NVL(this.unitSocialAmount) + Utils.NVL(item.unitSocialAmount) / size;
        this.perMedicalAmount = Utils.NVL(this.perMedicalAmount) + Utils.NVL(item.perMedicalAmount) / size;
        this.unitMedicalAmount = Utils.NVL(this.unitMedicalAmount) + Utils.NVL(item.unitMedicalAmount) / size;
        this.perUnempAmount = Utils.NVL(this.perUnempAmount) + Utils.NVL(item.perUnempAmount) / size;
        this.unitUnempAmount = Utils.NVL(this.unitUnempAmount) + Utils.NVL(item.unitUnempAmount) / size;
        this.unitUnionAmount = Utils.NVL(this.unitUnionAmount) + Utils.NVL(item.unitUnionAmount) / size;
        this.totalAmount = Utils.NVL(this.totalAmount) + Utils.NVL(item.totalAmount) / size;
    }

    public Long getDebitOrgId() {
        return debitOrgId == null ? orgId : debitOrgId;
    }

    public void add(InsuranceContributionsDto item) {
        this.insuranceFactor = Utils.NVL(this.insuranceFactor) + Utils.NVL(item.insuranceFactor);
        this.insuranceBaseSalary = Utils.NVL(this.insuranceBaseSalary) + Utils.NVL(item.insuranceBaseSalary);
        this.reserveFactor = Utils.NVL(this.reserveFactor) + Utils.NVL(item.reserveFactor);
        this.allowanceFactor = Utils.NVL(this.allowanceFactor) + Utils.NVL(item.allowanceFactor);
        this.seniorityPercent = Utils.NVL(this.seniorityPercent) + Utils.NVL(item.seniorityPercent);
        this.posSeniorityPercent = Utils.NVL(this.posSeniorityPercent) + Utils.NVL(item.posSeniorityPercent);
        this.contractSalary = Utils.NVL(this.contractSalary) + Utils.NVL(item.contractSalary);
        this.reserveSalary = Utils.NVL(this.reserveSalary) + Utils.NVL(item.reserveSalary);
        this.posAllowanceSalary = Utils.NVL(this.posAllowanceSalary) + Utils.NVL(item.posAllowanceSalary);
        this.senioritySalary = Utils.NVL(this.senioritySalary) + Utils.NVL(item.senioritySalary);
        this.posSenioritySalary = Utils.NVL(this.posSenioritySalary) + Utils.NVL(item.posSenioritySalary);
        this.totalSalary = Utils.NVL(this.totalSalary) + Utils.NVL(item.totalSalary);
        this.perSocialAmount = Utils.NVL(this.perSocialAmount) + Utils.NVL(item.perSocialAmount);
        this.unitSocialAmount = Utils.NVL(this.unitSocialAmount) + Utils.NVL(item.unitSocialAmount);
        this.perMedicalAmount = Utils.NVL(this.perMedicalAmount) + Utils.NVL(item.perMedicalAmount);
        this.unitMedicalAmount = Utils.NVL(this.unitMedicalAmount) + Utils.NVL(item.unitMedicalAmount);
        this.perUnempAmount = Utils.NVL(this.perUnempAmount) + Utils.NVL(item.perUnempAmount);
        this.unitUnempAmount = Utils.NVL(this.unitUnempAmount) + Utils.NVL(item.unitUnempAmount);
        this.unitUnionAmount = Utils.NVL(this.unitUnionAmount) + Utils.NVL(item.unitUnionAmount);
        this.totalAmount = Utils.NVL(this.totalAmount) + Utils.NVL(item.totalAmount);
        this.baseUnionAmount = Utils.NVL(this.baseUnionAmount) + Utils.NVL(item.baseUnionAmount);
        this.superiorUnionAmount = Utils.NVL(this.superiorUnionAmount) + Utils.NVL(item.superiorUnionAmount);
        this.modUnionAmount = Utils.NVL(this.modUnionAmount) + Utils.NVL(item.modUnionAmount);
        this.retirementSocialAmount = Utils.NVL(this.retirementSocialAmount) + Utils.NVL(item.retirementSocialAmount);
        this.sicknessSocialAmount = Utils.NVL(this.sicknessSocialAmount) + Utils.NVL(item.sicknessSocialAmount);
        this.accidentSocialAmount = Utils.NVL(this.accidentSocialAmount) + Utils.NVL(item.accidentSocialAmount);
    }

    public void resetContributionAmount() {
        this.unitSocialAmount = 0L;
        this.perSocialAmount = 0L;
        this.unitMedicalAmount = 0L;
        this.perMedicalAmount = 0L;
        this.unitUnempAmount = 0L;
        this.perUnempAmount = 0L;
        this.unitUnionAmount = 0L;
        this.totalAmount = 0L;
        this.baseUnionAmount = 0L;
        this.superiorUnionAmount = 0L;
        this.modUnionAmount = 0L;
        this.retirementSocialAmount = 0L;
        this.sicknessSocialAmount = 0L;
        this.accidentSocialAmount = 0L;
    }
}
