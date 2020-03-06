package com.hillel.items_exchange.controller;

import com.hillel.items_exchange.dto.CategoryDto;
import com.hillel.items_exchange.exception.InvalidDtoException;
import com.hillel.items_exchange.service.CategoryService;
import com.hillel.items_exchange.util.ExceptionTextMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.boot.model.naming.IllegalIdentifierException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

import static com.hillel.items_exchange.config.SecurityConfig.HAS_ROLE_ADMIN;

@RestController
@RequestMapping("/category")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
@Slf4j
public class CategoryController {
    private static final String NAME_OF_CLASS = "IN the CategoryController: ";
    private final CategoryService categoryService;

    @GetMapping("/names")
    public ResponseEntity<List<String>> getAllCategoriesNames() {
        List<String> categoriesNames = categoryService.findAllCategoryNames();
        if (categoriesNames.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(categoriesNames, HttpStatus.OK);
    }

    @GetMapping("/all")
    public ResponseEntity<List<CategoryDto>> getAllCategories() {
        List<CategoryDto> categories = categoryService.findAllCategories();
        if (categories.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(categories, HttpStatus.OK);
    }

    @GetMapping("/{category_id}")
    public ResponseEntity<CategoryDto> getCategoryById(@PathVariable("category_id") long id) {
        return categoryService.findCategoryById(id)
                .map(categoryDto -> new ResponseEntity<>(categoryDto, HttpStatus.OK))
                .orElseThrow(() -> new EntityNotFoundException(ExceptionTextMessage.NO_CATEGORY_BY_ID + id));
    }

    @PreAuthorize(HAS_ROLE_ADMIN)
    @PostMapping
    public ResponseEntity<CategoryDto> createCategory(@Valid @RequestBody CategoryDto categoryDto) {
        if (categoryService.isCategoryDtoCreatable(categoryDto)) {
            return new ResponseEntity<>(categoryService.addNewCategory(categoryDto), HttpStatus.CREATED);
        }
        throw new InvalidDtoException(ExceptionTextMessage.MUST_HAVE_ID_ZERO);
    }

    @PreAuthorize(HAS_ROLE_ADMIN)
    @PutMapping
    public ResponseEntity<CategoryDto> updateCategory(@Valid @RequestBody CategoryDto categoryDto) {
        if (categoryService.isCategoryDtoUpdatable(categoryDto)) {
            return new ResponseEntity<>(categoryService.updateCategory(categoryDto), HttpStatus.ACCEPTED);
        }
        throw new IllegalIdentifierException(ExceptionTextMessage.SUBCATEGORIES_MUST_EXIST_BY_ID_OR_ZERO);
    }

    @PreAuthorize(HAS_ROLE_ADMIN)
    @DeleteMapping("/{category_id}")
    public ResponseEntity<CategoryDto> deleteCategoryById(@PathVariable("category_id") long id) {
        if (categoryService.isCategoryDtoDeletable(id)) {
            categoryService.removeCategoryById(id);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        throw new InvalidDtoException(ExceptionTextMessage.CATEGORY_CAN_NOT_BE_DELETED + id);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleEntityNotFoundException(EntityNotFoundException e) {
        log.warn(NAME_OF_CLASS + e.getMessage(), e);
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(e.getMessage());
    }

    @ExceptionHandler(InvalidDtoException.class)
    public ResponseEntity<String> handleInvalidCategoryControllerDtoException(InvalidDtoException e) {
        log.warn(NAME_OF_CLASS + e.getMessage(), e);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
    }

    @ExceptionHandler(IllegalIdentifierException.class)
    public ResponseEntity<String> handleIdException(IllegalIdentifierException e) {
        log.warn(NAME_OF_CLASS + e.getMessage(), e);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
    }

    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public ResponseEntity<String> handleSqlException(SQLIntegrityConstraintViolationException e) {
        log.error(NAME_OF_CLASS + e.getMessage(), e);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ExceptionTextMessage.SQL_EXCEPTION + e.getLocalizedMessage());
    }
}
