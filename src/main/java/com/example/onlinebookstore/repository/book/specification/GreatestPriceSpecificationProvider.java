package com.example.onlinebookstore.repository.book.specification;

import com.example.onlinebookstore.model.Book;
import com.example.onlinebookstore.repository.SpecificationProvider;
import java.math.BigDecimal;
import java.util.Arrays;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class GreatestPriceSpecificationProvider implements SpecificationProvider<Book> {
    @Override
    public String getKey() {
        return "greatestPrice";
    }

    @Override
    public Specification<Book> getSpecification(String[] params) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(root.get("price"), Arrays.stream(params)
                .map(BigDecimal::new)
                .max(BigDecimal::compareTo)
                .get());
    }
}
