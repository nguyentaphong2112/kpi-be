package vn.hbtplus.models.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.utils.StrimDeSerializer;

import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContractProposalsFormDTO {
    private Long contractProposalId;

    private Long contractTypeId;

    private Long signerId;

    private Long curContractTypeId;

    private Long contractByLawId;

    @Size(max = 200)
    @JsonDeserialize(using = StrimDeSerializer.class)
    private String signerPosition;

    @Size(max = 200)
    @JsonDeserialize(using = StrimDeSerializer.class)
    private String delegacyNo;

    private List<ContractApproversDTO> contractApproveDTOs;

    @DateTimeFormat(pattern = BaseConstants.COMMON_DATE_FORMAT)
    @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date amountFeeFromDate;

    @DateTimeFormat(pattern = BaseConstants.COMMON_DATE_FORMAT)
    @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date amountFeeToDate;

    private Long amountFee;
}