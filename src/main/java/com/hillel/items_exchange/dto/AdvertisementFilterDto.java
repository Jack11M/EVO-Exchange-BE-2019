package com.hillel.items_exchange.dto;

import com.hillel.items_exchange.model.enums.AgeRange;
import com.hillel.items_exchange.model.enums.Gender;
import com.hillel.items_exchange.model.enums.Season;
import lombok.*;

import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class AdvertisementFilterDto {
    private AgeRange age;
    private Gender gender;
    private Season season;
    @Size(max = 50, message = "{invalid.max-size}")
    private String size;
    @PositiveOrZero(message = "{invalid.id}")
    private long subcategoryId;
    @PositiveOrZero(message = "{invalid.id}")
    private long categoryId;
    @PositiveOrZero(message = "{invalid.id}")
    private long locationId;
}
