package ad.lotfiz.assignment.customerhub;

import ad.lotfiz.assignment.customerhub.model.CustomerEntity;
import nl.customerhub.api.v1.model.CustomerRequest;
import nl.customerhub.api.v1.model.CustomerResponse;
import nl.customerhub.api.v1.model.CustomerUpdateRequest;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;

import java.time.OffsetDateTime;
import java.util.UUID;

public class RandomGenerator {

    private static String randomString() {
        int nameLength = 8;

        return RandomStringUtils.randomAlphabetic(nameLength);
    }

    public static CustomerEntity randomCustomerEntity() {
        UUID id = UUID.randomUUID();
        return CustomerEntity.builder().id(id)
                .firstName(randomString())
                .lastName(randomString())
                .address(randomString())
                .age(randomAge())
                .email(randomEmail())
                .created(OffsetDateTime.now().minusDays(1))
                .updated(OffsetDateTime.now())
                .build();
    }

    private static String randomEmail() {
        return randomString() + "@example.com";
    }

    private static int randomAge() {
        return RandomUtils.nextInt(18, 100);
    }

    public static CustomerResponse randomCustomerResponse() {
        return new CustomerResponse()
                .id(UUID.randomUUID().toString())
                .firstName(randomString())
                .lastName(randomString())
                .address(randomString())
                .age(randomAge())
                .email(randomEmail())
                .created(OffsetDateTime.now().minusDays(1))
                .updated(OffsetDateTime.now());
    }

    public static CustomerResponse mapRequestToResponse(CustomerRequest request) {
        return new CustomerResponse()
                .id(UUID.randomUUID().toString())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .address(request.getAddress())
                .age(request.getAge())
                .email(request.getEmail())
                .created(OffsetDateTime.now().minusDays(1))
                .updated(OffsetDateTime.now());
    }

    public static CustomerResponse mapEntityToResponse(CustomerEntity entity) {
        return new CustomerResponse()
                .id(entity.getId().toString())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .address(entity.getAddress())
                .age(entity.getAge())
                .email(entity.getEmail())
                .created(entity.getCreated())
                .updated(entity.getUpdated());
    }

    public static CustomerRequest randomCustomerRequest() {
        return new CustomerRequest()
                .firstName(randomString())
                .lastName(randomString())
                .age(randomAge())
                .address(randomString())
                .email(randomEmail());
    }
    public static CustomerUpdateRequest randomCustomerUpdateRequest() {
        return new CustomerUpdateRequest()
                .address(randomString())
                .email(randomEmail());
    }

    public static CustomerEntity mapRequestToEntity(CustomerRequest request) {
        UUID id = UUID.randomUUID();

        return CustomerEntity.builder()
                .id(id)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .address(request.getAddress())
                .age(request.getAge())
                .email(request.getEmail())
                .created(OffsetDateTime.now().minusDays(1))
                .updated(OffsetDateTime.now())
                .build();

    }

    public static CustomerEntity randomCustomerEntity(String firstName, String lastName) {
        CustomerEntity customerEntity = randomCustomerEntity();
        customerEntity.setFirstName(firstName);
        customerEntity.setLastName(lastName);
        return customerEntity;
    }
}
