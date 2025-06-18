package com.projectArka.product_service.domain.exception;

import lombok.Getter;

@Getter
public class ProductAlreadyExistsException extends RuntimeException {
  private final String field;

  public ProductAlreadyExistsException(String message, String field) {
    super(message);
    this.field = field;
  }

}