package com.familyos.familyos.domain.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("TASK")
public class Task extends Action {
}
