package com.example.onlinebookstore.dto.book;

public record BookSearchParametersDto(String[] titles,
                                      String[] authors,
                                      String[] lowestPrice,
                                      String[] greatestPrice) {
}
