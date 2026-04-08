package com.jad.dto;

import java.util.List;

public record MachineScheduleDTO(int idMachineTool,
                                 List<ManufactureOrderDTO> orders) {
}