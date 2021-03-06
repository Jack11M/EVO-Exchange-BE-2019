package space.obminyashka.items_exchange.service.impl;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import space.obminyashka.items_exchange.dao.AdvertisementRepository;
import space.obminyashka.items_exchange.dto.*;
import space.obminyashka.items_exchange.model.Advertisement;
import space.obminyashka.items_exchange.model.Image;
import space.obminyashka.items_exchange.model.User;
import space.obminyashka.items_exchange.model.enums.AgeRange;
import space.obminyashka.items_exchange.model.enums.Status;
import space.obminyashka.items_exchange.service.AdvertisementService;
import space.obminyashka.items_exchange.service.ImageService;
import space.obminyashka.items_exchange.service.LocationService;
import space.obminyashka.items_exchange.service.SubcategoryService;

import javax.persistence.EntityNotFoundException;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static space.obminyashka.items_exchange.mapper.UtilMapper.convertTo;

@Service
@RequiredArgsConstructor
public class AdvertisementServiceImpl implements AdvertisementService {

    private final ModelMapper modelMapper;
    private final AdvertisementRepository advertisementRepository;
    private final SubcategoryService subcategoryService;
    private final LocationService locationService;
    private final ImageService imageService;

    @Value("${display.adv.date.format}")
    private String dateFormat;

    @Override
    public List<AdvertisementDto> findAll(Pageable pageable) {
        List<Advertisement> content = advertisementRepository.findAll(pageable).getContent();
        return mapAdvertisementsToDto(content);
    }

    @Override
    public List<AdvertisementTitleDto> findAllThumbnails(Pageable pageable) {
        List<Advertisement> content = advertisementRepository.findAll(pageable).getContent();
        return mapAdvertisementsToTitleDto(content);
    }

    @Override
    public List<AdvertisementTitleDto> findAllByUsername(String username) {
        final var allForUser = advertisementRepository.findAllByUserUsername(username);
        return mapAdvertisementsToTitleDto(allForUser);
    }

    @Override
    public List<AdvertisementTitleDto> findFirst10ByTopic(String topic) {
        return mapAdvertisementsToTitleDto((Collection<Advertisement>) advertisementRepository.findFirst10ByTopicIgnoreCaseContaining(topic));
    }

    @Override
    public Optional<Advertisement> findById(long advertisementId) {
        return advertisementRepository.findById(advertisementId);
    }

    @Override
    public Optional<Advertisement> findByIdAndOwnerUsername(long advertisementId, String ownerName) {
        return advertisementRepository.findAdvertisementByIdAndUserUsername(advertisementId, ownerName);
    }

    @Override
    public Optional<AdvertisementDisplayDto> findDtoById(long id) {
        return findById(id).map(this::buildAdvertisementDisplayDto);
    }

    @Override
    public List<AdvertisementTitleDto> findFirst10ByFilter(AdvertisementFilterDto dto) {
        return mapAdvertisementsToTitleDto(
                (Collection<Advertisement>) advertisementRepository.findFirst10ByParams(
                        dto.getAge(),
                        dto.getGender(),
                        dto.getSize(),
                        dto.getSeason(),
                        dto.getSubcategoryId(),
                        dto.getCategoryId(),
                        dto.getLocationId()));
    }

    @Override
    public boolean isUserHasAdvertisementWithId(long id, User user) {
        return advertisementRepository.existsAdvertisementByIdAndUser(id, user);
    }

    @Override
    public AdvertisementModificationDto createAdvertisement(AdvertisementModificationDto createDto, User user) {
        Advertisement adv = mapDtoToAdvertisement(createDto);
        adv.setUser(user);
        adv.setStatus(Status.NEW);
        updateSubcategory(adv, createDto.getSubcategoryId());
        updateLocation(adv, createDto.getLocationId());
        return mapAdvertisementToDto(advertisementRepository.save(adv));
    }

    @Override
    public AdvertisementModificationDto updateAdvertisement(AdvertisementModificationDto dto) {
        Advertisement toUpdate = mapDtoToAdvertisement(dto);
        Advertisement fromDB = advertisementRepository.findById(dto.getId())
                .orElseThrow(EntityNotFoundException::new);

        updateAdvertisement(toUpdate, fromDB);
        updateSubcategory(fromDB, toUpdate.getSubcategory().getId());
        updateLocation(fromDB, toUpdate.getLocation().getId());
        fromDB.setStatus(Status.UPDATED);
        Advertisement updatedAdvertisement = advertisementRepository.saveAndFlush(fromDB);
        return mapAdvertisementToDto(updatedAdvertisement);
    }

    @Override
    public boolean existById(Long id) {
        return advertisementRepository.existsById(id);
    }

    private void updateAdvertisement(Advertisement toUpdate, Advertisement fromDB) {
        if (!fromDB.equals(toUpdate)) {
            BeanUtils.copyProperties(toUpdate, fromDB, "created", "updated", "status", "location", "user", "subcategory", "images", "chats");
        }
    }

    private void updateSubcategory(Advertisement fromDBAdvertisement, long id) {
        fromDBAdvertisement.setSubcategory(subcategoryService.findById(id)
                .orElseThrow(EntityNotFoundException::new));
    }

    private void updateLocation(Advertisement fromDBAdvertisement, long id) {
        fromDBAdvertisement.setLocation(locationService.findById(id)
                .orElseThrow(EntityNotFoundException::new));
    }

    @Override
    public void remove(long id) {
        advertisementRepository.deleteById(id);
        advertisementRepository.flush();
    }

    @Override
    public void setDefaultImage(Advertisement advertisement, Long imageId, User owner) {
        advertisement.getImages().stream()
                .filter(img -> img.getId() == imageId)
                .findFirst()
                .map(Image::getResource)
                .map(imageService::scale)
                .ifPresent(advertisement::setDefaultPhoto);

        advertisementRepository.saveAndFlush(advertisement);
    }

    private List<AdvertisementDto> mapAdvertisementsToDto(Iterable<Advertisement> advertisements) {
        return modelMapper.map(advertisements, new TypeToken<List<AdvertisementDto>>() {
        }.getType());
    }

    private List<AdvertisementTitleDto> mapAdvertisementsToTitleDto(Collection<Advertisement> advertisements) {
        return advertisements.stream().map(this::buildAdvertisementTitle).collect(Collectors.toList());
    }

    private Advertisement mapDtoToAdvertisement(AdvertisementModificationDto dto) {
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.LOOSE);
        return modelMapper.map(dto, Advertisement.class);
    }

    private AdvertisementModificationDto mapAdvertisementToDto(Advertisement advertisement) {
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.LOOSE);
        return modelMapper.map(advertisement, AdvertisementModificationDto.class);
    }

    @Override
    public boolean isUserHasAdvertisementAndItHasImageWithId(Long advertisementId, Long imageId, User owner) {
        return owner.getAdvertisements().stream()
                .filter(adv -> adv.getId() == advertisementId)
                .map(Advertisement::getImages)
                .flatMap(Collection::stream)
                .anyMatch(image -> image.getId() == imageId);
    }

    private AdvertisementTitleDto buildAdvertisementTitle(Advertisement advertisement) {
        return AdvertisementTitleDto.builder()
                .advertisementId(advertisement.getId())
                .image(getImage(advertisement))
                .title(advertisement.getTopic())
                .location(convertTo(advertisement.getLocation(), LocationDto.class))
                .ownerName(advertisement.getUser().getUsername())
                .ownerAvatar(advertisement.getUser().getAvatarImage())
                .build();
    }

    private AdvertisementDisplayDto buildAdvertisementDisplayDto(Advertisement advertisement) {
        String createdDate = advertisement.getCreated().format(DateTimeFormatter.ofPattern(dateFormat));
        String age = Optional.ofNullable(advertisement.getAge()).map(AgeRange::getValue).orElse("");
        AdvertisementDisplayDto displayDto = AdvertisementDisplayDto.builder()
                .advertisementId(advertisement.getId())
                .ownerName(getOwnerFullName(advertisement.getUser()))
                .ownerAvatar(advertisement.getUser().getAvatarImage())
                .age(age)
                .phone(getOwnerPhone(advertisement.getUser()))
                .category(convertTo(advertisement.getSubcategory().getCategory(), CategoryNameDto.class))
                .createdDate(createdDate)
                .build();

        AdvertisementDisplayDto mappedDto = modelMapper.map(advertisement, AdvertisementDisplayDto.class);
        BeanUtils.copyProperties(mappedDto, displayDto, "createdDate", "phone", "age", "ownerName",
                "category");
        return displayDto;
    }

    private String getOwnerPhone(User user) {
        var phones = user.getPhones();
        return phones.stream()
                .findFirst()
                .map(phone -> String.valueOf(phone.getPhoneNumber()))
                .orElse("");
    }

    private byte[] getImage(Advertisement advertisement) {
        return Optional.ofNullable(advertisement.getDefaultPhoto())
                .orElseGet(() -> advertisement.getImages().stream()
                        .findFirst()
                        .map(Image::getResource)
                        .orElse(new byte[0]));
    }

    private String getOwnerFullName(User user) {
        String formatted = String.format("%s %s", user.getFirstName(), user.getLastName());
        return formatted.isBlank() ? user.getUsername() : formatted.trim();
    }
}
