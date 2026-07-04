package com.familyos.familyos.controller;

import com.familyos.familyos.authentication.service.AuthenticationService;
import com.familyos.familyos.dto.DriveFileDto;
import com.familyos.familyos.service.DriveService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/drive")
public class DriveController {

    private final DriveService driveService;
    private final AuthenticationService authenticationService;

    public DriveController(DriveService driveService, AuthenticationService authenticationService) {
        this.driveService = driveService;
        this.authenticationService = authenticationService;
    }

    @GetMapping("/files")
    public List<DriveFileDto> files() {
        return driveService.readRecentFiles(authenticationService.currentUser().id());
    }
}
