package com.finance.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ReservationTimeResponse {

    private LocalDateTime minAvailableTime; // 선택 가능한 가장 빠른 시간
    private LocalDateTime maxAvailableTime; // 선택 가능한 가장 늦은 시간 (오늘로부터 30일 등)
    private String notice; // 안내 문구
}
