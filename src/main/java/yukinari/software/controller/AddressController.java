package yukinari.software.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import yukinari.software.entity.User;
import yukinari.software.model.AddressResponse;
import yukinari.software.model.CreateAddressRequest;
import yukinari.software.model.UpdateAddressRequest;
import yukinari.software.model.WebResponse;
import yukinari.software.service.AddressService;

import java.util.List;

@RestController
public class AddressController {

    @Autowired
    private AddressService addressService;

    @PostMapping(
            path = "/api/contacts/{idContact}/addresses",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<AddressResponse> create(
            User user,
            @RequestBody CreateAddressRequest request,
            @PathVariable(name = "idContact") String idContact
    ) {
        request.setContactId(idContact);
        AddressResponse addressResponse = addressService.create(user, request);

        return WebResponse.<AddressResponse>builder()
                .data(addressResponse)
                .build();

    }

    @GetMapping(
            path = "/api/contacts/{idContact}/addresses/{idAddress}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<AddressResponse> get(
            User user,
            @PathVariable(name = "idContact") String idContact,
            @PathVariable(name = "idAddress") String idAddress
    ) {

        AddressResponse addressResponse = addressService.get(user, idContact, idAddress);
        return WebResponse.<AddressResponse>builder()
                .data(addressResponse)
                .build();
    }


    @PutMapping(
            path = "/api/contacts/{idContact}/addresses/{idAddress}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<AddressResponse> update(
            User user,
            @RequestBody UpdateAddressRequest request,
            @PathVariable(name = "idContact") String idContacts,
            @PathVariable(name = "idAddress") String idAddress
    ) {

        request.setContactId(idContacts);
        request.setAddressId(idAddress);

        AddressResponse addressResponse = addressService.update(user, request);

        return WebResponse.<AddressResponse>builder()
                .data(addressResponse)
                .build();
    }

    @DeleteMapping(
            path = "/api/contacts/{contactId}/addresses/{addressId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<String> remove(User user,
                                      @PathVariable("contactId") String contactId,
                                      @PathVariable("addressId") String addressId) {
        addressService.remove(user, contactId, addressId);
        return WebResponse.<String>builder().data("ok").build();
    }

    @GetMapping(
            path = "/api/contacts/{idContact}/addresses",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<List<AddressResponse>> list(
            User user,
            @PathVariable(name = "idContact") String idContact
    ) {

        List<AddressResponse> addressResponses = addressService.list(user, idContact);
        return  WebResponse.<List<AddressResponse>>builder().data(addressResponses).build();

    }
}
