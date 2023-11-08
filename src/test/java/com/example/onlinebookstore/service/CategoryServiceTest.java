package com.example.onlinebookstore.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.onlinebookstore.dto.category.CategoryRequestDto;
import com.example.onlinebookstore.dto.category.CategoryResponseDto;
import com.example.onlinebookstore.exception.EntityNotFoundException;
import com.example.onlinebookstore.mapper.CategoryMapper;
import com.example.onlinebookstore.model.Category;
import com.example.onlinebookstore.repository.category.CategoryRepository;
import com.example.onlinebookstore.service.category.impl.CategoryServiceImpl;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {
    private static final Long VALID_ID_ONE = 1L;
    private static final Long VALID_ID_TWO = 2L;
    private static final Long INVALID_ID_FIVE = 5L;
    private static CategoryRequestDto firstRequest;
    private static CategoryRequestDto secondRequest;
    private static Category firstCategory;
    private static Category secondCategory;
    private static CategoryResponseDto firstExpected;
    private static CategoryResponseDto secondExpected;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private CategoryMapper categoryMapper;
    @InjectMocks
    private CategoryServiceImpl categoryService;

    @BeforeAll
    static void beforeAll() {
        firstRequest = createFirstRequest();
        secondRequest = createSecondRequest();
        firstCategory = createFirstCategory();
        secondCategory = createSecondCategory();
        firstExpected = createFirstExpectedCategory();
        secondExpected = createSecondExpectedCategory();
    }

    @Test
    @DisplayName("""
            Must return expected list
            """)
    void findAll_ValidPage_Ok() {
        Pageable pageable = PageRequest.of(0, 5);
        List<Category> categories = List.of(firstCategory, secondCategory);
        Page<Category> page = new PageImpl<>(categories, pageable, categories.size());

        Mockito.when(categoryRepository.findAll(pageable)).thenReturn(page);
        Mockito.when(categoryMapper.toDto(firstCategory)).thenReturn(firstExpected);
        Mockito.when(categoryMapper.toDto(secondCategory)).thenReturn(secondExpected);

        List<CategoryResponseDto> expected = List.of(firstExpected, secondExpected);
        List<CategoryResponseDto> actual = categoryService.findAll(pageable);

        Assertions.assertNotNull(actual);
        Assertions.assertEquals(expected.size(), actual.size());
        Assertions.assertEquals(expected, actual);
        Mockito.verify(categoryRepository, Mockito.times(1)).findAll(pageable);
        Mockito.verify(categoryMapper, Mockito.times(1)).toDto(firstCategory);
        Mockito.verify(categoryMapper, Mockito.times(1)).toDto(secondCategory);
        Mockito.verifyNoMoreInteractions(categoryRepository, categoryMapper);
    }

    @Test
    @DisplayName("""
            Must return expected CategoryDto with id two
            """)
    void getById_ValidId_Ok() {
        Mockito.when(categoryRepository.findById(VALID_ID_TWO))
                .thenReturn(Optional.of(secondCategory));
        Mockito.when(categoryMapper.toDto(secondCategory)).thenReturn(secondExpected);

        CategoryResponseDto actual = categoryService.getById(VALID_ID_TWO);

        assertThat(actual).isNotNull();
        assertThat(actual).isEqualTo(secondExpected);

        Mockito.verify(categoryRepository, Mockito.times(1)).findById(VALID_ID_TWO);
        Mockito.verify(categoryMapper, Mockito.times(1)).toDto(secondCategory);
        Mockito.verifyNoMoreInteractions(categoryRepository, categoryMapper);
    }

    @Test
    @DisplayName("""
            Must save category and return it
            """)
    void save_ValidRequest_Ok() {
        Mockito.when(categoryMapper.toModel(firstRequest)).thenReturn(firstCategory);
        Mockito.when(categoryRepository.save(firstCategory)).thenReturn(firstCategory);
        Mockito.when(categoryMapper.toDto(firstCategory)).thenReturn(firstExpected);

        CategoryResponseDto actual = categoryService.save(firstRequest);

        assertThat(actual).isNotNull();
        assertThat(actual).isEqualTo(firstExpected);
        Mockito.verify(categoryMapper, Mockito.times(1)).toModel(firstRequest);
        Mockito.verify(categoryRepository, Mockito.times(1)).save(firstCategory);
        Mockito.verify(categoryMapper, Mockito.times(1)).toDto(firstCategory);
        Mockito.verifyNoMoreInteractions(categoryRepository, categoryMapper);
    }

    @Test
    @DisplayName("""
            Must update Category and return it
            """)
    void update_ValidRequest_Ok() {
        CategoryRequestDto updateRequest = new CategoryRequestDto().setName("romance");
        Category updateCategory = new Category().setName(updateRequest.getName());
        CategoryResponseDto expected = new CategoryResponseDto().setName(updateCategory.getName());

        Mockito.when(categoryRepository.findById(VALID_ID_ONE))
                .thenReturn(Optional.of(firstCategory));
        Mockito.when(categoryMapper.toModel(updateRequest)).thenReturn(updateCategory);
        Mockito.when(categoryRepository.save(updateCategory)).thenReturn(updateCategory);
        Mockito.when(categoryMapper.toDto(updateCategory)).thenReturn(expected);

        CategoryResponseDto actual = categoryService.update(VALID_ID_ONE, updateRequest);

        assertThat(actual).isNotNull();
        assertThat(actual).isEqualTo(expected);
        Mockito.verify(categoryRepository, Mockito.times(1)).findById(VALID_ID_ONE);
        Mockito.verify(categoryMapper, Mockito.times(1)).toModel(updateRequest);
        Mockito.verify(categoryRepository, Mockito.times(1)).save(updateCategory);
        Mockito.verify(categoryMapper, Mockito.times(1)).toDto(updateCategory);
        Mockito.verifyNoMoreInteractions(categoryRepository, categoryMapper);
    }

    @Test
    @DisplayName("""
            Must delete Category with valid Id
            """)
    void deleteById_ValidId_Ok() {
        categoryService.deleteById(VALID_ID_TWO);
        Mockito.verify(categoryRepository, Mockito.times(1)).deleteById(VALID_ID_TWO);
        Mockito.verifyNoMoreInteractions(categoryRepository);
    }

    @Test
    @DisplayName("""
            Must throw an exception
            """)
    void getById_InvalidId_ThrowsException() {
        Mockito.when(categoryRepository.findById(INVALID_ID_FIVE)).thenReturn(Optional.empty());
        Exception exception = Assertions.assertThrows(
                EntityNotFoundException.class,
                () -> categoryService.getById(INVALID_ID_FIVE)
        );

        String expected = "Couldn't find Category by id: " + INVALID_ID_FIVE;
        String actual = exception.getMessage();

        assertThat(actual).isEqualTo(expected);
        Mockito.verify(categoryRepository, Mockito.times(1)).findById(INVALID_ID_FIVE);
        Mockito.verifyNoMoreInteractions(categoryRepository);
    }

    @Test
    @DisplayName("""
            Must throw an exception
            """)
    void update_InvalidId_ThrowsException() {
        CategoryRequestDto request = new CategoryRequestDto().setName("thriller");
        Mockito.when(categoryRepository.findById(INVALID_ID_FIVE)).thenReturn(Optional.empty());
        Exception exception = Assertions.assertThrows(
                EntityNotFoundException.class,
                () -> categoryService.update(INVALID_ID_FIVE, request)
        );

        String expected = "Couldn't find Category by id: " + INVALID_ID_FIVE;
        String actual = exception.getMessage();

        assertThat(actual).isEqualTo(expected);
        Mockito.verify(categoryRepository, Mockito.times(1)).findById(INVALID_ID_FIVE);
        Mockito.verifyNoMoreInteractions(categoryRepository);
    }

    private static CategoryRequestDto createFirstRequest() {
        return new CategoryRequestDto().setName("fairy tail");
    }

    private static Category createFirstCategory() {
        return new Category().setName(firstRequest.getName());
    }

    private static CategoryRequestDto createSecondRequest() {
        return new CategoryRequestDto().setName("detective");
    }

    private static Category createSecondCategory() {
        return new Category().setName(secondRequest.getName());
    }

    private static CategoryResponseDto createFirstExpectedCategory() {
        return new CategoryResponseDto()
                .setId(VALID_ID_ONE)
                .setName(firstCategory.getName());
    }

    private static CategoryResponseDto createSecondExpectedCategory() {
        return new CategoryResponseDto()
                .setId(VALID_ID_TWO)
                .setName(secondCategory.getName());
    }
}
