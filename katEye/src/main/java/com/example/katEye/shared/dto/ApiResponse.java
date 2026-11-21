package com.example.katEye.shared.dto;

public record ApiResponse<T>(boolean exito, String mensaje, T datos) {

    public static <T> ApiResponse<T> ok(T datos) {
        return new ApiResponse<>(true, null, datos);
    }

    public static <T> ApiResponse<T> ok(String mensaje, T datos) {
        return new ApiResponse<>(true, mensaje, datos);
    }

    public static <T> ApiResponse<T> error(String mensaje) {
        return new ApiResponse<>(false, mensaje, null);
    }
}
