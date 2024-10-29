package yukinari.software.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import yukinari.software.entity.Address;
import yukinari.software.entity.Contact;
import yukinari.software.entity.User;
import yukinari.software.model.AddressResponse;
import yukinari.software.model.CreateAddressRequest;
import yukinari.software.model.UpdateAddressRequest;
import yukinari.software.model.WebResponse;
import yukinari.software.repository.AddressRepository;
import yukinari.software.repository.ContactRepository;
import yukinari.software.repository.UserRepository;
import yukinari.software.security.BCrypt;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AddressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        addressRepository.deleteAll();
        contactRepository.deleteAll();
        userRepository.deleteAll();

        User user = new User();
        user.setName("Test");
        user.setUsername("test");
        user.setPassword(BCrypt.hashpw("test", BCrypt.gensalt()));
        user.setToken("test");
        user.setTokenExpiredAt(System.currentTimeMillis() * 100000000L);
        userRepository.save(user);

        Contact contact = new Contact();
        contact.setId("test");
        contact.setUser(user);
        contact.setFirstName("test");
        contact.setLastName("test");
        contact.setEmail("test@example.com");
        contact.setPhone("9238423432");
        contactRepository.save(contact);

    }

    @Test
    void createBadRequest() throws Exception {
        CreateAddressRequest request = new CreateAddressRequest();
        request.setCountry("");

        mockMvc.perform(post("/api/contacts/test/addresses")
                .header("X-API-TOKEN", "test")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)
                )).andExpectAll(
                        status().isBadRequest())
                .andDo(result -> {
                    WebResponse<String> response = objectMapper.readValue(result.getResponse()
                            .getContentAsString(), new TypeReference<>() {
                    });

                    assertNotNull(response.getErrors());
                });
    }

    @Test
    void createAddressSuccess() throws Exception {
        CreateAddressRequest request = new CreateAddressRequest();
        request.setStreet("street");
        request.setCity("kitami");
        request.setProvince("Hokkaido");
        request.setCountry("japan");
        request.setPostalCode("7654");

        mockMvc.perform(
                post("/api/contacts/test/addresses")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-API-TOKEN", "test")
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
            WebResponse<AddressResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });
            assertNull(response.getErrors());
            assertEquals(request.getStreet(), response.getData().getStreet());
            assertEquals(request.getCity(), response.getData().getCity());
            assertEquals(request.getProvince(), response.getData().getProvince());
            assertEquals(request.getCountry(), response.getData().getCountry());
            assertEquals(request.getPostalCode(), response.getData().getPostalCode());

            assertTrue(addressRepository.existsById(response.getData().getId()));
        });
    }


    @Test
    void getAddressBadRequest() throws Exception {
        mockMvc.perform(get("/api/contacts/test/addresses/unknown")
                        .header("X-API-TOKEN", "test")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        ).andExpectAll(
                        status().isBadRequest())
                .andDo(result -> {
                    WebResponse<String> response = objectMapper.readValue(result.getResponse()
                            .getContentAsString(), new TypeReference<>() {
                    });

                    assertNotNull(response.getErrors());
                });
    }

    @Test
    void getAddressSuccess() throws Exception {
        Contact contact = contactRepository.findById("test").orElseThrow();

        Address address = new Address();
        address.setContact(contact);
        address.setId("test");
        address.setStreet("street");
        address.setProvince("province");
        address.setCity("kitami");
        address.setCountry("japan");
        address.setPostalCode("7654");
        addressRepository.save(address);


        mockMvc.perform(get("/api/contacts/test/addresses/test")
                        .header("X-API-TOKEN", "test")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                ).andExpectAll(
                        status().isOk())
                .andDo(result -> {
                    WebResponse<AddressResponse> response = objectMapper.readValue(result.getResponse()
                            .getContentAsString(), new TypeReference<>() {
                    });

                    assertNull(response.getErrors());
                });
    }

    @Test
    void updateAddressBadRequest() throws Exception {

        UpdateAddressRequest request = new UpdateAddressRequest();
        request.setCountry("");

        mockMvc.perform(put("/api/contacts/test/addresses/unknown")
                        .header("X-API-TOKEN", "test")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                ).andExpectAll(
                        status().isBadRequest())
                .andDo(result -> {
                    WebResponse<String> response = objectMapper.readValue(result.getResponse()
                            .getContentAsString(), new TypeReference<>() {
                    });

                    assertNotNull(response.getErrors());
                });
    }

    @Test
    void updateAddressSuccess() throws Exception {

        Contact contact = contactRepository.findById("test").orElseThrow();

        Address address = new Address();
        address.setContact(contact);
        address.setId("test");
        address.setStreet("street");
        address.setProvince("province");
        address.setCity("kitami");
        address.setCountry("japan");
        address.setPostalCode("7654");
        addressRepository.save(address);

        UpdateAddressRequest request = new UpdateAddressRequest();
        request.setStreet("new street");
        request.setCity("new city");
        request.setProvince("new province");
        request.setCountry("new country");
        request.setPostalCode("12345");

        mockMvc.perform(put("/api/contacts/test/addresses/test")
                        .header("X-API-TOKEN", "test")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                ).andExpectAll(
                        status().isOk())
                .andDo(result -> {
                    WebResponse<AddressResponse> response = objectMapper.readValue(result.getResponse()
                            .getContentAsString(), new TypeReference<>() {
                    });

                    assertNull(response.getErrors());
                });
    }

    @Test
    void deleteAddressNotFound() throws Exception {
        mockMvc.perform(
                delete("/api/contacts/test/addresses/test")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-API-TOKEN", "test")
        ).andExpectAll(
                status().isNotFound()
        ).andDo(result -> {
            WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });
            assertNotNull(response.getErrors());
        });
    }

    @Test
    void deleteAddressSuccess() throws Exception {
        Contact contact = contactRepository.findById("test").orElseThrow();

        Address address = new Address();
        address.setContact(contact);
        address.setId("test");
        address.setStreet("street");
        address.setProvince("province");
        address.setCity("kitami");
        address.setCountry("japan");
        address.setPostalCode("7654");
        addressRepository.save(address);


        mockMvc.perform(delete("/api/contacts/test/addresses/test")
                        .header("X-API-TOKEN", "test")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                ).andExpectAll(
                        status().isOk())
                .andDo(result -> {
                    WebResponse<String> response = objectMapper.readValue(result.getResponse()
                            .getContentAsString(), new TypeReference<>() {
                    });

                    assertNull(response.getErrors());
                });
    }

    @Test
    void listAddressNotFound() throws Exception {
        mockMvc.perform(
                get("/api/contacts/salah/addresses")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-API-TOKEN", "test")
        ).andExpectAll(
                status().isNotFound()
        ).andDo(result -> {
            WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });
            assertNotNull(response.getErrors());
        });
    }

    @Test
    void listAddressSuccess() throws Exception {
        Contact contact = contactRepository.findById("test").orElseThrow();

        for (int i = 0; i < 5; i++) {
            Address address = new Address();
            address.setId("test-" + i);
            address.setContact(contact);
            address.setStreet("street");
            address.setCity("kitami");
            address.setProvince("hokkaido");
            address.setCountry("japan");
            address.setPostalCode("123123");
            addressRepository.save(address);
        }

        mockMvc.perform(
                get("/api/contacts/test/addresses")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-API-TOKEN", "test")
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
            WebResponse<List<AddressResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });
            assertNull(response.getErrors());
            assertEquals(5, response.getData().size());
        });
    }


}