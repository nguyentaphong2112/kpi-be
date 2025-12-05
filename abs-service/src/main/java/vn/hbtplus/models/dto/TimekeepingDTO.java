/*
 * Copyright (C) 2022 EcoIT. All rights reserved.
 * EcoIT. Use is subject to license terms.
 */
package vn.hbtplus.models.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.models.BaseSearchRequest;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;


/**
 * @author ecoIt
 * @version 1.0
 * @since 11/05/2022
 */
@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TimekeepingDTO {
    private Long employeeId;
    private Date dateTimekeeping;
    private Double totalHours;
    private Long workdayTypeId;
    private String workdayTypeCode;
}
