package com.familyos.familyos.domain.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("EVENT")
public class Event extends Action {
}
