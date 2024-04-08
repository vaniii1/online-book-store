package com.example.onlinebookstore.service.category.impl;

import com.example.onlinebookstore.dto.category.CategoryRequestDto;
import com.example.onlinebookstore.dto.category.CategoryResponseDto;
import com.example.onlinebookstore.exception.EntityNotFoundException;
import com.example.onlinebookstore.mapper.CategoryMapper;
import com.example.onlinebookstore.model.Category;
import com.example.onlinebookstore.repository.category.CategoryRepository;
import com.example.onlinebookstore.service.category.CategoryService;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public List<CategoryResponseDto> findAll(Pageable pageable) {
        return categoryRepository.findAll(pageable).stream()
                .map(categoryMapper::toDto)
                .toList();
    }

    @Override
    public CategoryResponseDto getById(Long id) {
        return categoryMapper.toDto(
                categoryRepository.findById(id).orElseThrow(() ->
                        new EntityNotFoundException("Couldn't find Category by id: " + id))
        );
    }

    @Override
    public CategoryResponseDto save(CategoryRequestDto request) {
        return categoryMapper.toDto(
                categoryRepository.save(categoryMapper.toModel(request))
        );
    }

    @Override
    public CategoryResponseDto update(Long id, CategoryRequestDto request) {
        Optional<Category> optionalCategory = categoryRepository.findById(id);
        if (optionalCategory.isPresent()) {
            Category category = categoryMapper
                    .updateCategoryModelFromCategoryDto(
                            optionalCategory.get(), request
                    );
            category.setId(id);
            return categoryMapper.toDto(categoryRepository.save(category));
        }
        throw new EntityNotFoundException("Couldn't find Category by id: " + id);
    }

    @Override
    public void deleteById(Long id) {
        categoryRepository.deleteById(id);
    }
}
