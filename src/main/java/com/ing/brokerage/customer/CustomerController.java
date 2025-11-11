package com.ing.brokerage.customer;

import com.ing.brokerage.exception.BusinessException;
import com.ing.brokerage.exception.RecordNotFoundException;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/customers")
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    @PreAuthorize(value = "hasRole('ADMIN')")
    ResponseEntity<CustomerResponse> createCustomer(@Valid @RequestBody CustomerCreateRequest request) throws BusinessException {

        CustomerResponse response = customerService.createCustomer(request);
        return ResponseEntity.created(ServletUriComponentsBuilder
                                          .fromCurrentRequest().path("/{customerId}")
                                          .buildAndExpand(response.getId()).toUri())
                             .body(response);
    }

    @GetMapping
    @PreAuthorize(value = "hasRole('ADMIN')")
    ResponseEntity<Page<CustomerResponse>> listCustomers(
        @ParameterObject @PageableDefault(sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {

        return ResponseEntity.ok(customerService.listCustomers(pageable));
    }

    @GetMapping("/{customerId}")
    @PreAuthorize(value = "@userSecurity.isSelfOrAdmin(authentication, #customerId)")
    ResponseEntity<CustomerResponse> getCustomer(@PathVariable("customerId") UUID customerId) throws RecordNotFoundException {

        return ResponseEntity.ok(customerService.getCustomer(customerId));
    }

    @PatchMapping("/{customerId}")
    @PreAuthorize(value = "@userSecurity.isSelfOrAdmin(authentication, #customerId)")
    ResponseEntity<CustomerResponse> updateCustomer(@PathVariable("customerId") UUID customerId,
        @RequestBody CustomerUpdateRequest request) throws RecordNotFoundException {

        return ResponseEntity.ok(customerService.updateCustomer(customerId, request));
    }

    @DeleteMapping("/{customerId}")
    @PreAuthorize(value = "hasRole('ADMIN')")
    ResponseEntity<CustomerResponse> deleteCustomer(@PathVariable("customerId") UUID customerId) throws RecordNotFoundException {

        customerService.deleteCustomer(customerId);
        return ResponseEntity.noContent().build();
    }


}
