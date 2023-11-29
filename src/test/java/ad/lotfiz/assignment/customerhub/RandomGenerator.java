package ad.lotfiz.assignment.customerhub;

import ad.lotfiz.assignment.customerhub.model.CustomerEntity;
import nl.customerhub.api.v1.model.CustomerRequest;
import nl.customerhub.api.v1.model.CustomerResponse;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;

import java.time.OffsetDateTime;
import java.util.UUID;

public class RandomGenerator {

    private static String randomString() {
        int nameLength = 8;

        return RandomStringUtils.randomAlphabetic(nameLength);
    }

    public static CustomerEntity staticCustomerEntity() {
        UUID id = UUID.randomUUID();
        return new CustomerEntity(id, "John", "Doe", 25, "123 Main St", "john.doe@example.com");
    }

    public static CustomerEntity randomCustomerEntity() {
        UUID id = UUID.randomUUID();
        return new CustomerEntity(id, randomString(), randomString(),
                randomAge(), randomString(), randomEmail());
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

    public static CustomerResponse randomCustomerResponse(CustomerRequest request) {
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

    public static CustomerRequest randomCustomerRequest() {
        return new CustomerRequest()
                .firstName(randomString())
                .lastName(randomString())
                .age(randomAge())
                .address(randomString())
                .email(randomEmail());
    }
}
