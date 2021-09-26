package pl.gr.veterinaryapp.controller.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import pl.gr.veterinaryapp.config.WebSecurityConfig;
import pl.gr.veterinaryapp.jwt.JwtAuthenticationFilter;
import pl.gr.veterinaryapp.mapper.ClientMapper;
import pl.gr.veterinaryapp.model.dto.ClientRequestDto;
import pl.gr.veterinaryapp.model.dto.ClientResponseDto;
import pl.gr.veterinaryapp.model.entity.Client;
import pl.gr.veterinaryapp.service.ClientService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ClientRestController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                        classes = {WebSecurityConfigurerAdapter.class, JwtAuthenticationFilter.class})
        },
        excludeAutoConfiguration = {WebSecurityConfig.class})
class ClientRestControllerTest {

    private static final long ID = 1L;
    private static final String CLIENT_NAME = "Konrad";
    private static final String CLIENT_SURNAME = "Gabrukiewicz";

    @MockBean
    private ClientService clientService;

    @MockBean
    private ClientMapper clientMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    void createClient_CorrectData_Created() throws Exception {
        var clientRequest = new ClientRequestDto();
        clientRequest.setName(CLIENT_NAME);
        clientRequest.setSurname(CLIENT_SURNAME);

        var client = new Client();

        var clientResponse = new ClientResponseDto();
        clientResponse.setName(CLIENT_NAME);
        clientResponse.setSurname(CLIENT_SURNAME);

        when(clientService.createClient(any(ClientRequestDto.class))).thenReturn(client);
        when(clientMapper.map(any(Client.class))).thenReturn(clientResponse);

        mockMvc.perform(post("/api/clients")
                .with(csrf())
                .content(objectMapper.writeValueAsString(clientRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(CLIENT_NAME))
                .andExpect(jsonPath("$.surname").value(CLIENT_SURNAME));

        verify(clientService).createClient(eq(clientRequest));
        verify(clientMapper).map(eq(client));
    }

    @Test
    @WithMockUser
    void getClient_CorrectData_Returned() throws Exception {
        var client = new Client();

        var clientResponse = new ClientResponseDto();
        clientResponse.setName(CLIENT_NAME);
        clientResponse.setSurname(CLIENT_SURNAME);

        when(clientService.getClientById(anyLong())).thenReturn(client);
        when(clientMapper.map(any(Client.class))).thenReturn(clientResponse);

        mockMvc.perform(get("/api/clients/{id}", ID)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(CLIENT_NAME))
                .andExpect(jsonPath("$.surname").value(CLIENT_SURNAME));

        verify(clientService).getClientById(ID);
        verify(clientMapper).map(eq(client));
    }

    @Test
    @WithMockUser
    void getAllClients_CorrectData_Returned() throws Exception {
        var clients = List.of(new Client(), new Client());

        var clientResponse = new ClientResponseDto();
        clientResponse.setName(CLIENT_NAME);
        clientResponse.setSurname(CLIENT_SURNAME);

        when(clientService.getAllClients()).thenReturn(clients);
        when(clientMapper.mapAsList(anyCollection())).thenReturn(List.of(clientResponse, clientResponse));

        mockMvc.perform(get("/api/clients")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].name").value(CLIENT_NAME))
                .andExpect(jsonPath("$.[0].surname").value(CLIENT_SURNAME))
                .andExpect(jsonPath("$.[1].name").value(CLIENT_NAME))
                .andExpect(jsonPath("$.[1].surname").value(CLIENT_SURNAME));

        verify(clientService).getAllClients();
        verify(clientMapper).mapAsList(eq(clients));
    }
}
