package com.surohi.backend.cric_scorer.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CustomResponseEntity<T> {
    private int status;
    private String message;
    private T data;

    public static <T> CustomResponseEntity<T> success(T data) {
        return new CustomResponseEntity<>(200, "Success", data);
    }
}
