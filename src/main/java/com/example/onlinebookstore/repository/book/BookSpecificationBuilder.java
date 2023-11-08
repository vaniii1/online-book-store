package com.example.onlinebookstore.repository.book;

import com.example.onlinebookstore.dto.book.BookSearchParametersDto;
import com.example.onlinebookstore.model.Book;
import com.example.onlinebookstore.repository.SpecificationBuilder;
import com.example.onlinebookstore.repository.SpecificationProviderManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class BookSpecificationBuilder implements SpecificationBuilder<Book> {
    private final SpecificationProviderManager<Book> providerManager;

    @Override
    public Specification<Book> build(BookSearchParametersDto searchParameters) {
        Specification<Book> specification = Specification.where(null);
        if (searchParameters.titles() != null && searchParameters.titles().length > 0) {
            specification = specification.and(providerManager.getSpecificationProvider("title")
                    .getSpecification(searchParameters.titles()));
        }
        if (searchParameters.authors() != null && searchParameters.authors().length > 0) {
            specification = specification.and(providerManager.getSpecificationProvider("author")
                    .getSpecification(searchParameters.authors()));
        }
        if (searchParameters.lowestPrice() != null && searchParameters.lowestPrice().length > 0) {
            specification =
                    specification.and(providerManager.getSpecificationProvider("lowestPrice")
                    .getSpecification(searchParameters.lowestPrice()));
        }
        if (searchParameters.greatestPrice() != null
                && searchParameters.greatestPrice().length > 0) {
            specification =
                    specification.and(providerManager.getSpecificationProvider("greatestPrice")
                    .getSpecification(searchParameters.greatestPrice()));
        }
        return specification;
    }
}
