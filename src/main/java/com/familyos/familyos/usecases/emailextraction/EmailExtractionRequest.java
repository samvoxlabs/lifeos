package com.familyos.familyos.usecases.emailextraction;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EmailExtractionRequest(
  String provider
) {}
