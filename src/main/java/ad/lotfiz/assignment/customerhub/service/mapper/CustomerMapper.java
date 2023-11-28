package ad.lotfiz.assignment.customerhub.service.mapper;

import ad.lotfiz.assignment.customerhub.model.CustomerEntity;
import nl.customerhub.api.v1.model.CustomerRequest;
import nl.customerhub.api.v1.model.CustomerResponse;
import org.mapstruct.Mapper;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    CustomerEntity mapFromCustomerRequest(CustomerRequest customerRequest);
    CustomerResponse mapFromCustomerEntity(CustomerEntity customerEntity);

    default String mapFromUuid(UUID theId){
        return theId.toString();
    }
}
