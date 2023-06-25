package rs.edu.raf.si.bank2.users.cucumber.integration.serviceAuthFailures;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import rs.edu.raf.si.bank2.users.services.interfaces.AuthorisationServiceInterface;
import rs.edu.raf.si.bank2.users.services.interfaces.UserServiceInterface;

public class ServiceAuthFailuresIntegrationSteps extends ServiceAuthIntegrationTestConfiguration {

    /**
     * Token for testing the validity.
     */
    String token;

    /**
     * ResultActions of the last executed mock request.
     */
    ResultActions resultActions;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    UserServiceInterface userServiceInterface;

    @Autowired
    CompositeMeterRegistry meterRegistry = new CompositeMeterRegistry();

    @Autowired
    AuthorisationServiceInterface authorisationServiceInterface;

    @Autowired
    MockMvc mockMvc;

    @Given("invalid token generated")
    public void invalid_token_generated() {
        token = "RandomTokForTestingThatShouldFAIL!";
    }

    @When("post to validate")
    public void post_to_validate() throws Exception {
        resultActions = mockMvc.perform(post("/api/serviceAuth/validate")
                .contentType("application/json")
                .content("{}")
                .header("Authorization", "Bearer " + token));
    }

    @Then("not ok response")
    public void not_ok_response() throws Exception {
        resultActions.andExpect(status().is(403)).andReturn();
    }
}
