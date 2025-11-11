package com.ing.brokerage.asset;

import static com.ing.brokerage.asset.AssetData.assetResponse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ing.brokerage.SecurityTestConfig;
import com.ing.brokerage.config.JwtService;
import com.ing.brokerage.config.UserSecurity;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AssetController.class)
@Import(SecurityTestConfig.class)
class AssetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AssetService assetService;

    @MockitoBean
    @Qualifier("customMessageResource")
    private MessageSource messageSource;

    @MockitoBean
    private UserSecurity userSecurity;

    @MockitoBean
    private JwtService jwtService;

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void shouldReturnOk_IfSelfOrAdmin_WhenListAssets() throws Exception {

        AssetResponse asset = assetResponse();
        when(userSecurity.isSelfOrAdmin(any(), any())).thenReturn(true);
        when(assetService.listAssets(any(AssetSearchRequest.class), any()))
            .thenReturn(new PageImpl<>(List.of(asset, asset),
                                       PageRequest.of(0, 10), 2));

        mockMvc.perform(get("/v1/assets")
                            .param("customerId", asset.getCustomerId().toString())
                            .param("page", "0")
                            .param("size", "10"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.content[0].customerId").value(asset.getCustomerId().toString()))
               .andExpect(jsonPath("$.content[0].assetName").value("TEST"))
               .andExpect(jsonPath("$.content[1].assetName").value("TEST"))
               .andExpect(jsonPath("$.totalElements").value(2));

        verify(assetService).listAssets(any(AssetSearchRequest.class), any());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void shouldReturnUnauthorized_IfNotSelfOrAdmin_WhenListAssets() throws Exception {
        UUID customerId = UUID.randomUUID();

        when(userSecurity.isSelfOrAdmin(any(), any())).thenReturn(false);

        mockMvc.perform(get("/v1/assets")
                            .param("customerId", customerId.toString()))
               .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnOk_IfAdmin_WhenListAssetsForAnyCustomer() throws Exception {
        UUID customerId = UUID.randomUUID();

        when(userSecurity.isSelfOrAdmin(any(), any())).thenReturn(true);
        when(assetService.listAssets(any(AssetSearchRequest.class), any()))
            .thenReturn(new PageImpl<>(List.of(assetResponse())));

        mockMvc.perform(get("/v1/assets")
                            .param("customerId", customerId.toString()))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.content[0].id").value(1));

        verify(assetService).listAssets(any(AssetSearchRequest.class), any());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void shouldReturnOk_IfFilteredByAssetName_WhenListAssets() throws Exception {
        UUID customerId = UUID.randomUUID();

        when(userSecurity.isSelfOrAdmin(any(), any())).thenReturn(true);
        when(assetService.listAssets(any(AssetSearchRequest.class), any()))
            .thenReturn(new PageImpl<>(List.of(assetResponse())));

        mockMvc.perform(get("/v1/assets")
                            .param("customerId", customerId.toString())
                            .param("assetName", "TEST"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.content[0].assetName").value("TEST"))
               .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void shouldReturnEmptyPage_IfNoAssets_WhenListAssets() throws Exception {
        UUID customerId = UUID.randomUUID();

        when(userSecurity.isSelfOrAdmin(any(), any())).thenReturn(true);
        when(assetService.listAssets(any(AssetSearchRequest.class), any()))
            .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/v1/assets")
                            .param("customerId", customerId.toString()))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.content").isEmpty())
               .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void shouldApplyPagination_IfPageableProvided_WhenListAssets() throws Exception {
        UUID customerId = UUID.randomUUID();

        when(userSecurity.isSelfOrAdmin(any(), any())).thenReturn(true);
        when(assetService.listAssets(any(AssetSearchRequest.class), any()))
            .thenReturn(new PageImpl<>(List.of(assetResponse()), PageRequest.of(1, 5), 10));

        mockMvc.perform(get("/v1/assets")
                            .param("customerId", customerId.toString())
                            .param("page", "1")
                            .param("size", "5")
                            .param("sort", "assetName,desc"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.number").value(1))
               .andExpect(jsonPath("$.size").value(5))
               .andExpect(jsonPath("$.totalElements").value(10));
    }
}
