package com.education.web.schedule;

import com.education.web.schedule.dto.ScheduleSlotResponse;
import com.education.web.schedule.dto.UpsertScheduleSlotRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/school-admin")
public class SchoolAdminScheduleController {

    private final SchoolAdminScheduleService scheduleService;

    public SchoolAdminScheduleController(SchoolAdminScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @GetMapping("/schedule")
    public List<ScheduleSlotResponse> list(@RequestParam("schoolId") String schoolId) {
        return scheduleService.list(schoolId);
    }

    @PostMapping("/schedule")
    @ResponseStatus(HttpStatus.CREATED)
    public ScheduleSlotResponse create(
            @RequestParam("schoolId") String schoolId,
            @Valid @RequestBody UpsertScheduleSlotRequest body
    ) {
        return scheduleService.create(schoolId, body);
    }

    @PutMapping("/schedule/{slotId}")
    public ScheduleSlotResponse update(
            @RequestParam("schoolId") String schoolId,
            @PathVariable("slotId") String slotId,
            @Valid @RequestBody UpsertScheduleSlotRequest body
    ) {
        return scheduleService.update(schoolId, slotId, body);
    }

    @DeleteMapping("/schedule/{slotId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@RequestParam("schoolId") String schoolId, @PathVariable("slotId") String slotId) {
        scheduleService.delete(schoolId, slotId);
    }
}
