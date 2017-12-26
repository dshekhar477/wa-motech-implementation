package org.motechproject.nms.api;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import org.motechproject.nms.api.web.contract.BadRequest;
import org.motechproject.nms.swc.service.SwcService;
import org.springframework.test.web.server.MockMvc;
import org.springframework.test.web.server.setup.MockMvcBuilders;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.server.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.server.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.server.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
public class BaseControllerTest {

   
    @Mock
    private SwcService subscriberService;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        
    }

    @Test
    public void testInternalError() throws Exception {
        String message = "error";
        ObjectMapper objectMapper = new ObjectMapper();
        BadRequest badRequest = new BadRequest(message);
        when(subscriberService.getByContactNumber(anyLong())).thenThrow(new NullPointerException(message));

        String url = "/kilkari/inbox?callingNumber=1111111111&callId=1234567891234561234512345";

       
    }
}
