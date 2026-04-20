package com.education.web.schedule;

import com.education.web.schedule.dto.ScheduleSlotResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/teacher")
public class TeacherScheduleController {

    private final ScheduleReadService scheduleReadService;

    public TeacherScheduleController(ScheduleReadService scheduleReadService) {
        this.scheduleReadService = scheduleReadService;
    }

    @GetMapping("/schedule")
    public List<ScheduleSlotResponse> schedule(@RequestParam("userId") String userId) {
        return scheduleReadService.listForTeacherUser(userId);
    }
}
