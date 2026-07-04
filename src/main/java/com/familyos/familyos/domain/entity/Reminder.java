package com.familyos.familyos.domain.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("REMINDER")
public class Reminder extends Action {
}
