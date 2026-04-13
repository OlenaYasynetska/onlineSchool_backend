package com.education.web.superadmin;

import com.education.web.superadmin.dto.DeactivateSchoolAdminRequest;
import com.education.web.superadmin.dto.SchoolAdminContactResponse;
import com.education.web.superadmin.dto.SchoolAdminUpdateRequest;
import com.education.web.superadmin.service.SuperAdminSchoolAdminsService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/super-admin")
public class SuperAdminSchoolAdminsController {
    private final SuperAdminSchoolAdminsService schoolAdminsService;

    public SuperAdminSchoolAdminsController(SuperAdminSchoolAdminsService schoolAdminsService) {
        this.schoolAdminsService = schoolAdminsService;
    }

    @GetMapping("/school-admins")
    public List<SchoolAdminContactResponse> listSchoolAdmins() {
        return schoolAdminsService.listSchoolAdmins();
    }

    @PutMapping("/school-admins/{userId}")
    public SchoolAdminContactResponse updateSchoolAdmin(
            @PathVariable("userId") String userId,
            @Valid @RequestBody SchoolAdminUpdateRequest body
    ) {
        return schoolAdminsService.updateSchoolAdmin(userId, body);
    }

    /**
     * Деактивація (не видалення) — {@code users.enabled = false}.
     * DELETE залишено для REST; клієнт за замовчуванням викликає POST (нижче) — деякі проксі/оточення дають 405 на DELETE.
     */
    @DeleteMapping("/school-admins/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivateSchoolAdminDelete(@PathVariable("userId") String userId) {
        schoolAdminsService.deactivateSchoolAdmin(userId);
    }

    /**
     * Деактивація через JSON-тіло (основний варіант для фронта): шлях без UUID у сегментах —
     * менше шансів на 404 через проксі/старі збірки.
     */
    @PostMapping(path = "/school-admins/deactivate", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivateSchoolAdmin(@Valid @RequestBody DeactivateSchoolAdminRequest body) {
        schoolAdminsService.deactivateSchoolAdmin(body.userId());
    }

    @PostMapping(path = "/school-admins/reactivate", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reactivateSchoolAdmin(@Valid @RequestBody DeactivateSchoolAdminRequest body) {
        schoolAdminsService.reactivateSchoolAdmin(body.userId());
    }
}
