package yukinari.software.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import yukinari.software.entity.Address;
import yukinari.software.entity.Contact;
import yukinari.software.entity.User;
import yukinari.software.model.AddressResponse;
import yukinari.software.model.CreateAddressRequest;
import yukinari.software.model.UpdateAddressRequest;
import yukinari.software.repository.AddressRepository;
import yukinari.software.repository.ContactRepository;

import java.util.List;
import java.util.UUID;

@Service
public class AddressService {

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private ValidationService validationService;

    @Transactional
    public AddressResponse create (User user, CreateAddressRequest request) {
        validationService.validate(request);

        Contact contact = contactRepository.findFirstByUserAndId(user, request.getContactId()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "contact not found")
        );

        Address address = new Address();
        address.setId(UUID.randomUUID().toString());
        address.setCity(request.getCity());
        address.setCountry(request.getCountry());
        address.setStreet(request.getStreet());
        address.setProvince(request.getProvince());
        address.setContact(contact);
        address.setPostalCode(request.getPostalCode());

        addressRepository.save(address);

        return toAddressResponse(address);
    }

    @Transactional
    public AddressResponse get (User user, String idContact, String idAddress) {
        Contact contact = contactRepository.findFirstByUserAndId(user, idContact).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "contact not found")
        );

        Address address = addressRepository.findFirstByContactAndId(contact, idAddress).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "address not found")
        );

        return toAddressResponse(address);
    }

    @Transactional
    public AddressResponse update (User user, UpdateAddressRequest request) {

        validationService.validate(request);

        Contact contact = contactRepository.findFirstByUserAndId(user, request.getContactId()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "contact not found")
        );

        Address address = addressRepository.findFirstByContactAndId(contact, request.getAddressId()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "address not found")
        );

        address.setCountry(request.getCountry());
        address.setStreet(request.getStreet());
        address.setProvince(request.getProvince());
        address.setCity(request.getCity());
        address.setPostalCode(request.getPostalCode());
        addressRepository.save(address);

        return toAddressResponse(address);

    }

    private AddressResponse toAddressResponse (Address address) {
        return AddressResponse.builder()
                .id(address.getId())
                .street(address.getStreet())
                .city(address.getCity())
                .province(address.getProvince())
                .country(address.getCountry())
                .postalCode(address.getPostalCode())
                .build();
    }

    @Transactional
    public void remove(User user, String contactId, String addressId){
        Contact contact = contactRepository.findFirstByUserAndId(user, contactId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contact is not found"));

        Address address = addressRepository.findFirstByContactAndId(contact, addressId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Address is not found"));

        addressRepository.delete(address);
    }

    @Transactional
    public List<AddressResponse> list (User user, String contactId) {
        Contact contact = contactRepository.findFirstByUserAndId(user, contactId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contact is not found"));

        List<Address> addresses = addressRepository.findAllByContact(contact);
        return addresses.stream().map(this::toAddressResponse).toList();
    }
}
