package com.example.onlinebookstore.dto;

public record BookSearchParametersDto(String[] titles,
                                      String[] authors,
                                      String[] lowestPrice,
                                      String[] greatestPrice) {
}
