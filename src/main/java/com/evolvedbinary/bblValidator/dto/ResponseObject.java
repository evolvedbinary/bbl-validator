package com.evolvedbinary.bblValidator.dto;

/**
 * This interface serves as a marker for all response types that can be returned
 * by a controller.
 *
 * @see ErrorResponse
 * @see ValidationResponse
 */
public sealed interface ResponseObject permits ErrorResponse, ValidationResponse {

}
