package com.ace.ai.admin.dtomodel;

import java.time.LocalDate;

import lombok.Data;

@Data
public class AttendanceDTO {
    private LocalDate date;
    private String studentName;
    private String attendStatus;
}
