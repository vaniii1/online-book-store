package com.example.onlinebookstore.repository.book;

import com.example.onlinebookstore.exception.NoSuchSpecificationException;
import com.example.onlinebookstore.model.Book;
import com.example.onlinebookstore.repository.SpecificationProvider;
import com.example.onlinebookstore.repository.SpecificationProviderManager;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class BookSpecificationProviderManager implements SpecificationProviderManager<Book> {
    private final List<SpecificationProvider<Book>> providersList;

    @Override
    public SpecificationProvider<Book> getSpecificationProvider(String key) {
        return providersList.stream()
                .filter(spec -> spec.getKey().equals(key))
                .findFirst().orElseThrow(() ->
                    new NoSuchSpecificationException("Can't find specification by key: " + key));
    }
}
