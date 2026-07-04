package com.familyos.familyos.storage;

import com.familyos.familyos.authentication.service.AuthenticationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/storage")
public class StorageController {

    private final StorageService storageService;
    private final AuthenticationService authenticationService;

    public StorageController(StorageService storageService, AuthenticationService authenticationService) {
        this.storageService = storageService;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/bootstrap")
    public StorageStatus bootstrap() {
        return storageService.bootstrap(authenticationService.currentUser().id());
    }

    @PostMapping("/save")
    public StorageStatus save() {
        return storageService.save(authenticationService.currentUser().id());
    }

    @GetMapping("/status")
    public StorageStatus status() {
        return storageService.status(authenticationService.currentUser().id());
    }
}
