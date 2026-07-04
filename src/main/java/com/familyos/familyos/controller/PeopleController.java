package com.familyos.familyos.controller;

import com.familyos.familyos.authentication.service.AuthenticationService;
import com.familyos.familyos.dto.ContactDto;
import com.familyos.familyos.service.PeopleService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/people")
public class PeopleController {

    private final PeopleService peopleService;
    private final AuthenticationService authenticationService;

    public PeopleController(PeopleService peopleService, AuthenticationService authenticationService) {
        this.peopleService = peopleService;
        this.authenticationService = authenticationService;
    }

    @GetMapping("/contacts")
    public List<ContactDto> contacts() {
        return peopleService.readContacts(authenticationService.currentUser().id());
    }
}
